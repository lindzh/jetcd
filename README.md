## a simple etcd client for java

watch auto notify for change,easy to use

>simple setting config operation

```java
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
```

>cas operation

```java
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
```


>use watch call back for change notify

```java
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

```
