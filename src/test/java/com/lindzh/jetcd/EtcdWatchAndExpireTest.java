package com.lindzh.jetcd;

public class EtcdWatchAndExpireTest {
	
	public static void main(String[] args) {
		
//		final String key = "/watchTestKey";
		final String key = "/mydir";
		final EtcdClient client = new EtcdClient("http://127.0.0.1:2379");
		client.start();
		
		EtcdWatchCallback callback = new EtcdWatchCallback(){
			public void onChange(EtcdChangeResult future) {
				System.out.println(JSONUtils.toJSON(future.getResult()));
				EtcdResult children = client.children(key, true, true);
				System.out.println("getnewChildren:"+JSONUtils.toJSON(children));
				client.watchChildren(key, true,true,this);
			}
		};
//		client.watch(key, callback);
		client.watchChildren(key, true,true,callback);
		System.out.println("hahahahah");
	}

}
