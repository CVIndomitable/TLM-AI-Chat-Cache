# TLM AI Chat Cache

[车万女仆](https://github.com/TartaricAcid/TouhouLittleMaid)（Touhou Little Maid）AI 对话缓存扩展模组。

将"玩家自然语言 → 游戏操作"的映射缓存到本地，命中缓存时跳过 LLM 调用，实现**零延迟响应**并**节省 API 费用**。内置 70+ 条常见中英文指令，装上即可使用，无需任何 LLM 配置。

## 工作原理

```
玩家对女仆说话
    │
    ▼
 文本归一化（去除语气词、标点、大小写统一）
    │
    ▼
  缓存查询
    ├─ 命中 → 立即执行（零延迟）
    └─ 未命中 → 交给 LLM 处理
                    │
                    ▼
              LLM 返回结果
                    │
                    ▼
              玩家确认后写入缓存
              （下次直接命中）
```

系统会随使用逐渐减少对 LLM 的依赖。

## 环境要求

| 依赖 | 版本 |
|-----|------|
| Minecraft | 1.20.1 |
| Forge | 47.2.0+ |
| Touhou Little Maid | 1.5.0+ |
| Java | 17 |

## 安装

1. 下载 [Releases](../../releases) 中的 `tlm-ai-cache-1.0.0.jar`
2. 放入游戏 `mods/` 文件夹
3. 启动游戏

首次启动后在 `config/tlm-ai-cache/` 目录下生成数据文件。

## 使用

### 内置指令

以下指令开箱即用，无需 LLM：

| 你可以说 | 女仆会做 |
|---------|---------|
| 攻击 / 打怪 / 战斗 / attack / fight | 攻击模式 |
| 种地 / 种菜 / farm / plant | 种植模式 |
| 跟着我 / 跟随 / follow / come | 跟随玩家 |
| 待命 / 别动 / stay / wait | 原地待命 |
| 剪羊毛 / shear | 剪羊毛模式 |
| 挤牛奶 / milk | 挤奶模式 |
| 喂食 / 喂动物 / feed | 喂食模式 |
| 钓鱼 / fish | 钓鱼模式 |
| 放火把 / torch | 放火把模式 |
| 灭火 / extinguish | 灭火模式 |
| 割草 / 采蜜 / 休息 | 对应模式 |

模组会自动处理语气词，例如 *"你能帮我去种一下地吗？"* 会被归一化为 *"种地"*，依然命中缓存。

### 学习新指令

当缓存未命中、LLM 返回结果后，聊天栏会出现确认消息：

```
[女仆名] 我理解为【种植模式】 [✔ 确认] [✘ 不对]
```

- **✔ 确认** — 记住这个映射，下次直接使用
- **✘ 不对** — 展示操作列表，选择正确的操作后记住
- **不点击** — 30 秒后超时，本次不缓存

学到的映射保存在 `config/tlm-ai-cache/learned.json`，重启不丢失。

## 管理命令

所有命令以 `/tlmcache` 开头。

| 命令 | 说明 | 权限 |
|------|------|------|
| `/tlmcache list [页码]` | 列出所有缓存条目（分页，按命中次数排序） | 所有人 |
| `/tlmcache stats` | 显示缓存统计（命中率、最常用映射等） | 所有人 |
| `/tlmcache remove <key>` | 删除指定缓存条目 | OP |
| `/tlmcache clear` | 清空学习缓存（不影响内置） | OP |
| `/tlmcache reload` | 从文件重新加载缓存 | OP |
| `/tlmcache export` | 导出学习缓存为 JSON | OP |
| `/tlmcache import <文件名>` | 从 JSON 导入缓存 | OP |

## 配置

配置通过 Forge Mod 配置界面或直接编辑配置文件修改。

| 配置项 | 默认值 | 说明 |
|-------|-------|------|
| `enable_cache` | `true` | 总开关，`false` 则所有对话直接走 LLM |
| `require_confirmation` | `true` | 新映射是否需要玩家确认才写入缓存 |
| `shared_cache` | `true` | 多人服务器是否共享缓存 |
| `op_only_confirm` | `false` | 仅 OP 可确认新映射（防恶意教学） |
| `extra_cn_stopwords` | `""` | 自定义中文停用词（逗号分隔） |
| `extra_en_stopwords` | `""` | 自定义英文停用词（逗号分隔） |
| `show_cache_debug` | `false` | 聊天栏显示缓存命中/未命中调试信息 |

## 数据文件

| 文件 | 说明 |
|-----|------|
| `config/tlm-ai-cache/builtin.json` | 内置预置映射，随模组更新覆盖 |
| `config/tlm-ai-cache/learned.json` | 玩家学习的映射，不被更新覆盖，优先级高于 builtin |

## 多人服务器

- 默认共享缓存，一人教过的指令所有人可用
- 公共服务器建议开启 `op_only_confirm` 防止恶意教学
- 管理命令（`clear`/`remove`/`reload`/`export`/`import`）需要 OP 权限

## 卸载

直接移除 `tlm-ai-cache-1.0.0.jar`。本模组是纯增量缓存层，不修改车万女仆的任何原有数据，卸载后 AI 对话功能完全恢复原样。

## 从源码构建

```bash
git clone <repo-url>
cd tlm-ai-cache
# 将 TLM 的 JAR 放入 libs/ 目录
./gradlew build
# 输出: build/libs/tlm-ai-cache-1.0.0.jar
```

## 许可证

MIT
