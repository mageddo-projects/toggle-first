package com.mageddo.togglefirst;

public class BasicFeature implements Feature {

	private final String name;

	public BasicFeature(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}
}
