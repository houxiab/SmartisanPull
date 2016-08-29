# 锤子下拉

“锤子下拉”，东半球最优雅的下拉控件。（也叫SmartisanRefreshableLayout。）

这是优酷上的演示视频：[演示视频](http://v.youku.com/v_show/id_XMTcwNTAyODU5Ng==.html?beta&)

# 说明
本项目模仿“锤子阅读”的下拉效果。仅供学习交流，请勿用于商业用途。


# 介绍
下面是我做的过程中觉得有意思的地方：
（下面把“可刷新”的最小距离叫刷新距离）

### 到达刷新距离前

![](http://7xj90z.com1.z0.glb.clouddn.com/smartisan_pull0.png)

![](http://7xj90z.com1.z0.glb.clouddn.com/smartisan_pull1.png)

1. 下拉时先把item0上面的分隔线滚动出来，该分隔线在下拉过程中一直显示，直到header完全消失，它才重新藏起来。
2. 到达刷新距离前，提示语逐渐清晰。
3. 在任何阶段，如果手指向上返回，动画逐渐回到原始状态。

### 到达刷新距离时

![](http://7xj90z.com1.z0.glb.clouddn.com/smartisan_pull2.png)

两圆弧刚好各转半圈，两圆弧间的两个缺口处于同一水平线。

### 到达刷新距离以下

![](http://7xj90z.com1.z0.glb.clouddn.com/smartisan_pull3.png)

刷新距离以下，摩擦系数越来越大。

两圆弧的旋转始终是平滑的，只有速度变化。

### 最后
![](http://7xj90z.com1.z0.glb.clouddn.com/smartisan_pull4.png)

最后，两圆弧逐渐过渡成线段，消失在两端。

# 拜托
喜欢的话，可以点击右上角的star。感谢。
