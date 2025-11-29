# NekoBwScoreBoardAddon 
基于 BedwarsScoreBoardAddon 重制和修复

给原帖 https://github.com/TheRamU/BedwarsScoreBoardAddon 擦屁股并且重置修复版本

# 升级事项！
请使用java21运行！此项目已经从1.12.2往上升级。1.12.2可能无法正常运行。如果有需要的话可以使用这个核心`https://github.com/NyaNyagulugulu/NekoCore`

这个核心是基于paper做的优化版本。并且攻击机制回退到1.8.未来对API，nms等的修改会在仓库的readme中标注清楚

# 源作者代码里面藏了什么？
验证`plugin.yml`是否被修改，太tm招笑了

![img.png](img.png)

# 我做了什么？

- 补全傻逼原帖作者发布的项目没有pom或者kts!
- TAB菜单的样式美化
- 修复了papi变量颜色无法解析!
- 把那个傻逼没给的依赖全部补全
- 添加床的右键消息移除
- 修复前

![img.png](img/img.png)
- 修复后

![img_5.png](img/img_5.png)

# 如何使用以及依赖

## 前置和依赖

需要安装BWrel https://github.com/NyaNyagulugulu/NekoBedwarsRel

依赖LP权限组插件并且在对应的组内有`suffix.0`

## 如何使用

将拿到的jar文件丢进plugins目录即可

## 如何构建？
打开你的idea。使用java8（1.8）点击右侧的M符号

![img_1.png](img/img_1.png)

点击生存期再点击package。你说什么？你不会选择java？

idea左上角三条杠

![img_2.png](img/img_2.png)

点击一下点击项目结构界面中的项目设置项目菜单。其中sdk就算java版本的选择
![img_3.png](img/img_3.png)