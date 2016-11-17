/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.module.rest.exception;

/**
 *
 * Class AlreadyExistingException
 *
 * Exception to indicates that the entity already exists.
 *
 * @deprecated use {@link EntityAlreadyExistsException}
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 */
@Deprecated
public class AlreadyExistingException extends EntityException {

    /**
     * Serial version
     */
    private static final long serialVersionUID = -2444321445173304039L;

    /**
     *
     * Constructor
     *
     * @param pEntityId
     *            entity identifier
     * @since 1.0-SNAPSHOT
     */
    public AlreadyExistingException(final String pEntityId) {
        super("Data with id : " + pEntityId + " already Exist");
    }
}
