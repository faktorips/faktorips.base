/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;

/**
 * Type of a delta.
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
        MISSING_PROPERTY_VALUE = new DeltaType(enumType,
                "MissingPropertyValue", Messages.DeltaType_missingValue, "DeltaTypeMissingPropertyValue.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 
        VALUE_WITHOUT_PROPERTY = new DeltaType(
                enumType,
                "ValueWithoutProperty", Messages.DeltaType_propertiesNotFoundInTheModel, "DeltaTypeValueWithoutProperty.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 
        PROPERTY_TYPE_MISMATCH = new DeltaType(
                enumType,
                "PropertyTypeMismatch", Messages.DeltaType_propertiesWithTypeMismatch, "DeltaTypePropertyTypeMismatch.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 
        VALUE_SET_MISMATCH = new DeltaType(enumType,
                "ValueSetMismatch", Messages.DeltaType_ValueSetMismatches, "DeltaTypeValueSetMismatch.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 
        LINK_WITHOUT_ASSOCIATION = new DeltaType(
                enumType,
                "LinkWithoutAssociation", Messages.DeltaType_LinksNotFoundInTheModel, "DeltaTypeLinkWithoutAssociation.gif"); //$NON-NLS-1$ //$NON-NLS-2$ 

        ALL_TYPES = new DeltaType[] { MISSING_PROPERTY_VALUE, VALUE_WITHOUT_PROPERTY, PROPERTY_TYPE_MISMATCH,
                VALUE_SET_MISMATCH, LINK_WITHOUT_ASSOCIATION };
    }

    private String imageName;

    public String getDescription() {
        return getName();
    }

    private DeltaType(DefaultEnumType type, String id, String name, String image) {
        super(type, id, name);
        imageName = image;
    }

}
