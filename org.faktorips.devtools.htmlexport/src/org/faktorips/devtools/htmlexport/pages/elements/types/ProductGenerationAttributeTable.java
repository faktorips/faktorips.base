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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
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
    private final DocumentationContext context;
    private final IProductCmptType productCmptType;

    /**
     * Creates a {@link ProductGenerationAttributeTable} for the specified {@link IProductCmpt}
     * 
     */
    public ProductGenerationAttributeTable(IProductCmpt productCmpt, IProductCmptType productCmptType,
            DocumentationContext context) {
        this.productCmpt = productCmpt;
        this.productCmptType = productCmptType;
        this.attributes = findAttributes(productCmptType);
        this.context = context;
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
        addAttributes();

        addFormulas();

        addPolicyCmptTypeAttibutes();

        addTableStructureUsages();

        addChildProductCmptTypes();
    }

    private void addPolicyCmptTypeAttibutes() {
        try {
            List<IPolicyCmptTypeAttribute> policyCmptTypeAttributes = new ArrayList<IPolicyCmptTypeAttribute>();

            IPolicyCmptType policyCmptType = productCmptType.findPolicyCmptType(context.getIpsProject());

            if (policyCmptType == null) {
                return;
            }

            for (IAttribute attribute : policyCmptType.findAllAttributes(context.getIpsProject())) {
                if (!(attribute instanceof IPolicyCmptTypeAttribute)) {
                    continue;
                }
                IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)attribute;
                if (policyCmptTypeAttribute.getProdDefPropertyType() == ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET
                        && policyCmptTypeAttribute.isProductRelevant()) {
                    policyCmptTypeAttributes.add(policyCmptTypeAttribute);
                }
            }

            if (policyCmptTypeAttributes.isEmpty()) {
                return;
            }

            addSubHeadline(Messages.ProductGenerationAttributeTable_defaultsAndValueSets);

            for (IPolicyCmptTypeAttribute policyCmptTypeAttribute : policyCmptTypeAttributes) {
                addPolicyCmptTypeAttibutesRow(policyCmptTypeAttribute);
            }

        } catch (Exception e) {
            IpsPlugin.log(new RuntimeException("Error at ProductComponent " + productCmpt.getQualifiedName(), e)); //$NON-NLS-1$
        }
    }

    private void addPolicyCmptTypeAttibutesRow(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

        cells[0] = new TextPageElement(policyCmptTypeAttribute.getName());

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            IConfigElement configElement = productCmptGeneration.getConfigElement(policyCmptTypeAttribute.getName());

            if (configElement == null) {
                cells[i + 1] = new TextPageElement("-"); //$NON-NLS-1$
                continue;
            }

            IValueSet valueSet = configElement.getValueSet();
            String defaultValue = configElement.getValue();

            cells[i + 1] = createValueSetCell(valueSet, defaultValue);

        }
        addSubElement(new TableRowPageElement(cells));
    }

    private WrapperPageElement createValueSetCell(IValueSet valueSet, String defaultValue) {
        WrapperPageElement pageElement = new WrapperPageElement(WrapperType.BLOCK);

        pageElement.addPageElements(new TextPageElement(Messages.ProductGenerationAttributeTable_defaultValue
                + ": " //$NON-NLS-1$
                + IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                        .formatValue(((ValueSet)valueSet).getValueDatatype(), defaultValue), TextType.BLOCK));

        if (valueSet.isEnum()) {
            pageElement.addPageElements(createEnumValueSetCell((EnumValueSet)valueSet));
        } else if (valueSet.isUnrestricted()) {
            pageElement.addPageElements(createUnrestrictedEnumValueCell());
        } else if (valueSet.isRange()) {
            pageElement.addPageElements(createRangeValueSetCell((RangeValueSet)valueSet));
        }
        return pageElement;
    }

    private TextPageElement createRangeValueSetCell(RangeValueSet rangeValueSet) {
        StringBuilder builder = new StringBuilder();
        builder.append(Messages.ProductGenerationAttributeTable_minMaxStep);
        builder.append(": "); //$NON-NLS-1$
        builder.append(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                .formatValue(rangeValueSet.getValueDatatype(), rangeValueSet.getLowerBound()));
        builder.append(", "); //$NON-NLS-1$
        builder.append(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                .formatValue(rangeValueSet.getValueDatatype(), rangeValueSet.getUpperBound()));
        builder.append(", "); //$NON-NLS-1$
        builder.append(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                .formatValue(rangeValueSet.getValueDatatype(), rangeValueSet.getStep()));

        TextPageElement textPageElement = new TextPageElement(builder.toString(), TextType.BLOCK);
        return textPageElement;
    }

    private TextPageElement createUnrestrictedEnumValueCell() {
        TextPageElement textPageElement = new TextPageElement(
                Messages.ProductGenerationAttributeTable_valueSetUnrestricted, TextType.BLOCK);
        return textPageElement;
    }

    private TextPageElement createEnumValueSetCell(EnumValueSet enumValueSet) {
        StringBuilder builder = new StringBuilder();

        for (String enumValue : enumValueSet.getValues()) {
            if (builder.length() > 0) {
                builder.append(", "); //$NON-NLS-1$
            }
            builder.append(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                    .formatValue(enumValueSet.getValueDatatype(), enumValue));

        }
        TextPageElement textPageElement = new TextPageElement(Messages.ProductGenerationAttributeTable_valueSet
                + ": " + builder.toString()); //$NON-NLS-1$
        return textPageElement;
    }

    private void addAttributes() {
        if (attributes.length == 0) {
            return;
        }

        for (IAttribute attribute : attributes) {
            addAttributeRow(attribute);
        }
    }

    private void addTableStructureUsages() {
        ITableStructureUsage[] tableStructureUsages = productCmptType.getTableStructureUsages();

        if (tableStructureUsages.length == 0) {
            return;
        }

        addSubHeadline(Messages.ProductGenerationAttributeTable_tables);

        for (ITableStructureUsage tableStructureUsage : tableStructureUsages) {
            addTableStructureUsageRow(tableStructureUsage);
        }
    }

    private void addFormulas() {
        IProductCmptTypeMethod[] formulaSignatures = productCmptType.getFormulaSignatures();

        if (formulaSignatures.length == 0) {
            return;
        }

        addSubHeadline(Messages.ProductGenerationAttributeTable_formulas);

        for (IProductCmptTypeMethod formulaSignature : formulaSignatures) {
            addFormulaRow(formulaSignature);
        }
    }

    private void addSubHeadline(String category) {
        PageElement[] pageElements = PageElementUtils.createTextPageElements(getHeadlineWithCategory(category), null,
                TextType.WITHOUT_TYPE);

        addSubElement(new TableRowPageElement(pageElements).addStyles(Style.TABLE_HEADLINE));
    }

    private void addTableStructureUsageRow(ITableStructureUsage tableStructureUsage) {
        try {
            PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

            cells[0] = new TextPageElement(tableStructureUsage.getRoleName());

            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

                ITableContentUsage usage = productCmptGeneration
                        .getTableContentUsage(tableStructureUsage.getRoleName());

                ITableContents tableContent = usage.findTableContents(context.getIpsProject());

                if (tableContent == null) {
                    cells[i + 1] = new TextPageElement("-"); //$NON-NLS-1$
                    continue;
                }
                PageElement linkPageElement = PageElementUtils.createLinkPageElement(context, tableContent, "content", //$NON-NLS-1$
                        tableContent.getName(), true);

                cells[i + 1] = linkPageElement;

            }

            addSubElement(new TableRowPageElement(cells));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addFormulaRow(IProductCmptTypeMethod formulaSignature) {
        PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

        String labelValue = formulaSignature.getLabelValue(context.getDescriptionLocale());
        labelValue = labelValue != null ? labelValue : formulaSignature.getName();

        cells[0] = new TextPageElement(labelValue);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            IFormula formula = productCmptGeneration.getFormula(formulaSignature.getFormulaName());
            cells[i + 1] = new TextPageElement(formula == null ? "-" : formula.getExpression()); //$NON-NLS-1$

        }
        addSubElement(new TableRowPageElement(cells));
    }

    private void addChildProductCmptTypes() {
        PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

        addSubHeadline(Messages.ProductGenerationAttributeTable_structure);

        cells[0] = new TextPageElement(Messages.ProductGenerationAttributeTable_associatedComponents);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            if (productCmptGeneration.getLinks().length == 0) {
                cells[i + 1] = new TextPageElement("-"); //$NON-NLS-1$
                continue;
            }

            AbstractCompositePageElement cellContent = new WrapperPageElement(WrapperType.BLOCK);

            addAssociatedProductCmpts(productCmptGeneration, cellContent);

            cells[i + 1] = cellContent;
        }

        addSubElement(new TableRowPageElement(cells));
    }

    private void addAssociatedProductCmpts(IProductCmptGeneration productCmptGeneration,
            AbstractCompositePageElement cellContent) {
        IAssociation[] associations = productCmptType.getAssociations();
        for (IAssociation association : associations) {
            TreeNodePageElement root = new TreeNodePageElement(PageElementUtils.createIpsElementRepresentation(
                    association, context.getLabel(association), true));
            IProductCmptLink[] links = productCmptGeneration.getLinks(association.getName());
            for (IProductCmptLink productCmptLink : links) {
                try {
                    IProductCmpt target = productCmptLink.findTarget(productCmpt.getIpsProject());
                    PageElement targetLink = PageElementUtils.createLinkPageElement(context, target, "content", target //$NON-NLS-1$
                            .getName(), true);

                    Set<Style> cardinalityStyles = new HashSet<Style>();
                    cardinalityStyles.add(Style.INDENTION);

                    int maxCardinality = productCmptLink.getMaxCardinality();
                    PageElement cardinalities = new TextPageElement(productCmptLink.getMinCardinality() + ".." //$NON-NLS-1$
                            + (maxCardinality == Integer.MAX_VALUE ? "*" : maxCardinality), cardinalityStyles); //$NON-NLS-1$

                    root.addPageElements(new WrapperPageElement(WrapperType.BLOCK).addPageElements(targetLink,
                            cardinalities));
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

        String caption = context.getLabel(attribute);
        cells[0] = new TextPageElement(caption);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {

            IAttributeValue attributeValue = productCmpt.getProductCmptGeneration(i).getAttributeValue(
                    attribute.getName());

            String value;
            try {
                value = IpsPlugin
                        .getDefault()
                        .getIpsPreferences()
                        .getDatatypeFormatter()
                        .formatValue(productCmpt.getIpsProject().findValueDatatype(attribute.getDatatype()),
                                attributeValue.getValue());
            } catch (CoreException e) {
                IpsPlugin.log(e);
                value = Messages.ProductGenerationAttributeTable_undefined;
            }
            cells[i + 1] = new TextPageElement(value);
        }

        addSubElement(new TableRowPageElement(cells));

    }

    @Override
    protected List<String> getHeadline() {
        return getHeadlineWithCategory(Messages.ProductGenerationAttributeTable_attributes);
    }

    private List<String> getHeadlineWithCategory(String productGenerationAttributeTableGenerationFrom) {
        List<String> headline = new ArrayList<String>();

        headline.add(productGenerationAttributeTableGenerationFrom);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            headline.add(context.getSimpleDateFormat().format(
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
