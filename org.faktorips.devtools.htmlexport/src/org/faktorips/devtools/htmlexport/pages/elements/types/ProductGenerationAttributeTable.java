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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * Represents a table with the values of the {@link IAttribute}s of an {@link IProductCmpt} as rows
 * and the generations of the {@link IProductCmpt} as columns
 * 
 * @author dicker
 * 
 */
public class ProductGenerationAttributeTable extends AbstractStandardTablePageElement {

    private final IProductCmpt productCmpt;
    private final IAttribute[] attributes;
    private final DocumentorConfiguration config;
    private final IProductCmptType productCmptType;

    /**
     * Creates a {@link ProductGenerationAttributeTable} for the specified {@link IProductCmpt}
     * 
     */
    public ProductGenerationAttributeTable(IProductCmpt productCmpt, IProductCmptType productCmptType,
            DocumentorConfiguration config) {
        this.productCmpt = productCmpt;
        this.productCmptType = productCmptType;
        this.attributes = findAttributes(productCmptType);
        this.config = config;
    }

    /**
     * returns the attributes of the given {@link ProductCmptType}
     * 
     */
    private IAttribute[] findAttributes(IProductCmptType productCmptType) {
        try {
            return productCmptType.findAllAttributes(productCmpt.getIpsProject());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void addDataRows() {
        for (IAttribute attribute : attributes) {
            addAttributeRow(attribute);
        }
        addChildProductCmptTypes();
    }

    private void addChildProductCmptTypes() {
        PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

        cells[0] = new TextPageElement(Messages.ProductGenerationAttributeTable_associatedComponents);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            ITableContentUsage[] tableContentUsages = productCmptGeneration.getTableContentUsages();
            List<ITableContents> tableContents = new ArrayList<ITableContents>();
            for (ITableContentUsage tableContentUsage : tableContentUsages) {
                try {
                    ITableContents tableContent = tableContentUsage.findTableContents(config.getIpsProject());
                    if (tableContent != null) {
                        tableContents.add(tableContent);
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }

            if (productCmptGeneration.getLinks().length == 0 && tableContents.size() == 0) {
                cells[i + 1] = new TextPageElement("-"); //$NON-NLS-1$
                continue;
            }

            AbstractCompositePageElement cellContent = new WrapperPageElement(WrapperType.BLOCK);

            addAssociatedProductCmpts(productCmptGeneration, cellContent);

            addUsedTableContent(tableContents, cellContent);

            cells[i + 1] = cellContent;
        }

        addSubElement(new TableRowPageElement(cells));
    }

    private void addUsedTableContent(List<ITableContents> tableContents, AbstractCompositePageElement cellContent) {
        for (ITableContents tableContent : tableContents) {
            PageElement linkPageElement = PageElementUtils.createLinkPageElement(config, tableContent,
                    "content", tableContent.getName(), true); //$NON-NLS-1$
            linkPageElement.makeBlock();
            cellContent.addPageElements(linkPageElement);
        }
    }

    private void addAssociatedProductCmpts(IProductCmptGeneration productCmptGeneration,
            AbstractCompositePageElement cellContent) {
        IAssociation[] associations = productCmptType.getAssociations();
        for (IAssociation association : associations) {
            TreeNodePageElement root = new TreeNodePageElement(PageElementUtils.createIpsElementRepresentation(
                    association, config.getLabel(association), true));
            IProductCmptLink[] links = productCmptGeneration.getLinks(association.getName());
            for (IProductCmptLink productCmptLink : links) {
                try {
                    IProductCmpt target = productCmptLink.findTarget(productCmpt.getIpsProject());
                    root.addPageElements(PageElementUtils.createLinkPageElement(config, target,
                            "content", target.getName(), true)); //$NON-NLS-1$
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
            cellContent.addPageElements(root);
        }
    }

    /**
     * adds the row of an attribute with the value of all generations
     * 
     */
    private void addAttributeRow(IAttribute attribute) {
        PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

        String caption = config.getLabel(attribute);
        cells[0] = new TextPageElement(caption);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IAttributeValue attributeValue = productCmpt.getProductCmptGeneration(i).getAttributeValue(
                    attribute.getName());
            String value = attributeValue == null ? Messages.ProductGenerationAttributeTable_undefined : attributeValue
                    .getValue();
            cells[i + 1] = new TextPageElement(value);
        }

        addSubElement(new TableRowPageElement(cells));

    }

    @Override
    protected List<String> getHeadline() {
        List<String> headline = new ArrayList<String>();

        headline.add(Messages.ProductGenerationAttributeTable_generationFrom);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            headline.add(config.getSimpleDateFormat().format(
                    productCmpt.getProductCmptGeneration(i).getValidFrom().getTime()));
        }
        return headline;
    }

    @Override
    public boolean isEmpty() {
        return productCmpt.getNumOfGenerations() == 0;
    }

    @Override
    protected void createId() {
        setId(productCmpt.getName() + "_ProductGenerationAttributeTable"); //$NON-NLS-1$
    }

}
