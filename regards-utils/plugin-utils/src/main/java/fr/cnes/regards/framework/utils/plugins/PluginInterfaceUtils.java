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

package fr.cnes.regards.framework.utils.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;

/**
 *
 * Plugin utilities
 *
 * @author Christophe Mertz
 */
public final class PluginInterfaceUtils {

    /**
     *
     * Constructor
     *
     */
    private PluginInterfaceUtils() {
        // Static class
    }

    /**
     *
     * Retrieve all annotated {@link PluginInterface}.
     * 
     * @param pPrefixs
     *            a list of package prefix used for the search
     * @return all class annotated {@link PluginInterface}
     */
    public static List<String> getInterfaces(final List<String> pPrefixs) {
        final List<String> interfaces = new ArrayList<>();

        pPrefixs.forEach(p -> {
            final List<String> ll = getInterfaces(p);
            if (ll != null && !ll.isEmpty()) {
                ll.forEach(s -> interfaces.add(s));
            }
        });

        return interfaces;
    }

    /**
     *
     * Retrieve all annotated {@link PluginInterface}.
     * 
     * @param pPrefix
     *            a package prefix used for the search
     * @return all class annotated {@link PluginInterface}
     */
    public static List<String> getInterfaces(final String pPrefix) {
        final List<String> interfaces = new ArrayList<>();

        // Scan class path with Reflections library
        final Reflections reflections = new Reflections(pPrefix);
        final Set<Class<?>> annotatedPlugins = reflections.getTypesAnnotatedWith(PluginInterface.class, true);

        if (!annotatedPlugins.isEmpty()) {
            annotatedPlugins.stream().forEach(str -> interfaces.add(str.getCanonicalName()));
        }

        return interfaces;
    }

}