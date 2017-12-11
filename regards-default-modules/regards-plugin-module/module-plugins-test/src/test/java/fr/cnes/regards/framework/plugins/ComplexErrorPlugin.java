/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.framework.plugins;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;

/**
 * ISamplePlugin
 *
 * @author Christophe Mertz
 */
@Plugin(description = "Complex plugin test", id = "aComplexErrorPlugin", version = "0.0.1", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class ComplexErrorPlugin implements ISamplePlugin {

    /**
     * constant PLG
     */
    static final String PLUGIN_PARAM = "plgInterface";

    /**
     * A {@link Integer} parameter
     */
    @PluginParameter(description = "int parameter", name = COEFF)
    private final Integer coef = 0;

    /**
     * A {@link String} parameter
     */
    @PluginParameter(description = "plugin parameter", name = PLUGIN_PARAM)
    private INotInterfacePlugin interfacePlugin;

    @Override
    public String echo(String pMessage) {
        final StringBuffer str = new StringBuffer();
        str.append(this.getClass().getName() + "-" + pMessage + interfacePlugin.toString());
        return str.toString();
    }

    @Override
    public int add(int pFist, int pSecond) {
        return 0;
    }

}