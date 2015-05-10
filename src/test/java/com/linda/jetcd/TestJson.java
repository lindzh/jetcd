package com.linda.jetcd;

import net.sf.json.JSONObject;

public class TestJson {
	
	public static void main(String[] args) {
		String json = "{\"id\":\"3232\",   \"user\":{\"myname\":\"linde\",   \"password\":\"adede\",    \"age\":\"32\"}}";
		JSONObject jsonObject = JSONObject.fromObject(json);
		String string = jsonObject.getString("user");
		System.out.println(string);
	}

}
