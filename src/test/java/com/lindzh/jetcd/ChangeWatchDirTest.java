package com.lindzh.jetcd;

public class ChangeWatchDirTest {
	
	public static void main1(String[] args) throws InterruptedException {
		final EtcdClient client = new EtcdClient("http://127.0.0.1:2379");
		client.start();
		String dir = "/mydir";
		
		client.delDir(dir, true);
		
		client.dir(dir);
		
		Thread.currentThread().sleep(10000);
		client.set("/mydir/mykey1", "this is a value 1", 30);
		Thread.sleep(5000);
		client.set("/mydir/mykey2", "this is a value 2", 10);
		Thread.sleep(5000);
		client.set("/mydir/mykey3", "this is a value 3", 20);

		client.set("/mydir2","hahahah");

		client.dir("/mydir3");

		client.stop();
	}

}
