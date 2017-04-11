/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.framework.modules.plugins.domain;

import java.util.List;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;

/**
 * Plugin configuration contains a unique Id, plugin meta-data and parameters.
 *
 * @author cmertz
 * @author oroussel
 */
@Entity
@Table(name = "t_plugin_configuration", indexes = { @Index(name = "idx_plugin_configuration", columnList = "pluginId"),
        @Index(name = "idx_plugin_configuration_label", columnList = "label") })
@SequenceGenerator(name = "pluginConfSequence", initialValue = 1, sequenceName = "seq_plugin_conf")
public class PluginConfiguration implements IIdentifiable<Long> {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfiguration.class);

    /**
     * A constant used to define a {@link String} constraint with length 255
     */
    private static final int MAX_STRING_LENGTH = 255;

    /**
     * Unique id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pluginConfSequence")
    private Long id;

    /**
     * Unique identifier of the plugin. This id is the id defined in the "@Plugin" annotation of the plugin
     * implementation class.
     */
    @Column(nullable = false)
    @NotNull
    private String pluginId;

    /**
     * Label to identify the configuration.
     */
    @NotBlank
    @Column(unique = true, name = "label", length = MAX_STRING_LENGTH)
    private String label;

    /**
     * Version of the plugin configuration. Is set with the plugin version. This attribute is used to check if the saved
     * configuration plugin version differs from the loaded plugin.
     */
    @NotNull
    @Column(nullable = false, updatable = true)
    private String version;

    /**
     * Priority order of the plugin.
     */
    @NotNull
    @Column(nullable = false, updatable = true)
    private Integer priorityOrder;

    /**
     * The plugin configuration is active.
     */
    private Boolean active;

    /**
     * The plugin class name
     */
    private String pluginClassName;

    /**
     * The interface implemented by the pluginClassName, an interface that implements {@link PluginInterface}
     */
    @Column(nullable = false)
    private String interfaceName;

    /**
     * Configuration parameters of the plugin
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_conf_id", foreignKey = @ForeignKey(name = "fk_plg_conf_param_id"))
    private List<PluginParameter> parameters;

    /**
     * Default constructor
     */
    public PluginConfiguration() {
        super();
        pluginId = "undefined";
        priorityOrder = 0;
        version = "0.0";
    }

    /**
     * A constructor with {@link PluginMetaData}.
     *
     * @param pPluginMetaData
     *            the plugin's metadata
     * @param pLabel
     *            the label
     */
    public PluginConfiguration(final PluginMetaData pPluginMetaData, final String pLabel) {
        super();
        pluginId = pPluginMetaData.getPluginId();
        version = pPluginMetaData.getVersion();
        pluginClassName = pPluginMetaData.getPluginClassName();
        interfaceName = pPluginMetaData.getInterfaceName();
        priorityOrder = 0;
        label = pLabel;
        active = Boolean.TRUE;
    }

    /**
     * A constructor with {@link PluginMetaData} and list of {@link PluginParameter}.
     *
     * @param pPluginMetaData
     *            the plugin's metadata
     * @param pLabel
     *            the label
     * @param pParameters
     *            the list of parameters
     */
    public PluginConfiguration(final PluginMetaData pPluginMetaData, final String pLabel,
            final List<PluginParameter> pParameters) {
        super();
        pluginId = pPluginMetaData.getPluginId();
        version = pPluginMetaData.getVersion();
        pluginClassName = pPluginMetaData.getPluginClassName();
        interfaceName = pPluginMetaData.getInterfaceName();
        parameters = pParameters;
        priorityOrder = 0;
        label = pLabel;
        active = Boolean.TRUE;
    }

    /**
     * A constructor with {@link PluginMetaData} and list of {@link PluginParameter}.
     *
     * @param pPluginMetaData
     *            the plugin's metadata
     * @param pLabel
     *            the label
     * @param pParameters
     *            the list of parameters
     * @param pOrder
     *            the order
     */
    public PluginConfiguration(final PluginMetaData pPluginMetaData, final String pLabel,
            final List<PluginParameter> pParameters, final int pOrder) {
        super();
        pluginId = pPluginMetaData.getPluginId();
        version = pPluginMetaData.getVersion();
        pluginClassName = pPluginMetaData.getPluginClassName();
        interfaceName = pPluginMetaData.getInterfaceName();
        parameters = pParameters;
        priorityOrder = pOrder;
        label = pLabel;
        active = Boolean.TRUE;
    }

    /**
     * A constructor with {@link PluginMetaData}.
     *
     * @param pPluginMetaData
     *            the plugin's metadata
     * @param pLabel
     *            the label
     * @param pOrder
     *            the order
     */
    public PluginConfiguration(final PluginMetaData pPluginMetaData, final String pLabel, final int pOrder) {
        super();
        pluginId = pPluginMetaData.getPluginId();
        version = pPluginMetaData.getVersion();
        pluginClassName = pPluginMetaData.getPluginClassName();
        interfaceName = pPluginMetaData.getInterfaceName();
        priorityOrder = pOrder;
        label = pLabel;
        active = Boolean.TRUE;
    }

    /**
     * Return the value of a specific parameter
     *
     * @param pParameterName
     *            the parameter to get the value
     * @return the value of the parameter
     */
    public final String getParameterValue(String pParameterName) {
        String value = null;
        if (parameters != null) {
            final Optional<PluginParameter> pluginParameter = parameters.stream()
                    .filter(s -> s.getName().equals(pParameterName)).findFirst();
            if (pluginParameter.isPresent()) {
                value = pluginParameter.get().getValue();
            }
        }
        return value;
    }

    /**
     * Change the value of the parameter which name is pParameterName if and only if such parameter exists and is
     * dynamic
     *
     * @param pParameterName
     *            Name of the parameter whose value should be setted
     * @param pValue
     *            value to be setted
     */
    public final void setParameterDynamicValue(String pParameterName, String pValue) {
        Optional<PluginParameter> parameter = parameters.stream()
                .filter(p -> p.getName().equals(pParameterName) && p.isDynamic()).findFirst();
        if (parameter.isPresent()) {
            parameter.get().setValue(pValue);
        }
    }

    /**
     * Return the {@link PluginParameter} of a specific parameter
     *
     * @param pParameterName
     *            the parameter to get the value
     * @return the {@link PluginParameter}
     */
    public final PluginParameter getParameter(String pParameterName) {
        PluginParameter searchPluginParam = null;
        if (parameters != null) {
            final Optional<PluginParameter> pluginParameter = parameters.stream()
                    .filter(s -> s.getName().equals(pParameterName)).findFirst();
            if (pluginParameter.isPresent()) {
                searchPluginParam = pluginParameter.get();
            }
        }
        return searchPluginParam;
    }

    /**
     * Return the value of a specific parameter {@link PluginConfiguration}
     *
     * @param pParameterName
     *            the parameter to get the value
     * @return the value of the parameter
     */
    public final PluginConfiguration getParameterConfiguration(String pParameterName) {
        PluginConfiguration value = null;
        if (parameters != null) {
            final Optional<PluginParameter> pluginParameter = parameters.stream()
                    .filter(s -> s.getName().equals(pParameterName)).findFirst();
            if (pluginParameter.isPresent()) {
                value = pluginParameter.get().getPluginConfiguration();
            }
        }
        return value;
    }

    /**
     * Log the {@link PluginParameter} of the {@link PluginConfiguration}.
     */
    public void logParams() {
        LOGGER.info("===> parameters <===");
        LOGGER.info("  ---> number of dynamic parameters : "
                + getParameters().stream().filter(p -> p.isDynamic()).count());

        getParameters().stream().filter(p -> p.isDynamic()).forEach(p -> {
            logParam(p, "  ---> dynamic parameter : ");
        });

        LOGGER.info("  ---> number of no dynamic parameters : "
                + getParameters().stream().filter(p -> !p.isDynamic()).count());
        getParameters().stream().filter(p -> !p.isDynamic()).forEach(p -> {
            logParam(p, "  ---> parameter : ");
        });
    }

    /**
     * Log a {@link PluginParameter}.
     *
     * @param pParam
     *            the {@link PluginParameter} to log
     * @param pPrefix
     *            a prefix to set in the log
     */
    private void logParam(PluginParameter pParam, String pPrefix) {
        LOGGER.info(pPrefix + pParam.getName() + "-def val:" + pParam.getValue());
        if (!pParam.getDynamicsValuesAsString().isEmpty()) {
            pParam.getDynamicsValuesAsString().forEach(v -> LOGGER.info("     --> val=" + v));
        }
    }

    public final String getLabel() {
        return label;
    }

    public final void setLabel(String pLabel) {
        label = pLabel;
    }

    public final String getVersion() {
        return version;
    }

    public final void setVersion(String pVersion) {
        version = pVersion;
    }

    public final String getPluginId() {
        return pluginId;
    }

    public final void setPluginId(String pPluginId) {
        pluginId = pPluginId;
    }

    public final Integer getPriorityOrder() {
        return priorityOrder;
    }

    public final void setPriorityOrder(Integer pOrder) {
        priorityOrder = pOrder;
    }

    public final List<PluginParameter> getParameters() {
        return parameters;
    }

    public final void setParameters(List<PluginParameter> pParameters) {
        parameters = pParameters;
    }

    public Boolean isActive() {
        return active;
    }

    public void setIsActive(Boolean pIsActive) {
        active = pIsActive;
    }

    public String getPluginClassName() {
        return pluginClassName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String pInterfaceName) {
        interfaceName = pInterfaceName;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((label == null) ? 0 : label.hashCode());
        result = (prime * result) + ((pluginId == null) ? 0 : pluginId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PluginConfiguration other = (PluginConfiguration) obj;
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }
        if (pluginId == null) {
            if (other.pluginId != null) {
                return false;
            }
        } else if (!pluginId.equals(other.pluginId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PluginConfiguration [id=" + id + ", pluginId=" + pluginId + ", label=" + label + ", version=" + version
                + ", priorityOrder=" + priorityOrder + ", active=" + active + ", pluginClassName=" + pluginClassName
                + ", interfaceName=" + interfaceName + "]";
    }

}
