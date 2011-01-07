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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ProductGenerationAttributeTable;

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
        IProductCmptType productCmptType;
        try {
            productCmptType = getContext().getIpsProject().findProductCmptType(
                    getDocumentedIpsObject().getProductCmptType());
        } catch (CoreException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR, "Error getting  " + getDocumentedIpsObject().getProductCmptType(), e)); //$NON-NLS-1$
            return;
        }

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
                new TextPageElement(IpsObjectType.PRODUCT_CMPT_TYPE.getDisplayName() + ": "), //$NON-NLS-1$
                PageElementUtils.createLinkPageElement(getContext(), productCmptType,
                        "content", productCmptType.getName(), true) })); //$NON-NLS-1$
    }

    @Override
    public void build() {
        super.build();

        addGenerationsList();

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
        } catch (CoreException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR, "Error getting " + getDocumentedIpsObject().getProductCmptType(), e)); //$NON-NLS-1$
            return;
        }
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(
                getContext().getMessage(HtmlExportMessages.ProductCmptContentPageElement_values), TextType.HEADING_2)); 

        wrapper.addPageElements(getTableOrAlternativeText(productGenerationAttributeTable,
                getContext().getMessage(HtmlExportMessages.ProductCmptContentPageElement_noGenerationsOrAttributes))); 
        addPageElements(wrapper);
    }

    /**
     * adds a list of generations
     */
    private void addGenerationsList() {
        IIpsObjectGeneration[] generations = getDocumentedIpsObject().getGenerationsOrderedByValidDate();

        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(getContext()
                .getMessage("ProductCmptContentPageElement_generations"), //$NON-NLS-1$
                TextType.HEADING_2));

        if (generations.length == 0) {
            wrapper.addPageElements(new TextPageElement("No generations")); //$NON-NLS-1$
            addPageElements(wrapper);
            return;
        }

        List<String> validFroms = new ArrayList<String>();

        for (IIpsObjectGeneration ipsObjectGeneration : generations) {
            GregorianCalendar validFrom = ipsObjectGeneration.getValidFrom();
            validFroms.add(getContext().getSimpleDateFormat().format(validFrom.getTime()));
        }

        wrapper.addPageElements(new ListPageElement(Arrays.asList(PageElementUtils.createTextPageElements(validFroms))));
        addPageElements(wrapper);
    }
}
