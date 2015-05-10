package com.linda.jetcd;

public class EtcdWatchAndExpireTest {
	
	public static void main(String[] args) {
		
//		final String key = "/watchTestKey";
		final String key = "/mydir";
		final EtcdClient client = new EtcdClient("http://192.168.139.129:2911");
		client.start();
		
		EtcdWatchCallback callback = new EtcdWatchCallback(){
			public void onChange(EtcdChangeResult future) {
				System.out.println(JSONUtils.toJSON(future.getResult()));
				EtcdResult children = client.children(key, true, true);
				System.out.println("newChildren:"+JSONUtils.toJSON(children));
				client.watchChildren(key, true,true,this);
			}
		};
//		client.watch(key, callback);
		client.watchChildren(key, true,true,callback);
		System.out.println("hahahahah");
	}

}
