package com.oetp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookRequest {

	@NotBlank(message="User Name cannot be blank")
	private String user;
	
	@Min(value=1, message = "Quantity must be at least 1")
	private int quantity;
	
	private int eventId;
	
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
