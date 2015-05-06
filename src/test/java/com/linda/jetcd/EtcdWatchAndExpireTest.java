package com.linda.jetcd;

public class EtcdWatchAndExpireTest {
	
	public static void main(String[] args) {
		
		final String key = "/watchTestKey";
		final EtcdClient client = new EtcdClient("http://192.168.139.129:2911");
		client.start();
		
		EtcdWatchCallback callback = new EtcdWatchCallback(){

			public void onChange(EtcdChangeResult future) {
				System.out.println(JSONUtils.toJSON(future.getResult()));
				client.watch(key, this);
			}
		};
//		client.watch(key, callback);
		client.watch(key, callback);
		System.out.println("hahahahah");
	}

}
