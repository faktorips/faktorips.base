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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.ProductGenerationAttributeTable;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * A page representing a {@link IProductCmpt}
 * 
 * @author dicker
 * 
 */
public class ProductCmptContentPageElement extends AbstractIpsObjectContentPageElement<IProductCmpt> {

    /**
     * creates a page for the given {@link IProductCmpt} with the given context
     * 
     */
    protected ProductCmptContentPageElement(IProductCmpt object, DocumentationContext context) {
        super(object, context);
    }

    @Override
    protected void addStructureData() {
        IProductCmptType productCmptType = findProductCmptType();
        if (productCmptType == null) {
            return;
        }

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext(),
                new TextPageElement(IpsObjectType.PRODUCT_CMPT_TYPE.getDisplayName() + ": ", getContext()), //$NON-NLS-1$
                new PageElementUtils(getContext()).createLinkPageElement(getContext(), productCmptType,
                        TargetType.CONTENT, getContext().getLabel(productCmptType), true)));
    }

    private IProductCmptType findProductCmptType() {
        return getContext().getIpsProject().findProductCmptType(
                getDocumentedIpsObject().getProductCmptType());
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();
        IProductCmptType productCmptType = findProductCmptType();
        if (productCmptType != null && productCmptType.isChangingOverTime()) {
            addGenerationsList();
        }
        addGenerationAttributeTable();
    }

    /**
     * adds a table with the attributes of the generations
     */
    private void addGenerationAttributeTable() {
        ProductGenerationAttributeTable productGenerationAttributeTable;
        try {
            productGenerationAttributeTable = new ProductGenerationAttributeTable(getDocumentedIpsObject(),
                    getContext());
        } catch (IpsException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR, "Error getting " + getDocumentedIpsObject().getProductCmptType(), e)); //$NON-NLS-1$
            return;
        }
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProductCmptContentPageElement_values), TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(productGenerationAttributeTable,
                getContext().getMessage(HtmlExportMessages.ProductCmptContentPageElement_noGenerationsOrAttributes)));
        addPageElements(wrapper);
    }

    /**
     * adds a list of generations
     */
    private void addGenerationsList() {
        IIpsObjectGeneration[] generations = getDocumentedIpsObject().getGenerationsOrderedByValidDate();

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProductCmptContentPageElement_generations), TextType.HEADING_2, getContext()));

        if (generations.length == 0) {
            wrapper.addPageElements(new TextPageElement("No generations", getContext())); //$NON-NLS-1$
            addPageElements(wrapper);
            return;
        }

        List<String> validFroms = new ArrayList<>();

        for (IIpsObjectGeneration ipsObjectGeneration : generations) {
            GregorianCalendar validFrom = ipsObjectGeneration.getValidFrom();
            validFroms.add(getContext().getSimpleDateFormat().format(validFrom.getTime()));
        }

        wrapper.addPageElements(new ListPageElement(Arrays.asList(new PageElementUtils(getContext())
                .createTextPageElements(validFroms)), getContext()));
        addPageElements(wrapper);
    }
}
