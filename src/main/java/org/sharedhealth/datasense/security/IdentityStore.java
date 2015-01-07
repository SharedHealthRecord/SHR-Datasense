package org.sharedhealth.datasense.security;

import org.springframework.stereotype.Component;

@Component
public class IdentityStore {
    private IdentityToken token;

    public IdentityToken getToken() {
        return token;
    }

    public void setToken(IdentityToken token) {
        this.token = token;
    }

    public void clearToken() {
        this.token = null;
    }
}
