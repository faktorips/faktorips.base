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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
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
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
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

    private static final int TWO_COLUMNS = 2;
    private static final String COLON_SEPARATOR = ": "; //$NON-NLS-1$
    private static final String COMMA_SEPARATOR = ", "; //$NON-NLS-1$
    private static final String NOT_AVAILABLE = "-"; //$NON-NLS-1$
    private static final String WHITE_SPACE = " "; //$NON-NLS-1$
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
        validationRules = policyCmptType.findAllValidationRules(getContext().getIpsProject());

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
        IPageElement[] cells = initCellsSize();

        cells[0] = new TextPageElement(getContext().getLabel(validationRule), getContext());

        if (productCmpt.allowGenerations()) {
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

                IValidationRuleConfig validationRuleConfig = productCmptGeneration
                        .getValidationRuleConfig(validationRule.getName());

                cells[i + 1] = new TextPageElement(
                        validationRuleConfig != null && validationRuleConfig.isActive() ? getContext().getMessage(
                                HtmlExportMessages.ProductGenerationAttributeTable_validationRulesActive)
                                : getContext().getMessage(
                                        HtmlExportMessages.ProductGenerationAttributeTable_validationRulesInactive),
                        TextType.BLOCK, getContext());
                cells[i + 1].addStyles(Style.CENTER);

            }
        }
        addSubElement(new TableRowPageElement(cells, getContext()));
    }

    private IPageElement[] initCellsSize() {
        if (productCmpt.allowGenerations()) {
            return new IPageElement[productCmpt.getNumOfGenerations() + 1];
        }
        return new IPageElement[TWO_COLUMNS];
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
        policyCmptType = productCmptType.findPolicyCmptType(getContext().getIpsProject());
        return policyCmptType;
    }

    private void addPolicyCmptTypeAttibutesRow(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        IPageElement[] cells = initCellsSize();

        cells[0] = new TextPageElement(getContext().getLabel(policyCmptTypeAttribute), getContext());

        if (productCmpt.allowGenerations()) {
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

                IConfigElement configElement = productCmptGeneration
                        .getConfigElement(policyCmptTypeAttribute.getName());

                if (configElement == null || configElement.getValueSet() == null) {
                    cells[i + 1] = new TextPageElement(NOT_AVAILABLE, getContext());
                    continue;
                }

                IValueSet valueSet = configElement.getValueSet();
                String defaultValue = configElement.getValue();

                cells[i + 1] = createValueSetCell(valueSet, defaultValue);

            }
        }
        addSubElement(new TableRowPageElement(cells, getContext()));
    }

    private WrapperPageElement createValueSetCell(IValueSet valueSet, String defaultValue) {
        WrapperPageElement pageElement = new WrapperPageElement(WrapperType.BLOCK, getContext());

        pageElement.addPageElements(new TextPageElement(getContext().getMessage(
                "ProductGenerationAttributeTable_defaultValue") //$NON-NLS-1$
                + COLON_SEPARATOR
                + getContext().getDatatypeFormatter().formatValue(
                        valueSet.findValueDatatype(getContext().getIpsProject()), defaultValue), TextType.BLOCK,
                getContext()));

        if (valueSet.isEnum()) {
            pageElement.addPageElements(createEnumValueSetCell((IEnumValueSet)valueSet));
        } else if (valueSet.isUnrestricted()) {
            pageElement.addPageElements(createUnrestrictedEnumValueCell());
        } else if (valueSet.isRange()) {
            pageElement.addPageElements(createRangeValueSetCell((IRangeValueSet)valueSet));
        }
        return pageElement;
    }

    private TextPageElement createRangeValueSetCell(IRangeValueSet rangeValueSet) {
        StringBuilder builder = new StringBuilder();
        builder.append(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_minMaxStep));
        builder.append(COLON_SEPARATOR);
        ValueDatatype valueDatatype = rangeValueSet.findValueDatatype(getContext().getIpsProject());
        builder.append(getContext().getDatatypeFormatter().formatValue(valueDatatype, rangeValueSet.getLowerBound()));
        builder.append(COMMA_SEPARATOR);
        builder.append(getContext().getDatatypeFormatter().formatValue(valueDatatype, rangeValueSet.getUpperBound()));
        builder.append(COMMA_SEPARATOR);
        builder.append(getContext().getDatatypeFormatter().formatValue(valueDatatype, rangeValueSet.getStep()));

        TextPageElement textPageElement = new TextPageElement(builder.toString(), TextType.BLOCK, getContext());
        return textPageElement;
    }

    private TextPageElement createUnrestrictedEnumValueCell() {
        TextPageElement textPageElement = new TextPageElement(getContext().getMessage(
                "ProductGenerationAttributeTable_valueSetUnrestricted"), TextType.BLOCK, getContext()); //$NON-NLS-1$
        return textPageElement;
    }

    private TextPageElement createEnumValueSetCell(IEnumValueSet enumValueSet) {
        StringBuilder builder = new StringBuilder();

        for (String enumValue : enumValueSet.getValues()) {
            if (builder.length() > 0) {
                builder.append(COMMA_SEPARATOR);
            }
            builder.append(getContext().getDatatypeFormatter().formatValue(
                    enumValueSet.findValueDatatype(getContext().getIpsProject()), enumValue));

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
        IPageElement[] cells = initCellsSize();
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
        if (productCmpt.allowGenerations()) {
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                cells[i + 1] = linkPageElement;
            }
        } else {
            cells[1] = linkPageElement;
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
        IPageElement[] cells = initCellsSize();

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
        IPageElement[] cells;
        if (formulaSignature.isChangingOverTime()) {
            cells = createChangeableFormulaRow(formulaSignature);
        } else {
            cells = createNotChangeableFormulaRow(formulaSignature);
        }
        addSubElement(new TableRowPageElement(cells, getContext()));
    }

    private IPageElement[] createChangeableFormulaRow(IProductCmptTypeMethod formulaSignature) {
        IPageElement[] cells = initCellsSize();

        String labelValue = getContext().getLabel(formulaSignature);

        cells[0] = new TextPageElement(labelValue, getContext());

        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

            IFormula formula = productCmptGeneration.getFormula(formulaSignature.getFormulaName());
            cells[i + 1] = new TextPageElement(formula == null ? NOT_AVAILABLE : formula.getExpression(), getContext());

        }
        return cells;
    }

    private IPageElement[] createNotChangeableFormulaRow(IProductCmptTypeMethod formulaSignature) {
        IPageElement[] cells = initCellsSize();
        addFirstCellWithNotChangeable(cells, getContext().getLabel(formulaSignature));

        IFormula formula = productCmpt.getFormula(formulaSignature.getFormulaName());
        if (productCmpt.allowGenerations()) {
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                cells[i + 1] = new TextPageElement(formula == null ? NOT_AVAILABLE : formula.getExpression(),
                        getContext());
            }
        } else {
            cells[1] = new TextPageElement(formula == null ? NOT_AVAILABLE : formula.getExpression(), getContext());
        }
        return cells;
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
            IPageElement[] cells = initCellsSize();
            cells[0] = new PageElementUtils(getContext()).createIpsElementRepresentation(getContext(), association,
                    getContext().getLabel(association), true);
            if (productCmpt.allowGenerations()) {
                for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                    IProductCmptGeneration productCmptGeneration = productCmpt.getProductCmptGeneration(i);

                    if (productCmptGeneration.getLinks().length == 0) {
                        cells[i + 1] = new TextPageElement(NOT_AVAILABLE, getContext());
                        continue;
                    }

                    cells[i + 1] = createAssociatedProductCmpts(productCmptGeneration, association);
                }
            } else {
                cells[1] = createStaticAssociatedProductCmpts(association);
            }
            TableRowPageElement pageElement = new TableRowPageElement(cells, getContext());

            pageElement.setId(association.getName());

            addSubElement(pageElement);
        }

    }

    private IPageElement createAssociatedProductCmpts(IProductCmptGeneration productCmptGeneration,
            IAssociation association) {
        AbstractCompositePageElement cellContent = new WrapperPageElement(WrapperType.BLOCK, getContext());

        List<IProductCmptLink> links = new ArrayList<IProductCmptLink>();
        if (productCmptGeneration != null) {
            links = productCmptGeneration.getLinksAsList(association.getName());
        } else {
            links = productCmpt.getLinksAsList(association.getName());
        }

        addProductCmptLinksToPageElements(cellContent, links);

        if (cellContent.isEmpty()) {
            cellContent.addPageElements(new TextPageElement(NOT_AVAILABLE, getContext()));
        }

        return cellContent;
    }

    private void addProductCmptLinksToPageElements(AbstractCompositePageElement cellContent,
            List<IProductCmptLink> links) {
        for (IProductCmptLink productCmptLink : links) {
            try {
                cellContent.addPageElements(createProductCmptLink(productCmptLink));
            } catch (CoreException e) {
                getContext().addStatus(
                        new IpsStatus(IStatus.ERROR,
                                "Could not get linked ProductCmpt within " + productCmptLink.getName(), e)); //$NON-NLS-1$
            }
        }
    }

    private IPageElement createStaticAssociatedProductCmpts(IAssociation association) {
        return createAssociatedProductCmpts(null, association);
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
        IPageElement[] cells = initCellsSize();
        String label = getContext().getLabel(attribute);

        addFirstCellWithNotChangeable(cells, label);

        IAttributeValue attributeValue = productCmpt.getAttributeValue(attribute.getName());
        String value = getValueOfAttribute(attributeValue, attribute);

        if (productCmpt.allowGenerations()) {
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                cells[i + 1] = new TextPageElement(value, getContext());
            }
        } else {
            cells[1] = new TextPageElement(value, getContext());
        }
        return cells;
    }

    private void addFirstCellWithNotChangeable(IPageElement[] cells, String label) {
        cells[0] = new WrapperPageElement(WrapperType.NONE, getContext()).addPageElements(
                new TextPageElement(label, getContext()),
                new TextPageElement(WHITE_SPACE, getContext()),
                new TextPageElement(getContext().getMessage(
                        HtmlExportMessages.ProductGenerationAttributeTable_notChangeable), Collections
                        .singleton(Style.SMALL), getContext()));
    }

    private IPageElement[] createChangeableAttributeRow(IAttribute attribute) {
        IPageElement[] cells = initCellsSize();
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
        value = getContext().getDatatypeFormatter().formatValue(
                productCmpt.getIpsProject().findValueDatatype(attribute.getDatatype()),
                attributeValue == null ? null : attributeValue.getPropertyValue());
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
        if (productCmpt.allowGenerations()) {
            addHeadlineForProductCmptWithGeneration(headline);
        } else {
            addHeadlineForProductCmptWithoutGeneration(headline);
        }
        return headline;
    }

    private void addHeadlineForProductCmptWithGeneration(List<String> headline) {
        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            headline.add(getContext().getSimpleDateFormat().format(
                    productCmpt.getProductCmptGeneration(i).getValidFrom().getTime()));
        }
    }

    private void addHeadlineForProductCmptWithoutGeneration(List<String> headline) {
        headline.add(getContext().getMessage(HtmlExportMessages.ProductGenerationAttributeTable_values));
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
