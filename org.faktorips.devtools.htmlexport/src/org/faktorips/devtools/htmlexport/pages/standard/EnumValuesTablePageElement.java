/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

/**
 * 
 */
package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;

/**
 * <p>
 * EnumValuesTablePageElement is table which represents the values of {@link IEnumType}s or
 * {@link IEnumContent}s.
 * </p>
 * <p>
 * The rows contain the enumValues and the columns contain the values of an enumAttribute
 * </p>
 * 
 * @author dicker
 * 
 */
class EnumValuesTablePageElement extends AbstractSpecificTablePageElement {

    private List<IEnumAttribute> enumAttributes;
    private List<IEnumValue> enumValues;

    /**
     * creates an EnumValuesTablePageElement basing on the given {@link IEnumType}
     * 
     * @param type
     */
    public EnumValuesTablePageElement(IEnumType type) {
        super();
        initEnumAttributes(type);
        enumValues = type.getEnumValues();
    }

    /**
     * creates an EnumValuesTablePageElement basing on the given {@link IEnumContent}
     * 
     * @param content
     */
    public EnumValuesTablePageElement(IEnumContent content) {
        super();
        try {
            initEnumAttributes(content.findEnumType(content.getIpsProject()));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        enumValues = content.getEnumValues();
    }

    /**
     * finds the enumAttributes and initializes the List
     * 
     * @param type
     */
    private void initEnumAttributes(IEnumType type) {
        try {
            enumAttributes = type.findAllEnumAttributesIncludeSupertypeOriginals(true, type.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#
     * addDataRows()
     */
    @Override
    protected void addDataRows() {
        for (IEnumValue value : enumValues) {
            addValueRow(value);
        }
    }

    /**
     * adds a row for the given {@link IEnumValue}
     * 
     * @param value
     */
    protected void addValueRow(IEnumValue value) {
        addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getValueData(value))));
    }

    /**
     * returns the data of the attributes of the given {@link IEnumValue}
     * 
     * @param value
     * @return
     */
    protected List<String> getValueData(IEnumValue value) {
        List<String> valueData = new ArrayList<String>();

        for (IEnumAttribute enumAttribute : enumAttributes) {
            valueData.add(value.getEnumAttributeValue(enumAttribute).getValue());
        }

        return valueData;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#
     * getHeadline()
     */
    @Override
    protected List<String> getHeadline() {
        List<String> headline = new ArrayList<String>();

        for (IEnumAttribute enumAttribute : enumAttributes) {
            headline.add(enumAttribute.getName());
        }

        return headline;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.DataPageElement#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return enumAttributes.isEmpty() || enumValues.isEmpty();
    }
}
