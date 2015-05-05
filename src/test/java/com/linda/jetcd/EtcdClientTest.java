package com.linda.jetcd;

import java.util.List;

public class EtcdClientTest {
	
	private EtcdClient client;
	
	public void testVersion(){
		EtcdResult version = client.getVersion();
		System.out.println("version:"+JSONUtils.toJSON(version));
	}
	
	public void testKey() throws InterruptedException{
		String key = "/testKey";
		EtcdResult result = client.get(key);
		System.out.println("init get:" +JSONUtils.toJSON(result));
		result = client.set(key, "11111111", false);
		System.out.println("set" + JSONUtils.toJSON(result));
		result = client.set(key, null, 10, false);
		System.out.println("set ttl:"+JSONUtils.toJSON(result));
		Thread.sleep(11000);
		result = client.get(key);
		System.out.println("after ttl get:"+JSONUtils.toJSON(result));
		result = client.set(key, "222222", false);
		System.out.println("new set:"+JSONUtils.toJSON(result));
		result = client.get(key);
		System.out.println("new get:"+JSONUtils.toJSON(result));
		result = client.del(key, false, false);
		System.out.println("del:"+JSONUtils.toJSON(result));
		result = client.get(key);
		System.out.println("after del:"+JSONUtils.toJSON(result));
	}
	
	public void testMembers() throws InterruptedException{
		List<EtcdMember> members = client.members();
		System.out.println("init"+JSONUtils.toJSON(members));
		EtcdMember member = members.get(2);
		client.delMember(member.getId());
		Thread.sleep(10000);
		members = client.members();
		System.out.println("after del:"+JSONUtils.toJSON(members));
		client.addMembers(member.getPeerURLs());
		Thread.sleep(10000);
		members = client.members();
		System.out.println("after add"+JSONUtils.toJSON(members));
		client.setMembers(member.getId(), member.getPeerURLs());
		Thread.sleep(10000);
		members = client.members();
		System.out.println("after set"+JSONUtils.toJSON(members));
	}
	
	public void testDir(){
		
	}
	
	public void testcas(){
		String key = "/testCas";
		client.cas(key, "testCas-1", false, false);
		EtcdResult result = client.get(key);
		System.out.println("init get:" +JSONUtils.toJSON(result));
		client.cas(key, "testCas-2", false, false);
		result = client.get(key);
		System.out.println("cas noexist get:" +JSONUtils.toJSON(result));
		client.cas(key, "testCas-2", false, true);
		result = client.get(key);
		System.out.println("cas exist get:" +JSONUtils.toJSON(result));
	}
	
	public void testCallback(){
		String key = "/testCallback";
		EtcdResult result = client.get(key);
		System.out.println("init get:" +JSONUtils.toJSON(result));
		result = client.set(key, "11111111", false);
		result = client.get(key);
		System.out.println("set:" +JSONUtils.toJSON(result));
		client.watch(key, new EtcdWatchCallback() {
			public void onChange(EtcdChangeResult future) {
				EtcdResult etcdResult = future.getResult();
				if(etcdResult!=null){
					System.out.println("change:" +JSONUtils.toJSON(etcdResult));
				}
			}
		});
	}
	
	public static void main(String[] args) throws InterruptedException {
		EtcdClientTest clientTest = new EtcdClientTest();
		clientTest.client = new EtcdClient("http://192.168.139.129:2911");
		clientTest.client.start();
//		clientTest.testVersion();
//		clientTest.testKey();
		clientTest.testMembers();
//		clientTest.testCallback();
//		clientTest.testcas();
	}
	
	

}
