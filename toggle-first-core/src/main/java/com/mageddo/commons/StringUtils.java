package com.mageddo.commons;

public final class StringUtils {

	private StringUtils() {
	}

	public static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
