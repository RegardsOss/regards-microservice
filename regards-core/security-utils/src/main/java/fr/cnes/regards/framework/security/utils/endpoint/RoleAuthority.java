/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.security.utils.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

/**
 * REGARDS authority
 *
 * @author Marc Sordi
 * @author Sébastien Binda
 * @since 1.0-SNAPSHOT
 *
 */
public class RoleAuthority implements GrantedAuthority {

    /**
     * Virtual Instance administrator ROLE name
     */
    public static final String INSTANCE_ADMIN_VIRTUAL_ROLE = "INSTANCE_ADMIN";

    /**
     * Role prefix
     */
    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Serial number for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Prefix for all systems ROLES. Systems roles are internal roles to allow microservices to communicate witch each
     * other.
     */
    private static final String SYS_ROLE_PREFIX = "SYS_";

    /**
     * Role name prefixed with {@link #ROLE_PREFIX}
     */
    private String autority;

    /**
     * List of authorized id addresses for the current rrole
     */
    private List<String> authorizedIpAdresses = new ArrayList<>();

    /**
     * Role does accept CORS Requests ?
     */
    private Boolean corsAccess = Boolean.TRUE;

    /**
     *
     * Constructor
     *
     * @param pRoleName
     *            The role name
     * @since 1.0-SNAPSHOT
     */
    public RoleAuthority(final String pRoleName) {
        if (!pRoleName.startsWith(ROLE_PREFIX)) {
            this.autority = getRoleAuthority(pRoleName);
        } else {
            this.autority = pRoleName;
        }
    }

    /**
     *
     * Remove Authority ROLE_ prefix to get real role name
     *
     * @param pRoleAuthorityName
     *            Authority role name with ROLE_PREFIX
     * @return role name
     * @since 1.0-SNAPSHOT
     */
    public static String getRoleName(final String pRoleAuthorityName) {
        String roleName = pRoleAuthorityName;
        if ((pRoleAuthorityName != null) && pRoleAuthorityName.startsWith(ROLE_PREFIX)) {
            roleName = roleName.substring(ROLE_PREFIX.length());
        }
        return roleName;
    }

    /**
     *
     * Add Authority PREFIX to given Role name
     *
     * @param pRoleName
     *            The role name
     * @return RoleAuthority
     * @since 1.0-SNAPSHOT
     */
    public static String getRoleAuthority(final String pRoleName) {
        return ROLE_PREFIX + pRoleName;
    }

    /**
     *
     * Retrieve the SYS ROLE for the current microservice. SYS ROLE is a specific role that permit access to all
     * administration endpoints.
     *
     * @param pMicroserviceName
     *            the current microservice name
     * @return SYS Role name
     * @since 1.0-SNAPSHOT
     */
    public static String getSysRole(final String pMicroserviceName) {
        return SYS_ROLE_PREFIX + pMicroserviceName;
    }

    /**
     *
     * Is the given role a system role ?
     *
     * @param pRoleName
     *            The role name
     * @return [true|false]
     * @since 1.0-SNAPSHOT
     */
    public static boolean isSysRole(final String pRoleName) {
        boolean isSysRole = false;
        if (pRoleName.startsWith(ROLE_PREFIX + SYS_ROLE_PREFIX)) {
            isSysRole = true;
        }
        return isSysRole;
    }

    /**
     *
     * Is the given role the virtual instance admin role ?
     *
     * @param pRoleName
     *            The role name
     * @return [true|false]
     * @since 1.0-SNAPSHOT
     */
    public static boolean isInstanceAdminRole(final String pRoleName) {
        boolean isInstanceAdminRole = false;
        if (pRoleName.equals(getRoleAuthority(INSTANCE_ADMIN_VIRTUAL_ROLE))) {
            isInstanceAdminRole = true;
        }
        return isInstanceAdminRole;
    }

    @Override
    public String getAuthority() {
        return autority;
    }

    public List<String> getAuthorizedIpAdresses() {
        return authorizedIpAdresses;
    }

    public Boolean getCorsAccess() {
        return corsAccess;
    }

    public void setAuthorizedIpAdresses(final List<String> pAuthorizedIpAdresses) {
        authorizedIpAdresses = pAuthorizedIpAdresses;
    }

    public void setCorsAccess(final Boolean pCorsAccess) {
        corsAccess = pCorsAccess;
    }

    @Override
    public boolean equals(final Object pObj) {
        if ((pObj != null) && (pObj instanceof RoleAuthority)) {
            return autority.equals(((RoleAuthority) pObj).getAuthority());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return autority.hashCode();
    }

}
