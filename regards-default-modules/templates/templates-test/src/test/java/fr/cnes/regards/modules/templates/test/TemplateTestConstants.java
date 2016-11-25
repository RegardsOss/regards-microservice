/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.templates.test;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants and data for testing all different templates business layers.
 *
 * @author Xavier-Alexandre Brochard
 */
public final class TemplateTestConstants {

    /**
     * The templates endpoint
     */
    public static final String API_TEMPLATES = "/templates";

    /**
     * The specific template endpoint
     */
    public static final String API_TEMPLATES_TEMPLATE_ID = "/templates/{template_id}";

    /**
     * Code
     */
    public static final String CODE = "DEFAULT";

    /**
     * Content
     */
    public static final String CONTENT = "Hello $name.";

    /**
     * Data
     */
    // @formatter:off
    @SuppressWarnings("serial")
    public static final Map<String, String> DATA = new HashMap<String, String>() {{ put("name", "Defaultname");put("age", DATA_VALUE_1);put("height", DATA_VALUE_2); }};
    // @formatter:on

    /**
     * Key for a value to store in the data map
     */
    public static final String DATA_KEY_0 = "name";

    /**
     * Key for a value to store in the data map
     */
    public static final String DATA_KEY_1 = "age";

    /**
     * Key for a value to store in the data map
     */
    public static final String DATA_KEY_2 = "height";

    /**
     * A value stored in the data map
     */
    public static final String DATA_VALUE_0 = "Defaultname";

    /**
     * A value stored in the data map
     */
    public static final String DATA_VALUE_1 = "26";

    /**
     * A value stored in the data map
     */
    public static final String DATA_VALUE_2 = "170";

    /**
     * Description
     */
    public static final String DESCRIPTON = "I'm describing what this template is good for";

    /**
     * Id
     */
    public static final Long ID = 0L;

    /**
     * Use this field to represent an inexistent id in db
     */
    public static final Long WRONG_ID = 99L;

    private TemplateTestConstants() {

    }

}
