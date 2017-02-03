
# ApkToolPlus

<a href="https://github.com/linchaolong/ApkToolPlus">
    <img src="doc/logo.png" alt="ApkToolPlus" title="ApkToolPlus" align="right" />
</a>
<br/><br/>

ApkToolPlus 是一个可视化的跨平台 apk 分析工具。

## 功能说明

### 1. ApkTool 

apk 反编译，回编译，签名。

![apktool](doc/apktool.jpg)

### 2. Apk 加固

dex 加密，防逆向，防止二次打包。（注意：该功能当前并非很完善，不建议商用，欢迎学习交流，欢迎提交 Pull requests）。

![apktool](doc/jiagu.jpg)

### 3. ApkInfoPrinter

apk 常见信息查看工具，如：AndroidManifest.xml，apk签名，版本号等。支持直接拖入查看apk信息。

![apktool](doc/apkinfoprinter.png)

### 4. Apk源码查看工具 

Apk 源码查看工具，支持 multi-dex。

![apktool](doc/jd.jpg)

### 5. 格式转换工具

jar2smali，class2smali，dex2smali（apk2smali），smali2dex，class2dex。

在设置界面，可关联Sublime，关联后通过工具转换的的文件会自动显示在Sublime。

![apktool](doc/settings.jpg)

### 6. 角标生成工具

icon 角标生成工具

![apktool](doc/icon_tool.jpg)

### 7. 其他

- JD（Java反编译工具）
- JAD（Java反编译工具），注意jar文件或目录不要在中文路径下。
- JBE（Java字节码编辑工具）
- Proguard（Java代码混淆工具）

## 工程结构

- app：应用主模块。
- app.Build：应用构建模块。
- lib.ApkParser：[apk-parser](https://github.com/clearthesky/apk-parser)，apk解析库。
- lib.AXMLPrinter： [AXMLPrinter2](https://code.google.com/archive/p/android4me/downloads)，二进制xml文件解析库。
- lib.Jad： [Jad](https://varaneckas.com/jad/) ，Java反编译工具。
- lib.JBE： [JBE](http://cs.ioc.ee/~ando/jbe/) ，Java字节码编辑器。
- lib.JiaGu：apk加固模块。
- lib.Proguard： [Proguard](https://sourceforge.net/projects/proguard/files/) ，代码混淆优化工具， [Usage](https://www.guardsquare.com/en/proguard/manual/usage) 。
- lib.Res：应用资源模块。
- lib.Utils：工具类模块。

> ApkToolPlus.jks
> - alias: ApkToolPlus
> - password: linchaolong
> - keystore password: linchaolong

## 构建说明

> 这是一个 IntelliJ IDEA 工程。
>
> 项目的构建依赖 ant， [点击这里下载 ant](https://ant.apache.org/bindownload.cgi)，并把 ant 的 bin 目录路径配置到 Path 环境变量，执行 `ant -version` 命令检测是否配置完成。

### 1. 运行项目

直接 Run `app` 模块中的 `com.linchaolong.apktoolplus.Main` 运行 ApkToolPlus。

### 2. 构建apk加固模块
  
`lib.JiaGu` 是 apk 加固模块，如果有更新修改，则执行 `app.Build` 模块的 `com.linchaolong.apktoolplus.build.UpdateJiaGu` 自动更新打包 apk 加固库到 app 模块。

### 3. 打包ApkToolPlus

`Build -> Artifacts... -> ApkToolPlus -> Build`，ApkToolPlus.jar 将生成在 `out\artifacts\ApkToolPlus` 目录下，如果已经安装 jdk 可以直接点击运行。

## 下载

> 项目地址：https://github.com/linchaolong/ApkToolPlus

点击 [这里](release) 下载 release 版 ApkToolPlus。

## 联系方式

- Email：linchaolong.dev@gmail.com
- Blog：http://www.jianshu.com/u/149dc6683cc7

> 最后，欢迎 Star，Fork，Issues 和提交 Pull requests，感谢 [ApkTool](https://github.com/iBotPeaches/Apktool) ，[apk-parser](https://github.com/clearthesky/apk-parser)，[AXMLPrinter](https://code.google.com/archive/p/android4me/downloads) 等开源项目的开发者。

