package com.grosshaeuser.olmp.security;

import com.grosshaeuser.olmp.security.entities.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface OlmpUserDetailsService extends UserDetailsService {
    UserDetails fromUserInstance(User user);
}
