package com.linda.jetcd;

public interface EtcdWatchCallback {
	
	public void onChange(EtcdFuture future);

}
