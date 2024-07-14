package com.my.goldmanager.rest.response;

import java.util.List;

import com.my.goldmanager.rest.entity.UserInfo;

import lombok.Getter;
import lombok.Setter;

/**
 * UserResponse
 */
public class ListUserResponse {
 
	@Getter
	@Setter
	private List<UserInfo> userInfos;
	
}
