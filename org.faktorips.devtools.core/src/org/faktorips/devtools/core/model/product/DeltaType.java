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

package org.faktorips.devtools.core.model.product;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;

/**
 * 
 * @author Jan Ortmann
 */
public class DeltaType extends DefaultEnumValue {

    public final static DeltaType MISSING_PROPERTY_VALUE;
    public final static DeltaType VALUE_WITHOUT_PROPERTY;
    public final static DeltaType VALUE_SET_MISMATCH;
    public final static DeltaType PROPERTY_TYPE_MISMATCH;
    public final static DeltaType LINK_WITHOUT_ASSOCIATION;
    
    public final static DeltaType[] ALL_TYPES;
    
    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("DeltaEntryType", DeltaType.class); //$NON-NLS-1$
        MISSING_PROPERTY_VALUE = new DeltaType(enumType, "MissingPropertyValue", "Missing values", "DeltaTypeMissingPropertyValue.gif");
        VALUE_WITHOUT_PROPERTY = new DeltaType(enumType, "ValueWithoutProperty", "Properties that aren't defined in the model", "DeltaTypeValueWithoutProperty.gif");
        PROPERTY_TYPE_MISMATCH = new DeltaType(enumType, "PropertyTypeMismatch", "Properties with a type mismacht", "DeltaTypePropertyTypeMismatch.gif");
        VALUE_SET_MISMATCH = new DeltaType(enumType, "ValueSetMismatch", "Value set mismatches", "DeltaTypeValueSetMismatch.gif");
        LINK_WITHOUT_ASSOCIATION = new DeltaType(enumType, "LinkWithoutAssocation", "Links that aren't defined in the model", "DeltaTypeLinkWithoutAssociation.gif");
        
        ALL_TYPES = new DeltaType[]{MISSING_PROPERTY_VALUE, VALUE_WITHOUT_PROPERTY, PROPERTY_TYPE_MISMATCH, VALUE_SET_MISMATCH, LINK_WITHOUT_ASSOCIATION};
    }
    
    private Image image;

    public Image getImage() {
        return image;
    }
    
    public String getDescription() {
        return getName();
    }
    
    private DeltaType(DefaultEnumType type, String id, String name, String image) {
        super(type, id, name);
        this.image = IpsPlugin.getDefault().getImage(image);
    }

}
