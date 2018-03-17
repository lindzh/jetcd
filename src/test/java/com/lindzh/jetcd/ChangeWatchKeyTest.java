package com.lindzh.jetcd;

public class ChangeWatchKeyTest {
	
	public static void main(String[] args) throws InterruptedException {
		final String key = "/watchTestKey";
		final EtcdClient client = new EtcdClient("http://192.168.139.129:2911");
		client.start();
		
		client.set(key, "1345566");
		
		Thread.sleep(10000);
		
		client.set(key, "5464645", 3);
		
		Thread.sleep(10000);
		
		client.set(key, "5464645",10);
	}

}
