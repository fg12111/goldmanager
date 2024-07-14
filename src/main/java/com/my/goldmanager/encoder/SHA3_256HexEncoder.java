package com.my.goldmanager.encoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SHA3_256HexEncoder implements PasswordEncoder {

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return encode(rawPassword.toString()).equals(encodedPassword);
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return DigestUtils.sha3_256Hex(rawPassword.toString());
	}

}
