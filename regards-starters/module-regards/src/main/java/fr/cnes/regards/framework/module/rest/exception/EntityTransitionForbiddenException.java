/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.module.rest.exception;

/**
 * Exception thrown when an illegal transition is called on an entity which state is managed by a workflow, like
 * {@link Account}s and {@link ProjectUser}s.<br>
 *
 * @author Xavier-Alexandre Brochard
 * @since 1.1-SNAPSHOT
 */
public class EntityTransitionForbiddenException extends EntityOperationForbiddenException {

    /**
     * Serial version
     *
     * @since 1.1-SNAPSHOT
     */
    private static final long serialVersionUID = -7255117056559968468L;

    /**
     * Creates a new exception with passed params
     *
     * @param <T>
     *
     * @param pEntityIdentifier
     *            Entity identifier
     * @param pEntityClass
     *            Entity class
     * @param pState
     *            the current entity's state
     * @param pTransition
     *            the illegally called transition
     * @since 1.1-SNAPSHOT
     */
    public <T> EntityTransitionForbiddenException(final String pEntityIdentifier, final Class<?> pEntityClass,
            final String pState, final String pTransition) {
        super(pEntityIdentifier, pEntityClass, "The transition " + pTransition
                + " called on this state-managed entity is illegal for its current the state " + pState);
    }

}