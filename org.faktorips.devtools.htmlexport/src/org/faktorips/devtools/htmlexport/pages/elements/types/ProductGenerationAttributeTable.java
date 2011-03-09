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
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
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
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
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
    private final List<IAttribute> attributes;
    private final DocumentationContext context;
    private final IProductCmptType productCmptType;

    /**
     * Creates a {@link ProductGenerationAttributeTable} for the specified {@link IProductCmpt}
     * 
     * 
     */
    public ProductGenerationAttributeTable(IProductCmpt productCmpt, DocumentationContext context) throws CoreException {
        this.productCmpt = productCmpt;
        this.context = context;
        this.productCmptType = context.getIpsProject().findProductCmptType(productCmpt.getProductCmptType());
        this.attributes = productCmptType.findAllAttributes(productCmpt.getIpsProject());
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
        List<IPolicyCmptTypeAttribute> policyCmptTypeAttributes = new ArrayList<IPolicyCmptTypeAttribute>();

        IPolicyCmptType policyCmptType = null;
        try {
            policyCmptType = productCmptType.findPolicyCmptType(context.getIpsProject());
        } catch (CoreException e) {
            context.addStatus(new IpsStatus(IStatus.ERROR, "Error finding PolicyCmptType of ProductCmptType " //$NON-NLS-1$
                    + productCmptType.getQualifiedName(), e));
            return;
        }

        if (policyCmptType == null) {
            return;
        }

        List<IAttribute> attributes = new ArrayList<IAttribute>();
        try {
            attributes = policyCmptType.findAllAttributes(context.getIpsProject());
        } catch (CoreException e) {
            context.addStatus(new IpsStatus(IStatus.WARNING, "Error finding Attributes of PolicyCmptType " //$NON-NLS-1$
                    + policyCmptType.getQualifiedName(), e));
            return;
        }
        for (IAttribute attribute : attributes) {
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

        addSubHeadline(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_defaultsAndValueSets));

        for (IPolicyCmptTypeAttribute policyCmptTypeAttribute : policyCmptTypeAttributes) {
            addPolicyCmptTypeAttibutesRow(policyCmptTypeAttribute);
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

        pageElement.addPageElements(new TextPageElement(getContext().getMessage(
                "ProductGenerationAttributeTable_defaultValue") //$NON-NLS-1$
                + ": " //$NON-NLS-1$
                + getContext().getDatatypeFormatter()
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
        builder.append(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_minMaxStep));
        builder.append(": "); //$NON-NLS-1$
        builder.append(getContext().getDatatypeFormatter().formatValue(rangeValueSet.getValueDatatype(),
                rangeValueSet.getLowerBound()));
        builder.append(", "); //$NON-NLS-1$
        builder.append(getContext().getDatatypeFormatter().formatValue(rangeValueSet.getValueDatatype(),
                rangeValueSet.getUpperBound()));
        builder.append(", "); //$NON-NLS-1$
        builder.append(getContext().getDatatypeFormatter().formatValue(rangeValueSet.getValueDatatype(),
                rangeValueSet.getStep()));

        TextPageElement textPageElement = new TextPageElement(builder.toString(), TextType.BLOCK);
        return textPageElement;
    }

    private TextPageElement createUnrestrictedEnumValueCell() {
        TextPageElement textPageElement = new TextPageElement(getContext().getMessage(
                "ProductGenerationAttributeTable_valueSetUnrestricted"), TextType.BLOCK); //$NON-NLS-1$
        return textPageElement;
    }

    private TextPageElement createEnumValueSetCell(EnumValueSet enumValueSet) {
        StringBuilder builder = new StringBuilder();

        for (String enumValue : enumValueSet.getValues()) {
            if (builder.length() > 0) {
                builder.append(", "); //$NON-NLS-1$
            }
            builder.append(getContext().getDatatypeFormatter().formatValue(enumValueSet.getValueDatatype(), enumValue));

        }
        TextPageElement textPageElement = new TextPageElement(getContext().getMessage(
                "ProductGenerationAttributeTable_valueSet") //$NON-NLS-1$
                + ": " + builder.toString()); //$NON-NLS-1$
        return textPageElement;
    }

    private void addAttributes() {
        if (attributes.size() == 0) {
            return;
        }

        for (IAttribute attribute : attributes) {
            addAttributeRow(attribute);
        }
    }

    private void addTableStructureUsages() {
        List<ITableStructureUsage> tableStructureUsages = productCmptType.getTableStructureUsages();

        if (tableStructureUsages.size() == 0) {
            return;
        }

        addSubHeadline(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_tables));

        for (ITableStructureUsage tableStructureUsage : tableStructureUsages) {
            addTableStructureUsageRow(tableStructureUsage);
        }
    }

    private void addFormulas() {
        List<IProductCmptTypeMethod> formulaSignatures = productCmptType.getFormulaSignatures();

        if (formulaSignatures.size() == 0) {
            return;
        }

        addSubHeadline(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_formulas));

        for (IProductCmptTypeMethod formulaSignature : formulaSignatures) {
            addFormulaRow(formulaSignature);
        }
    }

    private void addSubHeadline(String category) {
        PageElement[] pageElements = new PageElementUtils().createTextPageElements(getHeadlineWithCategory(category),
                null, TextType.WITHOUT_TYPE);

        addSubElement(new TableRowPageElement(pageElements).addStyles(Style.TABLE_HEADLINE));
    }

    private void addTableStructureUsageRow(ITableStructureUsage tableStructureUsage) {
        PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];

        cells[0] = new TextPageElement(tableStructureUsage.getRoleName());

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            ITableContentUsage usage = productCmptGeneration.getTableContentUsage(tableStructureUsage.getRoleName());

            ITableContents tableContent = null;
            try {
                tableContent = usage.findTableContents(context.getIpsProject());
            } catch (CoreException e) {
                context.addStatus(new IpsStatus(IStatus.WARNING, "Could not find contents of TableContentUsage " //$NON-NLS-1$
                        + usage.getName(), e));
            }

            if (tableContent == null) {
                cells[i + 1] = new TextPageElement("-"); //$NON-NLS-1$
                continue;
            }
            PageElement linkPageElement = new PageElementUtils().createLinkPageElement(context, tableContent,
                    "content", //$NON-NLS-1$
                    tableContent.getName(), true);

            cells[i + 1] = linkPageElement;

        }

        addSubElement(new TableRowPageElement(cells));
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
        addSubHeadline(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_associatedComponents));

        List<IAssociation> associations = getAllAssociations();
        for (IAssociation association : associations) {
            PageElement[] cells = new PageElement[productCmpt.getNumOfGenerations() + 1];
            cells[0] = new PageElementUtils().createIpsElementRepresentation(association, context,
                    context.getLabel(association), true);
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

                if (productCmptGeneration.getLinks().length == 0) {
                    cells[i + 1] = new TextPageElement("-"); //$NON-NLS-1$
                    continue;
                }

                cells[i + 1] = createAssociatedProductCmpts(productCmptGeneration, association);
            }
            addSubElement(new TableRowPageElement(cells));
        }

    }

    private PageElement createAssociatedProductCmpts(IProductCmptGeneration productCmptGeneration,
            IAssociation association) {
        AbstractCompositePageElement cellContent = new WrapperPageElement(WrapperType.BLOCK);

        IProductCmptLink[] links = productCmptGeneration.getLinks(association.getName());

        for (IProductCmptLink productCmptLink : links) {
            try {
                cellContent.addPageElements(createProductCmptLink(productCmptLink));
            } catch (CoreException e) {
                context.addStatus(new IpsStatus(IStatus.ERROR,
                        "Could not get linked ProductCmpt within " + productCmptLink.getName(), e)); //$NON-NLS-1$
            }
        }

        if (cellContent.isEmpty()) {
            cellContent.addPageElements(new TextPageElement("-")); //$NON-NLS-1$
        }

        return cellContent;
    }

    private PageElement createProductCmptLink(IProductCmptLink productCmptLink) throws CoreException {
        IProductCmpt target;
        target = productCmptLink.findTarget(productCmpt.getIpsProject());

        PageElement targetLink = new PageElementUtils().createLinkPageElement(context, target, "content", target //$NON-NLS-1$
                .getName(), true);

        Set<Style> cardinalityStyles = new HashSet<Style>();
        cardinalityStyles.add(Style.INDENTION);

        PageElement cardinalities = new TextPageElement(productCmptLink.getMinCardinality() + ".." //$NON-NLS-1$
                + getCardinalityRepresentation(productCmptLink.getMaxCardinality()) + " (" //$NON-NLS-1$
                + getCardinalityRepresentation(productCmptLink.getDefaultCardinality()) + ")", cardinalityStyles); //$NON-NLS-1$

        return new WrapperPageElement(WrapperType.BLOCK).addPageElements(targetLink, cardinalities);
    }

    private List<IAssociation> getAllAssociations() {
        List<IAssociation> associations;
        try {
            associations = productCmptType.findAllAssociations(productCmptType.getIpsProject());
        } catch (CoreException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error finding all associations of " //$NON-NLS-1$
                    + productCmptType.getQualifiedName(), e));
            associations = new ArrayList<IAssociation>();
        }
        return associations;
    }

    private String getCardinalityRepresentation(int cardinality) {
        return cardinality == Integer.MAX_VALUE ? "*" : Integer.toString(cardinality); //$NON-NLS-1$
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
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);
            String value = getAttributeValueInProductCmptGeneration(productCmptGeneration, attribute);
            cells[i + 1] = new TextPageElement(value);
        }

        addSubElement(new TableRowPageElement(cells));

    }

    private String getAttributeValueInProductCmptGeneration(IProductCmptGeneration productCmptGeneration,
            IAttribute attribute) {
        IAttributeValue attributeValue = productCmptGeneration.getAttributeValue(attribute.getName());

        String value;
        try {
            value = getContext().getDatatypeFormatter().formatValue(
                    productCmpt.getIpsProject().findValueDatatype(attribute.getDatatype()), attributeValue.getValue());
        } catch (CoreException e) {
            context.addStatus(new IpsStatus(IStatus.ERROR,
                    "Error formating AttributeValue " + attributeValue.getName(), e)); //$NON-NLS-1$
            value = attributeValue.getValue() == null ? getContext().getMessage(
                    "ProductGenerationAttributeTable_undefined") : attributeValue.getValue(); //$NON-NLS-1$
        }
        return value;
    }

    @Override
    protected List<String> getHeadline() {
        return getHeadlineWithCategory(getContext().getMessage(
                HtmlExportMessages.ProductGenerationAttributeTable_attributes));
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

    protected DocumentationContext getContext() {
        return context;
    }

}
