# TLM AI Chat Cache - 车万女仆 AI 对话缓存扩展模组

## 项目概述

为车万女仆 (Touhou Little Maid) 模组开发一个缓存层扩展，拦截女仆 AI 对话请求，将"玩家自然语言 → 操作指令"的映射缓存到本地。命中缓存时跳过 LLM 调用，实现零延迟响应并节省 API 费用。未命中时回退到 LLM，经玩家确认后写入缓存，系统会随使用逐渐减少对 LLM 的依赖。

## 技术背景

### 车万女仆现有 AI 对话系统

- 源码仓库：`https://github.com/TartaricAcid/TouhouLittleMaid`
- AI 聊天管理器：`src/main/java/com/github/tartaricacid/touhoulittlemaid/ai/manager/entity/MaidAIChatManager.java`
- 现有系统通过 LLM Function Calling 实现自然语言 → 游戏操作，支持的 function 包括：
  - `SwitchTask`：切换工作模式（攻击、种植、跟随等所有已注册 Task）
  - `SwitchFollowState`：切换跟随/待命状态
- 上下文注入：游戏时间、天气、维度、生物群系、手持物品、背包物品、生命值、药水效果等
- 配置文件路径：`config/touhou_little_maid/available_sites.yml`（LLM 站点配置）
- 自定义 prompt 路径：`config/touhou_little_maid/settings/*.yml`
- 游戏内重载命令：`/tlm ai_chat reload`
- Mod Loader：Forge / NeoForge，支持 MC 1.20.1 / 1.21.1
- 许可证：代码部分 MIT

### 需要重点阅读的源码

在开始编码前，务必阅读以下文件以理解现有的 AI 对话流程：

1. `MaidAIChatManager.java` — AI 对话的核心管理器，理解消息发送和接收流程
2. `EntityMaid.java` — 女仆实体类，理解 Task 切换和状态管理
3. `TaskManager.java` — 任务注册和管理
4. Function Calling 相关的实现（搜索 `IFunctionCall` 接口）
5. 网络包相关代码 — 理解客户端与服务端的消息同步

## 核心架构

```
玩家输入文字
    │
    ▼
[文本归一化] ── 去除语气词、标点、大小写统一
    │
    ▼
[缓存查询] ── HashMap<String, CachedAction> 内存查找 O(1)
    │
    ├─ 命中 → 直接执行操作（零延迟）
    │
    └─ 未命中 → 转发给 TLM 原有 AI 系统
                    │
                    ▼
              [LLM 返回操作]
                    │
                    ▼
              [玩家确认 UI]
                    │
                    ├─ 确认 → 执行操作 + 写入缓存
                    └─ 否定 → 玩家手选正确操作 → 写入缓存
```

## 功能模块

### 1. 文本归一化器 (TextNormalizer)

将玩家输入归一化为标准 key，提高缓存命中率。

**中文处理：**
- 去除常见语气词/助词：的、了、吧、啊、呢、哦、嘛、呀、哈、嗯、去、来、帮我、你、能不能、可以、请、一下、给我
- 去除标点符号和空白
- 示例："你能帮我去种一下地吗？" → "种地"

**英文处理：**
- 转小写
- 去除 stop words：please, can, you, could, would, the, a, an, my, me, go, help, start, do, switch, to, mode
- 去除标点
- 示例："Could you please switch to attack mode?" → "attack"

**通用处理：**
- trim 前后空白
- 归一化后如果为空字符串，不查缓存，直接走 LLM

### 2. 缓存存储 (ActionCache)

**内存结构：**
```java
public class ActionCache {
    // 归一化文本 → 缓存条目
    private final Map<String, CachedAction> cache = new ConcurrentHashMap<>();
}

public class CachedAction {
    String functionName;    // "SwitchTask" 或 "SwitchFollowState"
    String parameter;       // task ID 或 follow state 值
    int hitCount;           // 命中次数，用于统计
    long lastUsed;          // 最后使用时间戳
    String originalInput;   // 原始玩家输入（用于调试/导出）
}
```

**持久化：**
- 存储为 JSON 文件：`config/tlm-ai-cache/cache.json`
- 预置数据与用户学习数据分开存储：
  - `config/tlm-ai-cache/builtin.json` — 模组内置的预置映射，随模组版本更新
  - `config/tlm-ai-cache/learned.json` — 玩家确认后学习到的映射，不会被模组更新覆盖
- 启动时加载顺序：先加载 builtin，再加载 learned（learned 优先级更高，可覆盖 builtin）
- 写入时机：玩家确认新映射后立即异步写入 learned.json
- 编码：UTF-8

**预置映射示例（builtin.json 需内置）：**

覆盖所有 TLM 内置 Task 的常见中英文说法，至少包括：

| 归一化 key | functionName | parameter | 原始说法示例 |
|-----------|-------------|-----------|------------|
| 攻击 | SwitchTask | attack | 攻击、打怪、战斗、保护我 |
| attack | SwitchTask | attack | attack, fight, combat |
| 种植 | SwitchTask | farm | 种地、种菜、种田、farming |
| farm | SwitchTask | farm | farm, plant, grow |
| 跟随 | SwitchFollowState | follow | 跟着我、跟随、过来 |
| follow | SwitchFollowState | follow | follow, come, follow me |
| 待命 | SwitchFollowState | stay | 待命、别动、等着、站着 |
| stay | SwitchFollowState | stay | stay, wait, stop |
| 剪羊毛 | SwitchTask | shear | 剪羊毛、剪毛 |
| 挤牛奶 | SwitchTask | milk | 挤奶、挤牛奶 |
| 喂食 | SwitchTask | feed | 喂食、喂动物、繁殖 |
| 拾取 | SwitchTask | pickup | 捡东西、拾取、收集 |

> **注意**：具体的 task ID（parameter 列）需要对照 TLM 源码中 `TaskManager` 注册的实际 ID 确认，上表仅为示意。开发时务必从源码中提取完整的 task 列表。

### 3. 拦截层 (ChatInterceptor)

这是整个模组的核心——拦截 TLM 的 AI 对话请求。

**实现策略（按可行性排序，选择第一个可行的）：**

1. **Mixin 注入**（首选）：Mixin 到 `MaidAIChatManager` 的消息发送方法，在 LLM 请求发出前插入缓存查询逻辑。如果命中缓存，直接构造一个模拟的 Function Call 响应，跳过网络请求。

2. **事件监听**：如果 TLM 提供了 AI 对话相关的 Forge Event（检查源码），优先使用事件监听方式，侵入性更低。

3. **网络包拦截**：拦截客户端发送给服务端的 AI 对话网络包，在服务端侧做缓存判断。

**关键行为：**
- 缓存命中时：直接调用对应的 Task 切换逻辑，同时让女仆显示一条确认聊天气泡（如"好的，切换到攻击模式"）
- 缓存未命中时：完全透传给 TLM 原有 AI 系统，不做任何干预
- LLM 返回结果后：拦截 Function Call 的执行结果，弹出确认 UI

### 4. 确认界面 (ConfirmationUI)

当 LLM 返回了一个新的操作映射时，需要玩家确认。

**方案：使用聊天栏可点击文本（tellraw 风格）**

```
[女仆名] 我理解为【种植模式】 [✔ 确认] [✘ 不对]
```

- `[✔ 确认]`：ClickEvent 触发确认逻辑，将归一化文本 → 操作写入缓存
- `[✘ 不对]`：弹出一个操作选择列表（同样用可点击聊天文本，列出所有可用 Task），玩家点选正确操作后写入缓存
- 超时处理：30 秒无响应则本次不缓存，操作照常执行

### 5. 配置系统 (ModConfig)

使用 Forge 的配置系统，提供以下配置项：

```toml
[general]
# 是否启用缓存（总开关）
enable_cache = true

# 缓存未命中时是否需要玩家确认才写入
require_confirmation = true

# 是否在多人服务器中共享学习到的缓存（所有玩家共享 vs 每个玩家独立）
shared_cache = true

# 只有 OP 才能确认新的缓存映射（防止恶意教学）
op_only_confirm = false

[normalization]
# 自定义额外的中文停用词（逗号分隔）
extra_cn_stopwords = ""

# 自定义额外的英文停用词（逗号分隔）
extra_en_stopwords = ""

[debug]
# 显示缓存命中/未命中的调试信息
show_cache_debug = false
```

### 6. 管理命令

注册子命令挂到 `/tlm` 下，或使用独立命令前缀 `/tlmcache`：

- `/tlmcache list` — 列出所有缓存条目（分页）
- `/tlmcache clear` — 清空用户学习的缓存（不影响 builtin）
- `/tlmcache remove <key>` — 删除指定缓存条目
- `/tlmcache reload` — 从文件重新加载缓存
- `/tlmcache export` — 将当前学习到的缓存导出为可分享的 JSON
- `/tlmcache import <file>` — 从文件导入缓存（用于社区共享词库）
- `/tlmcache stats` — 显示缓存统计（总条目数、命中率、最常用的映射等）

## 项目结构

```
tlm-ai-cache/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── src/main/java/com/example/tlmaicache/
│   ├── TlmAiCache.java                 # Mod 主类，注册事件和命令
│   ├── config/
│   │   └── ModConfig.java              # Forge 配置
│   ├── cache/
│   │   ├── ActionCache.java            # 缓存核心逻辑
│   │   ├── CachedAction.java           # 缓存条目数据类
│   │   ├── CacheStorage.java           # JSON 持久化读写
│   │   └── BuiltinMappings.java        # 内置预置映射
│   ├── normalizer/
│   │   ├── TextNormalizer.java          # 归一化入口
│   │   ├── ChineseNormalizer.java       # 中文处理
│   │   └── EnglishNormalizer.java       # 英文处理
│   ├── intercept/
│   │   └── ChatInterceptor.java         # 对话拦截（Mixin 或事件）
│   ├── ui/
│   │   └── ConfirmationMessage.java     # 聊天栏确认 UI
│   └── command/
│       └── CacheCommands.java           # 管理命令
├── src/main/resources/
│   ├── META-INF/mods.toml
│   ├── tlm-ai-cache.mixins.json        # Mixin 配置（如果使用 Mixin 方案）
│   └── assets/tlmaicache/
│       └── lang/
│           ├── en_us.json
│           └── zh_cn.json
└── README.md
```

## 开发约束

- **MC 版本**：优先支持 1.20.1（Forge/NeoForge），TLM 用户基数最大的版本
- **强依赖**：Touhou Little Maid（作为前置 mod）
- **语言**：Java 17
- **不要破坏 TLM 原有功能**：缓存层是纯增量的，缓存关闭或模组卸载后 TLM 的 AI 对话应完全恢复原样
- **线程安全**：LLM 调用是异步的，缓存读写需要线程安全（使用 ConcurrentHashMap）
- **编码**：所有文件 UTF-8，JSON 存储时保留 Unicode（不转义中文）

## 开发步骤

1. **Clone TLM 源码**，阅读 AI 对话流程，确认拦截点
2. **搭建 mod 项目骨架**，配置 build.gradle 依赖 TLM
3. **实现 TextNormalizer**，编写中英文归一化逻辑和单元测试
4. **实现 ActionCache + CacheStorage**，内存缓存和 JSON 持久化
5. **编写 BuiltinMappings**，对照 TLM 源码中所有注册的 Task ID 生成预置映射
6. **实现 ChatInterceptor**，这是最关键也最有风险的一步，需要深入理解 TLM 的 AI 对话内部流程
7. **实现 ConfirmationMessage**，聊天栏可点击确认 UI
8. **实现 CacheCommands**，注册管理命令
9. **实现 ModConfig**，Forge 配置系统
10. **测试**：单人/多人环境下完整测试缓存命中、LLM 回退、确认写入、持久化重载等流程

## 注意事项

- TLM 的 AI 对话功能是可选的（需要配置 LLM 站点才启用），本模组在 AI 对话未启用时应优雅降级——可以考虑独立提供一个纯关键词匹配模式，不依赖 LLM 也能工作
- TLM 更新频繁，Mixin 注入点可能随版本变化，需要关注 TLM 的更新日志并及时适配
- 预置映射中的 task ID 必须和 TLM 注册的 ID 完全一致，这些 ID 在 TLM 源码的 `TaskManager` 或各 Task 类的注册处可以找到
