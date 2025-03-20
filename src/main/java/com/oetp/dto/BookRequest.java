package com.oetp.dto;

public class BookRequest {

	private String user;
	private int quantity;
	
	public BookRequest() {
		
	}

	public BookRequest(String user, int quantity) {
		super();
		this.user = user;
		this.quantity = quantity;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
}
