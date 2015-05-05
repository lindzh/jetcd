package com.linda.jetcd;

import java.util.concurrent.atomic.AtomicBoolean;

public class EtcdChangeResult {

	private String key;
	private String url;
	private EtcdResult result;
	private AtomicBoolean done = new AtomicBoolean(false);

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public EtcdResult getResult() {
		return result;
	}

	public void setResult(EtcdResult result) {
		this.result = result;
	}
	
	public boolean isDone(){
		return done.get();
	}
	
	protected void setDone(boolean doned){
		done.set(doned);
	}
}
