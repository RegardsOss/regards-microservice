/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameterType;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameterType.ParamType;

/**
 * Post process plugin instances to inject annotated parameters.
 *
 * @author Christophe Mertz
 * @author Marc Sordi
 */
public final class PluginParameterUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginParameterUtils.class);

    /**
     * Retrieve List of {@link PluginParameterType} by reflection on class fields
     *
     * @param pluginClass the target class
     * @return List of {@link PluginParameterType} or null.
     */
    public static List<PluginParameterType> getParameters(Class<?> pluginClass) {
        return getParameters(pluginClass, false, null);
    }

    /**
     * Retrieve {@link List} of {@link PluginParameterType} by reflection on class fields with in depth parameter
     * discovery
     * @param pluginClass the plugin implementation
     * @param isChildParameters if <code>true</code>, {@link PluginParameter} is not required for in depth parameters
     *            discovery.
     * @param alreadyManagedTypeNames List of already managed type names to detect cyclic references during in depth
     *            parameters discovery.
     * @return list of {@link PluginParameterType} or null
     */
    private static List<PluginParameterType> getParameters(Class<?> pluginClass, boolean isChildParameters,
            List<String> alreadyManagedTypeNames) {
        List<PluginParameterType> parameters = new ArrayList<>();

        for (final Field field : ReflectionUtils.getAllDeclaredFields(pluginClass)) {
            if (field.isAnnotationPresent(PluginParameter.class) || (isChildParameters && isToBeConsidered(field))) {
                // Initialize list of managed types for in depth scanning from root fields
                List<String> managedTypes = new ArrayList<>();
                if (isChildParameters) {
                    managedTypes.addAll(alreadyManagedTypeNames);
                }
                parameters.add(buildPluginParameter(field, pluginClass, managedTypes));
            }
        }
        return parameters.isEmpty() ? null : parameters;
    }

    /**
     * Help method allowing us to determine if the field should be taken into account or not.
     * @param field to examine
     * @return true if the field is neither final or static
     */
    private static boolean isToBeConsidered(Field field) {
        return !Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers());
    }

    /**
     * Create a new {@link PluginParameterType} from a class {@link Field}
     *
     * @param field {@link Field} to get type from
     * @param pluginClass the plugin implementation
     * @param alreadyManagedTypeNames List of already managed type names to detect cyclic references during in depth
     *            parameters discovery.
     * @return {@link PluginParameterType}
     */
    private static PluginParameterType buildPluginParameter(Field field, Class<?> pluginClass,
            List<String> alreadyManagedTypeNames) {
        PluginParameterType result;

        // Retrieve type of field
        ParamType paramType = getFieldParameterType(field);

        // Retrieve annotation if any
        PluginParameter pluginParameter = field.getAnnotation(PluginParameter.class);

        // Create PluginParameter
        if (pluginParameter == null) {
            // Guess values
            result = PluginParameterType.create(field.getName(), field.getName(), null, field.getType(), paramType,
                                                false, false);
        } else {
            // Report values from annotation
            String name = getFieldName(field, pluginParameter);

            result = PluginParameterType.create(name, pluginParameter.label(), pluginParameter.description(),
                                                field.getType(), paramType, pluginParameter.optional(),
                                                pluginParameter.unconfigurable());

            // Manage markdown description
            String markdown = AnnotationUtils.loadMarkdown(pluginClass, pluginParameter.markdown());
            result.setMarkdown(markdown);

            // Manage default value
            if ((pluginParameter.defaultValue() != null) && !pluginParameter.defaultValue().isEmpty()) {
                result.setDefaultValue(pluginParameter.defaultValue());
            }
        }

        // Do in depth discovery for OBJECT
        // Note : at the moment an OBJECT cannot be a PARAMETERIZED type : {@link #getFieldParameterType(Field, List)}
        if (ParamType.OBJECT.equals(paramType)) {
            // Register current type in already managed ones
            registerTypeName(alreadyManagedTypeNames, field.getType());
            // Propagate discovery
            result.addAllParameters(getParameters(field.getType(), true, alreadyManagedTypeNames));
        }

        // Do in depth discovery for COLLECTION and register parameterized sub type
        else if (ParamType.COLLECTION.equals(paramType)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            // Get single parameter type
            Class<?> argType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            result.setParameterizedSubTypes(argType.getName());
            // Propagate discovery
            tryPropagatingDiscovery(field.getType(), argType, alreadyManagedTypeNames, result);
        }

        // Do in depth discovery for MAP and register parameterized sub types
        else if (ParamType.MAP.equals(paramType)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            // Set key label
            if (pluginParameter != null) {
                result.setKeyLabel(pluginParameter.keylabel());
            }
            // Get parameter types
            Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
            result.setParameterizedSubTypes(keyType.getName(), valueType.getName());
            // Propagate discovery
            tryPropagatingDiscovery(field.getType(), keyType, alreadyManagedTypeNames, result);
            tryPropagatingDiscovery(field.getType(), valueType, alreadyManagedTypeNames, result);
        }
        return result;
    }

    /**
     * Get configuration parameter name.
     * @param field {@link Field}
     * @param pluginParameter {@link PluginParameter} field annotation
     * @return the parameter's name used as a key for database registration. If not specified, falling back to field
     *         name.
     */
    private static String getFieldName(Field field, PluginParameter pluginParameter) {
        return pluginParameter.name().isEmpty() ? field.getName() : pluginParameter.name();
    }

    /**
     * Try propagating parameter discovery to parameterized type arguments. May throw a
     * {@link PluginUtilsRuntimeException} if an argument is a {@link Collection} or {@link Map}.
     * @param rawType raw type
     * @param argType one argument type
     * @param alreadyManagedTypeNames List of already managed type names to detect cyclic references during in depth
     *            parameters discovery
     * @param result current {@link PluginParameterType}
     */
    private static void tryPropagatingDiscovery(Class<?> rawType, Class<?> argType,
            List<String> alreadyManagedTypeNames, PluginParameterType result) {
        if (isAPrimitiveType(argType).isPresent() || isAnInterface(argType)) {
            LOGGER.debug("Primitive or plugin type detected. Stop plugin parameters discovery.");
        } else if (Collection.class.isAssignableFrom(argType) || Map.class.isAssignableFrom(argType)) {
            String message = String.format("Parameterized argument of type collection or map is not supported : %s",
                                           rawType.getName());
            LOGGER.error(message);
            throw new PluginUtilsRuntimeException(message);
        } else {
            // Register current type in already managed ones
            registerTypeName(alreadyManagedTypeNames, argType);
            // Propagate discovery
            result.addAllParameters(getParameters(argType, true, alreadyManagedTypeNames));
        }
    }

    /**
     * Check if clazz not already managed and add it to the list if not
     * @param alreadyManagedTypeNames already managed type names
     * @param clazz class to check
     */
    private static void registerTypeName(List<String> alreadyManagedTypeNames, Class<?> clazz) {
        if (alreadyManagedTypeNames.contains(clazz.getName())) {
            String message = String.format("Cyclic parameter types detected !! for %s", clazz.getName());
            LOGGER.error(message);
            throw new PluginUtilsRuntimeException(message);
        } else {
            alreadyManagedTypeNames.add(clazz.getName());
        }
    }

    /**
     * Retrieve the {@link ParamType} associated to the given {@link Field}
     * @param field {@link Field} to get type from
     * @return
     */
    private static ParamType getFieldParameterType(Field field) {
        ParamType parameterType;
        if (isAPrimitiveType(field.getType()).isPresent()) {
            parameterType = ParamType.PRIMITIVE;
        } else if (isAnInterface(field.getType())) {
            parameterType = ParamType.PLUGIN;
        } else if (field.getGenericType() instanceof ParameterizedType) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                parameterType = ParamType.COLLECTION;
            } else if (Map.class.isAssignableFrom(field.getType())) {
                parameterType = ParamType.MAP;
            } else {
                throw new PluginUtilsRuntimeException(
                        String.format("Parameter type not supported : %s", field.getGenericType()));
            }
        } else {
            parameterType = ParamType.OBJECT;
        }
        return parameterType;
    }

    /**
     * Search a clazz in the {@link PrimitiveObject}
     *
     * @param clazz class to analyze
     * @return an {@link Optional} {@link PrimitiveObject}
     */
    private static Optional<PrimitiveObject> isAPrimitiveType(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            for (PrimitiveObject po : PrimitiveObject.values()) {
                if (clazz.isAssignableFrom(po.getPrimitive())) {
                    return Optional.of(po);
                }
            }
        } else {
            for (PrimitiveObject po : PrimitiveObject.values()) {
                if (clazz.isAssignableFrom(po.getType())) {
                    return Optional.of(po);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Search a field like a {@link PluginInterface}
     *
     * @param clazz class to analyse
     * @return true is the type of the field is a {@link PrimitiveObject}
     */
    private static boolean isAnInterface(Class<?> clazz) {
        boolean isSupportedType = false;
        Set<String> pluginInterfaces = PluginUtils.getPluginInterfaces();

        if ((pluginInterfaces != null) && !pluginInterfaces.isEmpty()) {
            isSupportedType = pluginInterfaces.stream().filter(s -> s.equalsIgnoreCase(clazz.getName())).count() > 0;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("interface parameter : %s --> %b", clazz.getName(), isSupportedType));
        }

        return isSupportedType;
    }

    /**
     * Use configured values to set field values.
     *
     * @param @param <T> a {@link Plugin} type
     * @param gson GSON deserializer instance. Fallback to default local Gson instance is {@link Optional#empty()}
     * @param returnPlugin the plugin instance
     * @param plgConf the {@link PluginConfiguration}
     * @param instantiatedPluginMap a {@link Map} of already instantiated {@link Plugin}
     * @param dynamicPluginParameters an optional set of
     *            {@link fr.cnes.regards.framework.modules.plugins.domain.PluginParameter} @ if any error occurs
     */
    public static <T> void postProcess(Optional<Gson> gson, T returnPlugin, PluginConfiguration plgConf,
            Map<Long, Object> instantiatedPluginMap,
            fr.cnes.regards.framework.modules.plugins.domain.PluginParameter... dynamicPluginParameters) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting postProcess :" + returnPlugin.getClass().getSimpleName());
        }

        // Test if the plugin configuration is active
        if (!plgConf.isActive()) {
            throw new PluginUtilsRuntimeException(String.format("The plugin configuration <%s-%s> is not active.",
                                                                plgConf.getId(), plgConf.getLabel()));
        }

        // Look for annotated fields
        for (final Field field : ReflectionUtils.getAllDeclaredFields(returnPlugin.getClass())) {
            if (field.isAnnotationPresent(PluginParameter.class)) {
                PluginParameter plgParamAnnotation = field.getAnnotation(PluginParameter.class);
                Gson gsonProcessor = gson.isPresent() ? gson.get() : PluginGsonUtils.getInstance();
                processPluginParameter(gsonProcessor, returnPlugin, plgConf, field, plgParamAnnotation,
                                       instantiatedPluginMap, dynamicPluginParameters);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Ending postProcess :" + returnPlugin.getClass().getSimpleName());
        }
    }

    /**
     * Use configured values to set field value for a {@link PluginParameter}
     *
     * @param <T> a {@link Plugin} type
     * @param gson GSON deserializer instance
     * @param pluginInstance the {@link Plugin} instance
     * @param plgConf the plugin configuration to used
     * @param field the parameter
     * @param plgParamAnnotation the {@link PluginParameter} annotation
     * @param instantiatedPluginMap a {@link Map} of already instantiated {@link Plugin}
     * @param dynamicPluginParameters an optional set of
     *            {@link fr.cnes.regards.framework.modules.plugins.domain.PluginParameter} @ if any error occurs
     */
    private static <T> void processPluginParameter(Gson gson, T pluginInstance, PluginConfiguration plgConf,
            Field field, PluginParameter plgParamAnnotation, Map<Long, Object> instantiatedPluginMap,
            fr.cnes.regards.framework.modules.plugins.domain.PluginParameter... dynamicPluginParameters) {

        // Inject value
        ReflectionUtils.makeAccessible(field);

        // Try to get a primitive type for the current parameter
        final Optional<PrimitiveObject> typeWrapper = isAPrimitiveType(field.getType());

        ParamType paramType = getFieldParameterType(field);
        switch (paramType) {
            case PRIMITIVE:
                LOGGER.debug(String.format("primitive parameter : %s --> %s", field.getName(), field.getType()));
                postProcessPrimitiveType(gson, pluginInstance, plgConf, field, typeWrapper, plgParamAnnotation,
                                         dynamicPluginParameters);
                break;
            case PLUGIN:
                LOGGER.debug(String.format("interface parameter : %s --> %s", field.getName(), field.getType()));
                postProcessInterface(pluginInstance, plgConf, field, plgParamAnnotation, instantiatedPluginMap);
                break;
            case OBJECT:
            case COLLECTION:
            case MAP:
                LOGGER.debug(String.format("Object parameter %s : %s --> %s", paramType, field.getName(),
                                           field.getType()));
                postProcessObjectType(gson, pluginInstance, plgConf, field, plgParamAnnotation, paramType,
                                      dynamicPluginParameters);
                break;
            default:
                throw new PluginUtilsRuntimeException(String.format("Type parameter <%s> is unknown.", field));
        }
    }

    /**
     * Compute the parameter value from the plugin configuration or possibly from dynamic plugin parameters if there are
     * any!
     * @param parameterName parameter name
     * @param pluginConf plugin configuration
     * @param dynamicPluginParameters dynamic plugin parameters
     * @return paramater value
     */
    private static String getParameterValue(String parameterName, PluginConfiguration pluginConf,
            fr.cnes.regards.framework.modules.plugins.domain.PluginParameter... dynamicPluginParameters) {

        // Default value comes from plugin configuration
        String paramValue = pluginConf.getParameterValue(parameterName);

        // Manage dynamic parameters
        if ((dynamicPluginParameters != null) && (dynamicPluginParameters.length > 0)) {

            // Search dynamic parameter for current parameter name
            Optional<fr.cnes.regards.framework.modules.plugins.domain.PluginParameter> dynamicParameterOpt = Arrays
                    .asList(dynamicPluginParameters).stream().filter(s -> s.getName().equals(parameterName))
                    .findFirst();
            if (dynamicParameterOpt.isPresent()) {
                fr.cnes.regards.framework.modules.plugins.domain.PluginParameter dynamicParameter = dynamicParameterOpt
                        .get();
                // Check if the parameter is only dynamic, in that case, we do not even lookup into the plugin conf
                if (dynamicParameter.isOnlyDynamic()) {
                    paramValue = dynamicParameter.getValue();
                } else {

                    // Check if parameter can be dynamic in plugin configuration
                    Optional<fr.cnes.regards.framework.modules.plugins.domain.PluginParameter> confParameter = pluginConf
                            .getParameters().stream()
                            .filter(s -> s.getName().equals(dynamicParameter.getName()) && s.isDynamic()).findFirst();
                    if (confParameter.isPresent()) {

                        // Override plugin configuration value with dynamic one
                        paramValue = dynamicParameterOpt.get().getValue();
                        LOGGER.debug("Parameter with name \"{}\" set to its dynamic value \"{}\"", parameterName,
                                     paramValue);

                        // Check if this value is valid
                        if (!confParameter.get().isValidDynamicValue(paramValue)) {
                            // The dynamic parameter value is not a possible value
                            throw new PluginUtilsRuntimeException(String
                                    .format("The dynamic value <%s> is not an authorized value for the parameter %s.",
                                            paramValue, parameterName));
                        }
                    }
                }
            }
        }

        return paramValue;
    }

    /**
     * Use configured values to set field values for a parameter of type OBJECT or PARAMETERIZED_OBJECT
     *
     * @param <T> a {@link Plugin} type
     * @param gson GSON deserializer instance
     * @param pluginInstance the {@link Plugin} instance
     * @param plgConf the plugin configuration to used
     * @param field the parameter
     * @param plgParamAnnotation the {@link PluginParameter} annotation
     * @param paramType param type to consider
     * @param dynamicPluginParameters an optional set of
     *            {@link fr.cnes.regards.framework.modules.plugins.domain.PluginParameter}
     */
    private static <T> void postProcessObjectType(Gson gson, T pluginInstance, PluginConfiguration plgConf, Field field,
            PluginParameter plgParamAnnotation, ParamType paramType,
            fr.cnes.regards.framework.modules.plugins.domain.PluginParameter... dynamicPluginParameters) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting postProcessObjectType :" + plgParamAnnotation.label());
        }

        // Retrieve parameter name
        String parameterName = getFieldName(field, plgParamAnnotation);

        if (field.getType().isInterface()
                && !(ParamType.COLLECTION.equals(paramType) || ParamType.MAP.equals(paramType))) {
            throw new PluginUtilsRuntimeException(String
                    .format("Invalid plugin parameter of non instanciable interface %s", field.getType().getName()));
        }
        if (ParamType.COLLECTION.equals(paramType) && !Collection.class.isAssignableFrom(field.getType())) {
            throw new PluginUtilsRuntimeException(String
                    .format("Invalid plugin parameter: plugin parameter %s is supposed to be of type %s but is not. It is of type : %s",
                            parameterName, Collection.class.getName(), field.getType().getName()));
        }
        if (ParamType.MAP.equals(paramType) && !Map.class.isAssignableFrom(field.getType())) {
            throw new PluginUtilsRuntimeException(String
                    .format("Invalid plugin parameter: plugin parameter %s is supposed to be of type %s but is not. It is of type : %s",
                            parameterName, Map.class.getName(), field.getType().getName()));
        }
        // Get parameter value
        String paramValue = getParameterValue(parameterName, plgConf, dynamicPluginParameters);

        // Do not handle default value for object types.
        if (paramValue != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Object parameter json value : %s", paramValue));
            }
            // Deserialize object from JSON value
            Object objectParamValue = gson.fromJson(paramValue, field.getGenericType());

            if (objectParamValue != null) {
                try {
                    field.set(pluginInstance, objectParamValue);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    LOGGER.error(String.format("Error during Object parameter deserialization for parameter %s",
                                               parameterName),
                                 e);
                }
            }
        }
    }

    /**
     * Use configured values to set field values for a parameter of type {@link PrimitiveObject}
     *
     * @param <T> a {@link Plugin} type
     * @param gson GSON JSON engine
     * @param pluginInstance the {@link Plugin} instance
     * @param plgConf the {@link PluginConfiguration} to used
     * @param field the parameter
     * @param typeWrapper the type wrapper of the parameter
     * @param plgParamAnnotation the {@link PluginParameter} annotation
     * @param dynamicPluginParameters an optional set of
     *            {@link fr.cnes.regards.framework.modules.plugins.domain.PluginParameter} @ if any error occurs
     */
    private static <T> void postProcessPrimitiveType(Gson gson, T pluginInstance, PluginConfiguration plgConf,
            Field field, final Optional<PrimitiveObject> typeWrapper, PluginParameter plgParamAnnotation,
            fr.cnes.regards.framework.modules.plugins.domain.PluginParameter... dynamicPluginParameters) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting postProcessPrimitiveType :" + plgParamAnnotation.label());
        }

        // Retrieve parameter name
        String parameterName = getFieldName(field, plgParamAnnotation);

        // Get parameter value
        String paramValue = getParameterValue(parameterName, plgConf, dynamicPluginParameters);

        // If the parameter value is not defined, get the default parameter value
        if (Strings.isNullOrEmpty(paramValue) && !plgParamAnnotation.defaultValue().isEmpty()) {
            paramValue = plgParamAnnotation.defaultValue();
        }

        // Stop if no value and optional parameter
        if (Strings.isNullOrEmpty(paramValue) && plgParamAnnotation.optional()) {
            LOGGER.debug(String.format("Skipping value injection for optional parameter %s. No value specified!",
                                       parameterName));
            return;
        }

        // At this point, if the parameter value is not set, there is a problem( value is not defaulted and parameter is
        // not optional)
        if (Strings.isNullOrEmpty(paramValue)) {
            throw new IllegalArgumentException(String
                    .format("Issue with Plugin %s and one of its configuration %s, parameter %s has no default value and is required.",
                            plgConf.getPluginId(), plgConf.getLabel(), parameterName));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("primitive parameter value : %s", paramValue));
        }

        try {
            Object effectiveVal;
            if (typeWrapper.get().getType().equals(PrimitiveObject.STRING.getType())) {
                // Strip quotes using Gson
                JsonElement el = gson.fromJson(paramValue, JsonElement.class);
                // FIXME : Handle datasource plugin configurations
                if ((el != null) && el.isJsonPrimitive()) {
                    effectiveVal = el.getAsString();
                } else {
                    if (paramValue.startsWith("\"") && paramValue.endsWith("\"") && (paramValue.length() > 2)) {
                        effectiveVal = paramValue.substring(1, paramValue.length() - 1);
                    } else {
                        effectiveVal = paramValue;
                    }
                }
            } else {
                final Method method = typeWrapper.get().getType().getDeclaredMethod("valueOf", String.class);
                effectiveVal = method.invoke(null, paramValue);
            }
            field.set(pluginInstance, effectiveVal);
        } catch (final IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException
                | InvocationTargetException e) {
            // Propagate exception
            throw new PluginUtilsRuntimeException(
                    String.format("Exception while processing param <%s> in plugin class <%s> with value <%s>.",
                                  plgParamAnnotation.label(), pluginInstance.getClass(), paramValue),
                    e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Ending postProcessPrimitiveType :" + plgParamAnnotation.label());
        }
    }

    /**
     * Use configured values to set field values for a parameter of type {@link PluginParameter}
     *
     * @param <T> a {@link Plugin} type
     * @param pluginInstance the {@link Plugin} instance
     * @param plgConf the plugin configuration to used
     * @param field the parameter
     * @param plgParamAnnotation the {@link PluginParameter} annotation
     * @param instantiatedPluginMap a Map of all already instantiated plugins
     */
    private static <T> void postProcessInterface(T pluginInstance, PluginConfiguration plgConf, Field field,
            PluginParameter plgParamAnnotation, Map<Long, Object> instantiatedPluginMap) {
        LOGGER.debug("Starting postProcessInterface: {}", plgParamAnnotation.label());

        // Get setup value
        PluginConfiguration paramValue = plgConf.getParameterConfiguration(getFieldName(field, plgParamAnnotation));

        LOGGER.debug(String.format("interface parameter value : %s", paramValue));

        try {
            // Retrieve instantiated plugin from cache map
            Object effectiveVal = instantiatedPluginMap.get(paramValue.getId());
            field.set(pluginInstance, effectiveVal);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Propagate exception
            throw new PluginUtilsRuntimeException(
                    String.format("Exception while processing param <%s> in plugin class <%s> with value <%s>.",
                                  plgParamAnnotation.label(), pluginInstance.getClass(), paramValue),
                    e);
        }
        LOGGER.debug("Ending postProcessInterface: {}", plgParamAnnotation.label());
    }

    /**
     * PrimitiveObject for the plugin parameters
     *
     * @author Christophe Mertz
     */
    public enum PrimitiveObject {
        /**
         * A primitive of {@link String}. No primitive for string! Fallback to void!
         */
        STRING(String.class, void.class),

        /**
         * A primitive of {@link Byte}
         */
        BYTE(Byte.class, byte.class),

        /**
         * A primitive of {@link Short}
         */
        SHORT(Short.class, short.class),

        /**
         * A primitive of {@link Integer}
         */
        INT(Integer.class, int.class),

        /**
         * A primitive of {@link Long}
         */
        LONG(Long.class, long.class),

        /**
         * A primitive of {@link Float}
         */
        FLOAT(Float.class, float.class),

        /**
         * A primitive of {@link Double}
         */
        DOUBLE(Double.class, double.class),

        /**
         * A primitive of {@link Boolean}
         */
        BOOLEAN(Boolean.class, boolean.class);

        private final Class<?> type;

        private final Class<?> primitive;

        /**
         * Constructor
         *
         * @param type primitive object type
         * @param primitive primitive class
         */
        private PrimitiveObject(final Class<?> type, Class<?> primitive) {
            this.type = type;
            this.primitive = primitive;
        }

        /**
         * Get method.
         *
         * @return the type
         */
        public Class<?> getType() {
            return type;
        }

        public Class<?> getPrimitive() {
            return primitive;
        }
    }
}
