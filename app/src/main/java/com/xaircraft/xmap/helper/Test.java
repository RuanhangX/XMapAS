package com.xaircraft.xmap.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * ����ģ��HTTP������GET/POST��ʽ
 *
 * @author Sarin
 *
 */
public class Test {
	/**
	 * ����GET����
	 *
	 * @param url
	 *            Ŀ�ĵ�ַ
	 * @param parameters
	 *            �������Map���͡�
	 * @return Զ����Ӧ���
	 */
	public static String sendGet(String url, Map<String, String> parameters) {
		String result = "";// ���صĽ��
		BufferedReader in = null;// ��ȡ��Ӧ������
		StringBuffer sb = new StringBuffer();// �洢����
		String params = "";// ����֮��Ĳ���
		try {
			// �����������
			if (parameters.size() == 1) {
				for (String name : parameters.keySet()) {
					sb.append(name)
							.append("=")
							.append(java.net.URLEncoder.encode(
									parameters.get(name), "UTF-8"));
				}
				params = sb.toString();
			} else {
				for (String name : parameters.keySet()) {
					sb.append(name)
							.append("=")
							.append(java.net.URLEncoder.encode(
									parameters.get(name), "UTF-8")).append("&");
				}
				String temp_params = sb.toString();
				params = temp_params.substring(0, temp_params.length() - 1);
			}
			String full_url = url + "&" + params;
			System.out.println(full_url);
			// ����URL����
			java.net.URL connURL = new java.net.URL(full_url);
			// ��URL����
			java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL
					.openConnection();
			// ����ͨ������
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			// ����ʵ�ʵ�����
			httpConn.connect();
			// ��Ӧͷ����ȡ
			Map<String, List<String>> headers = httpConn.getHeaderFields();
			// �������е���Ӧͷ�ֶ�
			for (String key : headers.keySet()) {
				System.out.println(key + "\t��\t" + headers.get(key));
			}
			// ����BufferedReader����������ȡURL����Ӧ,�����ñ��뷽ʽ
			in = new BufferedReader(new InputStreamReader(
					httpConn.getInputStream(), "UTF-8"));
			String line;
			// ��ȡ���ص�����
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * ����POST����
	 *
	 * @param url
	 *            Ŀ�ĵ�ַ
	 * @param parameters
	 *            �������Map���͡�
	 * @return Զ����Ӧ���
	 */
	public static String sendPost(String strURL, String params) {
		try {
			URL url = new URL(strURL);// ��������
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("POST"); // ��������ʽ
			connection.setRequestProperty("Accept", "application/json"); // ���ý�����ݵĸ�ʽ
			connection.setRequestProperty("Content-Type", "application/json"); // ���÷�����ݵĸ�ʽ
			connection.connect();
			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream(), "UTF-8"); // utf-8����
			out.append(params);
			out.flush();
			out.close();
			// ��ȡ��Ӧ
			int length = (int) connection.getContentLength();// ��ȡ����
			InputStream is = connection.getInputStream();
			if (length != -1) {
				byte[] data = new byte[length];
				byte[] temp = new byte[512];
				int readLen = 0;
				int destPos = 0;
				while ((readLen = is.read(temp)) > 0) {
					System.arraycopy(temp, 0, data, destPos, readLen);
					destPos += readLen;
				}
				String result = new String(data, "UTF-8"); // utf-8����
				System.out.println(result);
				return result;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error"; // �Զ��������Ϣ
	}

	public static String encryptSHA1(String data) {
		if (data.isEmpty()) {
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			digest.update(data.getBytes("utf-8"));
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {

		} catch (UnsupportedEncodingException e) {

		}
		return null;
	}
}