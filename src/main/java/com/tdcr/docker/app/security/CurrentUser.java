package com.tdcr.docker.app.security;

import com.tdcr.docker.backend.data.entity.User;

@FunctionalInterface
public interface CurrentUser {

	User getUser();
}
