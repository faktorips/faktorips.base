/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

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

    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("ConfigElementType", ConfigElementType.class); //$NON-NLS-1$
        PRODUCT_ATTRIBUTE = new ConfigElementType(enumType, "productAttribute", Messages.ConfigElementType_productAttribute); //$NON-NLS-1$
        POLICY_ATTRIBUTE = new ConfigElementType(enumType, "policyAttribute", Messages.ConfigElementType_policyAttribute); //$NON-NLS-1$
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
		else {
			return IpsPlugin.getDefault().getImage("<undefined>"); //$NON-NLS-1$
		}
	}
    
}
