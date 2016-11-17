/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.module.rest.exception;

/**
 *
 * Class InvalidEntityException
 *
 * Exception to indicates that the entity requested is invalid.
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 * @deprecated use {@link EntityInvalidException}
 */
@Deprecated
public class InvalidEntityException extends EntityException {

    /**
     * serialVersionUID field.
     *
     * @author CS
     * @since 1.0-SNAPSHOT
     */
    private static final long serialVersionUID = 1677039769133438679L;

    /**
     *
     * Constructor
     *
     * @param pMessage
     *            Entity error message
     * @since 1.0-SNAPSHOT
     */
    public InvalidEntityException(final String pMessage) {
        super(pMessage);
    }

}
