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
import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;

/**
 * a table representing the table structures of the given productCmptType
 */
public class TableStructureTablePageElement extends
        AbstractIpsObjectPartsContainerTablePageElement<ITableStructureUsage> {

    private static final String IS_FALSE = "-"; //$NON-NLS-1$
    private static final String IS_TRUE = "X"; //$NON-NLS-1$

    public TableStructureTablePageElement(IProductCmptType productCmptType, DocumentationContext context) {
        super(productCmptType.getTableStructureUsages(), context);
    }

    @Override
    protected List<IPageElement> createRowWithIpsObjectPart(ITableStructureUsage tableStructureUsage) {
        List<IPageElement> pageElements = new ArrayList<>();

        pageElements.add(new TextPageElement(tableStructureUsage.getRoleName(), getContext()));
        pageElements.add(new TextPageElement(getContext().getLabel(tableStructureUsage), getContext()));
        pageElements.add(getTableStructureLinks(tableStructureUsage));
        pageElements.add(new TextPageElement(tableStructureUsage.isMandatoryTableContent() ? IS_TRUE : IS_FALSE,
                getContext()));
        pageElements.add(new TextPageElement(tableStructureUsage.isChangingOverTime() ? IS_TRUE : IS_FALSE,
                getContext()));
        pageElements.add(new TextPageElement(getContext().getDescription(tableStructureUsage), getContext()));

        return pageElements;
    }

    private IPageElement getTableStructureLinks(ITableStructureUsage tableStructureUsage) {
        String[] tableStructures = tableStructureUsage.getTableStructures();
        if (tableStructures.length == 0) {
            return new TextPageElement("No " + IpsObjectType.TABLE_STRUCTURE.getDisplayNamePlural(), getContext()); //$NON-NLS-1$
        }

        List<IPageElement> links = new ArrayList<>();
        for (String tableStructure : tableStructures) {
            addLinkToTableStructure(links, tableStructureUsage, tableStructure);
        }

        if (links.size() == 1) {
            return links.get(0);
        }

        return new ListPageElement(links, getContext());
    }

    private void addLinkToTableStructure(List<IPageElement> links,
            ITableStructureUsage tableStructureUsage,
            String tableStructure) {
        IIpsObject ipsObject;
        ipsObject = tableStructureUsage.getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, tableStructure);
        IPageElement link = new PageElementUtils(getContext()).createLinkPageElement(getContext(), ipsObject,
                TargetType.CONTENT, tableStructure, true);
        links.add(link);
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<>();

        headline.add(getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_roleName));
        headline.add(getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_headlineLabel));
        headline.add(IpsObjectType.TABLE_STRUCTURE.getDisplayName());

        addHeadlineAndColumnLayout(
                headline,
                IpsObjectType.TABLE_CONTENTS.getDisplayName()
                        + getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_mandatory),
                Style.CENTER);
        addHeadlineAndColumnLayout(headline,
                getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_changeableInAdjustment),
                Style.CENTER);
        headline.add(getContext().getMessage(HtmlExportMessages.ProductCmptTypeContentPageElement_description));

        return headline;
    }
}