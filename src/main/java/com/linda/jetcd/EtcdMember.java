package com.linda.jetcd;

import java.util.List;

public class EtcdMember {

	private String id;
	private String name;
	private List<String> peerURLs;
	private List<String> clientURLs;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPeerURLs() {
		return peerURLs;
	}

	public void setPeerURLs(List<String> peerURLs) {
		this.peerURLs = peerURLs;
	}

	public List<String> getClientURLs() {
		return clientURLs;
	}

	public void setClientURLs(List<String> clientURLs) {
		this.clientURLs = clientURLs;
	}

}
