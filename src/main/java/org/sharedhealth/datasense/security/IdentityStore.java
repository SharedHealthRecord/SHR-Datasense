package org.sharedhealth.datasense.security;

import org.springframework.stereotype.Component;

@Component
public class IdentityStore {
    IdentityToken token;

    public IdentityToken getToken() {
        return token;
    }

    public void setToken(IdentityToken token) {
        this.token = token;
    }
}
