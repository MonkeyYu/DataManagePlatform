package com.etone.universe.dmp.problem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class FileCharsetTest {

	public static String DefaultSrcEncodeFormat = "GBK";
	public static String DefaultDestEncodeFormat = "UTF-8";
	public static String UnsupportedEncodingExceptionError = "编码格式错误！";
	public static String FileNotFoundExceptionError = "文件不存在！";
	public static String IOExceptionError = "文件读写错误！";

	public static String readFile(String path, String encodeFormat) {
		if ((encodeFormat == null || encodeFormat.equals(""))) {
			encodeFormat = DefaultSrcEncodeFormat;
		}
		try {
			String context = "";
			InputStreamReader isr;
			isr = new InputStreamReader(new FileInputStream(path), encodeFormat);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				context += line + "\r\n";
				System.out.println(line);
			}
			br.close();

			return context;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println(UnsupportedEncodingExceptionError);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(FileNotFoundExceptionError);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(IOExceptionError);
			e.printStackTrace();
		}
		;
		return "";

	}

	public static String transfer(String context, String encodeFormat) {
		if (encodeFormat == null || encodeFormat.equals(""))
			encodeFormat = DefaultDestEncodeFormat;
		try {
			byte[] content = context.getBytes();
			String result = new String(content, encodeFormat);
			return result;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			System.out.println(UnsupportedEncodingExceptionError);
			e.printStackTrace();
		}
		return "";

	}

	public static void writeFile(String context, String path, String destEncode) {
		File file = new File(path);
		if (file.exists())
			file.delete();
		BufferedWriter writer;
		try {
			FileOutputStream fos = new FileOutputStream(path, true);
			writer = new BufferedWriter(new OutputStreamWriter(fos, destEncode));
			writer.append(context);
			writer.close();
		} catch (IOException e) {
			System.out.println(IOExceptionError);
			e.printStackTrace();
		}

	}

	public static void writeFile(String context, String path) {
		File file = new File(path);
		if (file.exists())
			file.delete();
		Writer writer;
		try {
			writer = new FileWriter(file, true);
			writer.append(context);
			writer.close();
		} catch (IOException e) {
			System.out.println(IOExceptionError);
			e.printStackTrace();
		}

	}

	public static void transfer(String srcPath, String destPath,
			String srcEncode, String destEncode) {
		if (destPath == null || destPath.equals(""))
			destPath = srcPath;
		String context = readFile(srcPath, srcEncode);
		context = transfer(context, destEncode);
		writeFile(context, destPath, destEncode);

	}

	public static void transfer(String srcPath, String destPath,
			String destEncode) {
		transfer(srcPath, destPath, DefaultSrcEncodeFormat, destEncode);
	}

	public static void main(String args[]) {
		String path1 = "/opt/ltemr/edmp_problem/lte_data/111.csv";
		String path2 = "/opt/ltemr/edmp_problem/lte_data/222.csv";
		transfer(path1, path2, "UTF-8");

	}
}