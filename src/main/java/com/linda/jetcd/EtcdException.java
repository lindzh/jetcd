package com.linda.jetcd;

public class EtcdException extends RuntimeException{

	private static final long serialVersionUID = -7986057373963720777L;

	public EtcdException() {
		super();
	}

	public EtcdException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EtcdException(String message, Throwable cause) {
		super(message, cause);
	}

	public EtcdException(String message) {
		super(message);
	}

	public EtcdException(Throwable cause) {
		super(cause);
	}
	
}
