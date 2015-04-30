package com.linda.jetcd;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class EtcdWatcher implements Runnable{
	
	private LinkedList<EtcdFuture> futures = new LinkedList<EtcdFuture>();
	
	private Map<EtcdFuture, EtcdWatchCallback> futureCallbackMap = new ConcurrentHashMap<EtcdFuture, EtcdWatchCallback>();

	private Thread checkThread;
	
	private AtomicBoolean stop = new AtomicBoolean(false);
	
	public void start(){
		if(checkThread==null){
			checkThread = new Thread(this);
			checkThread.start();
		}
	}
	
	public void addCallback(EtcdFuture future,EtcdWatchCallback callback){
		futures.offer(future);
		futureCallbackMap.put(future, callback);
	}
	
	public void run() {
		while(!stop.get()){
			if(futures.size()>0){
				EtcdFuture future = futures.peek();
				while(future!=null){
					if(future!=null){
						futures.pop();
					}
					if(future.isDone()){
						EtcdWatchCallback callback = futureCallbackMap.get(future);
						if(callback!=null){
							callback.onChange(future);
						}
					}else{
						futures.offer(future);
					}
					future = futures.peek();
					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						break;
					}
				}
			}else{
				try {
					Thread.sleep(200L);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		futures.clear();
		futureCallbackMap.clear();
	}

	public void stop(){
		checkThread.interrupt();
		stop.set(true);
	}
}
