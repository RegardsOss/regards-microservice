/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.plugins.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.springframework.hateoas.Identifiable;

/**
 * Class PluginParameter
 *
 * Parameter associated to a plugin configuration <PluginConfiguration>
 *
 * @author cmertz
 */
@Entity(name = "T_PLUGIN_PARAMETER_VALUE")
@SequenceGenerator(name = "pluginParameterSequence", initialValue = 1, sequenceName = "SEQ_PLUGIN_PARAMETER")
public class PluginParameter implements Identifiable<Long> {

    /**
     * The max size of a {@link String} value
     */
    private static final int MAX_STRING_VALUE = 2048;

    /**
     * Parameter unique id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pluginParameterSequence")
    @Column(name = "id")
    private Long id;

    /**
     * Parameter name
     */
    private String name;

    /**
     * Parameter value
     */
    @Column(length = MAX_STRING_VALUE)
    private String value;

    /**
     * {@link PluginConfiguration} parameter is optional
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PLUGIN_CONF_ID", referencedColumnName = "id", unique = true, nullable = true, insertable = true,
            updatable = true, foreignKey = @javax.persistence.ForeignKey(name = "FK_PLUGIN_CONF"))
    private PluginConfiguration pluginConfiguration;

    /**
     * The parameter is dynamic
     */
    private Boolean isDynamic = false;

    /**
     * The list of values for a dynamic parameters TODO CMZ enlever le @Transient
     */
    @Transient
    private List<String> dynamicsValues;

    /**
     * Default constructor
     *
     */
    public PluginParameter() {
        super();
    }

    /**
     * Constructor
     *
     * @param pName
     *            the parameter name
     * @param pValue
     *            the parameter value
     */
    public PluginParameter(final String pName, final String pValue) {
        super();
        name = pName;
        value = pValue;
    }

    /**
     * Constructor
     * 
     * @param pName
     *            the parameter name
     * @param pPluginConfiguration
     *            the plugin configuration
     */
    public PluginParameter(final String pName, final PluginConfiguration pPluginConfiguration) {
        super();
        name = pName;
        pluginConfiguration = pPluginConfiguration;
    }

    @Override
    public Long getId() {
        return id;
    }

    public final void setId(Long pId) {
        id = pId;
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String pName) {
        name = pName;
    }

    public final String getValue() {
        return value;
    }

    public final void setValue(String pValue) {
        value = pValue;
    }

    public final Boolean getIsDynamic() {
        return isDynamic;
    }

    public final void setIsDynamic(Boolean pIsDynamic) {
        this.isDynamic = pIsDynamic;
    }

    public final List<String> getDynamicsValues() {
        return dynamicsValues;
    }

    public final void setDynamicsValues(List<String> pDynamicsValues) {
        this.dynamicsValues = pDynamicsValues;
    }

    public final PluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }

}