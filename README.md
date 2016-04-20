
# ApkToolPlus

<a href="https://github.com/linchaolong/ApkToolPlus">
    <img src="https://raw.githubusercontent.com/linchaolong/ApkToolPlus/master/img/logo.png" alt="ApkToolPlus" title="ApkToolPlus" align="right" />
</a>
<br/>

ApkToolPlus是一个包含apk逆向分析，apk加固，角标生成等一系列相关功能的工具。

## 前言

运行ApkToolPlus需要Java Runtime，我这里用的是jdk8，jdk下载地址：http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

作者博客：http://blog.csdn.net/linchaolong


##工具说明

1.ApkTool：反编译，回编译，apk签名，支持Android6.0应用，支持批量操作。<br/>
2.Apk加固：dex加密，防止还原真实代码逻辑，避免应用被复制，支持批量加固。<br/>
3.ApkInfoPrinter：apk信息查看工具，如：AndroidManifest.xml，签名,包名等常见信息，支持拖入直接查看。<br/>
4.Apk源码查看工具：用于查看apk源码，支持Multi-Dex。<br/>
5.文件转换工具<br/>
（1）ja2smali：jar文件转换smali文件；<br/>
（2）class2smali：class文件转换smali文件；<br/>
（3）dex2smali：dex/apk文件转换smali文件，支持Multi-Dex；<br/>
（4）smali2dex：smali文件转换dex文件；<br/>
（5）class2dex：class文件转换dex文件。<br/>
6.Proguard：代码混淆工具。<br/>
7.JAD：java反编译工具，注意jar文件或目录不要在中文路径下。<br/>
8.JD：java反编译工具。<br/>
9.角标生成工具：icon角标生成工具，可调整角标位置。<br/>
10.JBE：java汇编代码查看编辑工具。<br/>


## 界面预览

1.主界面
<img src="https://raw.githubusercontent.com/linchaolong/ApkToolPlus/master/img/Main1.jpg" alt="ApkToolPlus" title="ApkToolPlus"/>
<br/>
<img src="https://raw.githubusercontent.com/linchaolong/ApkToolPlus/master/img/Main2.jpg" alt="ApkToolPlus" title="ApkToolPlus"/>
<br/>

2.Debug界面，可查看日志输出。
<img src="https://raw.githubusercontent.com/linchaolong/ApkToolPlus/master/img/Debug.jpg" alt="ApkToolPlus" title="ApkToolPlus"/>
<br/>

3.设置界面，可关联Sublime，关联后通过工具转换的的文件会自动显示在Sublime。
<img src="https://raw.githubusercontent.com/linchaolong/ApkToolPlus/master/img/Settings.jpg" alt="ApkToolPlus" title="ApkToolPlus"/>
<br/>

4.ApkTool，可对apk进行反编译、回编译、签名操作，支持Andrid6.0应用。
<img src="https://raw.githubusercontent.com/linchaolong/ApkToolPlus/master/img/ApkTool.jpg" alt="ApkToolPlus" title="ApkToolPlus"/>
<br/>

5.Apk加固，支持批量加固。
<img src="https://raw.githubusercontent.com/linchaolong/ApkToolPlus/master/img/ApkProtector.jpg" alt="ApkToolPlus" title="ApkToolPlus"/>
<br/>

6.角标生成工具
<img src="https://raw.githubusercontent.com/linchaolong/ApkToolPlus/master/img/IconTool.jpg" alt="ApkToolPlus" title="ApkToolPlus"/>
<br/>

#版本更新说明

**1.0.1**<br/>
1.优化Apk加固加解密算法，执行效率更高；<br/>
2.修复keystore配置别名显示问题；<br/>

