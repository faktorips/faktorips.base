/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;

/**
 * Type for product component deltas
 * 
 * @author Thorsten Guenther
 */
class ProductCmptDeltaType extends DefaultEnumValue {
	
	/**
	 * A config element exists, but the attribute referenced by the config element does not
	 * exist.
	 */
	public static final ProductCmptDeltaType MISSING_ATTRIBUTE;
	
	/**
	 * An attribute exists, but no config element refers to it.
	 */
	public static final ProductCmptDeltaType MISSING_CFGELEMENT;
	
	/**
	 * The valueset of the config element is not compatible with the valueset of the attribute.
	 */
	public static final ProductCmptDeltaType VALUESET_MISMATCH;
	
	/**
	 * The type of the config element (formula, product or policy) does not match the
	 * type which is given by the attribute.
	 */
	public static final ProductCmptDeltaType CFGELEMENT_TYPE_MISMATCH;
	
	/**
	 * A relation exists in the product component, but its type is not defined.
	 */
	public static final ProductCmptDeltaType RELATION_MISMATCH;
	
    private final static DefaultEnumType enumType; 

    static {
        enumType = new DefaultEnumType("ProductCmptDeltaType", ProductCmptDeltaType.class); //$NON-NLS-1$
        MISSING_ATTRIBUTE = new ProductCmptDeltaType(enumType, "missingAttr", Messages.ProductCmptDeltaType_missingAttribute, "MissingAttribute.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        MISSING_CFGELEMENT = new ProductCmptDeltaType(enumType, "missingCfg", Messages.ProductCmptDeltaType_missingCfgElement, "MissingConfigElement.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        VALUESET_MISMATCH = new ProductCmptDeltaType(enumType, "valuesetMismatch", Messages.ProductCmptDeltaType_valuesetMismatch, "ValueSetMismatch.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        CFGELEMENT_TYPE_MISMATCH = new ProductCmptDeltaType(enumType, "typeMismatch", Messages.ProductCmptDeltaType_cfgElementTypeMismatch, "CfgElementTypeMismatch.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        RELATION_MISMATCH = new ProductCmptDeltaType(enumType, "relationMismatch", Messages.ProductCmptDeltaType_missingRelationDefinition, "RelationMismatch.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private Image icon;
    
    private ProductCmptDeltaType(DefaultEnumType type, String id, String name, String icon) {
        super(type, id, name);
        this.icon = IpsPlugin.getDefault().getImage(icon);
    }
    
    /**
     * @return The image for this type.
     */
    public Image getImage() {
    	return icon;
    }
}