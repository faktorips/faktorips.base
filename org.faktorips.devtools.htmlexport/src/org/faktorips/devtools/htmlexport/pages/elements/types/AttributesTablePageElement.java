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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;

/**
 * Represents a table with the attributes of an {@link IType} as rows and the attributes of the
 * attribute as columns
 * 
 * @author dicker
 * 
 */
public class AttributesTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IAttribute> {

    protected IType type;

    /**
     * Creates an {@link AttributesTablePageElement} for the specified {@link IType}
     * 
     */
    public AttributesTablePageElement(IType type) {
        super(Arrays.asList(type.getAttributes()));
        this.type = type;
        setId(type.getName() + "_" + "attributes"); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    protected List<? extends PageElement> createRowWithIpsObjectPart(IAttribute attribute) {
        return Arrays.asList(PageElementUtils.createTextPageElements(getAttributeData(attribute)));
    }

    /**
     * returns a list with the values of the attributes of the attribute
     * 
     */
    protected List<String> getAttributeData(IAttribute attribute) {
        List<String> attributeData = new ArrayList<String>();

        attributeData.add(attribute.getName());
        attributeData.add(attribute.getDatatype());
        attributeData.add(attribute.getModifier().toString());
        attributeData.add(attribute.getDefaultValue());
        // TODO AW: HTML-Export description needs to be configured
        attributeData.add(attribute.getDescription());

        return attributeData;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<String>();

        headline.add(Messages.AttributesTablePageElement_headlineName);
        headline.add(Messages.AttributesTablePageElement_headlineDatatype);
        headline.add(Messages.AttributesTablePageElement_headlineModifier);
        headline.add(Messages.AttributesTablePageElement_headlineDefaultValue);
        headline.add(Messages.AttributesTablePageElement_headlineDescription);

        return headline;
    }

}
