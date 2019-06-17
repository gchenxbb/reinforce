package com.shell.apk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dalvik.system.DexClassLoader;

public class ShellApplication extends Application {
    //拿到脱壳apk里面的dex文件，这是通过java项目生成的新dex文件，包括源程序apk
    private String TAG = "ShellApplication";

    String odexPath;
    String libPath;

    String apkFileName;

    @Override
    public void onCreate() {
        super.onCreate();
        //路由到源apk的Application的onCreate方法中运行
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        try {
            File odex = getDir("shell_odex", MODE_PRIVATE);
            File libs = getDir("shell_libs", MODE_PRIVATE);
            odexPath = odex.getAbsolutePath();
            libPath = libs.getAbsolutePath();

            String location = this.getExternalCacheDir().getAbsolutePath();
            apkFileName = location + "/app.apk";
//            apkFileName = odex.getAbsolutePath() + "/app.apk";
            Log.d(TAG, "解密出来的apk存储位置：" + apkFileName);
            //目前待解决，用这个再打一个apk，提取dex，去windows上解压提取，去除shell.dex，传给java项目，
            // 将源apk加到后面生成新dex，新dex再进来，直接复制进到该apk中，重新签名。
            // 安装后，运行这里的代码，会将新dex和源apk提取出来。
            //
            File dexFile = new File(apkFileName);

            if (!dexFile.exists()) {
                dexFile.createNewFile();
            }

            //从当前apk文件中读取dex
            byte[] dexData = readDexFileFromApk();
            //从dex获取源apk
            this.splitSourceApkFromDex(dexData);


            //加载目标程序apk
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
//            Object currentActivityThread =
            String packageName = this.getPackageName();
            Object currentActivityThread = getField(activityThreadClass, null, "sCurrentActivityThread");
            ArrayMap mPackages = (ArrayMap) getField(activityThreadClass, currentActivityThread, "mPackages");

            WeakReference weakReference = (WeakReference) mPackages.get(packageName);

            Class<?> loadedApk = Class.forName("android.app.LoadedApk");
            ClassLoader classLoader = (ClassLoader) getField(loadedApk, weakReference.get(), "mClassLoader");
            ClassLoader myClassLoader = getClassLoader();
            Log.d(TAG, myClassLoader.toString());
            Log.d(TAG, classLoader.toString());
            //新建一个DexLoader
            DexClassLoader dexClassLoader = new DexClassLoader(apkFileName, odexPath, libPath, classLoader);
            //复制给LoaderApk内部的mClassLoader
            setField(loadedApk, weakReference.get(), dexClassLoader, "mClassLoader");

            Object act = dexClassLoader.loadClass("com.reinforce.apk.MainActivity");
            Log.d(TAG, act.toString());
            //


        } catch (Exception e) {

        }
    }


    private Object getField(Class<?> clazz, Object target, String name) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private void setField(Class<?> clazz, Object target, Object object, String name) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, object);

    }

    //从apk文件中读取dex文件
    private byte[] readDexFileFromApk() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // /data/app/com.shell.apk-1/base.apk
            String sourceDir = getApplicationInfo().sourceDir;
            Log.d(TAG, sourceDir);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(sourceDir)));

            //拿到zip压缩文件块
            while (true) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) {
                    zis.close();
                    break;
                }
                //是classes.dex类型,如果多个dex文件的处理？
                if (ze.getName().equals("classes.dex")) {
                    byte[] arrayBytes = new byte[1024];
                    while (true) {
                        int count = zis.read(arrayBytes);
                        if (count == -1) {
                            break;
                        }
                        bos.write(arrayBytes, 0, count);
                    }
                }
                zis.closeEntry();
            }
            zis.close();
            return bos.toByteArray();
        } catch (Exception e) {


        }

        return null;
    }

    //从新dex文件中获取源apk文件,通过java工具被加进去的
    private void splitSourceApkFromDex(byte[] apkData) {
        try {
            int apkDexLength = apkData.length;
            //获取长度，长度在最后4个字节保存
            byte[] dexLen = new byte[4];
            System.arraycopy(apkData, apkDexLength - 4, dexLen, 0, 4);
            ByteArrayInputStream bis = new ByteArrayInputStream(dexLen);
            DataInputStream din = new DataInputStream(bis);
            int apkLen = din.readInt();
            Log.d(TAG, "解密出来的apk大小：" + apkLen);
            //去除长度后，apk在最后
            byte[] newApk = new byte[apkLen];
            System.arraycopy(apkData, apkDexLength - 4 - apkLen, newApk, 0, apkLen);
            //将apk写入文件
            File file = new File(apkFileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(newApk);
            fos.close();


        } catch (Exception e) {


        }


    }
}
