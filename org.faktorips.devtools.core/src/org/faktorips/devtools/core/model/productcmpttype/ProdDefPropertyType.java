/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.enums.EnumType;

/**
 * Enumeration that specifies the different (sub)types of product definition properties. So instead
 * of using inheritance for the different (sub)types of product definition properties, the
 * {@link IProdDefProperty) has a type flag {@link IProdDefProperty#getProdDefPropertyType()}.
 * 
 * @author Jan Ortmann
 */
public class ProdDefPropertyType extends DefaultEnumValue {

    /**
     * The product definition property is an attribute of a product component type.
     */
    public final static ProdDefPropertyType VALUE;

    public final static ProdDefPropertyType FORMULA;

    public final static ProdDefPropertyType TABLE_CONTENT_USAGE;

    public final static ProdDefPropertyType DEFAULT_VALUE_AND_VALUESET;

    public final static int MAX_SORT_ORDER = 40;

    public final static DefaultEnumType enumType;

    public final static ProdDefPropertyType[] ALL_TYPES;

    static {
        enumType = new DefaultEnumType("ProdDefPropertyType", ProdDefPropertyType.class); //$NON-NLS-1$
        VALUE = new ProdDefPropertyType(enumType,
                "attribute", Messages.ProdDefPropertyType_productAttribute, 10, "ProductAttribute.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        TABLE_CONTENT_USAGE = new ProdDefPropertyType(enumType,
                "tableContentUsage", Messages.ProdDefPropertyType_tableUsage, 20, "TableContentsUsage.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        FORMULA = new ProdDefPropertyType(enumType, "formula", Messages.ProdDefPropertyType_fomula, 30, "Formula.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        DEFAULT_VALUE_AND_VALUESET = new ProdDefPropertyType(enumType,
                "config", Messages.ProdDefPropertyType_defaultValueAndValueSet, MAX_SORT_ORDER, "PolicyAttribute.gif"); //$NON-NLS-1$ //$NON-NLS-2$

        ALL_TYPES = new ProdDefPropertyType[] { VALUE, TABLE_CONTENT_USAGE, FORMULA, DEFAULT_VALUE_AND_VALUESET };
    }

    public final static EnumType getEnumType() {
        return enumType;
    }

    private int sortOrder;
    private String imageName;

    public int getSortOrder() {
        return sortOrder;
    }

    public Image getImage() {
        return IpsPlugin.getDefault().getImage(imageName);
    }

    private ProdDefPropertyType(DefaultEnumType type, String id, String name, int sortOrder, String imageName) {
        super(type, id, name);
        this.sortOrder = sortOrder;
        this.imageName = imageName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object o) {
        ProdDefPropertyType otherType = (ProdDefPropertyType)o;
        return sortOrder - otherType.sortOrder;
    }

}
