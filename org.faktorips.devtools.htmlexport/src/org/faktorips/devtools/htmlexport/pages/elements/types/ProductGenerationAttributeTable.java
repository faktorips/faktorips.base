/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collections;
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
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
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
 */
public class ProductGenerationAttributeTable extends AbstractStandardTablePageElement {

    private static final String COLON_SEPARATOR = ": "; //$NON-NLS-1$
    private static final String COMMA_SEPARATOR = ", "; //$NON-NLS-1$
    private static final String NOT_AVAILABLE = "-"; //$NON-NLS-1$
    private static final String EMPTY_CHARACTER = " "; //$NON-NLS-1$
    private final IProductCmpt productCmpt;
    private final List<IAttribute> attributes;
    private final IProductCmptType productCmptType;

    /**
     * Creates a {@link ProductGenerationAttributeTable} for the specified {@link IProductCmpt}
     * 
     * 
     */
    public ProductGenerationAttributeTable(IProductCmpt productCmpt, DocumentationContext context) throws CoreException {
        super(context);
        this.productCmpt = productCmpt;
        this.productCmptType = context.getIpsProject().findProductCmptType(productCmpt.getProductCmptType());
        this.attributes = productCmptType.findAllAttributes(productCmpt.getIpsProject());
    }

    @Override
    protected void addDataRows() {
        addAttributes();

        addFormulas();

        addPolicyCmptTypeAttibutes();

        addTableStructureUsages();

        addAssociations();

        addValidationRules();

    }

    private void addValidationRules() {
        IPolicyCmptType policyCmptType = getPolicyCmptType();

        if (policyCmptType == null) {
            return;
        }

        List<IValidationRule> validationRules;
        try {
            validationRules = policyCmptType.findAllValidationRules(getContext().getIpsProject());
        } catch (CoreException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error finding ValidationsRules of PolicyCmptType " //$NON-NLS-1$
                    + policyCmptType.getQualifiedName(), e));
            return;
        }

        List<IValidationRule> productConfigurableValidationRules = new ArrayList<IValidationRule>();

        for (IValidationRule validationRule : validationRules) {
            if (validationRule.isConfigurableByProductComponent()) {
                productConfigurableValidationRules.add(validationRule);
            }
        }

        if (productConfigurableValidationRules.isEmpty()) {
            return;
        }

        addSubHeadline(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_validationRules));

        for (IValidationRule validationRule : productConfigurableValidationRules) {
            addValidationRulesRow(validationRule);
        }

    }

    private void addValidationRulesRow(IValidationRule validationRule) {
        IPageElement[] cells = new IPageElement[productCmpt.getNumOfGenerations() + 1];

        cells[0] = new TextPageElement(getContext().getLabel(validationRule), getContext());

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            IValidationRuleConfig validationRuleConfig = productCmptGeneration.getValidationRuleConfig(validationRule
                    .getName());

            cells[i + 1] = new TextPageElement(
                    validationRuleConfig != null && validationRuleConfig.isActive() ? getContext().getMessage(
                            HtmlExportMessages.ProductGenerationAttributeTable_validationRulesActive) : getContext()
                            .getMessage(HtmlExportMessages.ProductGenerationAttributeTable_validationRulesInactive),
                            TextType.BLOCK, getContext());
            cells[i + 1].addStyles(Style.CENTER);

        }
        addSubElement(new TableRowPageElement(cells, getContext()));

    }

    private void addPolicyCmptTypeAttibutes() {
        List<IPolicyCmptTypeAttribute> policyCmptTypeAttributes = new ArrayList<IPolicyCmptTypeAttribute>();

        IPolicyCmptType policyCmptType = getPolicyCmptType();

        if (policyCmptType == null) {
            return;
        }

        List<IAttribute> allAttributes;
        try {
            allAttributes = policyCmptType.findAllAttributes(getContext().getIpsProject());
        } catch (CoreException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error finding Attributes of PolicyCmptType " //$NON-NLS-1$
                    + policyCmptType.getQualifiedName(), e));
            return;
        }

        for (IAttribute attribute : allAttributes) {
            if (!(attribute instanceof IPolicyCmptTypeAttribute)) {
                continue;
            }
            IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)attribute;
            if (policyCmptTypeAttribute.getProductCmptPropertyType() == ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE
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

    private IPolicyCmptType getPolicyCmptType() {
        IPolicyCmptType policyCmptType = null;
        try {
            policyCmptType = productCmptType.findPolicyCmptType(getContext().getIpsProject());
        } catch (CoreException e) {
            getContext().addStatus(new IpsStatus(IStatus.ERROR, "Error finding PolicyCmptType of ProductCmptType " //$NON-NLS-1$
                    + productCmptType.getQualifiedName(), e));
        }
        return policyCmptType;
    }

    private void addPolicyCmptTypeAttibutesRow(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        IPageElement[] cells = new IPageElement[productCmpt.getNumOfGenerations() + 1];

        cells[0] = new TextPageElement(getContext().getLabel(policyCmptTypeAttribute), getContext());

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            IConfigElement configElement = productCmptGeneration.getConfigElement(policyCmptTypeAttribute.getName());

            if (configElement == null || configElement.getValueSet() == null) {
                cells[i + 1] = new TextPageElement(NOT_AVAILABLE, getContext());
                continue;
            }

            IValueSet valueSet = configElement.getValueSet();
            String defaultValue = configElement.getValue();

            cells[i + 1] = createValueSetCell(valueSet, defaultValue);

        }
        addSubElement(new TableRowPageElement(cells, getContext()));
    }

    private WrapperPageElement createValueSetCell(IValueSet valueSet, String defaultValue) {
        WrapperPageElement pageElement = new WrapperPageElement(WrapperType.BLOCK, getContext());

        pageElement.addPageElements(new TextPageElement(getContext().getMessage(
                "ProductGenerationAttributeTable_defaultValue") //$NON-NLS-1$
                + COLON_SEPARATOR
                + getContext().getDatatypeFormatter()
                .formatValue(((ValueSet)valueSet).getValueDatatype(), defaultValue), TextType.BLOCK,
                getContext()));

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
        builder.append(COLON_SEPARATOR);
        builder.append(getContext().getDatatypeFormatter().formatValue(rangeValueSet.getValueDatatype(),
                rangeValueSet.getLowerBound()));
        builder.append(COMMA_SEPARATOR);
        builder.append(getContext().getDatatypeFormatter().formatValue(rangeValueSet.getValueDatatype(),
                rangeValueSet.getUpperBound()));
        builder.append(COMMA_SEPARATOR);
        builder.append(getContext().getDatatypeFormatter().formatValue(rangeValueSet.getValueDatatype(),
                rangeValueSet.getStep()));

        TextPageElement textPageElement = new TextPageElement(builder.toString(), TextType.BLOCK, getContext());
        return textPageElement;
    }

    private TextPageElement createUnrestrictedEnumValueCell() {
        TextPageElement textPageElement = new TextPageElement(getContext().getMessage(
                "ProductGenerationAttributeTable_valueSetUnrestricted"), TextType.BLOCK, getContext()); //$NON-NLS-1$
        return textPageElement;
    }

    private TextPageElement createEnumValueSetCell(EnumValueSet enumValueSet) {
        StringBuilder builder = new StringBuilder();

        for (String enumValue : enumValueSet.getValues()) {
            if (builder.length() > 0) {
                builder.append(COMMA_SEPARATOR);
            }
            builder.append(getContext().getDatatypeFormatter().formatValue(enumValueSet.getValueDatatype(), enumValue));

        }
        TextPageElement textPageElement = new TextPageElement(getContext().getMessage(
                "ProductGenerationAttributeTable_valueSet") //$NON-NLS-1$
                + COLON_SEPARATOR + builder.toString(), getContext());
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
        IPageElement[] pageElements = new PageElementUtils(getContext()).createTextPageElements(
                getHeadlineWithCategory(category), null, TextType.WITHOUT_TYPE);

        addSubElement(new TableRowPageElement(pageElements, getContext()).addStyles(Style.TABLE_HEADLINE));
    }

    private void addTableStructureUsageRow(ITableStructureUsage tableStructureUsage) {
        IPageElement[] cells;
        if (tableStructureUsage.isChangingOverTime()) {
            cells = createChangeableTableStructureUsageRow(tableStructureUsage);
        } else {
            cells = createNotChangeableTableStructureUsageRow(tableStructureUsage);
        }
        addSubElement(new TableRowPageElement(cells, getContext()));
    }

    private IPageElement[] createNotChangeableTableStructureUsageRow(ITableStructureUsage tableStructureUsage) {
        IPageElement[] cells = new IPageElement[productCmpt.getNumOfGenerations() + 1];
        String roleName = tableStructureUsage.getRoleName();
        addFirstCellWithNotChangeable(cells, roleName);

        ITableContentUsage usage = productCmpt.getTableContentUsage(roleName);
        if (usage == null) {
            setTableContentUnknown(cells);
            return cells;
        }

        ITableContents tableContent = findTableContents(usage);

        if (tableContent == null) {
            setTableContentUnknown(cells);
            return cells;
        }

        IPageElement linkPageElement = createTableContentLinkPageElement(tableContent);
        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            cells[i + 1] = linkPageElement;
        }
        return cells;
    }

    private ITableContents findTableContents(ITableContentUsage usage) {
        ITableContents tableContent = null;
        try {
            tableContent = usage.findTableContents(getContext().getIpsProject());
        } catch (CoreException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Could not find contents of TableContentUsage " //$NON-NLS-1$
                    + usage.getName(), e));
        }
        return tableContent;
    }

    private IPageElement createTableContentLinkPageElement(ITableContents tableContent) {
        return new PageElementUtils(getContext()).createLinkPageElement(getContext(), tableContent, TargetType.CONTENT,
                getContext().getLabel(tableContent), true);
    }

    private void setTableContentUnknown(IPageElement[] cells) {
        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            cells[i + 1] = new TextPageElement(NOT_AVAILABLE, getContext());
        }
    }

    private IPageElement[] createChangeableTableStructureUsageRow(ITableStructureUsage tableStructureUsage) {
        IPageElement[] cells = new IPageElement[productCmpt.getNumOfGenerations() + 1];

        String roleName = tableStructureUsage.getRoleName();
        cells[0] = new TextPageElement(roleName, getContext());

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            ITableContentUsage usage = productCmptGeneration.getTableContentUsage(roleName);

            if (usage == null) {
                cells[i + 1] = new TextPageElement(NOT_AVAILABLE, getContext());
                continue;
            }

            ITableContents tableContent = findTableContents(usage);

            if (tableContent == null) {
                cells[i + 1] = new TextPageElement(NOT_AVAILABLE, getContext());
                continue;
            }
            IPageElement linkPageElement = createTableContentLinkPageElement(tableContent);

            cells[i + 1] = linkPageElement;

        }
        return cells;
    }

    private void addFormulaRow(IProductCmptTypeMethod formulaSignature) {
        IPageElement[] cells = new IPageElement[productCmpt.getNumOfGenerations() + 1];

        String labelValue = getContext().getLabel(formulaSignature);

        cells[0] = new TextPageElement(labelValue, getContext());

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            IFormula formula = productCmptGeneration.getFormula(formulaSignature.getFormulaName());
            cells[i + 1] = new TextPageElement(formula == null ? NOT_AVAILABLE : formula.getExpression(), getContext());

        }
        addSubElement(new TableRowPageElement(cells, getContext()));
    }

    private void addAssociations() {
        List<IAssociation> notDerivedUnionAssociations = new ArrayList<IAssociation>();

        List<IAssociation> allAssociations = getAllAssociations();
        for (IAssociation association : allAssociations) {
            if (!association.isDerivedUnion()) {
                notDerivedUnionAssociations.add(association);
            }
        }

        if (notDerivedUnionAssociations.isEmpty()) {
            return;
        }

        addSubHeadline(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_associatedComponents));

        for (IAssociation association : notDerivedUnionAssociations) {
            IPageElement[] cells = new IPageElement[productCmpt.getNumOfGenerations() + 1];
            cells[0] = new PageElementUtils(getContext()).createIpsElementRepresentation(getContext(), association,
                    getContext().getLabel(association), true);
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

                if (productCmptGeneration.getLinks().length == 0) {
                    cells[i + 1] = new TextPageElement(NOT_AVAILABLE, getContext());
                    continue;
                }

                cells[i + 1] = createAssociatedProductCmpts(productCmptGeneration, association);
            }
            TableRowPageElement pageElement = new TableRowPageElement(cells, getContext());

            pageElement.setId(association.getName());

            addSubElement(pageElement);
        }

    }

    private IPageElement createAssociatedProductCmpts(IProductCmptGeneration productCmptGeneration,
            IAssociation association) {
        AbstractCompositePageElement cellContent = new WrapperPageElement(WrapperType.BLOCK, getContext());

        IProductCmptLink[] links = productCmptGeneration.getLinks(association.getName());

        for (IProductCmptLink productCmptLink : links) {
            try {
                cellContent.addPageElements(createProductCmptLink(productCmptLink));
            } catch (CoreException e) {
                getContext().addStatus(
                        new IpsStatus(IStatus.ERROR,
                                "Could not get linked ProductCmpt within " + productCmptLink.getName(), e)); //$NON-NLS-1$
            }
        }

        if (cellContent.isEmpty()) {
            cellContent.addPageElements(new TextPageElement(NOT_AVAILABLE, getContext()));
        }

        return cellContent;
    }

    private IPageElement createProductCmptLink(IProductCmptLink productCmptLink) throws CoreException {
        IProductCmpt target;
        target = productCmptLink.findTarget(productCmpt.getIpsProject());

        IPageElement targetLink = new PageElementUtils(getContext()).createLinkPageElement(getContext(), target,
                TargetType.CONTENT, target.getName(), true);

        Set<Style> cardinalityStyles = new HashSet<Style>();
        cardinalityStyles.add(Style.INDENTION);

        IPageElement cardinalities = new TextPageElement(
                productCmptLink.getMinCardinality() + ".." //$NON-NLS-1$
                + getCardinalityRepresentation(productCmptLink.getMaxCardinality()) + " (" //$NON-NLS-1$
                + getCardinalityRepresentation(productCmptLink.getDefaultCardinality()) + ")", cardinalityStyles, getContext()); //$NON-NLS-1$

        return new WrapperPageElement(WrapperType.BLOCK, getContext()).addPageElements(targetLink, cardinalities);
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
        IPageElement[] cells;

        if (((IProductCmptTypeAttribute)attribute).isChangingOverTime()) {
            cells = createChangeableAttributeRow(attribute);
        } else {
            cells = createNotChangeableAttributeRow(attribute);
        }
        addSubElement(new TableRowPageElement(cells, getContext()));
    }

    private IPageElement[] createNotChangeableAttributeRow(IAttribute attribute) {
        IPageElement[] cells = new IPageElement[productCmpt.getNumOfGenerations() + 1];
        String label = getContext().getLabel(attribute);

        addFirstCellWithNotChangeable(cells, label);

        IAttributeValue attributeValue = productCmpt.getAttributeValue(attribute.getName());
        String value = getValueOfAttribute(attributeValue, attribute);

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            cells[i + 1] = new TextPageElement(value, getContext());
        }
        return cells;
    }

    private void addFirstCellWithNotChangeable(IPageElement[] cells, String label) {
        cells[0] = new WrapperPageElement(WrapperType.NONE, getContext()).addPageElements(
                new TextPageElement(label, getContext()),
                new TextPageElement(EMPTY_CHARACTER, getContext()),
                new TextPageElement(getContext().getMessage(
                        HtmlExportMessages.ProductGenerationAttributeTable_notChangeable), Collections
                        .singleton(Style.SMALL), getContext()));
    }

    private IPageElement[] createChangeableAttributeRow(IAttribute attribute) {
        IPageElement[] cells = new IPageElement[productCmpt.getNumOfGenerations() + 1];
        cells[0] = new TextPageElement(getContext().getLabel(attribute), getContext());
        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);
            IAttributeValue attributeValue = productCmptGeneration.getAttributeValue(attribute.getName());

            String value = getValueOfAttribute(attributeValue, attribute);
            cells[i + 1] = new TextPageElement(value, getContext());
        }
        return cells;
    }

    private String getValueOfAttribute(IAttributeValue attributeValue, IAttribute attribute) {

        String value;
        try {
            value = getContext().getDatatypeFormatter().formatValue(
                    productCmpt.getIpsProject().findValueDatatype(attribute.getDatatype()),
                    attributeValue == null ? null : attributeValue.getPropertyValue());
        } catch (CoreException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR, "Error formating AttributeValue " + attribute.getName(), e)); //$NON-NLS-1$
            value = attributeValue == null || attributeValue.getPropertyValue() == null ? getContext().getMessage(
                    "ProductGenerationAttributeTable_undefined") : attributeValue.getPropertyValue(); //$NON-NLS-1$
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
            headline.add(getContext().getSimpleDateFormat().format(
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
