package com.my.goldmanager.rest.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
		
	@Getter
	@Setter
	private int status;
	@Getter
	@Setter
	private String message;
	
	
}
