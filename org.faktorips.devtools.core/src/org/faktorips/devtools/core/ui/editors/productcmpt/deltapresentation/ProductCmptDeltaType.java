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
 * Type for test case deltas
 * 
 * @author Thorsten Guenther
 */
class ProductCmptDeltaType extends DefaultEnumValue {
	
	/**
	 * A config element exists, but the attribute referenced by the config element does not
	 * exist.
	 */
	public static final ProductCmptDeltaType VALUE_WITHOUT_PROPERTY;
	
	/**
	 * An attribute exists, but no config element refers to it.
	 */
	public static final ProductCmptDeltaType MISSING_PROPERTY_VALUE;
	
	/**
	 * The valueset of the config element is not compatible with the valueset of the attribute.
	 */
	public static final ProductCmptDeltaType VALUESET_MISMATCH;
	
	/**
	 * The type of the config element (formula, product or policy) does not match the
	 * type which is given by the attribute.
	 */
	public static final ProductCmptDeltaType PROPERTY_TYPE_MISMATCH;
	
	/**
	 * A relation exists in the product component, but its type is not defined.
	 */
	public static final ProductCmptDeltaType RELATION_MISMATCH;

    /**
     * A table content usage exists, but the table structure usage referenced by the content usage
     * does not exist.
     */
    public static final ProductCmptDeltaType MISSING_STRUCTUREUSAGE;
    
    /**
     * A table structure usage exists, but no table content usage exits for this structure usage
     */
    public static final ProductCmptDeltaType MISSING_CONTENTUSAGE;
	
    private final static DefaultEnumType enumType; 

    static {
        enumType = new DefaultEnumType("ProductCmptDeltaType", ProductCmptDeltaType.class); //$NON-NLS-1$
        VALUE_WITHOUT_PROPERTY = new ProductCmptDeltaType(enumType, "missingAttr", Messages.ProductCmptDeltaType_missingAttribute, "MissingAttribute.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        MISSING_PROPERTY_VALUE = new ProductCmptDeltaType(enumType, "missingCfg", Messages.ProductCmptDeltaType_missingCfgElement, "MissingConfigElement.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        VALUESET_MISMATCH = new ProductCmptDeltaType(enumType, "valuesetMismatch", Messages.ProductCmptDeltaType_valuesetMismatch, "ValueSetMismatch.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        PROPERTY_TYPE_MISMATCH = new ProductCmptDeltaType(enumType, "typeMismatch", Messages.ProductCmptDeltaType_cfgElementTypeMismatch, "CfgElementTypeMismatch.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        RELATION_MISMATCH = new ProductCmptDeltaType(enumType, "relationMismatch", Messages.ProductCmptDeltaType_missingRelationDefinition, "RelationMismatch.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        MISSING_STRUCTUREUSAGE = new ProductCmptDeltaType(enumType, "missingStructure", Messages.ProductCmptDeltaType_missingTableStructureUsage, "MissingTableStructureUsage.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        MISSING_CONTENTUSAGE = new ProductCmptDeltaType(enumType, "missingContent", Messages.ProductCmptDeltaType_missingTableContentUsage, "MissingTableContentUsage.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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