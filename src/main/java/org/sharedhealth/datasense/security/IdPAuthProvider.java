package org.sharedhealth.datasense.security;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.client.IdentityServiceClient;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.IOException;
import java.util.List;

public class IdPAuthProvider implements AuthenticationProvider {

    private IdentityServiceClient idPService;

    public IdPAuthProvider(IdentityServiceClient idPService) {
        this.idPService = idPService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String principal = (String) authentication.getPrincipal();
        String credentials = (String) authentication.getCredentials();
        try {
            IdentityToken tokenForUser = idPService.authenticateUser(principal, credentials);
            if (tokenForUser != null) {
                UserInfo userInfo = idPService.getUserInfo(tokenForUser);
                if (hasDatasenseRoles(userInfo)) {
                    if (userInfo != null) {
                        return new TokenAuthentication(userInfo, grantedAuthorities(userInfo), true);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean hasDatasenseRoles(UserInfo userInfo) {
        return userInfo.getProperties().getGroups().contains("ROLE_SHR System Admin");
    }

    private List<? extends GrantedAuthority> grantedAuthorities(UserInfo userInfo) {
        String commaSeparatedRoles = StringUtils.join(userInfo.getProperties().getGroups(), ",");
        return AuthorityUtils.commaSeparatedStringToAuthorityList(commaSeparatedRoles);
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
