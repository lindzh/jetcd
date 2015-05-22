package com.linda.jetcd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

public class EtcdClient implements EtcdAdminClient{
	
	private String etcdUrl;
	
	private ThreadFactory threadFactory = Executors.defaultThreadFactory();
	
	private EtcdWatcher watcher = new EtcdWatcher();
	
	private Logger logger = Logger.getLogger("etcd");
	
	private HttpComponent httpComponent = new HttpComponent();
	
	public void start(){
		watcher.start();
	}
	
	public void stop(){
		watcher.stop();
	}
	
	public EtcdClient(String url){
		this.etcdUrl = url;
	}
	
	private void logRequest(String url,Map<String,Object> bodyParams,String body,HttpResponseMeta responseMeta){
		StringBuilder sb = new StringBuilder();
		sb.append("url:"+url);
		if(bodyParams!=null){
			sb.append("body:"+JSONUtils.toJSON(bodyParams));
		}
		if(body!=null){
			sb.append("body:"+body);
		}
		if(responseMeta!=null){
			sb.append(" response:"+responseMeta.getStatusCode()+" "+responseMeta.getResponseAsString());
		}
		logger.info(sb.toString());
	}
	
	private EtcdResult handleIOException(IOException e){
		if(e instanceof FileNotFoundException){
			EtcdResult result = new EtcdResult();
			result.setErrorCode(100);
			result.setMessage("resource not found");
			result.setCause("key not exist");
			result.setAction("get");
			return result;
		}else{
			throw new EtcdException(e);
		}
	}
	
	private EtcdResult parse(HttpResponseMeta response){
		if(response!=null){
			String responseAsString = response.getResponseAsString();
			return JSONUtils.fromJSON(responseAsString, EtcdResult.class);
		}else{
			throw new EtcdException("can't get response");
		}
	}
	
	private String genUrl(String base,String path){
		return etcdUrl + base + path;
	}
	
	public EtcdResult version(){
		String url = this.genUrl("","/version");
		HttpResponseMeta responseMeta = httpComponent.httpGet(url, null, null);
		this.logRequest(url, null, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	/**
	 * create a dir or set a key value,when create dir , value is not supported
	 * @param key
	 * @param value
	 * @param dir
	 * @return
	 */
	public EtcdResult set(String key,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("value", value);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	/**
	 * create a dir or set a key value,when create dir , value is not supported
	 * @param key
	 * @param value
	 * @param ttl
	 * @param dir
	 * @return
	 */
	public EtcdResult set(String key,String value,int ttl){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("ttl", ttl);
		params.put("value", value);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult get(String key){
		String url = this.genUrl("/v2/keys",key);
		HttpResponseMeta responseMeta = httpComponent.httpGet(url, null, null);
		this.logRequest(url, null, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult del(String key){
		String url = this.genUrl("/v2/keys",key);
		HttpResponseMeta responseMeta = httpComponent.httpDelete(url, null, null);
		this.logRequest(url, null, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	/**
	 * this method is used to create a dir
	 * @param key
	 * @return
	 */
	public EtcdResult dir(String key){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("dir", true);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	/**
	 * del a dir
	 * @param key
	 * @param recursive
	 * @return
	 */
	public EtcdResult delDir(String key,boolean recursive){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dir", true);
		if(recursive){
			params.put("recursive", recursive);
		}
		HttpResponseMeta responseMeta = httpComponent.httpDelete(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	/**
	 * 获取某一目录下节点
	 * @param dir
	 * @param recursive
	 * @return
	 */
	public EtcdResult children(String dir,boolean recursive,boolean sorted){
		String url = this.genUrl("/v2/keys",dir);
		Map<String, Object> params = new HashMap<String,Object>();
		if(recursive){
			params.put("recursive", recursive);
		}
		if(sorted){
			params.put("sorted", sorted);
		}
		HttpResponseMeta responseMeta = httpComponent.httpGet(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	/**
	 * 创建队列，用于获取锁，无超时时间
	 * @param queue
	 * @param value
	 * @return
	 */
	public EtcdResult queue(String queue,String value){
		String url = this.genUrl("/v2/keys",queue);
		Map<String, Object> params = new HashMap<String,Object>();
		if(value!=null){
			params.put("value", value);
		}
		HttpResponseMeta responseMeta = httpComponent.httpPost(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	/**
	 * 创建有序队列，用于获取锁，同时设置ttl超时时间
	 * @param queue
	 * @param value
	 * @param ttl
	 * @return
	 */
	public EtcdResult queue(String queue,String value,int ttl){
		String url = this.genUrl("/v2/keys",queue);
		Map<String, Object> params = new HashMap<String,Object>();
		if(value!=null){
			params.put("value", value);
		}
		params.put("ttl", ttl);
		HttpResponseMeta responseMeta = httpComponent.httpPost(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	/**
	 * watch a key for change
	 * use a http long connection if a key change the text response,then use callback to notify
	 * @param key
	 * @param callback
	 */
	private void watch0(String key,EtcdWatchCallback callback){
		final String url = this.genUrl("/v2/keys", key);
		final Map<String, Object> params = new HashMap<String, Object>();
		final EtcdChangeResult future = new EtcdChangeResult();
		future.setKey(key);
		future.setUrl(url);
		params.put("wait", true);
		watcher.addCallback(future, callback);
		Thread executeThread = threadFactory.newThread(new Runnable() {
			public void run() {
				try {
					HttpResponseMeta responseMeta = httpComponent.httpGet(url, null, params);
					EtcdResult etcdResult = EtcdClient.this.parse(responseMeta);
					future.setResult(etcdResult);
					future.setDone(true);
				} catch (Exception e) {
					future.setResult(null);
					future.setDone(true);
					future.setFailReason("http request exception:"+e.getMessage());
					throw new EtcdException(e);
				}
			}
		});
		executeThread.start();
	}
	
	public void watchChildren(String dir,boolean recursive,boolean sorted,EtcdWatchCallback callback){
		Map<String, Object> params = new HashMap<String,Object>();
		if(recursive){
			params.put("recursive", recursive);
		}
		if(sorted){
			params.put("sorted", sorted);
		}
		this.watch0(dir, params, callback);
	}
	
	private void watch0(String key,final Map<String,Object> params,EtcdWatchCallback callback){
		final String url = this.genUrl("/v2/keys", key);
		final EtcdChangeResult future = new EtcdChangeResult();
		future.setKey(key);
		future.setUrl(url);
		params.put("wait", true);
		watcher.addCallback(future, callback);
		FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>(){
			@Override
			public void cancelled() {
				EtcdClient.this.logRequest(url, params, null, null);
				future.setResult(null);
				future.setDone(true);
				future.setFailReason("http request cancelled");
			}

			@Override
			public void completed(HttpResponse response) {
				try{
					HttpResponseMeta responseMeta = httpComponent.getResponse(response);
					EtcdClient.this.logRequest(url, params, null, responseMeta);
					EtcdResult etcdResult = EtcdClient.this.parse(responseMeta);
					future.setResult(etcdResult);
					future.setDone(true);
				}catch(Exception e){
					future.setResult(null);
					future.setDone(true);
					future.setFailReason("http request exception:"+e.getMessage());
				}
			}
			@Override
			public void failed(Exception e) {
				EtcdClient.this.logRequest(url, params, null, null);
				future.setResult(null);
				future.setDone(true);
				future.setFailReason("http request exception:"+e.getMessage());
			}
		};
		httpComponent.httpAsyncGet(url, null, params, futureCallback);
	}
	
	public void watch(String key,EtcdWatchCallback callback){
		Map<String, Object> params = new HashMap<String,Object>();
		this.watch0(key, params, callback);
	}
	
	public EtcdResult cas(String key,String value,boolean prevExist){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("value", value);
		params.put("prevExist", prevExist);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult cas(String key,String value,int ttl,boolean prevExist){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		if(value!=null){
			params.put("value", value);
		}
		params.put("ttl", ttl);
		params.put("prevExist", prevExist);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult cas(String key,String value,String prevValue){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("value", value);
		params.put("prevValue", prevValue);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult cas(String key,String value,int ttl,String prevValue){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		if(value!=null){
			params.put("value", value);
		}
		params.put("ttl", ttl);
		params.put("prevValue", prevValue);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult cas(String key,String value,int prevIndex){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("value", value);
		params.put("prevIndex", prevIndex);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult cas(String key,String value,int ttl,int prevIndex){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		if(value!=null){
			params.put("value", value);
		}
		params.put("ttl", ttl);
		params.put("prevIndex", prevIndex);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult cad(String key,String prevValue,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevValue", prevValue);
		params.put("value", value);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public EtcdResult cad(String key,boolean prevExist,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevExist", prevExist);
		params.put("value", value);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}

	public EtcdResult cad(String key,int prevIndex,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevIndex", prevIndex);
		params.put("value", value);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, null, params);
		this.logRequest(url, params, null, responseMeta);
		return this.parse(responseMeta);
	}
	
	public List<EtcdMember> members(){
		String url = this.genUrl("/v2/members","");
		HttpResponseMeta responseMeta = httpComponent.httpGet(url, null, null);
		this.logRequest(url, null, null, responseMeta);
		if(responseMeta!=null){
			if(responseMeta.getStatusCode()==200){
				String resp = responseMeta.getResponseAsString();
				Map json = JSONUtils.fromJSON(resp, Map.class);
				String memberJson = json.get("members").toString();
				return JSONUtils.fromJSON(memberJson, new TypeReference<List<EtcdMember>>(){});
			}else{
				throw new EtcdException("status code:"+responseMeta.getStatusCode()+" resp:"+responseMeta.getResponseAsString());
			}
		}else{
			throw new EtcdException("null response");
		}
	}
	
	public EtcdMember addMembers(List<String> members){
		String url = this.genUrl("/v2/members","");
		HashMap<String, Object> body = new HashMap<String,Object>();
		body.put("peerURLs", members);
		String json = JSONUtils.toJSON(body);
		HttpResponseMeta responseMeta = httpComponent.httpPost(url, httpComponent.HEAD_JSON, json);
		this.logRequest(url, null, json, responseMeta);
		if(responseMeta!=null){
			int code = responseMeta.getStatusCode();
			if(code>=200&&code<300){
				String resp = responseMeta.getResponseAsString();
				return JSONUtils.fromJSON(resp, EtcdMember.class);
			}else{
				throw new EtcdException("status code:"+responseMeta.getStatusCode()+" resp:"+responseMeta.getResponseAsString());
			}
		}else{
			throw new EtcdException("null response");
		}
	}
	
	public boolean delMember(String id){
		String url = this.genUrl("/v2/members/",id);
		HttpResponseMeta responseMeta = httpComponent.httpDelete(url, null, null);
		this.logRequest(url, null, null, responseMeta);
		if(responseMeta!=null){
			int code = responseMeta.getStatusCode();
			if(code/200==1&&code%200<100){
				return true;
			}
		}
		return false;
	}
	
	public EtcdMember setMembers(String id,List<String> members){
		String url = this.genUrl("/v2/members/",id);
		HashMap<String, Object> body = new HashMap<String,Object>();
		body.put("peerURLs", members);
		String json = JSONUtils.toJSON(body);
		HttpResponseMeta responseMeta = httpComponent.httpPut(url, httpComponent.HEAD_JSON, json);
		this.logRequest(url, null, json, responseMeta);
		if(responseMeta!=null){
			int code = responseMeta.getStatusCode();
			String resp = responseMeta.getResponseAsString();
			return JSONUtils.fromJSON(resp, EtcdMember.class);
		}else{
			throw new EtcdException("null response");
		}
	}
}
