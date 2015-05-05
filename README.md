## a simple etcd client for java

watch auto notify for change,easy to use

>simple setting config operation

```java
	String key = "/testKey";
	EtcdResult result = client.get(key);
	result = client.set(key, "11111111");
	result = client.set(key, null, 10);
	Thread.sleep(11000);
	result = client.get(key);
	result = client.set(key, "222222");
	result = client.get(key);
	result = client.del(key);
	result = client.get(key);
```

>cas operation

```java
String key = "/testCas";
client.cas(key, "testCas-1", false);
EtcdResult result = client.get(key);
client.cas(key, "testCas-2", false);
result = client.get(key);
client.cas(key, "testCas-2", true);
result = client.get(key);
```


>use watch call back for change notify

```java
String key = "/testCallback";
EtcdResult result = client.get(key);
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

```

>dir operation

```java
String dirKey = "/dirTestKey";
//delete dir
EtcdResult result = client.delDir(dirKey, true);
//create dir
result = client.dir(dirKey);
//add sub dir
result = client.dir(dirKey+"/subdir1");
result = client.dir(dirKey+"/subdir2");
result = client.children(dirKey, true, false);
result = client.delDir(dirKey+"/subdir2", true);
result = client.children(dirKey, true, false);
result = client.delDir(dirKey, true);
result = client.children(dirKey, true, false);
```

>suquence ordered queue for lock

```java
String queue = "/testQueue";
//queue name is a directory
client.delDir(queue, true);
EtcdResult result = client.queue(queue, "job1");
result = client.queue(queue, "job2");
EtcdResult job2 = result;
result = client.queue(queue, "job3");
String delJob3 = result.getNode().getKey();
result = client.children(queue, true, true);
result = client.queue(queue, "job-ttl",10);
result = client.children(queue, true, true);
Thread.currentThread().sleep(10000);
result = client.children(queue, true, true);
//set key ttl
EtcdResult etcdResult = client.set(job2.getNode().getKey(), job2.getNode().getValue(), 6);
Thread.currentThread().sleep(10000);
result = client.children(queue, true, true);
client.del(delJob3);
result = client.children(queue, true, true);
```
