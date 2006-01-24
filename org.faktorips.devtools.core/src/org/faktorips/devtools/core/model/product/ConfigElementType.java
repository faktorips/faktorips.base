package org.faktorips.devtools.core.model.product;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

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

	public Image getImage() {
		if (this.equals(PRODUCT_ATTRIBUTE)) {
			return IpsPlugin.getDefault().getImage("ProductAttribute.gif");
		}
		else if (this.equals(POLICY_ATTRIBUTE)) {
			return IpsPlugin.getDefault().getImage("PolicyAttribute.gif");
		}
		else if (this.equals(FORMULA)) {
			return IpsPlugin.getDefault().getImage("Formula.gif");
		}
		else {
			return IpsPlugin.getDefault().getImage("<undefined>");
		}
	}
    
}
