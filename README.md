# VotePlugin

一个可以用箱子 GUI 浏览和操作 PlotSquared 地皮的 Bukkit 插件。

# 启发

写这个插件的原因很简单，因为每次建筑比赛的最终评分环节太麻烦了。首先要收集选手名单，然后需要共享文档记录评分和投票，最后还要人工统计票数。主要是每次比赛都要把这些再做一遍……重复的工作，枯燥又乏味。

西西木也洞察到了这一点，提议要不要把评分环节自动化。在考虑如何自动化时，西木用箱子和物品命名做了个箱子GUI的原型，提出“打开箱子GUI就可以开始评分”的设想。小米和老欧看过之后都感觉不错，于是小米决定尝试实现一下。

经过小米差不多四五天的研究以及和西木共同测试，投票插件诞生了！

# 功能

- 通过GUI浏览所有地皮
- 为地皮投票或放弃投票
- 传送到指定地皮
- 实时更新地皮信息
- 实时统计地皮评分信息
- 评分信息保存到文件
- 多世界地皮管理
- 异步加载玩家头颅
- 所有指令自动补全

# 使用

如果你是普通玩家（e.g. 作品评委），直接输入 `/votes` 打开GUI看着操作就可以了，不太需要往下看。

如果你是OP或是活动的主持者，你需要进行一些简单的设置，普通玩家才可以开始正常使用 `/votes` 指令。

具体操作如下~

---

**首先，载入一个世界**

由于一般的地皮系统本身就支持多世界（地皮世界可能存在多个），因此本插件在设计之初也考虑到了多世界。

使用指令 `/votes pull <世界名>`。这个指令会导入指定世界中全部有主的地皮，然后呈现在 GUI 中。

1. 该指令不能载入一个没有有主地皮的世界
2. 多次使用该指令载入同一个世界 *不会* 有“刷新”的效果（本系统也不太需要手动刷新）
3. 不用担心新圈的地皮不会出现在 GUI 中。系统会监测并自动添加新的地皮信息到系统中
4. 已经载入A世界的情况下，再尝试载入B世界，然后再载入回A世界，不会导致A世界或B世界丢失评分信息。换言之，一旦一个世界载入过，除非有意而为之，它的评分状态将被永远保存，这避免了OP误操作导致评分信息丢失

---

**时机恰当时，把将系统状态设置为“就绪”**

“就绪”意味着玩家可以使用 `/votes` 指令在 GUI 中对作品进行评分操作了。

使用指令 `/votes ready` 切换系统的就绪状态，指令会反馈 *当前* 的系统状态。

系统默认情况下，就绪状态为 `false`。

---

**比赛结束后，查看评分统计数据**

要读懂本系统反馈的评分信息，你需要知道几个简单的定义：

- 两类主体：
  - 评分人：给予评分的客体
  - 地皮/作品/参赛选手：三个词意思一样，即参与比赛和被评分的客体
- 两类评分：
  - 绿票：评分人表示赞成
  - 红票：评分人表示弃权
- 评分的有效性：
  - 有效：如果一个评分人对所有已完成的作品做出了评分，则他给予的所有评分有效
  - 无效：如果一个评分人没有对所有已完成的作品做出评分，则他给予的所有评分无效

使用指令 `/votes stats` 可以查看最粗略的地皮得分情况。

每一行显示了一个作品的得分情况，从左往右分别代表：

> `有效绿票数` | `有效红票数` | `有效总票数` | `有效绿票数÷有效总票数×100%` | `参赛选手名`

注：`有效总票数 = 有效绿票数 + 有效红票数`

---

需要更详细的统计数据？请看下面！

使用指令 `/votes stats rate <玩家名>` 可以单独查看一个评分人的详细评分情况。

使用指令 `/votes stats work <玩家名>` 可以单独查看一个参赛选手的详细得分情况。

# 指令

- /votes - 打开主 GUI
- /votes pull <世界名> - 载入指定世界的地皮信息，呈现在主 GUI 中
- /votes purge <世界名> - 删除指定世界的地皮信息（这不会删除地皮，而仅仅删除本系统中的地皮信息）
- /votes ready
- /votes stats - 查看统计概览
- /votes stats rate <玩家名>
- /votes stats work <玩家名>
- /votes reload
- /votes save
- /votes cache clear - 清空玩家的头颅皮肤缓存

# 截图

![](https://mimaru-jp.oss-ap-northeast-1.aliyuncs.com/imageshover-on-work.jpg)
![](https://mimaru-jp.oss-ap-northeast-1.aliyuncs.com/imagesmain-gui.jpg)
![](https://mimaru-jp.oss-ap-northeast-1.aliyuncs.com/imagesback.jpg)

![](https://mimaru-jp.oss-ap-northeast-1.aliyuncs.com/imagesvideo2.gif)
![](https://mimaru-jp.oss-ap-northeast-1.aliyuncs.com/imagesvideo1.gif)


# 项目依赖

编译时依赖的API

- [paper-api](https://papermc.io/)
- [IntellectualSites/PlotSquared](https://github.com/IntellectualSites/PlotSquared)

一些辅助开发的库

- [mojang/authlib](https://mvnrepository.com/artifact/com.mojang/authlib/1.5.25) - some useful stuff about minecraft authentication
- [lucko/helper](https://github.com/lucko/helper) - A collection of utilities and extended APIs to support the rapid development of Bukkit plugins.
- [SpongePowered/configurate](https://github.com/SpongePowered/Configurate) - A simple configuration library for Java applications
- [Phoenix616/lang](https://github.com/Phoenix616/lang) - Language config framework for Bukkit, Bungee and standalone java programs
- [aikar/commands](https://github.com/aikar/commands) - Java Command Dispatch Framework

# TODO

- [ ] 不直接依赖 PlotSquared，而是抽象出一个中间接口

# 设计理念

TL;DR 这里我想简单的说一下实现过程中的一些取舍和考量，这些都是为了让项目更具可维护性和拓展性，可以说占用了开发的绝大部分时间。跟项目的客户没什么关系。如果你想贡献本项目，可以稍微瞧一瞧 :)

## 多世界支持

WIP.

## 头颅异步加载

WIP.

## 序列化

WIP.

# 鸣谢

@ChesNez 和 所有参与测试的人员