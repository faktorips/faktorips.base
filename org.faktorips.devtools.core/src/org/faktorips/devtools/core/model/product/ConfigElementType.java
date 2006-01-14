package org.faktorips.devtools.core.model.product;

import org.faktorips.datatype.DefaultEnumType;
import org.faktorips.datatype.DefaultEnumValue;
import org.faktorips.datatype.EnumType;

/**
 * Describes the kind of attribute. 
 */
public class ConfigElementType extends DefaultEnumValue {
    
    public final static ConfigElementType PRODUCT_ATTRIBUTE;

    public final static ConfigElementType POLICY_ATTRIBUTE;

    public final static ConfigElementType FORMULA;
    
    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("ConfigElementType", ConfigElementType.class);
        PRODUCT_ATTRIBUTE = new ConfigElementType(enumType, "productAttribute", "Product IAttribute");
        POLICY_ATTRIBUTE = new ConfigElementType(enumType, "policyAttribute", "Policy IAttribute");
        FORMULA = new ConfigElementType(enumType, "formula", "Formula");
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    public final static ConfigElementType getConfigElementType(String id) {
        return (ConfigElementType)enumType.getEnumValue(id);
    }
    
    private ConfigElementType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }
    
}
