/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.plugins.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.modules.plugins.annotations.PluginParameter;

/**
 * ISamplePlugin
 * 
 * @author cmertz
 *
 */
@Plugin(author = "CSSI", description = "Sample plugin test", id = "aSamplePlugin", version = "0.0.1")
public class SamplePlugin implements ISamplePlugin {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SamplePlugin.class);

    /**
     * A {@link String} parameter
     */
    @PluginParameter(description = "string parameter", name = SUFFIXE)
    private String suffix_ = "";

    /**
     * A {@link Integer} parameter
     */
    @PluginParameter(description = "int parameter", name = COEFF)
    private Integer coef_ = 0;

    /**
     * A {@link Boolean} parameter
     */
    @PluginParameter(description = "boolean parameter", name = ACTIVE)
    private Boolean isActive_ = Boolean.FALSE;

    @Override
    public String echo(String pMessage) {
        final StringBuffer str = new StringBuffer();
        if (this.isActive_) {
            str.append(this.getClass().getName() + "-" + pMessage + this.suffix_);
        } else {

            str.append(this.getClass().getName() + ":is not active");
        }
        return str.toString();
    }

    @Override
    public int add(int pFist, int pSecond) {
        final int res = this.coef_ * (pFist + pSecond);
        LOGGER.info(this.getClass().getName() + ":" + res);
        return res;
    }

    /**
     * Init method
     */
    @PluginInit
    private void aInit() {
        LOGGER.info("Init method call : " + this.getClass().getName() + "suffixe:" + this.suffix_ + "|active:"
                + this.isActive_ + "|coeff:" + this.coef_);
    }

}
