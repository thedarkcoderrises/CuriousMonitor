package com.tdcr.docker.backend.data;

public class Role {
	public static final String CHECKER = "checker";
	public static final String MAKER = "maker";
	// This role implicitly allows access to all views.
	public static final String ADMIN = "admin";

	private Role() {
		// Static methods and fields only
	}

	public static String[] getAllRoles() {
		return new String[] { CHECKER, MAKER, ADMIN };
	}

}
