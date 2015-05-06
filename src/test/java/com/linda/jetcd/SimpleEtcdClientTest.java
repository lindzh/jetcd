package com.linda.jetcd;

import java.util.List;

public class SimpleEtcdClientTest {
	
	private EtcdClient client;
	
	public void testVersion(){
		EtcdResult version = client.version();
		System.out.println("version:"+JSONUtils.toJSON(version));
	}
	
	public void testKey() throws InterruptedException{
		String key = "/testKey";
		EtcdResult result = client.get(key);
		System.out.println("init get:" +JSONUtils.toJSON(result));
		result = client.set(key, "11111111");
		System.out.println("set" + JSONUtils.toJSON(result));
		result = client.set(key, null, 10);
		System.out.println("set ttl:"+JSONUtils.toJSON(result));
		Thread.sleep(11000);
		result = client.get(key);
		System.out.println("after ttl get:"+JSONUtils.toJSON(result));
		result = client.set(key, "222222");
		System.out.println("new set:"+JSONUtils.toJSON(result));
		result = client.get(key);
		System.out.println("new get:"+JSONUtils.toJSON(result));
		result = client.del(key);
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
		client.cas(key, "testCas-1", false);
		EtcdResult result = client.get(key);
		System.out.println("init get:" +JSONUtils.toJSON(result));
		client.cas(key, "testCas-2", false);
		result = client.get(key);
		System.out.println("cas noexist get:" +JSONUtils.toJSON(result));
		client.cas(key, "testCas-2", true);
		result = client.get(key);
		System.out.println("cas exist get:" +JSONUtils.toJSON(result));
	}
	
	public void testCallback(){
		String key = "/testCallback";
		EtcdResult result = client.get(key);
		System.out.println("init get:" +JSONUtils.toJSON(result));
		result = client.set(key, "11111111");
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
	
	public void dirTest() throws InterruptedException{
		String dirKey = "/dirTestKey";
		EtcdResult result = client.delDir(dirKey, true);
		System.out.println("dir delete:" +JSONUtils.toJSON(result));
		result = client.dir(dirKey);
		System.out.println("dir set:" +JSONUtils.toJSON(result));
		
		result = client.dir(dirKey+"/subdir1");
		System.out.println("dir sub1:" +JSONUtils.toJSON(result));
		
		result = client.dir(dirKey+"/subdir2");
		System.out.println("dir sub2:" +JSONUtils.toJSON(result));
		
		result = client.children(dirKey, true, false);
		System.out.println("dir children:" +JSONUtils.toJSON(result));
		
		result = client.delDir(dirKey+"/subdir2", true);
		System.out.println("dir del sub2:" +JSONUtils.toJSON(result));
		
		result = client.children(dirKey, true, false);
		System.out.println("dir children:" +JSONUtils.toJSON(result));
		
		result = client.delDir(dirKey, true);
		System.out.println("dir del:" +JSONUtils.toJSON(result));
		
		result = client.children(dirKey, true, false);
		System.out.println("dir children:" +JSONUtils.toJSON(result));
	}
	
	public void testQueue() throws InterruptedException{
		String queue = "/testQueue";
		//queue name is a directory
		client.delDir(queue, true);
		EtcdResult result = client.queue(queue, "job1");
		result = client.queue(queue, "job2");
		System.out.println("queue job2:" +JSONUtils.toJSON(result));
		EtcdResult job2 = result;
		result = client.queue(queue, "job3");
		String delJob3 = result.getNode().getKey();
		System.out.println("queue job3:" +JSONUtils.toJSON(result));
		result = client.children(queue, true, true);
		System.out.println("queue children:" +JSONUtils.toJSON(result));
		result = client.queue(queue, "job-ttl",10);
		System.out.println("queue job-ttl:" +JSONUtils.toJSON(result));
		result = client.children(queue, true, true);
		System.out.println("queue children:" +JSONUtils.toJSON(result));
		Thread.currentThread().sleep(10000);
		result = client.children(queue, true, true);
		System.out.println("queue ttl then children:" +JSONUtils.toJSON(result));
		EtcdResult etcdResult = client.set(job2.getNode().getKey(), job2.getNode().getValue(), 6);
		System.out.println("ttl job2 result:" +JSONUtils.toJSON(etcdResult));
		Thread.currentThread().sleep(10000);
		result = client.children(queue, true, true);
		System.out.println("queue ttl job2 then children:" +JSONUtils.toJSON(result));
		client.del(delJob3);
		result = client.children(queue, true, true);
		System.out.println("queue del job3 children:" +JSONUtils.toJSON(result));
	}
	
	public static void main(String[] args) throws InterruptedException {
		SimpleEtcdClientTest clientTest = new SimpleEtcdClientTest();
		clientTest.client = new EtcdClient("http://192.168.139.129:2911");
		clientTest.client.start();
//		clientTest.testVersion();
//		clientTest.testKey();
//		clientTest.testMembers();
//		clientTest.testCallback();
//		clientTest.testcas();
//		clientTest.dirTest();
		clientTest.testQueue();
	}
}
