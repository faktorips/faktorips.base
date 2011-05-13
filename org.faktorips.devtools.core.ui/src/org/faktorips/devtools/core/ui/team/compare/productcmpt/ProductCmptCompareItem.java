/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
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

    /**
     * Creates a ProductCmptCompareItem with the given parent and the given content. If parent is
     * null this ProductCmptCompareItem is marked as a root element, as indicated by the method
     * isRoot().
     */
    public ProductCmptCompareItem(ProductCmptCompareItem parent, IIpsElement content) {
        super(parent, content);
    }

    /**
     * A CompareItem representing a generation is processed separately in
     * initGenerationContentString().
     * 
     * @see #initGenerationContentString(StringBuffer, int)
     */
    @Override
    protected int initTreeContentString(StringBuffer sb, int offset) {
        int startIndex = sb.length();
        sb.append(getContentString());
        for (AbstractCompareItem compareItem : children) {
            ProductCmptCompareItem child = (ProductCmptCompareItem)compareItem;
            sb.append(NEWLINE);
            if (child.getIpsElement() instanceof IIpsObjectGeneration) {
                child.initGenerationContentString(sb, sb.length() - startIndex + offset);
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
     * Creates the string representation of this <code>ProductCmptCompareItem</code> and expects a
     * <code>IIpsObjectGeneration</code> as contained <code>IIpsElement</code>.
     * <p>
     * Similarly to initTreeContentString() this method also calculates the document-ranges for this
     * CompareItem and its children. The children of a generation (relations and config-elements)
     * are not treated equal. They are ordered and displayed in two separate lists, one with header
     * "relations" the other with header "attributes".
     * <p>
     * Except for the ordering of children the creation of the content string and calculation of
     * text ranges works similarly to the initTreeContentString() method.
     */
    private int initGenerationContentString(StringBuffer sb, int offset) {
        IProductCmptGeneration generation = (IProductCmptGeneration)getIpsElement();
        int startIndex = sb.length();
        sb.append(getContentString());
        List<ProductCmptCompareItem> attributes = getPropertyValues(children);
        List<ProductCmptCompareItem> relations = getRelations(children);
        List<ProductCmptCompareItem> rules = getRules(children);
        if (!attributes.isEmpty()) {
            sb.append(NEWLINE);
            String attrHeader = getAttributeListHeader(generation);
            sb.append(attrHeader);
            for (ProductCmptCompareItem attribute : attributes) {
                sb.append(NEWLINE);
                attribute.initTreeContentString(sb, sb.length() - startIndex + offset);
            }
        }
        if (!relations.isEmpty()) {
            sb.append(NEWLINE);
            String relationsHeader = getRelationListHeader(generation);
            sb.append(relationsHeader);
            String[] relationTypes = getRelationTypes(generation);
            for (String relationType : relationTypes) {
                List<ProductCmptCompareItem> relationsByType = getRelations(relations, relationType);
                if (!relationsByType.isEmpty()) {
                    String relTypeHeader = getRelationTypeHeader(generation, relationType);
                    sb.append(relTypeHeader);
                }
                for (ProductCmptCompareItem rel : relationsByType) {
                    sb.append(NEWLINE);
                    rel.initTreeContentString(sb, sb.length() - startIndex + offset);
                }
            }
        }
        if (!rules.isEmpty()) {
            sb.append(NEWLINE);
            sb.append(getRuleListHeader(generation));
            for (ProductCmptCompareItem rule : rules) {
                sb.append(NEWLINE);
                rule.initTreeContentString(sb, sb.length() - startIndex + offset);
            }
        }
        setRange(offset, sb.length() - startIndex);
        return sb.length() - startIndex;
    }

    private String getRuleListHeader(IProductCmptGeneration gen) {
        StringBuffer sb = new StringBuffer();
        String ruleHeaderName = org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ValidationRuleSection_DefaultTitle;
        appendGenerationDateForLineStart(gen, sb);
        sb.append(TAB).append(TAB).append(ruleHeaderName).append(COLON_BLANK);
        return sb.toString();
    }

    /**
     * Returns a string used as header for the list of attributes (configelements) in the string
     * representation of this CompareItem.
     */
    private String getAttributeListHeader(IIpsObjectGeneration gen) {
        StringBuffer sb = new StringBuffer();
        String attrString = org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_attribute;
        appendGenerationDateForLineStart(gen, sb);
        sb.append(TAB).append(TAB).append(attrString).append(COLON_BLANK);
        return sb.toString();
    }

    /**
     * Returns a string used as header for the list of relations in the string representation of
     * this CompareItem.
     */
    private String getRelationListHeader(IIpsObjectGeneration gen) {
        StringBuffer sb = new StringBuffer();
        String relString = org.faktorips.devtools.core.ui.editors.productcmpt.Messages.PropertiesPage_relations;
        appendGenerationDateForLineStart(gen, sb);
        sb.append(TAB).append(TAB).append(relString).append(COLON_BLANK);
        return sb.toString();
    }

    /**
     * Returns a string used as header for the list of relations of a common relationType (the given
     * String).
     */
    private String getRelationTypeHeader(IIpsObjectGeneration gen, String relationType) {
        StringBuffer sb = new StringBuffer();
        sb.append(NEWLINE);
        appendGenerationDateForLineStart(gen, sb);
        sb.append(TAB).append(TAB).append(TAB).append(relationType);
        return sb.toString();
    }

    protected void appendGenerationDateForLineStart(IIpsObjectGeneration gen, StringBuffer sb) {
        String validFrom = IpsPlugin.getDefault().getIpsPreferences().getDateFormat()
                .format(gen.getValidFrom().getTime());
        sb.append(validFrom);
    }

    /**
     * Returns a list containing all <code>ProductCmptCompareItem</code>s in the given list that
     * contain a <code>IConfigElement</code>.
     */
    private List<ProductCmptCompareItem> getPropertyValues(List<AbstractCompareItem> items) {
        List<ProductCmptCompareItem> ces = new ArrayList<ProductCmptCompareItem>();
        for (AbstractCompareItem item : items) {
            if (item.getIpsElement() instanceof IPropertyValue) {
                ces.add((ProductCmptCompareItem)item);
            }
        }
        return ces;
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
     * contain an <code>IProductComponentRelation</code>.
     */
    private List<ProductCmptCompareItem> getRelations(List<AbstractCompareItem> items) {
        List<ProductCmptCompareItem> rels = new ArrayList<ProductCmptCompareItem>();
        for (AbstractCompareItem item : items) {
            if (item.getIpsElement() instanceof IProductCmptLink) {
                rels.add((ProductCmptCompareItem)item);
            }
        }
        return rels;
    }

    /**
     * Returns a list containing all <code>ProductCmptCompareItem</code>s in the given list that
     * represent an <code>IValidationRule</code>.
     */
    private List<ProductCmptCompareItem> getRules(List<AbstractCompareItem> items) {
        List<ProductCmptCompareItem> rels = new ArrayList<ProductCmptCompareItem>();
        for (AbstractCompareItem item : items) {
            if (item.getIpsElement() instanceof IValidationRuleConfig) {
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
    private String[] getRelationTypes(IProductCmptGeneration gen) {
        // use TreeSet to avoid duplicate IDs and at the same time maintain their order.
        Set<String> relationTypes = new TreeSet<String>();
        IProductCmptLink[] relations = gen.getLinks();
        for (IProductCmptLink relation : relations) {
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
            IProductCmptLink rel = (IProductCmptLink)getIpsElement();
            appendGenerationDateForLineStart(rel.getProductCmptGeneration(), sb);
            sb.append(TAB).append(TAB).append(TAB).append(TAB).append(rel.getTarget());
            sb.append(BLANK).append(BLANK).append("["); //$NON-NLS-1$
            if (rel.isMandatory()) {
                sb.append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.CardinalityPanel_labelMandatory);
            } else if (rel.isOptional()) {
                sb.append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.CardinalityPanel_labelOptional);
            } else {
                sb.append(Messages.ProductCmptCompareItem_RelationCardinalityOther_minimum).append(COLON)
                        .append(rel.getMinCardinality()).append(COMMA).append(BLANK);
                sb.append(Messages.ProductCmptCompareItem_RelationCardinalityOther_maximum).append(COLON)
                        .append(rel.getMaxCardinality()).append(COMMA).append(BLANK);
                sb.append(Messages.ProductCmptCompareItem_RelationCardinalityOther_default).append(COLON)
                        .append(rel.getDefaultCardinality());
            }
            sb.append("]"); //$NON-NLS-1$
            sb.append(BLANK).append(BLANK).append("(").append(rel.getId()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (getIpsElement() instanceof IConfigElement) {
            IConfigElement configElement = (IConfigElement)getIpsElement();
            appendGenerationDateForLineStart(configElement.getProductCmptGeneration(), sb);
            sb.append(TAB).append(TAB).append(TAB).append(configElement.getName()).append(NEWLINE);
            appendGenerationDateForLineStart(configElement.getProductCmptGeneration(), sb);
            sb.append(TAB)
                    .append(TAB)
                    .append(TAB)
                    .append(TAB)
                    .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.PolicyAttributeEditDialog_defaultValue)
                    .append(BLANK);
            sb.append(configElement.getValue()).append(NEWLINE);
            appendGenerationDateForLineStart(configElement.getProductCmptGeneration(), sb);
            sb.append(TAB).append(TAB).append(TAB).append(TAB).append(Messages.ProductCmptCompareItem_ValueSet)
                    .append(COLON_BLANK);
            sb.append(getValueSetContent(configElement));
        } else if (getIpsElement() instanceof IPropertyValue) {
            IPropertyValue value = (IPropertyValue)getIpsElement();
            appendGenerationDateForLineStart(value.getProductCmptGeneration(), sb);
            sb.append(TAB).append(TAB).append(TAB).append(value.getPropertyName()).append(COLON_BLANK);
            sb.append(value.getPropertyValue());
        } else if (getIpsElement() instanceof IValidationRuleConfig) {
            IValidationRuleConfig rule = (IValidationRuleConfig)getIpsElement();
            appendGenerationDateForLineStart(rule.getProductCmptGeneration(), sb);
            sb.append(TAB).append(TAB).append(TAB).append(rule.getName()).append(COLON_BLANK);
            sb.append(rule.isActive() ? Messages.ProductCmptCompareItem_VRule_active
                    : Messages.ProductCmptCompareItem_VRule_inactive);
        } else if (getIpsElement() instanceof IIpsObjectGeneration) {
            IIpsObjectGeneration gen = (IIpsObjectGeneration)getIpsElement();
            appendGenerationDateForLineStart(gen, sb);
            sb.append(TAB).append(changingNamingConventionGenerationString).append(COLON_BLANK);
            sb.append(QUOTE).append(gen.getName()).append(QUOTE).append(NEWLINE);
            appendGenerationDateForLineStart(gen, sb);
            sb.append(TAB)
                    .append(TAB)
                    .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.GenerationEditDialog_labelValidFrom)
                    .append(BLANK);
            sb.append(dateFormat.format(gen.getValidFrom().getTime()));
        } else if (getIpsElement() instanceof IProductCmpt) {
            IProductCmpt product = (IProductCmpt)getIpsElement();
            sb.append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductCmptEditor_productComponent);
            sb.append(QUOTE).append(product.getName()).append(QUOTE).append(NEWLINE);
            sb.append(TAB)
                    .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_attribute)
                    .append(COLON_BLANK).append(NEWLINE);
            sb.append(TAB)
                    .append(TAB)
                    .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_template)
                    .append(COLON_BLANK);
            sb.append(QUOTE).append(product.getProductCmptType()).append(QUOTE).append(NEWLINE);
            sb.append(TAB)
                    .append(TAB)
                    .append(org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductAttributesSection_labelRuntimeId)
                    .append(COLON_BLANK);
            sb.append(QUOTE).append(product.getRuntimeId()).append(QUOTE);
        } else if (getIpsElement() instanceof IIpsSrcFile) {
            // no text for srcfile
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of a <code>ValueSet</code>. An <code>EnumValueSet</code> is
     * represented as a list of values separated by comma ("[1,5,7]"). A <code>RangeValueSet</code>
     * is represented by its upper and lower bound ("[1..7]"). A <code>AllValuesValueSet</code> is
     * represented by "[all values]".
     */
    private StringBuffer getValueSetContent(IConfigElement configElement) {
        StringBuffer sb = new StringBuffer();
        IValueSet set = configElement.getValueSet();
        if (set instanceof IEnumValueSet) {
            sb.append("["); //$NON-NLS-1$
            String[] values = ((IEnumValueSet)set).getValues();
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i]);
                if (i < values.length - 1) {
                    sb.append(COMMA);
                }
            }
            sb.append("]"); //$NON-NLS-1$
        } else if (set instanceof IRangeValueSet) {
            IRangeValueSet rangeSet = (IRangeValueSet)set;
            sb.append("["); //$NON-NLS-1$
            String unlimited = Messages.ProductCmptCompareItem_unlimited;
            if (rangeSet.getLowerBound() == null) {
                sb.append(unlimited);
            } else {
                sb.append(rangeSet.getLowerBound());
            }
            sb.append(".."); //$NON-NLS-1$
            if (rangeSet.getUpperBound() == null) {
                sb.append(unlimited);
            } else {
                sb.append(rangeSet.getUpperBound());
            }
            sb.append("]"); //$NON-NLS-1$
        } else if (set instanceof IUnrestrictedValueSet) {
            sb.append("["); //$NON-NLS-1$
            sb.append(Messages.ProductCmptCompareItem_AllValues);
            sb.append("]"); //$NON-NLS-1$
        } else {
            sb.append("Unknown value set type " + set.getClass()); //$NON-NLS-1$
        }
        return sb;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String initName() {
        StringBuffer sb = new StringBuffer();
        if (getIpsElement() != null) {
            if (getIpsElement() instanceof IProductCmptLink) {
                IProductCmptLink rel = (IProductCmptLink)getIpsElement();
                sb.append(Messages.ProductCmptCompareItem_Relation).append(COLON_BLANK).append(QUOTE)
                        .append(getCaption(rel)).append(QUOTE);
            } else if (getIpsElement() instanceof IAttributeValue) {
                IAttributeValue attrValue = (IAttributeValue)getIpsElement();
                sb.append(Messages.ProductCmptCompareItem_Attribute).append(COLON_BLANK).append(QUOTE)
                        .append(getCaption(attrValue)).append(QUOTE);
            } else if (getIpsElement() instanceof IFormula) {
                IFormula formula = (IFormula)getIpsElement();
                sb.append(Messages.ProductCmptCompareItem_Formula).append(COLON_BLANK).append(QUOTE)
                        .append(getCaption(formula)).append(QUOTE);
            } else if (getIpsElement() instanceof ITableContentUsage) {
                ITableContentUsage tableUsage = (ITableContentUsage)getIpsElement();
                sb.append(Messages.ProductCmptCompareItem_TableContentsLabel).append(COLON_BLANK).append(QUOTE)
                        .append(getCaption(tableUsage)).append(QUOTE);
            } else if (getIpsElement() instanceof IValidationRuleConfig) {
                IValidationRuleConfig vRuleConfig = (IValidationRuleConfig)getIpsElement();
                sb.append(Messages.ProductCmptCompareItem_RuleLabel).append(COLON_BLANK).append(QUOTE)
                        .append(getCaption(vRuleConfig)).append(QUOTE);
            } else if (getIpsElement() instanceof IIpsObjectGeneration) {
                IIpsObjectGeneration gen = (IIpsObjectGeneration)getIpsElement();
                sb.append(changingNamingConventionGenerationString).append(COLON_BLANK).append(QUOTE)
                        .append(getCaption(gen)).append(QUOTE);
            } else if (getIpsElement() instanceof IProductCmpt) {
                IProductCmpt product = (IProductCmpt)getIpsElement();
                sb.append(
                        org.faktorips.devtools.core.ui.editors.productcmpt.Messages.ProductCmptEditor_productComponent)
                        .append(QUOTE).append(IpsPlugin.getMultiLanguageSupport().getDefaultCaption(product))
                        .append(QUOTE);
            } else if (getIpsElement() instanceof IIpsSrcFile) {
                IIpsSrcFile srcFile = (IIpsSrcFile)getIpsElement();
                IFile file = srcFile.getCorrespondingFile();
                if (file != null) {
                    sb.append(Messages.ProductCmptCompareItem_SourceFile).append(COLON_BLANK);
                    sb.append(QUOTE).append(file.getName()).append(QUOTE);
                } else {
                    sb.append(Messages.ProductCmptCompareItem_SourceFile);
                }
            }
        }
        return sb.toString();
    }

    private String getCaption(IIpsObjectPart part) {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(part);
    }

    /**
     * Returns "ipsproduct". {@inheritDoc}
     */
    @Override
    public String getType() {
        return IpsObjectType.PRODUCT_CMPT.getFileExtension();
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
        Collections.sort(children, new ProductCmptCompareItemComparator());
        for (AbstractCompareItem element : children) {
            ((ProductCmptCompareItem)element).sortChildren();
        }
    }
}
