package com.mageddo.featureswitch;

import java.util.Objects;

public enum Status {

	/**
	 * Feature is active for all users
	 */
	ACTIVE(1),

	/**
	 * Feature is inactive for all users
	 */
	INACTIVE(0),

	/**
	 * Feature is active but you need to check if it is active for a specific user
	 */
	RESTRICTED(2),
	;

	private final int code;

	Status(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public String getCodeAsString() {
		return String.valueOf(code);
	}

	public static Status fromCode(String code){
		return fromCode(code, Status.INACTIVE);
	}

	public static Status fromCode(String code, Status defaultStatus){
		for (final Status value : values()) {
			if(Objects.equals(code, String.valueOf(value.getCode()))){
				return value;
			}
		}
		return defaultStatus;
	}
}
