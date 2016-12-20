package org.sharedhealth.datasense.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class TokenAuthentication implements Authentication {
    private UserInfo userInfo;
    private List<? extends GrantedAuthority> groups;
    private boolean isAuthenticated;

    public TokenAuthentication(UserInfo userInfo, List<? extends GrantedAuthority> grantedAuthorities, boolean isAuthenticated) {
        this.userInfo = userInfo;
        this.isAuthenticated = isAuthenticated;
        this.groups = grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return groups;
    }

    @Override
    public Object getCredentials() {
        return userInfo.getProperties().getAccessToken();
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userInfo;
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenAuthentication tokenAuthentication = (TokenAuthentication) o;

        if (!userInfo.equals(tokenAuthentication.userInfo)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return userInfo.hashCode();
    }

    @Override
    public String getName() {
        return userInfo.getProperties().getName();
    }
}