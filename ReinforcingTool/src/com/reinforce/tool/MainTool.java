package com.reinforce.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

//输入:源Apk,脱壳dex
//输出:新classes.dex
public class MainTool {

	public static void main(String[] args) {
		try {
			// 读取两个文件，一个是源apk，另一个是脱壳的dex文件
			File sourceApk = new File("reinforingfile/app-release.apk");
			if (!sourceApk.exists()) {
				return;
			}
			File dstDex = new File("reinforingfile/shell.dex");
			if (!dstDex.exists()) {
				return;
			}
			byte[] apkArray = readFileBytes(sourceApk);
			byte[] dexArray = readFileBytes(dstDex);
			if (apkArray == null || dexArray == null) {
				return;
			}
			int apkArrayLength = apkArray.length;
			int dexArrayLength = dexArray.length;

			System.out.println(apkArrayLength);
			System.out.println(dexArrayLength);
			// 对源apk加密,暂时不做，加密后也是一个字节数组

			// 将加密后对apk放到dex文件尾部
			int newDexLength = apkArrayLength + dexArrayLength + 4;
			byte[] newDex = new byte[newDexLength];
			System.arraycopy(dexArray, 0, newDex, 0, dexArrayLength);
			System.arraycopy(apkArray, 0, newDex, dexArrayLength, apkArrayLength);
			// 最后加上源apk的长度
			System.arraycopy(int2Byte(apkArrayLength), 0, newDex, apkArrayLength + dexArrayLength, 4);
			// 修改新dex的内容，file size，sha1，和checksum文件

			// checksum，文件校验码，alder32算法，检查magic，checksum外的文件区域。
			// signature sha1签名hash算法。对checksum，signture外的文件区域进行加密，识别唯一文件。
			// file_size。文件大小
			// 文件改变时，这三个值要修改

			fixFileSize(newDex);// 32-35
			fixSHA1(newDex);// 12-32
			fixCheckSum(newDex);

			// 新dex文件输出到文件。
			String dst = "reinforingfile/classes.dex";
			File dstFile = new File(dst);
			if (!dstFile.exists()) {
				dstFile.createNewFile();
			}
			System.out.println(newDex.length);
			FileOutputStream fos = new FileOutputStream(dstFile);
			fos.write(newDex);
			fos.flush();
			fos.close();
			System.out.println("成功生成新dex文件");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// 更新checksum 8-12字节处。
	private static void fixCheckSum(byte[] dexBytes) {
		Adler32 alder = new Adler32();
		alder.update(dexBytes, 12, dexBytes.length - 12);
		int value = (int) alder.getValue();
		byte[] newcs = int2Byte(value);
		byte[] recs = new byte[4];
		for (int i = 0; i < 4; i++) {
			recs[i] = newcs[newcs.length - 1 - i];
		}
		// 4个字节
		System.arraycopy(recs, 0, dexBytes, 8, 4);
	}

	// 更新SHA1值，在12-32字节处
	private static void fixSHA1(byte[] dexBytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			// 从字节数组32位开始，计算,
			md.update(dexBytes, 32, dexBytes.length - 32);
			// 拿到新的sha1计算的字节
			byte[] newdt = md.digest();
			// 从12字节开始，拷贝20字节
			System.arraycopy(newdt, 0, dexBytes, 12, 20);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	// 更新file_size，在32-35字节处
	private static void fixFileSize(byte[] dexBytes) {
		// int类型转换字节是32位，4个字节
		byte[] newFileSize = int2Byte(dexBytes.length);

		byte[] refs = new byte[4];
		// 字节数组高低位互换
		for (int i = 0; i < 4; i++) {
			refs[i] = newFileSize[newFileSize.length - 1 - i];
		}
		// 从32个字节开始，修改4个字节。
		System.arraycopy(refs, 0, dexBytes, 32, 4);
	}

	// int类型转换字节数组
	public static byte[] int2Byte(int value) {
		byte[] bytes = new byte[4];
		for (int i = 3; i >= 0; i--) {
			bytes[i] = (byte) (value % 256);
			value >>= 8;
		}
		return bytes;
	}

	// 文件流转换字节数组
	private static byte[] readFileBytes(File file) {
		byte[] arrayBytes = new byte[1024];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			FileInputStream fis = new FileInputStream(file);
			// int exeCount = 1;
			// 一次读取1024字节
			while (true) {
				// System.out.println(exeCount++);
				int count = fis.read(arrayBytes);
				if (count != -1) {
					out.write(arrayBytes, 0, count);
				} else {
					return out.toByteArray();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
