package com.oetp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class BookRequest {

	private String user;
	
	private int eventId;

	@Min(value=1, message = "Quantity must be at least 1")
	private int quantity;

	public BookRequest() {}

}
