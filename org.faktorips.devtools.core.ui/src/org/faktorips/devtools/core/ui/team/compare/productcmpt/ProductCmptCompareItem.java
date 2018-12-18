/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem;

/**
 * Treeitem used to create a tree/structure for representing a product component in the
 * ProductCmptCompareViewer (more specifically the StructureDiffViewer). The IpsSrcFile, the product
 * component, its generations, relations and configelements are represented by
 * ProductCmptCompareItems. Instances of this class are used to compare the structures of a product
 * component.
 * <p>
 * Since product components are displayed in the compareViewer using a simple text format,
 * differences between products are displayed as ranges in the text representation. The
 * ProductCmptCompareItem class therefore implements the IDocumentRange Interface. It lets the
 * TextMergeViewer retrieve the document corresponding to this product component and the
 * (text-)range the compareitem represents in the document. The TextMergeViewer uses this
 * information to display differences in a way similar to the java source compare.
 * 
 * @see org.eclipse.compare.contentmergeviewer.IDocumentRange
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class ProductCmptCompareItem extends AbstractCompareItem {

    private static final String IPSPRODUCT_ELEMENT_TYPE = "ipsproductElement"; //$NON-NLS-1$

    /**
     * Creates a ProductCmptCompareItem with the given parent and the given content. If parent is
     * null this ProductCmptCompareItem is marked as a root element, as indicated by the method
     * isRoot().
     */
    public ProductCmptCompareItem(ProductCmptCompareItem parent, IIpsElement content) {
        super(parent, content);
    }

    /**
     * Writes the content string of this compare item and its children to the given buffer and set
     * the style ranges.
     * <p>
     * A CompareItem representing a product component or one of its generations is processed
     * separately in
     * {@link #initPropertyValueContainerContentString(IProductPartsContainer, StringBuffer, int)}.
     * 
     * @see #getContentString()
     */
    @Override
    protected int initTreeContentString(StringBuffer sb, int offset) {
        int startIndex = sb.length();
        sb.append(getContentString());
        for (AbstractCompareItem compareItem : getChildItems()) {
            ProductCmptCompareItem child = (ProductCmptCompareItem)compareItem;
            if (child.getIpsElement() instanceof IProductCmpt) {
                /*
                 * End recursion and initialize string with custom method (for the component and its
                 * children)
                 */
                child.initPropertyValueContainerContentString((IProductPartsContainer)child.getIpsElement(), sb,
                        sb.length() - startIndex + offset);
            } else {
                child.initTreeContentString(sb, sb.length() - startIndex + offset);
            }
            // do not include separator line in child ranges
            if (child.needsTextSeparator()) {
                sb.append(NEWLINE).append(DASH);
            }
        }
        setRange(offset, sb.length() - startIndex);
        return sb.length() - startIndex;
    }

    /**
     * Creates the string representation of this <code>ProductCmptCompareItem</code> (and its
     * children) and expects an {@link IPropertyValueContainer} as contained {@link IIpsElement}
     * <p>
     * Similarly to initTreeContentString() this method also calculates the document-ranges for this
     * CompareItem and its children. The children of a generation or product component (e.g.
     * relations and config-elements) are ordered and displayed in separate groups.
     */
    private int initPropertyValueContainerContentString(IProductPartsContainer partContainer,
            StringBuffer sb,
            int offset) {

        int startIndex = sb.length();
        sb.append(getContentString());

        initAttributes(partContainer, sb, offset, startIndex);
        initConfigElementComposites(partContainer, sb, offset, startIndex);
        initFormulas(partContainer, sb, offset, startIndex);
        initLinks(partContainer, sb, offset, startIndex);
        initTableUsages(partContainer, sb, offset, startIndex);
        initRules(partContainer, sb, offset, startIndex);
        if (getIpsElement() instanceof IProductCmpt) {
            // call this method recursively for all generations in a product component
            List<ProductCmptCompareItem> genItems = getCompareItemsOfClass(getChildItems(),
                    IProductCmptGeneration.class);
            for (ProductCmptCompareItem genItem : genItems) {
                sb.append(NEWLINE);
                genItem.initPropertyValueContainerContentString((IPropertyValueContainer)genItem.getIpsElement(), sb,
                        sb.length() - startIndex + offset);
            }
        }
        setRange(offset, sb.length() - startIndex);
        return sb.length() - startIndex;

    }

    private void initAttributes(IProductPartsContainer partContainer, StringBuffer sb, int offset, int startIndex) {
        List<ProductCmptCompareItem> attributes = getCompareItemsOfClass(getChildItems(), IAttributeValue.class);
        if (!attributes.isEmpty()) {
            sb.append(NEWLINE);
            sb.append(getAttributeListHeader(partContainer));
            for (ProductCmptCompareItem attribute : attributes) {
                sb.append(NEWLINE);
                attribute.initTreeContentString(sb, sb.length() - startIndex + offset);
            }
        }
    }

    private void initConfigElementComposites(IProductPartsContainer partContainer,
            StringBuffer sb,
            int offset,
            int startIndex) {
        List<ProductCmptCompareItem> configElements = getCompareItemsOfClass(getChildItems(), IConfigElement.class);
        if (!configElements.isEmpty()) {
            sb.append(NEWLINE);
            sb.append(getConfigElementListHeader(partContainer));
            for (ProductCmptCompareItem configElement : configElements) {
                sb.append(NEWLINE);
                configElement.initTreeContentString(sb, sb.length() - startIndex + offset);
            }
        }
    }

    private void initFormulas(IProductPartsContainer partContainer, StringBuffer sb, int offset, int startIndex) {
        List<ProductCmptCompareItem> formulas = getCompareItemsOfClass(getChildItems(), IFormula.class);
        if (!formulas.isEmpty()) {
            sb.append(NEWLINE);
            sb.append(getFormulaListHeader(partContainer));
            for (ProductCmptCompareItem formula : formulas) {
                sb.append(NEWLINE);
                formula.initTreeContentString(sb, sb.length() - startIndex + offset);
            }
        }
    }

    private void initLinks(IProductPartsContainer partContainer, StringBuffer sb, int offset, int startIndex) {
        List<ProductCmptCompareItem> links = getCompareItemsOfClass(getChildItems(), IProductCmptLink.class);
        if (!links.isEmpty()) {
            sb.append(NEWLINE);
            String linksHeader = getLinkListHeader(partContainer);
            sb.append(linksHeader);
            String[] associations = getAssociations(partContainer);
            for (String association : associations) {
                List<ProductCmptCompareItem> linksByType = getRelations(links, association);
                if (!linksByType.isEmpty()) {
                    String linkTypeHeader = getRelationTypeHeader(partContainer, association);
                    sb.append(linkTypeHeader);
                }
                for (ProductCmptCompareItem link : linksByType) {
                    sb.append(NEWLINE);
                    link.initTreeContentString(sb, sb.length() - startIndex + offset);
                }
            }
        }
    }

    private void initTableUsages(IProductPartsContainer partContainer, StringBuffer sb, int offset, int startIndex) {
        List<ProductCmptCompareItem> tableUsages = getCompareItemsOfClass(getChildItems(), ITableContentUsage.class);
        if (!tableUsages.isEmpty()) {
            sb.append(NEWLINE);
            sb.append(getTableUsageListHeader(partContainer));
            for (ProductCmptCompareItem tableUsage : tableUsages) {
                sb.append(NEWLINE);
                tableUsage.initTreeContentString(sb, sb.length() - startIndex + offset);
            }
        }
    }

    private void initRules(IProductPartsContainer partContainer, StringBuffer sb, int offset, int startIndex) {
        List<ProductCmptCompareItem> rules = getCompareItemsOfClass(getChildItems(), IValidationRuleConfig.class);
        if (!rules.isEmpty()) {
            sb.append(NEWLINE);
            sb.append(getRuleListHeader(partContainer));
            for (ProductCmptCompareItem rule : rules) {
                sb.append(NEWLINE);
                rule.initTreeContentString(sb, sb.length() - startIndex + offset);
            }
        }
    }

    protected void conditionalAppendGenerationDateAndTab(IProductPartsContainer valueContainer, StringBuffer sb) {
        if (valueContainer instanceof IIpsObjectGeneration) {
            sb.append(getGenerationDateText((IIpsObjectGeneration)valueContainer)).append(TAB);
        }
    }

    private String getRuleListHeader(IProductPartsContainer valueContainer) {
        StringBuffer sb = new StringBuffer();
        String ruleHeaderName = org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ValidationRuleSection_DefaultTitle;
        conditionalAppendGenerationDateAndTab(valueContainer, sb);
        sb.append(TAB).append(ruleHeaderName).append(COLON_BLANK);
        return sb.toString();
    }

    private Object getTableUsageListHeader(IProductPartsContainer valueContainer) {
        StringBuffer sb = new StringBuffer();
        String headerName = Messages.ProductCmptCompareItem_TableUsagesHeader;
        conditionalAppendGenerationDateAndTab(valueContainer, sb);
        sb.append(TAB).append(headerName).append(COLON_BLANK);
        return sb.toString();
    }

    /**
     * Returns a string used as header for the list of attributes (configelements) in the string
     * representation of this CompareItem.
     */
    private String getAttributeListHeader(IProductPartsContainer valueContainer) {
        StringBuffer sb = new StringBuffer();
        String attrString = org.faktorips.devtools.core.ui.editors.productcmpt.Messages.GenerationPropertiesPage_pageTitle;
        conditionalAppendGenerationDateAndTab(valueContainer, sb);
        sb.append(TAB).append(attrString).append(COLON_BLANK);
        return sb.toString();
    }

    private Object getConfigElementListHeader(IProductPartsContainer valueContainer) {
        StringBuffer sb = new StringBuffer();
        conditionalAppendGenerationDateAndTab(valueContainer, sb);
        sb.append(TAB).append(Messages.ProductCmptCompareItem_DefaultsAndValueSets).append(COLON_BLANK);
        return sb.toString();
    }

    private Object getFormulaListHeader(IProductPartsContainer valueContainer) {
        StringBuffer sb = new StringBuffer();
        conditionalAppendGenerationDateAndTab(valueContainer, sb);
        sb.append(TAB).append(Messages.ProductCmptCompareItem_FormulaHeader).append(COLON_BLANK);
        return sb.toString();
    }

    /**
     * Returns a string used as header for the list of relations in the string representation of
     * this CompareItem.
     */
    private String getLinkListHeader(IProductPartsContainer valueContainer) {
        StringBuffer sb = new StringBuffer();
        String relString = org.faktorips.devtools.core.ui.editors.productcmpt.Messages.PropertiesPage_relations;
        conditionalAppendGenerationDateAndTab(valueContainer, sb);
        sb.append(TAB).append(relString).append(COLON_BLANK);
        return sb.toString();
    }

    /**
     * Returns a string used as header for the list of relations of a common relationType (the given
     * String).
     */
    private String getRelationTypeHeader(IProductPartsContainer partContainer, String relationType) {
        StringBuffer sb = new StringBuffer();
        sb.append(NEWLINE);
        conditionalAppendGenerationDateAndTab(partContainer, sb);
        sb.append(TAB).append(TAB).append(TAB).append(relationType);
        return sb.toString();
    }

    private String getGenerationDateText(IIpsObjectGeneration gen) {
        return IpsPlugin.getDefault().getIpsPreferences().getDateFormat().format(gen.getValidFrom().getTime());
    }

    /**
     * Returns a list containing all <code>ProductCmptCompareItem</code>s in the given list that
     * contain an <code>IProductComponentRelation</code> with the given relationType.
     */
    private List<ProductCmptCompareItem> getRelations(List<ProductCmptCompareItem> items, String relationType) {
        List<ProductCmptCompareItem> rels = new ArrayList<ProductCmptCompareItem>();
        for (ProductCmptCompareItem item : items) {
            if (item.getIpsElement() instanceof IProductCmptLink) {
                if (((IProductCmptLink)item.getIpsElement()).getAssociation().equals(relationType)) {
                    rels.add(item);
                }
            }
        }
        return rels;
    }

    /**
     * Returns a list containing all <code>ProductCmptCompareItem</code>s in the given list that
     * contain an instance of the given class.
     */
    protected List<ProductCmptCompareItem> getCompareItemsOfClass(List<AbstractCompareItem> items, Class<?> clazz) {
        List<ProductCmptCompareItem> rels = new ArrayList<ProductCmptCompareItem>();
        for (AbstractCompareItem item : items) {
            if (clazz.isAssignableFrom(item.getIpsElement().getClass())) {
                rels.add((ProductCmptCompareItem)item);
            }
        }
        return rels;
    }

    /**
     * Returns all RelationTypes for the given <code>IProductCmptGeneration</code> in the order of
     * occurrence. Multiple occurrences of the same ID are ignored.
     * <p>
     * Thus a list of relations with the IDs 1,2,1,3,1 returns the array {1,2,3}.
     */
    private String[] getAssociations(IProductPartsContainer gen) {
        // use TreeSet to avoid duplicate IDs and at the same time maintain their order.
        Set<String> relationTypes = new TreeSet<String>();
        List<IProductCmptLink> links = gen.getProductParts(IProductCmptLink.class);
        for (IProductCmptLink relation : links) {
            relationTypes.add(relation.getAssociation());
        }
        return relationTypes.toArray(new String[relationTypes.size()]);
    }

    /**
     * {@inheritDoc} For every generation as well as its configelements and relations the validFrom
     * date is prepended to every line of the text representation. The format of the date prefix is
     * not necessarily equal to the faktorips format, instead SimpleDateFormat.MEDIUM is used. The
     * prepended date is needed for the rangedifferencing (calculation of differences based on the
     * text representation) that is performed by the <code>TextMergeViewer</code> internally.
     */
    @Override
    protected String initContentString() {
        StringBuffer sb = new StringBuffer();
        if (getIpsElement() == null) {
            return sb.toString();
        }
        if (getIpsElement() instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)getIpsElement();
            initContentStringForProductCmptLink(sb, link);
        } else if (getIpsElement() instanceof IConfiguredDefault) {
            IConfiguredDefault configuredDefault = (IConfiguredDefault)getIpsElement();
            initContentString(sb, configuredDefault);
        } else if (getIpsElement() instanceof IConfiguredValueSet) {
            IConfiguredValueSet configValueSet = (IConfiguredValueSet)getIpsElement();
            initContentString(sb, configValueSet);
        } else if (getIpsElement() instanceof IValidationRuleConfig) {
            IValidationRuleConfig rule = (IValidationRuleConfig)getIpsElement();
            initContentStringForValidationRuleConfig(sb, rule);
        } else if (getIpsElement() instanceof IPropertyValue) {
            IPropertyValue value = (IPropertyValue)getIpsElement();
            initContentStringForPropertyValue(sb, value);
        } else if (getIpsElement() instanceof IIpsObjectGeneration) {
            IIpsObjectGeneration gen = (IIpsObjectGeneration)getIpsElement();
            initContentStringForGeneration(sb, gen);
        } else if (getIpsElement() instanceof IProductCmpt) {
            IProductCmpt product = (IProductCmpt)getIpsElement();
            initContentStringForProductCmpt(sb, product);
        }
        return sb.toString();
    }

    private void initContentStringForValidationRuleConfig(StringBuffer sb, IValidationRuleConfig rule) {
        conditionalAppendGenerationDateAndTab(rule.getPropertyValueContainer(), sb);
        sb.append(TAB).append(TAB).append(rule.getName()).append(COLON_BLANK);
        sb.append(rule.isActive() ? Messages.ProductCmptCompareItem_VRule_active
                : Messages.ProductCmptCompareItem_VRule_inactive);
    }

    private void initContentStringForPropertyValue(StringBuffer sb, IPropertyValue value) {
        conditionalAppendGenerationDateAndTab(value.getPropertyValueContainer(), sb);
        sb.append(TAB).append(TAB).append(value.getPropertyName()).append(COLON_BLANK);
        sb.append(value.getPropertyValue());
    }

    private void initContentStringForGeneration(StringBuffer sb, IIpsObjectGeneration gen) {
        sb.append(getGenerationDateText(gen));
        sb.append(TAB).append(getChangingNamingConventionGenerationString()).append(COLON_BLANK);
        sb.append(QUOTE).append(gen.getName()).append(QUOTE).append(NEWLINE);
        sb.append(getGenerationDateText(gen));
        sb.append(TAB).append(TAB)
                .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.GenerationEditDialog_labelValidFrom)
                .append(BLANK);
        sb.append(getDateFormat().format(gen.getValidFrom().getTime()));
    }

    private void initContentStringForProductCmpt(StringBuffer sb, IProductCmpt product) {
        sb.append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductCmptEditor_productComponent);
        sb.append(QUOTE).append(product.getName()).append(QUOTE).append(NEWLINE);
        sb.append(TAB).append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_type)
                .append(COLON_BLANK);
        sb.append(QUOTE).append(product.getProductCmptType()).append(QUOTE).append(NEWLINE);
        sb.append(TAB)
                .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_labelRuntimeId)
                .append(COLON_BLANK);
        sb.append(QUOTE).append(product.getRuntimeId()).append(QUOTE);
    }

    private void initContentStringForProductCmptLink(StringBuffer sb, IProductCmptLink rel) {
        conditionalAppendGenerationDateAndTab(rel.getProductCmptLinkContainer(), sb);
        sb.append(TAB).append(TAB).append(TAB).append(TAB).append(rel.getTarget());
        sb.append(BLANK).append(BLANK);
        sb.append('[').append(rel.getMinCardinality()).append("..").append(rel.getMaxCardinality()).append(COMMA) //$NON-NLS-1$
                .append(BLANK).append(rel.getDefaultCardinality()).append(']');
        sb.append(getMandatoryOrOptional(rel));
    }

    private StringBuffer getMandatoryOrOptional(IProductCmptLink rel) {
        StringBuffer sb = new StringBuffer();
        if (rel.isMandatory()) {
            sb.append('(')
                    .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.CardinalityPanel_labelMandatory)
                    .append(')');
        } else if (rel.isOptional()) {
            sb.append('(')
                    .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.CardinalityPanel_labelOptional)
                    .append(')');
        }
        return sb;
    }

    private void initContentString(StringBuffer sb, IConfiguredValueSet configValueSet) {
        conditionalAppendGenerationDateAndTab(configValueSet.getPropertyValueContainer(), sb);
        sb.append(TAB).append(TAB);
        sb.append(configValueSet.getName()).append(NEWLINE);
        conditionalAppendGenerationDateAndTab(configValueSet.getPropertyValueContainer(), sb);
        sb.append(TAB).append(TAB).append(TAB)
                .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ConfigElementEditComposite_valueSet)
                .append(BLANK).append(configValueSet.getValueSet().getCanonicalString());
    }

    private void initContentString(StringBuffer sb, IConfiguredDefault configuredDefault) {
        conditionalAppendGenerationDateAndTab(configuredDefault.getPropertyValueContainer(), sb);
        sb.append(TAB).append(TAB).append(TAB);
        sb.append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ConfigElementEditComposite_defaultValue)
                .append(BLANK);
        sb.append(configuredDefault.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String initName() {
        StringBuffer sb = new StringBuffer();
        if (getIpsElement() != null) {
            initNameIntenal(sb);
        }
        return sb.toString();
    }

    // CSOFF: CyclomaticComplexityCheck
    private void initNameIntenal(StringBuffer sb) {
        if (getIpsElement() instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)getIpsElement();
            sb.append(Messages.ProductCmptCompareItem_Relation).append(COLON_BLANK).append(QUOTE)
                    .append(link.getAssociation()).append(QUOTE);
        } else if (getIpsElement() instanceof IAttributeValue) {
            IAttributeValue attrValue = (IAttributeValue)getIpsElement();
            sb.append(Messages.ProductCmptCompareItem_Attribute).append(COLON_BLANK).append(QUOTE)
                    .append(attrValue.getPropertyName()).append(QUOTE);
        } else if (getIpsElement() instanceof IConfiguredDefault) {
            IConfiguredDefault configDefault = (IConfiguredDefault)getIpsElement();
            sb.append(Messages.ProductCmptCompareItem_Default).append(COLON_BLANK).append(QUOTE)
                    .append(configDefault.getPropertyName()).append(QUOTE);
        } else if (getIpsElement() instanceof IConfiguredValueSet) {
            IConfiguredValueSet configValueSet = (IConfiguredValueSet)getIpsElement();
            sb.append(Messages.ProductCmptCompareItem_ValueSet).append(COLON_BLANK).append(QUOTE)
                    .append(configValueSet.getPropertyName()).append(QUOTE);
        } else if (getIpsElement() instanceof IFormula) {
            IFormula formula = (IFormula)getIpsElement();
            sb.append(Messages.ProductCmptCompareItem_Formula).append(COLON_BLANK).append(QUOTE)
                    .append(formula.getPropertyName()).append(QUOTE);
        } else if (getIpsElement() instanceof ITableContentUsage) {
            ITableContentUsage tableUsage = (ITableContentUsage)getIpsElement();
            sb.append(Messages.ProductCmptCompareItem_TableContentsLabel).append(COLON_BLANK).append(QUOTE)
                    .append(tableUsage.getPropertyName()).append(QUOTE);
        } else if (getIpsElement() instanceof IValidationRuleConfig) {
            IValidationRuleConfig vRuleConfig = (IValidationRuleConfig)getIpsElement();
            sb.append(Messages.ProductCmptCompareItem_RuleLabel).append(COLON_BLANK).append(QUOTE)
                    .append(vRuleConfig.getPropertyName()).append(QUOTE);
        } else if (getIpsElement() instanceof IIpsObjectGeneration) {
            IIpsObjectGeneration gen = (IIpsObjectGeneration)getIpsElement();
            sb.append(getChangingNamingConventionGenerationString()).append(COLON_BLANK).append(QUOTE)
                    .append(getGenerationDateText(gen)).append(QUOTE);
        } else if (getIpsElement() instanceof IProductCmpt) {
            IProductCmpt product = (IProductCmpt)getIpsElement();
            sb.append(product.getName());
        } else if (getIpsElement() instanceof IIpsSrcFile) {
            IIpsSrcFile srcFile = (IIpsSrcFile)getIpsElement();
            sb.append(getFileName(srcFile));
        }
    }

    // CSON: CyclomaticComplexityCheck
    private StringBuffer getFileName(IIpsSrcFile srcFile) {
        StringBuffer sb = new StringBuffer();
        IFile file = srcFile.getCorrespondingFile();
        if (file != null) {
            sb.append(Messages.ProductCmptCompareItem_SourceFile).append(COLON_BLANK);
            sb.append(QUOTE).append(file.getName()).append(QUOTE);
        } else {
            sb.append(Messages.ProductCmptCompareItem_SourceFile);
        }
        return sb;
    }

    /**
     * For the root element we return the file extension. For all other elements we return the a
     * constant that is also registered in the content merge viewer extension in attribute
     * 'extensions'. This seems to be ugly because the attribute 'extensions' is documented as
     * listing file name extensions, but JDT does the same for java elements.
     */
    @Override
    public String getType() {
        if (isRoot()) {
            return IpsObjectType.PRODUCT_CMPT.getFileExtension();
        }
        return IPSPRODUCT_ELEMENT_TYPE;
    }

    /**
     * Sorts the list of children as in sortChildren(). Initializes the range for all
     * ProductCmptCompareItems that are descendants of this CompareItem. Additionally the document
     * for this CompareItem, as returned by getDocument(), is initialized.
     * <p>
     * initDocumentRange() has effect only if this CompareItem is the root of its structure.
     * 
     */
    @Override
    public void init() {
        if (isRoot()) {
            sortChildren();
        }
        super.init();
    }

    /**
     * Sorts the list of child <code>ProductCmptCompareItem</code>s using the
     * <code>ProductCmptCompareItemComparator</code>.
     * 
     * @see ProductCmptCompareItemComparator
     */
    private void sortChildren() {
        Collections.sort(getChildItems(), new ProductCmptCompareItemComparator());
        for (AbstractCompareItem element : getChildItems()) {
            ((ProductCmptCompareItem)element).sortChildren();
        }
    }

    @Override
    protected boolean isEqualIpsObjectPart(IIpsObjectPart part1, IIpsObjectPart part2) {
        if (part1 instanceof IPropertyValue && part2 instanceof IPropertyValue) {
            return ((IPropertyValue)part1).getPropertyName().equals(((IPropertyValue)part2).getPropertyName());
        } else if (part1 instanceof IProductCmptLink && part2 instanceof IProductCmptLink) {
            IProductCmptLink link1 = (IProductCmptLink)part1;
            IProductCmptLink link2 = (IProductCmptLink)part2;
            return link1.getAssociation().equals(link2.getAssociation()) && link1.getTarget().equals(link2.getTarget());
        }
        return super.isEqualIpsObjectPart(part1, part2);
    }

    @Override
    public int hashCode() {
        IIpsElement ipsElement = getIpsElement();
        if (ipsElement instanceof IPropertyValue) {
            return ((IPropertyValue)ipsElement).getPropertyName().hashCode();
        } else if (ipsElement instanceof IProductCmptLink) {
            return ((IProductCmptLink)ipsElement).getAssociation().hashCode();
        } else {
            return super.hashCode();
        }
    }

    @Override
    public String toString() {
        return "CompareItem: " + getContentString(); //$NON-NLS-1$
    }

}
