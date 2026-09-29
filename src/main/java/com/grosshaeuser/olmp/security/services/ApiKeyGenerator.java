package com.grosshaeuser.olmp.security.services;

import com.grosshaeuser.olmp.security.entities.ApiKey;
import com.grosshaeuser.olmp.security.entities.User;

public interface ApiKeyGenerator {
    ApiKey generateApiKeyForUser(User user);
}
