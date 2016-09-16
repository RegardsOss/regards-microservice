/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.core.security.jwt;

/**
 * This object store REGARDS security principal<br/>
 * After request authentication, this object can be retrieved calling {@link JWTAuthentication#getPrincipal()}
 *
 * @author msordi
 *
 */
public class UserDetails {

    /**
     * User real name
     */
    private String name_;

    /**
     * User email
     */
    private String email_;

    /**
     * @return the name
     */
    public String getName() {
        return name_;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name_ = pName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email_;
    }

    /**
     * @param pEmail
     *            the email to set
     */
    public void setEmail(String pEmail) {
        email_ = pEmail;
    }

}
