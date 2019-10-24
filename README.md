## 单个资源，多种渠道加载框架

通过学习Lottie动画库的加载资源源码部分，总结与抽取了他的资源加载的流程

## 框架逻辑架构

相关类的关系说明如下

![avatar](class_describe.png)

## 扩展思路

适用于单类型资源，多路径加载的逻辑。使用时，将MyComposition修改为最终需要输出的统一资源类，
在ResourcesFactory中扩展多种加载渠道