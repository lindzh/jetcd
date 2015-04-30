package com.linda.jetcd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class EtcdClient implements EtcdAdminClient{
	
	private String etcdUrl;
	
	private ThreadFactory threadFactory = Executors.defaultThreadFactory();
	
	private EtcdWatcher watcher = new EtcdWatcher();
	
	private int watchTimeOut = 300000;
	
	public void start(){
		watcher.start();
	}
	
	public void stop(){
		watcher.stop();
	}
	
	public EtcdClient(String url){
		this.etcdUrl = url;
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
			String responseAsString = response.getResponseMessage();
			return JSONUtils.fromJSON(responseAsString, EtcdResult.class);
		}else{
			throw new EtcdException("can't get response");
		}
	}
	
	private String genUrl(String base,String path){
		return etcdUrl + base + path;
	}
	
	public EtcdResult getVersion(){
		try {
			String url = this.genUrl("","/version");
			HttpResponseMeta responseMeta = JdkHttpUtils.httpGet(url, null, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	public EtcdResult set(String key,String value,boolean dir){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("value", value);
		if(dir){
			params.put("dir", true);
		}
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithParams(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	public EtcdResult set(String key,String value,int ttl,boolean dir){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		if(value!=null){
			params.put("value", value);
		}
		params.put("ttl", ttl);
		if(dir){
			params.put("dir", true);
		}
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithParams(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	public EtcdResult get(String key){
		String url = this.genUrl("/v2/keys",key);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpGet(url, null, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	public EtcdResult del(String key,boolean dir,boolean recursive){
		String url = this.genUrl("/v2/keys",key);
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			if(dir){
				params.put("dir", dir);
				if(recursive){
					params.put("recursive", recursive);
				}
			}
			HttpResponseMeta responseMeta = JdkHttpUtils.httpDelete(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	/**
	 * watch a key for change
	 * use a http long connection if a key change the text response,then use callback to notify
	 * @param key
	 * @param callback
	 */
	public void watch(String key,EtcdWatchCallback callback){
		final String url = this.genUrl("/v2/keys", key);
		final Map<String, Object> params = new HashMap<String, Object>();
		final EtcdFuture future = new EtcdFuture();
		future.setKey(key);
		future.setUrl(url);
		params.put("wait", true);
		watcher.addCallback(future, callback);
		Thread executeThread = threadFactory.newThread(new Runnable() {
			public void run() {
				try {
					HttpResponseMeta responseMeta = JdkHttpUtils.httpGet(url,params, null,watchTimeOut);
					EtcdResult etcdResult = EtcdClient.this.parse(responseMeta);
					future.setResult(etcdResult);
					future.setDone(true);
				} catch (Exception e) {
					future.setResult(null);
					future.setDone(true);
					throw new EtcdException(e);
				}
			}
		});
		executeThread.start();
	}
	
	public EtcdResult compareAndSwap(String key,String prevValue,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevValue", prevValue);
		params.put("value", value);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithParams(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	public EtcdResult compareAndSwap(String key,boolean prevExist,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevExist", prevExist);
		params.put("value", value);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithParams(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}

	public EtcdResult compareAndSwap(String key,int prevIndex,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevIndex", prevIndex);
		params.put("value", value);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithParams(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	
	public EtcdResult compareAndDelete(String key,String prevValue,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevValue", prevValue);
		params.put("value", value);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithParams(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	public EtcdResult compareAndDelete(String key,boolean prevExist,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevExist", prevExist);
		params.put("value", value);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithParams(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}

	public EtcdResult compareAndDelete(String key,int prevIndex,String value){
		String url = this.genUrl("/v2/keys",key);
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("prevIndex", prevIndex);
		params.put("value", value);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithParams(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	public EtcdResult children(String dir,boolean recursive){
		String url = this.genUrl("/v2/keys",dir);
		Map<String, Object> params = new HashMap<String,Object>();
		if(recursive){
			params.put("recursive", recursive);
		}
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpGet(url, params, null);
			return this.parse(responseMeta);
		} catch (IOException e) {
			return this.handleIOException(e);
		}
	}
	
	public List<EtcdMember> members(){
		String url = this.genUrl("/v2/members","");
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpGet(url, null, null);
			if(responseMeta!=null){
				if(responseMeta.getResponseCode()==200){
					String resp = responseMeta.getResponseMessage();
					JSONObject jsonObject = JSONUtils.fromJSON(resp, JSONObject.class);
					JSONArray array = jsonObject.getJSONArray("members");
					Collection collection = JSONArray.toCollection(array, EtcdMember.class);
					List<EtcdMember> members = new ArrayList<EtcdMember>();
					members.addAll(collection);
					return members;
				}else{
					throw new EtcdException("status code:"+responseMeta.getResponseCode()+" resp:"+responseMeta.getResponseMessage());
				}
			}else{
				throw new EtcdException("null response");
			}
		} catch (IOException e) {
			throw new EtcdException(e);
		}
	}
	
	public EtcdMember addMembers(List<String> members){
		String url = this.genUrl("/v2/members","");
		HashMap<String, Object> body = new HashMap<String,Object>();
		body.put("peerURLs", members);
		String json = JSONUtils.toJSON(body);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPostWithBody(url, json, JdkHttpUtils.HEAD_JSON);
			if(responseMeta!=null){
				if(responseMeta.getResponseCode()==200){
					String resp = responseMeta.getResponseMessage();
					return JSONUtils.fromJSON(resp, EtcdMember.class);
				}else{
					throw new EtcdException("status code:"+responseMeta.getResponseCode()+" resp:"+responseMeta.getResponseMessage());
				}
			}else{
				throw new EtcdException("null response");
			}
		} catch (IOException e) {
			throw new EtcdException(e);
		}
	}
	
	public boolean delMember(String id){
		String url = this.genUrl("/v2/members/",id);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpDelete(url, null, null);
			if(responseMeta!=null){
				int code = responseMeta.getResponseCode();
				if(code/200==1&&code%200<100){
					return true;
				}
			}
			return false;
		} catch (IOException e) {
			throw new EtcdException(e);
		}
	}
	
	public EtcdMember setMembers(String id,List<String> members){
		String url = this.genUrl("/v2/members/",id);
		HashMap<String, Object> body = new HashMap<String,Object>();
		body.put("peerURLs", members);
		String json = JSONUtils.toJSON(body);
		try {
			HttpResponseMeta responseMeta = JdkHttpUtils.httpPutWithBody(url, json, JdkHttpUtils.HEAD_JSON);
			if(responseMeta!=null){
				if(responseMeta.getResponseCode()==200){
					String resp = responseMeta.getResponseMessage();
					return JSONUtils.fromJSON(resp, EtcdMember.class);
				}else{
					throw new EtcdException("status code:"+responseMeta.getResponseCode()+" resp:"+responseMeta.getResponseMessage());
				}
			}else{
				throw new EtcdException("null response");
			}
		} catch (IOException e) {
			throw new EtcdException(e);
		}
	}
}
