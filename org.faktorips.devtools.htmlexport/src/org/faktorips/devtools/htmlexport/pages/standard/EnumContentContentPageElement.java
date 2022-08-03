/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsStatus;

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
    protected EnumContentContentPageElement(IEnumContent object, DocumentationContext context) {
        super(object, context);
        enumType = object.getIpsProject().findEnumType(object.getEnumType());
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext()).addPageElements(
                new TextPageElement(IpsObjectType.ENUM_TYPE.getDisplayName() + ": ", //$NON-NLS-1$
                        getContext()))
                .addPageElements(
                        new PageElementUtils(getContext()).createLinkPageElement(getContext(), getEnumType(),
                                TargetType.CONTENT, getEnumType().getQualifiedName(), true)));

        addValuesTable();
    }

    /**
     * adds a table with the values of the enumContent
     */
    protected void addValuesTable() {
        EnumValuesTablePageElement tablePageElement;
        try {
            tablePageElement = new EnumValuesTablePageElement(getDocumentedIpsObject(), getContext());
        } catch (IpsException e) {
            IpsStatus status = new IpsStatus(IStatus.ERROR,
                    "Error creating EnumValuesTable of " + getDocumentedIpsObject().getQualifiedName(), e); //$NON-NLS-1$
            getContext().addStatus(status);
            return;
        }
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.EnumContentContentPageElement_values), TextType.HEADING_2, getContext()));

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
