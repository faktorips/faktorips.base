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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
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
    public AttributesTablePageElement(IType type, DocumentationContext context) {
        super(type.getAttributes(), context);
        this.type = type;
        setId(type.getName() + "_" + "attributes"); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    protected List<? extends PageElement> createRowWithIpsObjectPart(IAttribute attribute) {
        PageElement[] textPageElements = new PageElementUtils().createTextPageElements(getAttributeData(attribute));
        textPageElements[0].setAnchor(new PageElementUtils().createAnchorId(attribute));
        return Arrays.asList(textPageElements);
    }

    /**
     * returns a list with the values of the attributes of the attribute
     * 
     */
    protected List<String> getAttributeData(IAttribute attribute) {
        List<String> attributeData = new ArrayList<String>();

        attributeData.add(getContext().getLabel(attribute));
        attributeData.add(getContext().getLabel(attribute));
        attributeData.add(attribute.getDatatype());
        attributeData.add(attribute.getModifier().toString());

        try {
            attributeData.add(getContext().getDatatypeFormatter().formatValue(
                    getContext().getIpsProject().findValueDatatype(attribute.getDatatype()),
                    attribute.getDefaultValue()));
        } catch (CoreException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Unable to find ValueDatatype for attribute " //$NON-NLS-1$
                    + attribute.getName()));
            attributeData.add(attribute.getDefaultValue());
        }

        attributeData.add(getContext().getDescription(attribute));

        return attributeData;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<String>();

        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineName));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineLabel));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineDatatype));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineModifier));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineDefaultValue));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineDescription));

        return headline;
    }

}
