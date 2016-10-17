/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.jpa.instance.properties;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 * Class InstanceDaoProperties
 *
 * DAO Instance database configuration
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 */
@ConfigurationProperties("regards.jpa.instance")
public class InstanceDaoProperties {

    /**
     * Is Instance database enabled ?
     */
    private Boolean enabled;

    /**
     * Instance JPA Datasource
     */
    @NestedConfigurationProperty
    private DataSourceProperties datasource;

    /**
     * Does the instance dao is embedded ?
     */
    private Boolean embedded = Boolean.FALSE;

    /**
     * Path for embedded databases
     */
    private String embeddedPath;

    /**
     * Hibernate dialect
     */
    private String dialect;

    /**
     *
     * Setter
     *
     * @param pDatasource
     *            instance JPA datasource
     * @since 1.0-SNAPSHOT
     */
    public void setDatasource(final DataSourceProperties pDatasource) {
        this.datasource = pDatasource;
    }

    /**
     *
     * Getter
     *
     * @return instance JPA Datasource
     * @since 1.0-SNAPSHOT
     */
    public DataSourceProperties getDatasource() {
        return this.datasource;
    }

    /**
     *
     * Getter
     *
     * @return Is Instance database enabled ?
     * @since 1.0 SNAPSHOT
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     *
     * Setter
     *
     * @param pEnabled
     *            Is Instance database enabled ?
     * @since 1.0 SNAPSHOT
     */
    public void setEnabled(final Boolean pEnabled) {
        enabled = pEnabled;
    }

    public Boolean getEmbedded() {
        return embedded;
    }

    public void setEmbedded(final Boolean pEmbedded) {
        embedded = pEmbedded;
    }

    public String getEmbeddedPath() {
        return embeddedPath;
    }

    public void setEmbeddedPath(final String pEmbeddedPath) {
        embeddedPath = pEmbeddedPath;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(final String pDialect) {
        dialect = pDialect;
    }

}
