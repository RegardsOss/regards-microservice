/**
 *
 */
package fr.cnes.regards.modules.jobs.domain.converters;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lmieulet
 *
 */

@Converter
public class PathConverter implements AttributeConverter<Path, String> {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PathConverter.class);

    @Override
    public String convertToDatabaseColumn(final Path pAttribute) {
        if (pAttribute == null) {
            return "";
        }
        return pAttribute.toString();
    }

    @Override
    public Path convertToEntityAttribute(final String pDbData) {
        return Paths.get(pDbData);
    }

}