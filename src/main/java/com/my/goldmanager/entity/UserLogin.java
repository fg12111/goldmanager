package com.my.goldmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "USER_LOGIN")
public class UserLogin {

	@Id
	@Getter
	@Setter
	private String userid;

	@Column
	@Getter
	@Setter
	private String password;

	@Column
	@Getter
	@Setter
	private boolean active;
}
