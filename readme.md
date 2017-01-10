# 物联网设备服务器
## 主要功能
- 维持物联网设备的长连接
- 适用于数据采集和反向控制场景
- 便捷的二次开发

## 软件结构
软件拥有4大组件：  
- DeviceService
- Device
- DeviceRepo
- MsgQueue

## 代码示例
```java
DeviceService.getInstance().startService();
```
用户端只要执行以上的一条指令即可启动整个服务。当接收到设备消息后，会根据消息自动创建Device对象，Device对象集成有send方法以及消息回调，用于收发消息。创建完成的Device会将自身加入DeviceRepo中，这是一个简易版本的(K,V)内存型数据库(基于Map实现)，后期可以继承Redis等专用的内存型数据库。可以再DeviceMsg、DeviceCmd中定制设备消息(由设备产生、发往服务器)和设备控制命令(由用户比如手机产生发往设备)的消息帧格式。

## 扩展

待续