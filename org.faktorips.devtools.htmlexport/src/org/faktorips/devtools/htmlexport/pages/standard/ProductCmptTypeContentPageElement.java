/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MethodsTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ProductCmptTypeAttributesTablePageElement;

/**
 * a page representing an {@link IProductCmptType}
 * 
 * @author dicker
 * 
 */
public class ProductCmptTypeContentPageElement extends AbstractTypeContentPageElement<IProductCmptType> {

    /**
     * creates a page for the given {@link ProductCmptType} with the given context
     * 
     */
    protected ProductCmptTypeContentPageElement(IProductCmptType productCmptType, DocumentationContext context) {
        super(productCmptType, context);
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addTableStructureTable();

        addProductCmptList();
    }

    /**
     * adds a table with the table structure
     */
    private void addTableStructureTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProductCmptTypeContentPageElement_tableStructures), TextType.HEADING_2, getContext()));
        wrapper.addPageElements((getTableOrAlternativeText(new TableStructureTablePageElement(getDocumentedIpsObject(),
                getContext()),
                getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_noTableStructures))));
        addPageElements(wrapper);
    }

    /**
     * adds a list with the productCmpts
     */
    private void addProductCmptList() {
        List<IIpsSrcFile> allProductCmptSrcFiles;
        try {
            allProductCmptSrcFiles = new ArrayList<IIpsSrcFile>(getDocumentedIpsObject().searchMetaObjectSrcFiles(true));
        } catch (CoreException e) {
            IpsStatus status = new IpsStatus(IStatus.ERROR,
                    "Error getting ProductCmpts of " + getDocumentedIpsObject().getQualifiedName(), e); //$NON-NLS-1$
            getContext().addStatus(status);
            return;
        }

        allProductCmptSrcFiles.retainAll(getContext().getDocumentedSourceFiles());

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProductCmptTypeContentPageElement_productComponents), TextType.HEADING_2,
                getContext()));

        if (allProductCmptSrcFiles.size() == 0) {
            wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                    HtmlExportMessages.ProductCmptTypeContentPageElement_noProductComponents), getContext()));
            addPageElements(wrapper);
            return;
        }

        List<IPageElement> linkPageElements = new PageElementUtils(getContext()).createLinkPageElements(
                allProductCmptSrcFiles, TargetType.CONTENT, new LinkedHashSet<Style>(), getContext());
        ListPageElement liste = new ListPageElement(linkPageElements, getContext());

        wrapper.addPageElements(liste);
        addPageElements(wrapper);
    }

    @Override
    protected void addStructureData() {
        super.addStructureData();

        IPolicyCmptType to = null;
        to = getDocumentedIpsObject().getIpsProject().findPolicyCmptType(getDocumentedIpsObject().getPolicyCmptType());

        if (to == null) {
            addPageElements(TextPageElement
                    .createParagraph(
                            IpsObjectType.POLICY_CMPT_TYPE.getDisplayName()
                                    + ": " + getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_none), getContext())); //$NON-NLS-1$
            return;
        }

        addPageElements(new WrapperPageElement(
                WrapperType.BLOCK,
                getContext(),
                new IPageElement[] {
                    new TextPageElement(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName() + ": ", getContext()), new PageElementUtils(getContext()).createLinkPageElement(getContext(), to, TargetType.CONTENT, getContext().getLabel(to), true) })); //$NON-NLS-1$

    }

    @Override
    MethodsTablePageElement getMethodsTablePageElement() {
        return new TableMethodContentPageElement(getDocumentedIpsObject(), getContext());
    }

    @Override
    AttributesTablePageElement getAttributesTablePageElement() {
        return new ProductCmptTypeAttributesTablePageElement(getDocumentedIpsObject(), getContext());
    }

    private static final class TableMethodContentPageElement extends MethodsTablePageElement {

        private static final String IS_FALSE = "-"; //$NON-NLS-1$
        private static final String IS_TRUE = "X"; //$NON-NLS-1$

        private TableMethodContentPageElement(IType type, DocumentationContext context) {
            super(type, context);
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {

            List<String> headline = super.getHeadlineWithIpsObjectPart();
            headline.add(getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_formulaName));
            addHeadlineAndColumnLayout(headline,
                    getContext()
                    .getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_changeableInAdjustment),
                    Style.CENTER);
            return headline;
        }

        @Override
        protected List<String> getMethodData(IMethod method) {
            List<String> methodData = super.getMethodData(method);

            IProductCmptTypeMethod productMethod = (IProductCmptTypeMethod)method;
            methodData.add(productMethod.getFormulaName());
            methodData.add(productMethod.isChangingOverTime() ? IS_TRUE : IS_FALSE);
            return methodData;
        }
    }
}
