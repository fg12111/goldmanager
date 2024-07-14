package com.my.goldmanager.rest.request;

import lombok.Getter;
import lombok.Setter;

public class UpdateUserPasswordRequest {

	@Getter
	@Setter
	private String newPassword;
}
