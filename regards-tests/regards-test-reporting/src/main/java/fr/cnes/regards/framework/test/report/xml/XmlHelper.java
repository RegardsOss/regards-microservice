/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.test.report.xml;

import java.io.File;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.test.report.exception.ReportException;

/**
 *
 * Help to read and write XML based on JAXB JAVA annotation.
 *
 * @author msordi
 *
 */
public final class XmlHelper {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XmlHelper.class);

    private XmlHelper() {
    }

    /**
     * Write data to a file
     *
     * @param <T>
     *            JAXBElement
     * @param pDirectory
     *            file directory (created if don't exist)
     * @param pFilename
     *            filename
     * @param pClass
     *            type of JAXB element to write
     * @param pJaxbElement
     *            JAXB element
     * @throws ReportException
     *             if report cannot be write
     */
    public static <T> void write(Path pDirectory, String pFilename, Class<T> pClass, T pJaxbElement)
            throws ReportException {

        // Validate
        assertNotNull(pDirectory, "Missing directory path");
        assertNotNull(pFilename, "Missing filename");
        assertNotNull(pClass, "Missing JAXB annotated class");
        assertNotNull(pJaxbElement, "No element to write");

        // Create directory
        if (!pDirectory.toFile().exists()) {
            pDirectory.toFile().mkdirs();
        }

        final File targetFile = pDirectory.resolve(pFilename).toFile();

        try {
            // Init marshaller
            final JAXBContext jaxbContext = JAXBContext.newInstance(pClass);
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // Format output
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshall data
            jaxbMarshaller.marshal(pJaxbElement, targetFile);
        } catch (JAXBException e) {
            final String message = "Error while marshalling data";
            LOG.error("Error while marshalling data", e);
            throw new ReportException(message);
        }

    }

    /**
     * Read data from file
     *
     * @param <T>
     *            JAXB annotated class
     * @param pDirectory
     *            file directory
     * @param pFilename
     *            filename
     * @param pClass
     *            type of JAXB element to read
     * @return JAXB element
     * @throws ReportException
     *             if report cannot be read
     */
    public static <T> T read(Path pDirectory, String pFilename, Class<T> pClass) throws ReportException {
        // Validate
        assertNotNull(pDirectory, "Missing directory path");
        assertNotNull(pFilename, "Missing filename");
        assertNotNull(pClass, "Missing JAXB annotated class");

        final File sourceFile = pDirectory.resolve(pFilename).toFile();

        try {
            // Init unmarshaller
            final JAXBContext jaxbContext = JAXBContext.newInstance(pClass);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // Unmarshall data
            @SuppressWarnings("unchecked")
            final T results = (T) jaxbUnmarshaller.unmarshal(sourceFile);

            return results;
        } catch (JAXBException e) {
            final String message = "Error while marshalling data";
            LOG.error(message, e);
            throw new ReportException(message);
        }

    }

    /**
     * Read data from file
     *
     * @param <T>
     *            JAXB annotated class
     * @param pFilePath
     *            full file path
     * @param pClass
     *            type of JAXB element to read
     * @return JAXB element
     * @throws ReportException
     *             if report cannot be read
     */
    public static <T> T read(Path pFilePath, Class<T> pClass) throws ReportException {
        // Validate
        assertNotNull(pFilePath, "Missing full file path");
        assertNotNull(pClass, "Missing JAXB annotated class");
        return read(pFilePath.getParent(), pFilePath.getFileName().toString(), pClass);
    }

    /**
     * Check if object is not null
     *
     * @param pObject
     *            objet to check
     * @param pMessage
     *            error message
     * @throws ReportException
     *             if a report parameter is null
     */
    private static void assertNotNull(Object pObject, String pMessage) throws ReportException {
        if (pObject == null) {
            LOG.error(pMessage);
            throw new ReportException(pMessage);
        }
    }
}