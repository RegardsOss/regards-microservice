/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.plugins.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Exception for plugin utils package
 *
 * @author cmertz
 */
public class PluginUtilsException extends Exception {

    /**
     * Class Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginUtilsException.class);

    /**
     * serialVersionUID field.
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * Constructor
     *
     * @param pMessage
     *            an error message
     */
    public PluginUtilsException(String pMessage) {
        super(pMessage);
        LOGGER.error(pMessage);
    }

    /**
     *
     * Constructor
     *
     * @param pMessage
     *            an error message
     * @param pCause
     *            the exception
     */
    public PluginUtilsException(String pMessage, Throwable pCause) {
        super(pMessage, pCause);
        LOGGER.error(pMessage, pCause);
    }

    /**
     *
     * Constructor
     *
     * @param pCause
     *            the exception
     */
    public PluginUtilsException(Throwable pCause) {
        super(pCause);
    }
}
