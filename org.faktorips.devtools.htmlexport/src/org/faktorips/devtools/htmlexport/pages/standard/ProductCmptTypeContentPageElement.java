package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
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
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement;
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
    private class TableStructureTablePageElement extends AbstractSpecificTablePageElement {
        private IProductCmptType productCmptType;

        public TableStructureTablePageElement(IProductCmptType productCmptType) {
            super();
            this.productCmptType = productCmptType;
        }

        @Override
        protected void addDataRows() {
            ITableStructureUsage[] tableStructureUsages = productCmptType.getTableStructureUsages();
            for (ITableStructureUsage tableStructureUsage : tableStructureUsages) {
                addTableStructureUsageRow(tableStructureUsage);
            }

        }

        private void addTableStructureUsageRow(ITableStructureUsage tableStructureUsage) {
            addSubElement(new TableRowPageElement(new PageElement[] {
                    new TextPageElement(tableStructureUsage.getRoleName()),
                    getTableStructureLinks(tableStructureUsage),
                    new TextPageElement(tableStructureUsage.isMandatoryTableContent() ? "X" : "-"), //$NON-NLS-1$ //$NON-NLS-2$
                    new TextPageElement(tableStructureUsage.getDescription()) }));
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
        protected List<String> getHeadline() {
            List<String> headline = new ArrayList<String>();

            headline.add(Messages.ProductCmptTypeContentPageElement_roleName);
            headline.add(IpsObjectType.TABLE_STRUCTURE.getDisplayName());

            addHeadlineAndColumnLayout(headline, IpsObjectType.TABLE_CONTENTS.getDisplayName()
                    + Messages.ProductCmptTypeContentPageElement_mandatory, Style.CENTER);

            headline.add(Messages.ProductCmptTypeContentPageElement_description);

            return headline;
        }

        @Override
        public boolean isEmpty() {
            return ArrayUtils.isEmpty(productCmptType.getTableStructureUsages());
        }

    }

    /**
     * creates a page for the given {@link ProductCmptType} with the given config
     * 
     * @param productCmptType
     * @param config
     */
    protected ProductCmptTypeContentPageElement(IProductCmptType productCmptType, DocumentorConfiguration config) {
        super(productCmptType, config);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.standard. AbstractTypeContentPageElement#build()
     */
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
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
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
            allProductCmptSrcFiles = new ArrayList(Arrays.asList(getDocumentedIpsObject()
                    .searchMetaObjectSrcFiles(true)));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        allProductCmptSrcFiles.retainAll(getConfig().getDocumentedSourceFiles());

        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
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

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.standard.
     * AbstractTypeContentPageElement#addStructureData()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.standard.
     * AbstractTypeContentPageElement#getMethodsTablePageElement()
     */
    @Override
    MethodsTablePageElement getMethodsTablePageElement() {
        return new MethodsTablePageElement(getDocumentedIpsObject()) {

            @Override
            protected List<String> getHeadline() {

                List<String> headline = super.getHeadline();
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
