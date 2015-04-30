package com.linda.jetcd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class JSONUtils {

	private static final JsonConfig config = new JsonConfig();

	public static JSONObject formatJSONObject(String json){
		return JSONObject.fromObject(json, config);
	}
	
	public static <T> T fromJSON(String json,Class<T> clazz){
		JSONObject object = JSONObject.fromObject(json, config);
		try {
			return (T)JSONObject.toBean(object, clazz);
		} catch (Exception e) {
			throw new JSONException("parse json error. json:"+json+" class:"+clazz,e);
		}
	}
	
	public static <T> List<T> fromJSONArray(String json,Class clazz){
		JSONArray jsonArray = JSONArray.fromObject(json, config);
		Object array = JSONArray.toCollection(jsonArray, clazz);
		try{
			List<T> list = new ArrayList<T>();
			if(array!=null){
				list.addAll((Collection)array);
			}
			return list;
		}catch(Exception e){
			throw new JSONException("parse array json error. json:"+json+" class:"+clazz,e);
		}
	}

	public static String toJSON(Object obj) {
		if(obj==null){
			return null;
		}
		if (obj instanceof Collection||obj.getClass().isArray()) {
			JSONArray jsonArray = JSONArray.fromObject(obj);
			return jsonArray.toString();
		}else{
			JSONObject jsonObject = JSONObject.fromObject(obj);
			return jsonObject.toString();
		}
	}
}
