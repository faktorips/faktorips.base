/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

/**
 * 
 */
package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.plugin.IpsStatus;

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
    public EnumValuesTablePageElement(IEnumType type, DocumentationContext context) {
        super(type.getEnumValues(), context);
        enumAttributes = type.findAllEnumAttributes(true, type.getIpsProject());
    }

    /**
     * creates an EnumValuesTablePageElement basing on the given {@link IEnumContent}
     * 
     */
    public EnumValuesTablePageElement(IEnumContent content, DocumentationContext context) {
        super(content.getEnumValues(), context);
        IEnumType type = content.findEnumType(content.getIpsProject());
        enumAttributes = type.findAllEnumAttributes(true, type.getIpsProject());
    }

    @Override
    protected List<IPageElement> createRowWithIpsObjectPart(IEnumValue rowData) {
        List<String> valueData = new ArrayList<>();

        for (IEnumAttribute enumAttribute : enumAttributes) {
            addEnumAttributeValue(valueData, rowData, enumAttribute);
        }

        return Arrays.asList(new PageElementUtils(getContext()).createTextPageElements(valueData));
    }

    private void addEnumAttributeValue(List<String> valueData, IEnumValue rowData, IEnumAttribute enumAttribute) {
        IEnumAttributeValue enumAttributeValue = rowData.getEnumAttributeValue(enumAttribute);
        if (enumAttributeValue != null && enumAttributeValue.getValue() != null) {
            String value = enumAttributeValue.getValue().getDefaultLocalizedContent(enumAttribute.getIpsProject());
            try {
                ValueDatatype datatype = enumAttribute.findDatatype(getContext().getIpsProject());
                valueData.add(getContext().getDatatypeFormatter().formatValue(datatype, value));
            } catch (IpsException e) {
                IpsStatus status = new IpsStatus(IStatus.WARNING,
                        "Could not format " + enumAttributeValue.getName() + " " + value, e); //$NON-NLS-1$ //$NON-NLS-2$
                getContext().addStatus(status);
                valueData.add(value);
            }
        }

    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<>();

        for (IEnumAttribute enumAttribute : enumAttributes) {
            headline.add(getContext().getLabel(enumAttribute));
        }

        return headline;
    }
}
