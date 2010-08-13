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
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
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
    private class TableStructureTablePageElement extends
            AbstractIpsObjectPartsContainerTablePageElement<ITableStructureUsage> {
        public TableStructureTablePageElement(IProductCmptType productCmptType) {
            super(Arrays.asList(productCmptType.getTableStructureUsages()));
        }

        @Override
        protected List<? extends PageElement> createRowWithIpsObjectPart(ITableStructureUsage tableStructureUsage) {
            List<PageElement> pageElements = new ArrayList<PageElement>();

            pageElements.add(new TextPageElement(tableStructureUsage.getRoleName()));
            pageElements.add(getTableStructureLinks(tableStructureUsage));
            pageElements.add(new TextPageElement(tableStructureUsage.isMandatoryTableContent() ? "X" : "-")); //$NON-NLS-1$ //$NON-NLS-2$
            pageElements.add(new TextPageElement(tableStructureUsage.getDescription()));

            return pageElements;
        }

        private PageElement getTableStructureLinks(ITableStructureUsage tableStructureUsage) {
            String[] tableStructures = tableStructureUsage.getTableStructures();
            if (tableStructures.length == 0) {
                return new TextPageElement("No " + IpsObjectType.TABLE_STRUCTURE.getDisplayNamePlural()); //$NON-NLS-1$
            }

            List<PageElement> links = new ArrayList<PageElement>();
            for (String tableStructure : tableStructures) {
                try {
                    IIpsObject ipsObject = tableStructureUsage.getIpsProject().findIpsObject(
                            IpsObjectType.TABLE_STRUCTURE, tableStructure);
                    links.add(PageElementUtils.createLinkPageElement(getConfig(), ipsObject,
                            "content", tableStructure, true)); //$NON-NLS-1$
                } catch (CoreException e) {
                    new RuntimeException(e);
                }
            }

            if (links.size() == 1) {
                return links.get(0);
            }

            return new ListPageElement(links);
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<String>();

            headline.add(Messages.ProductCmptTypeContentPageElement_roleName);
            headline.add(IpsObjectType.TABLE_STRUCTURE.getDisplayName());

            addHeadlineAndColumnLayout(headline, IpsObjectType.TABLE_CONTENTS.getDisplayName()
                    + Messages.ProductCmptTypeContentPageElement_mandatory, Style.CENTER);

            headline.add(Messages.ProductCmptTypeContentPageElement_description);

            return headline;
        }
    }

    /**
     * creates a page for the given {@link ProductCmptType} with the given config
     * 
     */
    protected ProductCmptTypeContentPageElement(IProductCmptType productCmptType, DocumentorConfiguration config) {
        super(productCmptType, config);
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

        wrapper.addPageElements(new TableStructureTablePageElement(getDocumentedIpsObject()));
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
            throw new RuntimeException(e);
        }

        allProductCmptSrcFiles.retainAll(getConfig().getDocumentedSourceFiles());

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
                new LinkedHashSet<Style>(), getConfig());
        ListPageElement liste = new ListPageElement(linkPageElements);

        wrapper.addPageElements(liste);
        addPageElements(wrapper);
    }

    @Override
    protected void addStructureData() {
        super.addStructureData();

        try {
            IPolicyCmptType to = getDocumentedIpsObject().getIpsProject().findPolicyCmptType(
                    getDocumentedIpsObject().getPolicyCmptType());
            if (to == null) {
                addPageElements(TextPageElement.createParagraph(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName()
                        + ": " + Messages.ProductCmptTypeContentPageElement_none)); //$NON-NLS-1$
                return;
            }
            addPageElements(new WrapperPageElement(
                    WrapperType.BLOCK,
                    new PageElement[] {
                            new TextPageElement(IpsObjectType.POLICY_CMPT_TYPE.getDisplayName() + ": "), PageElementUtils.createLinkPageElement(getConfig(), to, "content", to.getName(), true) })); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (CoreException e) {
            e.printStackTrace();
        }

    }

    @Override
    MethodsTablePageElement getMethodsTablePageElement() {
        return new MethodsTablePageElement(getDocumentedIpsObject()) {

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
