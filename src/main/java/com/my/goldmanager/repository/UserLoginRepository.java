package com.my.goldmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.my.goldmanager.entity.UserLogin;

public interface UserLoginRepository extends JpaRepository<com.my.goldmanager.entity.UserLogin, String> {

	@Query(value = "from UserLogin where userid=:userid and password = :password and active=true")
	UserLogin findActiveUserByUserIDAndPassword(@Param("userid") String userid, @Param("password") String password);
}
