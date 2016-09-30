/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.security.utils.endpoint;

import org.springframework.security.core.GrantedAuthority;

/**
 * REGARDS authority
 *
 * @author msordi
 *
 */
public class RoleAuthority implements GrantedAuthority {

    private static final String ROLE_PREFIX = "ROLE_";

    private static final long serialVersionUID = 1L;

    private String autority_;

    public RoleAuthority(String pAuthority) {
        if (!pAuthority.startsWith(ROLE_PREFIX)) {
            autority_ = ROLE_PREFIX + pAuthority;
        }
        else {
            autority_ = pAuthority;
        }
    }

    @Override
    public String getAuthority() {
        return autority_;
    }

}
