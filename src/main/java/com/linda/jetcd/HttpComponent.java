package com.linda.jetcd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

public class HttpComponent {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private static final int EOF = -1;
	
	private Logger logger = Logger.getLogger(HttpComponent.class);

	public static final Map<String, String> HEAD_JSON = new HashMap<String, String>();
	
	private int defaultTimeout = 30000; //30s
	
	private int longConnectionTimeout = 300000;//5 min
	
	private PoolingHttpClientConnectionManager connectionManager;
	
	private PoolingNHttpClientConnectionManager nhttpConnectionManager;	
	private CloseableHttpClient defaultHttpClient;
	
	private CloseableHttpClient longHttpClient;
	
	private CloseableHttpAsyncClient asyncHttpClient;
	
	static{
		HEAD_JSON.put("Content-Type", "application/json");
		HEAD_JSON.put("Accept", "application/json");
	}
	
	private HttpClient getInstance() {
		return defaultHttpClient;
	}
	
	private HttpClient getLongHttpClient(){
		return longHttpClient;
	}
	
	private HttpAsyncClient getHttpAsyncClient(){
		return asyncHttpClient;
	}
	
	public HttpComponent(){
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(50);
		try {
			nhttpConnectionManager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor());
		} catch (IOReactorException e) {
			throw new EtcdException(e);
		}
		nhttpConnectionManager.setMaxTotal(200);
		nhttpConnectionManager.setDefaultMaxPerRoute(50);
		
		RequestConfig defaultConfig = RequestConfig.custom().setSocketTimeout(defaultTimeout).setConnectTimeout(defaultTimeout).build();

		RequestConfig longConConfig = RequestConfig.custom().setSocketTimeout(longConnectionTimeout).setConnectTimeout(longConnectionTimeout).build();

		defaultHttpClient = HttpClients.custom().setDefaultRequestConfig(defaultConfig).setConnectionManager(connectionManager).build();
		longHttpClient = HttpClients.custom().setDefaultRequestConfig(longConConfig).setConnectionManager(connectionManager).build();
		asyncHttpClient = HttpAsyncClients.custom().setDefaultRequestConfig(longConConfig).setConnectionManager(nhttpConnectionManager).build();
		asyncHttpClient.start();
	}

	private String defaultEncoding = "utf-8";

	private String parseURL(String url) {
		if (url != null) {
			if (url.startsWith("http")) {
				return url;
			} else {
				return "http://" + url;
			}
		} else {
			return null;
		}
	}
	
	public String encodeParams(Map<String, Object> params) {
		StringBuilder sb = new StringBuilder();
		if (params != null) {
			Set<String> keys = params.keySet();
			int first = 0;
			for (String key : keys) {
				Object value = params.get(key);
				if (first > 0) {
					sb.append("&");
				}
				first++;
				sb.append(key);
				sb.append("=");
				String v = String.valueOf(value);
				try {
					String encodeValue = URLEncoder.encode(v, defaultEncoding);
					sb.append(encodeValue);
				} catch (UnsupportedEncodingException e) {
					logger.error("UnsupportedEncoding:" + defaultEncoding);
				}
			}
		}
		return sb.toString();
	}

	private void setHeaders(HttpRequestBase request, Map<String, String> headers) {
		if (request != null && headers != null) {
			Set<String> keys = headers.keySet();
			for (String key : keys) {
				String value = headers.get(key);
				request.setHeader(key, value);
			}
		}
	}

	private String getEncoding(String contentType) {
		if (contentType != null) {
			String[] strs = contentType.split(";");
			if (strs != null && strs.length > 1) {
				String charSet = strs[1].trim();
				String[] charSetKeyValues = charSet.split("=");
				if (charSetKeyValues.length == 2 && charSetKeyValues[0].equalsIgnoreCase("charset")) {
					return charSetKeyValues[1];
				}
			}
		}
		return defaultEncoding;
	}

	public HttpResponseMeta getResponse(HttpResponse response) {
		if (response != null) {
			StatusLine line = response.getStatusLine();
			if (line != null) {
				HttpResponseMeta responseMeta = new HttpResponseMeta();
				int code = line.getStatusCode();
				responseMeta.setStatusCode(code);
				if (code<500&&code!=204) {
					try {
						InputStream inputStream = response.getEntity().getContent();
						if (inputStream != null) {
							byte[] bs = this.toByteArray(inputStream);
							responseMeta.setResponse(bs);
							Header contentType = response.getEntity().getContentType();
							responseMeta.setContentType(contentType.getValue());
							responseMeta.setEncode(getEncoding(contentType.getValue()));
						}
					} catch (ClientProtocolException e) {
						this.handleNetException(e);
					} catch (IOException e) {
						this.handleNetException(e);
					}
				}
				return responseMeta;
			}
		}
		throw new EtcdException("http response null");
	}
	
	private void setBodyParameters(HttpEntityEnclosingRequestBase request,Map<String, Object> params) throws UnsupportedEncodingException{
		if (params != null) {
			List<NameValuePair> list = new LinkedList<NameValuePair>();
			Set<String> keys = params.keySet();
			for (String key : keys) {
				Object v = params.get(key);
				if(v!=null){
					list.add(new BasicNameValuePair(key, params.get(key).toString()));
				}
			}
			HttpEntity entity = new UrlEncodedFormEntity(list);
			request.setEntity(entity);
		}
	}
	
	public HttpResponseMeta httpPut(String url, Map<String, String> headers, Map<String, Object> params) {
		String newUrl = parseURL(url);
		HttpPut put = new HttpPut(newUrl);
		setHeaders(put, headers);
		HttpClient client = getInstance();
		try {
			this.setBodyParameters(put, params);
			HttpResponse response = client.execute(put);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}
	
	public HttpResponseMeta httpPut(String url, Map<String,Object> urlParams,Map<String, String> headers, String body) {
		String newUrl = parseURL(url);
		if (newUrl == null) {
			return null;
		}
		if (urlParams != null) {
			newUrl = newUrl + "?" + encodeParams(urlParams);
		}
		HttpPut put = new HttpPut(newUrl);
		setHeaders(put, headers);
		HttpClient client = getInstance();
		try {
			if (body != null) {
				StringEntity entity = new StringEntity(body,"utf-8");
				put.setEntity(entity);
			}
			HttpResponse response = client.execute(put);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}

	public HttpResponseMeta httpPut(String url, Map<String, String> headers, String body) {
		String newUrl = parseURL(url);
		HttpPut put = new HttpPut(newUrl);
		setHeaders(put, headers);
		HttpClient client = getInstance();
		try {
			if (body != null) {
				StringEntity entity = new StringEntity(body,"utf-8");
				put.setEntity(entity);
			}
			HttpResponse response = client.execute(put);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}

	public HttpResponseMeta httpPost(String url, Map<String, String> headers, Map<String, Object> params) {
		String newUrl = parseURL(url);
		HttpPost post = new HttpPost(newUrl);
		setHeaders(post, headers);
		HttpClient client = getInstance();
		try {
			this.setBodyParameters(post, params);
			HttpResponse response = client.execute(post);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}
	
	public HttpResponseMeta httpPost(String url, Map<String,Object> urlParams,Map<String, String> headers, String body) {
		String newUrl = parseURL(url);
		if (newUrl == null) {
			return null;
		}
		if (urlParams != null) {
			newUrl = newUrl + "?" + encodeParams(urlParams);
		}
		HttpPost post = new HttpPost(newUrl);
		setHeaders(post, headers);
		HttpClient client = getInstance();
		try {
			if (body != null) {
				StringEntity entity = new StringEntity(body,"utf-8");
				post.setEntity(entity);
			}
			HttpResponse response = client.execute(post);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}

	public HttpResponseMeta httpPost(String url, Map<String, String> headers, String body) {
		String newUrl = parseURL(url);
		HttpPost post = new HttpPost(newUrl);
		setHeaders(post, headers);
		HttpClient client = getInstance();
		try {
			if (body != null) {
				StringEntity entity = new StringEntity(body,"utf-8");
				post.setEntity(entity);
			}
			HttpResponse response = client.execute(post);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}
	
	public HttpResponseMeta httpDelete(String url, Map<String, String> headers, Map<String, Object> params) {
		String newUrl = parseURL(url);
		if (newUrl == null) {
			return null;
		}
		if (params != null) {
			newUrl = newUrl + "?" + encodeParams(params);
		}
		HttpDelete delete = new HttpDelete(newUrl);
		setHeaders(delete, headers);
		HttpClient client = getInstance();
		try {
			HttpResponse response = client.execute(delete);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}
	
	private void handleNetException(Exception e){
		logger.error("http exception:"+e.getMessage(),e);
		throw new EtcdException(e);
	}
	
	/**
	 * 发送异步请求回调
	 * @param url
	 * @param headers
	 * @param params
	 * @param callback
	 */
	public void httpAsyncGet(String url, Map<String, String> headers, Map<String, Object> params,final FutureCallback<HttpResponse> callback){
		String newUrl = parseURL(url);
		if (params != null) {
			newUrl = newUrl + "?" + encodeParams(params);
		}
		HttpGet get = new HttpGet(newUrl);
		setHeaders(get, headers);
		HttpAsyncClient asyncClient = getHttpAsyncClient();
		asyncClient.execute(get, callback);
	}
	
	public HttpResponseMeta httpGet(String url, Map<String, String> headers, Map<String, Object> params) {
		String newUrl = parseURL(url);
		if (newUrl == null) {
			return null;
		}
		if (params != null) {
			newUrl = newUrl + "?" + encodeParams(params);
		}
		HttpGet get = new HttpGet(newUrl);
		setHeaders(get, headers);
		HttpClient client = getInstance();
		try {
			HttpResponse response = client.execute(get);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}

	public HttpResponseMeta httpLongGet(String url, Map<String, String> headers, Map<String, Object> params) {
		String newUrl = parseURL(url);
		if (newUrl == null) {
			return null;
		}
		if (params != null) {
			newUrl = newUrl + "?" + encodeParams(params);
		}
		HttpGet get = new HttpGet(newUrl);
		setHeaders(get, headers);
		HttpClient client = getLongHttpClient();
		try {
			HttpResponse response = client.execute(get);
			return getResponse(response);
		} catch (ClientProtocolException e) {
			this.handleNetException(e);
		} catch (IOException e) {
			this.handleNetException(e);
		}
		throw new EtcdException("http "+url+" response null");
	}
	
	public byte[] toByteArray(InputStream ins) throws IOException {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			copy(ins, output);
			return output.toByteArray();
		} finally {
			ins.close();
		}
	}

	public int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
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
}
