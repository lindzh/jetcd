package com.linda.jetcd;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class EtcdWatcher implements Runnable{
	
	private LinkedList<EtcdChangeResult> futures = new LinkedList<EtcdChangeResult>();
	
	private Map<EtcdChangeResult, EtcdWatchCallback> futureCallbackMap = new ConcurrentHashMap<EtcdChangeResult, EtcdWatchCallback>();

	private Thread checkThread;
	
	private AtomicBoolean stop = new AtomicBoolean(false);
	
	private ExecutorService executor = Executors.newFixedThreadPool(3);
	
	public void start(){
		if(checkThread==null){
			checkThread = new Thread(this);
			checkThread.start();
		}
	}
	
	public void addCallback(EtcdChangeResult future,EtcdWatchCallback callback){
		futures.offer(future);
		futureCallbackMap.put(future, callback);
	}
	
	public void run() {
		while(!stop.get()){
			if(futures.size()>0){
				EtcdChangeResult future = futures.peek();
				while(future!=null){
					if(future!=null){
						futures.pop();
					}
					if(future.isDone()){
						final EtcdWatchCallback callback = futureCallbackMap.get(future);
						final EtcdChangeResult futureRef = future;
						if(callback!=null){
							executor.execute(new Runnable(){
								public void run() {
									callback.onChange(futureRef);
								}
							});
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
