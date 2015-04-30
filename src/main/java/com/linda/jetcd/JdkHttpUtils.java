package com.linda.jetcd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class JdkHttpUtils {
	
	public static int DEFAULT_CONNECTION_TIMEOUT = 30000;
	public static int DEFAULT_READ_TIMEOUT = 30000;
	public static String DEFAULT_ENCODING = "utf-8";
	
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
    private static final int EOF = -1;
	
	public static final Map<String,String> HEAD_JSON = new HashMap<String,String>();
	
	private static Logger logger = Logger.getLogger(JdkHttpUtils.class);
	
	static {
		HEAD_JSON.put("Content-Type", "Application/json");
		HEAD_JSON.put("Accept", "Application/json");
	}
	
	public static HttpURLConnection getConnection(String uri) throws IOException{
		return JdkHttpUtils.getConnection(uri, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
	}
	
	public static HttpURLConnection getConnection(String uri,int timeout) throws IOException{
		return JdkHttpUtils.getConnection(uri, timeout, timeout);
	}
	
	public static HttpURLConnection getConnection(String uri,int connectionTimeout,int readTimeout) throws IOException{
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setConnectTimeout(connectionTimeout);
		connection.setReadTimeout(readTimeout);
		return connection;
	}
	
	public static void initMethod(HttpURLConnection connection,String method) throws ProtocolException{
		connection.setRequestMethod(method);
		connection.setUseCaches(false);
		connection.setDoInput(true);
		if(!method.equalsIgnoreCase("get")){
			connection.setDoOutput(true);
		}
	}
	public static HttpResponseMeta getHttpResponse(HttpURLConnection connection) throws IOException{
		HttpResponseMeta resp = new HttpResponseMeta();
		int code = connection.getResponseCode();
		resp.setResponseCode(code);
		String message = connection.getResponseMessage();
		Object content = connection.getContent();
		resp.setResponseMessage(message);
//		if(code/200==1&&code%200<100){
//			byte[] buffer = JdkHttpUtils.toByteArray(connection);
//			resp.setResponse(buffer);
//		}

		connection.disconnect();
		return resp;
	}
	
	public static String getEncoding(String contentType) {
		if (contentType != null) {
			String[] strs = contentType.split(";");
			if (strs != null && strs.length > 1) {
				String charSet = strs[1].trim();
				String[] charSetKeyValues = charSet.split("=");
				if (charSetKeyValues.length == 2&&charSetKeyValues[0].equalsIgnoreCase("charset")) {
					return charSetKeyValues[1];
				}
			}
		}
		return DEFAULT_ENCODING;
	}
	
	public static HttpResponseMeta httpGet(String url,Map<String,Object> params,Map<String,String> headers) throws IOException{
		return JdkHttpUtils.httpURL(url, params, headers, "GET",DEFAULT_READ_TIMEOUT);
	}
	
	public static HttpResponseMeta httpGet(String url,Map<String,Object> params,Map<String,String> headers,int timeout) throws IOException{
		return JdkHttpUtils.httpURL(url, params, headers, "GET",timeout);
	}
	
	public static HttpResponseMeta httpDelete(String url,Map<String,Object> params,Map<String,String> headers) throws IOException{
		return JdkHttpUtils.httpURL(url, params, headers, "DELETE",DEFAULT_READ_TIMEOUT);
	}
	
	private static String genUrl(String url,Map<String,Object> params){
		if(params==null||params.size()<1){
			return url;
		}else{
			return url+"?"+JdkHttpUtils.encodeParams(params);
		}
	}
	
	private static HttpResponseMeta httpURL(String url,Map<String,Object> params,Map<String,String> headers,String method,int timeout) throws IOException{
		url = JdkHttpUtils.genUrl(url, params);
		HttpURLConnection connection = JdkHttpUtils.getConnection(url,timeout);
		JdkHttpUtils.setHeaders(connection, headers);
		JdkHttpUtils.setEncoding(connection, DEFAULT_ENCODING);
		JdkHttpUtils.initMethod(connection, method);
		connection.connect();
		return JdkHttpUtils.getHttpResponse(connection);
	}
	
	public static HttpResponseMeta httpPostWithParams(String url,Map<String,Object> params,Map<String,String> headers) throws IOException{
		return JdkHttpUtils.httpWithParams(url, params, headers, "POST");
	}
	
	private static HttpResponseMeta httpWithParams(String url,Map<String,Object> params,Map<String,String> headers,String method) throws IOException{
		HttpURLConnection connection = JdkHttpUtils.getConnection(url);
		JdkHttpUtils.setHeaders(connection, headers);
		JdkHttpUtils.setEncoding(connection, DEFAULT_ENCODING);
		JdkHttpUtils.initMethod(connection, method);
		String body = JdkHttpUtils.encodeParams(params);
		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
		JdkHttpUtils.sendBody(connection, body.getBytes());
		return JdkHttpUtils.getHttpResponse(connection);
	}
	
	public static HttpResponseMeta httpPutWithParams(String url,Map<String,Object> params,Map<String,String> headers) throws IOException{
		return JdkHttpUtils.httpWithParams(url, params, headers, "PUT");
	}
	
	public static void setEncoding(HttpURLConnection connection,String encoding){
		connection.setRequestProperty("Content-Encoding", encoding);
	}
	
	public static void sendBody(HttpURLConnection connection,byte[] buffer) throws IOException{
		OutputStream stream = connection.getOutputStream();
		JdkHttpUtils.write(buffer, stream);
	}
	
	public static HttpResponseMeta httpUrlParamsWithBody(String url,Map<String,String> headers,Map<String,Object> urlParams,String body,String method) throws IOException{
		url = JdkHttpUtils.genUrl(url, urlParams);
		HttpURLConnection connection = JdkHttpUtils.getConnection(url);
		JdkHttpUtils.setHeaders(connection, headers);
		JdkHttpUtils.setEncoding(connection, DEFAULT_ENCODING);
		JdkHttpUtils.initMethod(connection, method);
		if(body!=null){
			JdkHttpUtils.sendBody(connection, body.getBytes());
		}else{
			connection.connect();
		}
		return JdkHttpUtils.getHttpResponse(connection);
	}
	
	public static HttpResponseMeta httpPostUrlParamsWithBody(String url,Map<String,String> headers,Map<String,Object> urlParams,String body) throws IOException{
		return JdkHttpUtils.httpUrlParamsWithBody(url, headers, urlParams, body, "POST");
	}
	
	public static HttpResponseMeta httpPutUrlParamsWithBody(String url,Map<String,String> headers,Map<String,Object> urlParams,String body,String method) throws IOException{
		return JdkHttpUtils.httpUrlParamsWithBody(url, headers, urlParams, body, "PUT");
	}
	
	private static HttpResponseMeta httpWithBody(String url,String body,Map<String,String> headers,String method) throws IOException{
		HttpURLConnection connection = JdkHttpUtils.getConnection(url);
		JdkHttpUtils.setHeaders(connection, headers);
		JdkHttpUtils.setEncoding(connection, DEFAULT_ENCODING);
		JdkHttpUtils.initMethod(connection, method);
		if(body!=null){
			JdkHttpUtils.sendBody(connection, body.getBytes());
		}else{
			connection.connect();
		}
		return JdkHttpUtils.getHttpResponse(connection);
	}
	
	public static HttpResponseMeta httpPostWithBody(String url,String body,Map<String,String> headers) throws IOException{
		return JdkHttpUtils.httpWithBody(url, body, headers, "POST");
	}
	
	public static HttpResponseMeta httpPutWithBody(String url,String body,Map<String,String> headers) throws IOException{
		return JdkHttpUtils.httpWithBody(url, body, headers, "PUT");
	}
	
	public static void setHeaders(HttpURLConnection connection,Map<String,String> headers){
		if(headers!=null){
			Set<String> keys = headers.keySet();
			Iterator<String> it = keys.iterator();
			while(it.hasNext()){
				String key = it.next();
				String value = headers.get(key);
				connection.setRequestProperty(key, value);
			}
		}
	}
	
	public static String encodeParams(Map<String, Object> params) {
		StringBuilder sb = new StringBuilder();
		if (params != null) {
			Set<String> keys = params.keySet();
			int first = 0;
			for (String key : keys) {
				Object value = params.get(key);
				if(first>0){
					sb.append("&");
				}
				first++;
				sb.append(key);
				sb.append("=");
				String v = String.valueOf(value);
				try {
					String encodeValue = URLEncoder.encode(v, DEFAULT_ENCODING);
					sb.append(encodeValue);
				} catch (UnsupportedEncodingException e) {
					logger.error("UnsupportedEncoding:"+DEFAULT_ENCODING);
				}
			}
		}
		return sb.toString();
	}
	
	public static byte[] toByteArray(URLConnection urlConn) throws IOException{
		try {
			InputStream ins = urlConn.getInputStream();
			try{
		        ByteArrayOutputStream output = new ByteArrayOutputStream();
		        copy(ins, output);
		        return output.toByteArray();
			}finally{
				ins.close();
			}
		} catch (IOException e) {
			throw e;
		}
	}
	
    public static int copy(InputStream input, OutputStream output) throws IOException {
    	byte[] buffer =new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }
    
    public static void write(byte[] data, OutputStream output)
            throws IOException {
        if (data != null) {
            output.write(data);
        }
    }
}
