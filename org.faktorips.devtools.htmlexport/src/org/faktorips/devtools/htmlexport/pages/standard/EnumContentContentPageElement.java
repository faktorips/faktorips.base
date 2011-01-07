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

package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

/**
 * A complete page representing an {@link IEnumContent}
 * 
 * @author dicker
 * 
 */
public class EnumContentContentPageElement extends AbstractIpsObjectContentPageElement<IEnumContent> {

    private IEnumType enumType;

    /**
     * 
     * creates a page, which represents the given enumContent according to the given context
     * 
     */
    protected EnumContentContentPageElement(IEnumContent object, DocumentationContext context) throws CoreException {
        super(object, context);
        this.enumType = object.getIpsProject().findEnumType(object.getEnumType());
    }

    @Override
    public void build() {
        super.build();

        addPageElements(new WrapperPageElement(WrapperType.BLOCK).addPageElements(
                new TextPageElement(IpsObjectType.ENUM_TYPE.getDisplayName() + ": ")).addPageElements( //$NON-NLS-1$
                PageElementUtils.createLinkPageElement(getContext(), getEnumType(),
                        "content", getEnumType().getQualifiedName(), true))); //$NON-NLS-1$

        addValuesTable();
    }

    /**
     * adds a table with the values of the enumContent
     */
    protected void addValuesTable() {
        EnumValuesTablePageElement tablePageElement;
        try {
            tablePageElement = new EnumValuesTablePageElement(getDocumentedIpsObject(), getContext());
        } catch (CoreException e) {
            IpsStatus status = new IpsStatus(IStatus.ERROR,
                    "Error creating EnumValuesTable of " + getDocumentedIpsObject().getQualifiedName(), e); //$NON-NLS-1$
            getContext().addStatus(status);
            return;
        }
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(
                getContext().getMessage(HtmlExportMessages.EnumContentContentPageElement_values), TextType.HEADING_2)); 

        wrapper.addPageElements(getTableOrAlternativeText(tablePageElement,
                getContext().getMessage(HtmlExportMessages.EnumContentContentPageElement_noValues))); 

        addPageElements(wrapper);
    }

    /**
     * returns the enumType
     * 
     */
    protected IEnumType getEnumType() {
        return enumType;
    }
}
