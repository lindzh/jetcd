package com.lindzh.jetcd;

public class EtcdResult {

	// data resp
	private String action;
	private EtcdNode node;
	private EtcdNode prevNode;

	// error
	private Integer errorCode;
	private String message;
	private String cause;
	private int errorIndex;

	private int index;

	// version
	private String releaseVersion;
	private String internalVersion;

	public boolean isSuccess() {
		return errorCode == null;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public EtcdNode getNode() {
		return node;
	}

	public void setNode(EtcdNode node) {
		this.node = node;
	}

	public EtcdNode getPrevNode() {
		return prevNode;
	}

	public void setPrevNode(EtcdNode prevNode) {
		this.prevNode = prevNode;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public int getErrorIndex() {
		return errorIndex;
	}

	public void setErrorIndex(int errorIndex) {
		this.errorIndex = errorIndex;
	}

	public String getReleaseVersion() {
		return releaseVersion;
	}

	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	public String getInternalVersion() {
		return internalVersion;
	}

	public void setInternalVersion(String internalVersion) {
		this.internalVersion = internalVersion;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
