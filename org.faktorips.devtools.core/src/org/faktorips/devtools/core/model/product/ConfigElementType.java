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
        enumType = new DefaultEnumType("ConfigElementType", ConfigElementType.class); //$NON-NLS-1$
        PRODUCT_ATTRIBUTE = new ConfigElementType(enumType, "productAttribute", "Product IAttribute"); //$NON-NLS-1$ //$NON-NLS-2$
        POLICY_ATTRIBUTE = new ConfigElementType(enumType, "policyAttribute", "Policy IAttribute"); //$NON-NLS-1$ //$NON-NLS-2$
        FORMULA = new ConfigElementType(enumType, "formula", "Formula"); //$NON-NLS-1$ //$NON-NLS-2$
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
			return IpsPlugin.getDefault().getImage("ProductAttribute.gif"); //$NON-NLS-1$
		}
		else if (this.equals(POLICY_ATTRIBUTE)) {
			return IpsPlugin.getDefault().getImage("PolicyAttribute.gif"); //$NON-NLS-1$
		}
		else if (this.equals(FORMULA)) {
			return IpsPlugin.getDefault().getImage("Formula.gif"); //$NON-NLS-1$
		}
		else {
			return IpsPlugin.getDefault().getImage("<undefined>"); //$NON-NLS-1$
		}
	}
    
}
