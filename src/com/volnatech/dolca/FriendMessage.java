package com.volnatech.dolca;

import java.io.Serializable;

public class FriendMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message = null;
	private String from = null;
	private String to = null;
	private boolean isOutgoing = false;
	
	FriendMessage(String from, String to, String message, boolean isOutgoing){
		this.setMessage(message);
		this.setFrom(from);
		this.setTo(to);
		this.setOutgoing(isOutgoing);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getFrom() {
		return from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getTo() {
		return to;
	}

	public void setOutgoing(boolean isOutgoing) {
		this.isOutgoing = isOutgoing;
	}

	public boolean isOutgoing() {
		return isOutgoing;
	}
	
}
