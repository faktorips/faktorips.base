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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;

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
class EnumValuesTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IEnumValue> {

    private List<IEnumAttribute> enumAttributes;

    /**
     * creates an EnumValuesTablePageElement basing on the given {@link IEnumType}
     * 
     */
    public EnumValuesTablePageElement(IEnumType type, DocumentorConfiguration config) {
        super(type.getEnumValues(), config);
        initEnumAttributes(type);
    }

    /**
     * creates an EnumValuesTablePageElement basing on the given {@link IEnumContent}
     * 
     */
    public EnumValuesTablePageElement(IEnumContent content, DocumentorConfiguration config) {
        super(content.getEnumValues(), config);
        try {
            initEnumAttributes(content.findEnumType(content.getIpsProject()));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * finds the enumAttributes and initializes the List
     * 
     */
    private void initEnumAttributes(IEnumType type) {
        try {
            enumAttributes = type.findAllEnumAttributesIncludeSupertypeOriginals(true, type.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<? extends PageElement> createRowWithIpsObjectPart(IEnumValue rowData) {
        List<String> valueData = new ArrayList<String>();

        for (IEnumAttribute enumAttribute : enumAttributes) {
            String value = rowData.getEnumAttributeValue(enumAttribute).getValue();
            try {
                ValueDatatype datatype = enumAttribute.findDatatype(getConfig().getIpsProject());

                valueData.add(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(datatype,
                        value));
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        return Arrays.asList(PageElementUtils.createTextPageElements(valueData));
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<String>();

        for (IEnumAttribute enumAttribute : enumAttributes) {
            headline.add(enumAttribute.getName());
        }

        return headline;
    }
}
