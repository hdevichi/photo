package org.devichi.photo.utils;

public class PhotoException extends RuntimeException {

	public static final long serialVersionUID = 0;
	
	public PhotoException( String s, Exception e) {
		super(s,e);
	}
	
	public PhotoException( String s) {
		super(s);
	}
}
