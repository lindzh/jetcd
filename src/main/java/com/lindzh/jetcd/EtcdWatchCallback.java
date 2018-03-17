package com.lindzh.jetcd;

public interface EtcdWatchCallback {
	
	public void onChange(EtcdChangeResult future);

}
