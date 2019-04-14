package com.mageddo.featureswitch.utils;

public final class StringUtils {

	private StringUtils() {
	}

	public static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}
