1，ReinforceApk目录：原App工程
build release包，签名和alias在project目录下。

2，将release目录下的apk复制到ReinforcingTool\reinforingfile目录下。

3，ShellApk目录，目标App工程，build，直接编译launch会报错，从build目录下
获取生成的debug apk。将它单独拿出来，放在外面，app-debug.apk。

4，将app-debug.apk解压，获取classes.dex文件，重命名shell.dex。将该文件放入ReinforcingTool\reinforingfile目录下。

5，运行ReinforcingTool工程，生成一个新class.dex文件。将该文件放入到app-debug.apk中，替换原来的classes.dex。
这个新classes.dex文件，包含原App工程的release包apk。

6，替换后的文件重命名app-debug-new.apk。

7，对app-debug-new.apk文件进行重新签名，因ShellApk工程一直用的debug签名，因此使用.android目录下的debug.keystore签名，也在外面目录。
在D:\android\Sdk\build-tools\28.0.3目录下，将app-debug-new.apk文件和debug.keystore文件复制进去。
签名命令
apksigner sign  --ks debug.keystore  --ks-key-alias androiddebugkey  --ks-pass pass:android  --key-pass pass:android  --out app-debug-new-sign.apk  app-debug-new.apk

8，最终得到文件app-debug-new-sign.apk，安装，adb install -t app-debug-new-sign.apk。

9，运行后，可以在Android/com.shell.apk/cache下，看到解析出来的原App工程生成的 release apk文件，可以点击安装。可以在解析后直接运行非安装apk的组件。


