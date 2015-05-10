package com.linda.jetcd;

public class ChangeWatchDirTest {
	
	public static void main(String[] args) throws InterruptedException {
		final EtcdClient client = new EtcdClient("http://192.168.139.129:2911");
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
		client.stop();
	}

}
