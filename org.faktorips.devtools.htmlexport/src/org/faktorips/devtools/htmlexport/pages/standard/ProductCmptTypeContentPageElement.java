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
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MethodsTablePageElement;

/**
 * a page representing an {@link IProductCmptType}
 * 
 * @author dicker
 * 
 */
public class ProductCmptTypeContentPageElement extends AbstractTypeContentPageElement<IProductCmptType> {

    /**
     * a table representing the table structures of the given productCmptType
     * 
     * @author dicker
     * 
     */
    private static class TableStructureTablePageElement extends
            AbstractIpsObjectPartsContainerTablePageElement<ITableStructureUsage> {
        public TableStructureTablePageElement(IProductCmptType productCmptType, DocumentationContext context) {
            super(Arrays.asList(productCmptType.getTableStructureUsages()), context);
        }

        @Override
        protected List<? extends PageElement> createRowWithIpsObjectPart(ITableStructureUsage tableStructureUsage) {
            List<PageElement> pageElements = new ArrayList<PageElement>();

            pageElements.add(new TextPageElement(tableStructureUsage.getRoleName()));
            pageElements.add(new TextPageElement(getContext().getLabel(tableStructureUsage)));
            pageElements.add(getTableStructureLinks(tableStructureUsage));
            pageElements.add(new TextPageElement(tableStructureUsage.isMandatoryTableContent() ? "X" : "-")); //$NON-NLS-1$ //$NON-NLS-2$
            pageElements.add(new TextPageElement(getContext().getDescription(tableStructureUsage)));

            return pageElements;
        }

        private PageElement getTableStructureLinks(ITableStructureUsage tableStructureUsage) {
            String[] tableStructures = tableStructureUsage.getTableStructures();
            if (tableStructures.length == 0) {
                return new TextPageElement("No " + IpsObjectType.TABLE_STRUCTURE.getDisplayNamePlural()); //$NON-NLS-1$
            }

            List<PageElement> links = new ArrayList<PageElement>();
            for (String tableStructure : tableStructures) {
                addLinkToTableStructure(links, tableStructureUsage, tableStructure);
            }

            if (links.size() == 1) {
                return links.get(0);
            }

            return new ListPageElement(links);
        }

        private void addLinkToTableStructure(List<PageElement> links,
                ITableStructureUsage tableStructureUsage,
                String tableStructure) {
            IIpsObject ipsObject;
            try {
                ipsObject = tableStructureUsage.getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE,
                        tableStructure);
            } catch (CoreException e) {
                getContext().addStatus(
                        new IpsStatus(IStatus.ERROR, "Could not find TableStructure " + tableStructure, e)); //$NON-NLS-1$
                return;
            }
            PageElement link = PageElementUtils.createLinkPageElement(getContext(), ipsObject,
                    "content", tableStructure, true); //$NON-NLS-1$
            links.add(link);
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<String>();

            headline.add(Messages.ProductCmptTypeContentPageElement_roleName);
            headline.add(Messages.ProductCmptTypeContentPageElement_headlineLabel);
            headline.add(IpsObjectType.TABLE_STRUCTURE.getDisplayName());

            addHeadlineAndColumnLayout(headline, IpsObjectType.TABLE_CONTENTS.getDisplayName()
                    + Messages.ProductCmptTypeContentPageElement_mandatory, Style.CENTER);

            headline.add(Messages.ProductCmptTypeContentPageElement_description);

            return headline;
        }
    }

    /**
     * creates a page for the given {@link ProductCmptType} with the given context
     * 
     */
    protected ProductCmptTypeContentPageElement(IProductCmptType productCmptType, DocumentationContext context) {
        super(productCmptType, context);
    }

    @Override
    public void build() {
        super.build();

        addTableStructureTable();

        addProductCmptList();
    }

    /**
     * adds a table with the table structure
     */
    private void addTableStructureTable() {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(IpsObjectType.TABLE_STRUCTURE.getDisplayNamePlural(),
                TextType.HEADING_2));
        wrapper.addPageElements((getTableOrAlternativeText(new TableStructureTablePageElement(getDocumentedIpsObject(),
                getContext()), Messages.ProductCmptTypeContentPageElement_noTableStructures)));
        addPageElements(wrapper);
    }

    /**
     * adds a list with the productCmpts
     */
    private void addProductCmptList() {
        List<IIpsSrcFile> allProductCmptSrcFiles;
        try {
            allProductCmptSrcFiles = new ArrayList<IIpsSrcFile>(Arrays.asList(getDocumentedIpsObject()
                    .searchMetaObjectSrcFiles(true)));
        } catch (CoreException e) {
            IpsStatus status = new IpsStatus(IStatus.ERROR,
                    "Error getting ProductCmpts of " + getDocumentedIpsObject().getQualifiedName(), e); //$NON-NLS-1$
            getContext().addStatus(status);
            return;
        }

        allProductCmptSrcFiles.retainAll(getContext().getDocumentedSourceFiles());

        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(IpsObjectType.PRODUCT_CMPT.getDisplayNamePlural(),
                TextType.HEADING_2));

        if (allProductCmptSrcFiles.size() == 0) {
            wrapper.addPageElements(new TextPageElement(Messages.ProductCmptTypeContentPageElement_no
                    + IpsObjectType.PRODUCT_CMPT.getDisplayNamePlural()));
            addPageElements(wrapper);
            return;
        }

        List<PageElement> linkPageElements = PageElementUtils.createLinkPageElements(allProductCmptSrcFiles, "content", //$NON-NLS-1$
                new LinkedHashSet<Style>(), getContext());
        ListPageElement liste = new ListPageElement(linkPageElements);

        wrapper.addPageElements(liste);
        addPageElements(wrapper);
    }

    @Override
    protected void addStructureData() {
        super.addStructureData();

        IPolicyCmptType to = null;
        try {
            to = getDocumentedIpsObject().getIpsProject().findPolicyCmptType(
                    getDocumentedIpsObject().getPolicyCmptType());
        } catch (CoreException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR,
                            "Error getting PolicyCmptType of " + getDocumentedIpsObject().getQualifiedName(), e)); //$NON-NLS-1$
            return;
        }

        if (to == null) {
            addPageElements(TextPageElement.createParagraph(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName()
                    + ": " + Messages.ProductCmptTypeContentPageElement_none)); //$NON-NLS-1$
            return;
        }

        addPageElements(new WrapperPageElement(
                WrapperType.BLOCK,
                new PageElement[] {
                        new TextPageElement(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName() + ": "), PageElementUtils.createLinkPageElement(getContext(), to, "content", to.getName(), true) })); //$NON-NLS-1$ //$NON-NLS-2$

    }

    @Override
    MethodsTablePageElement getMethodsTablePageElement() {
        return new MethodsTablePageElement(getDocumentedIpsObject(), getContext()) {

            @Override
            protected List<String> getHeadlineWithIpsObjectPart() {

                List<String> headline = super.getHeadlineWithIpsObjectPart();
                headline.add(Messages.ProductCmptTypeContentPageElement_formulaName);

                return headline;
            }

            @Override
            protected List<String> getMethodData(IMethod method) {
                List<String> methodData = super.getMethodData(method);

                IProductCmptTypeMethod productMethod = (IProductCmptTypeMethod)method;
                methodData.add(productMethod.getFormulaName());

                return methodData;
            }

        };
    }
}
