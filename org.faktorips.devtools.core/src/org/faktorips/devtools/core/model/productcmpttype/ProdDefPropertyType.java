/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

/**
 * Enumeration that specifies the different (sub)types of product definition properties.
 * 
 * @see IProdDefProperty#getProdDefPropertyType()
 * 
 * @author Jan Ortmann
 */
public enum ProdDefPropertyType {

    /**
     * The product definition property is an attribute of a product component type.
     */
    VALUE("attribute", Messages.ProdDefPropertyType_productAttribute), //$NON-NLS-1$

    TABLE_CONTENT_USAGE("tableContentUsage", Messages.ProdDefPropertyType_tableUsage), //$NON-NLS-1$

    FORMULA("formula", Messages.ProdDefPropertyType_fomula), //$NON-NLS-1$

    DEFAULT_VALUE_AND_VALUESET("config", Messages.ProdDefPropertyType_defaultValueAndValueSet); //$NON-NLS-1$

    private final String name;
    private final String id;

    private ProdDefPropertyType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id.
     */
    public String getId() {
        return id;
    }

}
