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

/**
 * 
 */
package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
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
    public EnumValuesTablePageElement(IEnumType type, DocumentationContext context) throws CoreException {
        super(type.getEnumValues(), context);
        enumAttributes = type.findAllEnumAttributesIncludeSupertypeOriginals(true, type.getIpsProject());
    }

    /**
     * creates an EnumValuesTablePageElement basing on the given {@link IEnumContent}
     * 
     */
    public EnumValuesTablePageElement(IEnumContent content, DocumentationContext context) throws CoreException {
        super(content.getEnumValues(), context);
        IEnumType type = content.findEnumType(content.getIpsProject());
        enumAttributes = type.findAllEnumAttributesIncludeSupertypeOriginals(true, type.getIpsProject());
    }

    @Override
    protected List<? extends IPageElement> createRowWithIpsObjectPart(IEnumValue rowData) {
        List<String> valueData = new ArrayList<String>();

        for (IEnumAttribute enumAttribute : enumAttributes) {
            addEnumAttributeValue(valueData, rowData, enumAttribute);
        }

        return Arrays.asList(new PageElementUtils().createTextPageElements(valueData));
    }

    private void addEnumAttributeValue(List<String> valueData, IEnumValue rowData, IEnumAttribute enumAttribute) {
        IEnumAttributeValue enumAttributeValue = rowData.getEnumAttributeValue(enumAttribute);
        String value = enumAttributeValue.getValue();
        try {
            ValueDatatype datatype = enumAttribute.findDatatype(getContext().getIpsProject());
            valueData.add(getContext().getDatatypeFormatter().formatValue(datatype, value));
        } catch (CoreException e) {
            IpsStatus status = new IpsStatus(IStatus.WARNING,
                    "Could not format " + enumAttributeValue.getName() + " " + value, e); //$NON-NLS-1$ //$NON-NLS-2$
            getContext().addStatus(status);
            valueData.add(value);
        }

    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<String>();

        for (IEnumAttribute enumAttribute : enumAttributes) {
            headline.add(getContext().getLabel(enumAttribute));
        }

        return headline;
    }
}
