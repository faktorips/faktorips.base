/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Collections2.filter;

import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValue;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValueIdentifier;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplatedValueFormatter;
import org.faktorips.devtools.core.util.Histogram;
import org.faktorips.devtools.core.util.Tree;
import org.faktorips.devtools.core.util.Tree.Node;

public class TemplatePropertyUsagePmo extends IpsObjectPartPmo {

    public static final String PROPERTY_INHERITED_VALUES_LABEL_TEXT = "inheritedValuesLabelText"; //$NON-NLS-1$
    public static final String PROPERTY_DEFINED_VALUES_LABEL_TEXT = "definedValuesLabelText"; //$NON-NLS-1$

    public TemplatePropertyUsagePmo() {
        super();
    }

    public TemplatePropertyUsagePmo(ITemplatedValue propertyValue) {
        this();
        setTemplatedValue(propertyValue);
    }

    public void setTemplatedValue(ITemplatedValue templatedValue) {
        setIpsObjectPartContainer(templatedValue);
    }

    /**
     * Returns the templated value which the PMO currently uses. This is a templated value from a
     * product template.
     */
    private ITemplatedValue getTemplatedValue() {
        return (ITemplatedValue)getIpsObjectPartContainer();
    }

    protected boolean hasData() {
        return getTemplatedValue() != null;
    }

    public String getInheritedValuesLabelText() {
        if (hasData()) {
            return getInheritedValuesLabelWithData();
        } else {
            return Messages.TemplatePropertyUsageView_InheritedValue_fallbackLabel;
        }
    }

    private String getInheritedValuesLabelWithData() {
        String propertyName = getTemplatedValueLabel();
        String formattedValue = TemplatedValueFormatter.shortedFormat(getTemplatedValue());
        int inheritedCount = getInheritingTemplatedValues().size();
        String inheritedPercent = getInheritPercent(inheritedCount).stripTrailingZeros().toPlainString();
        return NLS.bind(Messages.TemplatePropertyUsageView_InheritedValue_label, new Object[] { propertyName,
                formattedValue, inheritedCount, inheritedPercent });
    }

    private BigDecimal getInheritPercent(int inheritedCount) {
        int count = getCount();
        if (count == 0) {
            return BigDecimal.ZERO;
        } else {
            BigDecimal inheritedPercent = new BigDecimal(inheritedCount).multiply(new BigDecimal(100)).divide(
                    new BigDecimal(count), 1, RoundingMode.HALF_UP);
            return inheritedPercent;
        }
    }

    public String getDefinedValuesLabelText() {
        if (hasData()) {
            return getDefinedLabelWithData();
        } else {
            return Messages.TemplatePropertyUsageView_DifferingValues_fallbackLabel;
        }
    }

    private String getDefinedLabelWithData() {
        return NLS.bind(Messages.TemplatePropertyUsageView_DifferingValues_label, getTemplatedValueLabel());
    }

    /** Returns the label for property value. */
    private String getTemplatedValueLabel() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getTemplatedValue());
    }

    /** Returns the template whose property usage is displayed. */
    public ITemplatedValueContainer getTemplate() {
        return getTemplatedValue().getTemplatedValueContainer();
    }

    /** Returns the product components that inherit the value from the template. */
    public Collection<ITemplatedValue> getInheritingTemplatedValues() {
        if (hasData()) {
            return filter(findTemplatedValuesBasedOnTemplate(), valueStatus(TemplateValueStatus.INHERITED));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns a histogram for the custom (i.e. non-inherited) values that are defined in product
     * components. The histogram's values are the custom values, the histogram's elements are the
     * product components defining these values.
     * <p>
     * Note that the values in the histogram are not {@code IPropertyValue} instances but their
     * actual values, e.g. it contains strings with table names and not {@code ITableContentUsage}
     * objects.
     */
    public Histogram<Object, ITemplatedValue> getDefinedValuesHistogram() {
        if (hasData()) {
            return getHistogramInternal();
        } else {
            return Histogram.emptyHistogram();
        }
    }

    private Histogram<Object, ITemplatedValue> getHistogramInternal() {
        return new Histogram<Object, ITemplatedValue>(valueFunction(), getValueComparator(),
                getDefiningTemplatedValues());
    }

    public int getCount() {
        return getInheritingTemplatedValues().size() + getDefinedValuesHistogram().countElements();
    }

    /**
     * Returns the "actual" value of the templated value in the template. See
     * {@link TemplatePropertyUsagePmo#valueFunction()} for a definition of "actual" value.
     */
    protected Object getActualTemplateValue() {
        ITemplatedValue templatePropertyValue = findTemplatedValue(getTemplate());
        return valueFunction().apply(templatePropertyValue);
    }

    /** Returns all templated values that define a custom value. */
    protected Collection<ITemplatedValue> getDefiningTemplatedValues() {
        return filter(findTemplatedValuesBasedOnTemplate(), valueStatus(TemplateValueStatus.DEFINED));
    }

    /**
     * Find property values that are based on the given template. This list includes
     * <ul>
     * <li>property values that use the given template</li>
     * <li>property values that use the given template and define a value (i.e. do not inherit the
     * value from the given template)</li>
     * <li>property values that use templates inheriting their values from the given template</li>
     * </ul>
     */
    private List<ITemplatedValue> findTemplatedValuesBasedOnTemplate() {
        Tree<IIpsSrcFile> templateSrcFileHierarchy = getIpsProject().findTemplateHierarchy(
                getTemplate().getProductCmpt());
        if (templateSrcFileHierarchy.isEmpty()) {
            return Collections.emptyList();
        }
        Tree<ITemplatedValueContainer> templateHierarchy = templateSrcFileHierarchy.transform(srcFileToContainer());
        return findTemplatedValuesBasedOnTemplate(templateHierarchy.getRoot());
    }

    private List<ITemplatedValue> findTemplatedValuesBasedOnTemplate(Node<ITemplatedValueContainer> node) {
        List<ITemplatedValue> result = Lists.newArrayList();
        result.addAll(filter(Lists.transform(getContainerNodes(node), nodeToTemplatedValue()), notNull()));

        List<Node<ITemplatedValueContainer>> templateNodes = getTemplateNodes(node);
        for (Node<ITemplatedValueContainer> templateNode : templateNodes) {
            result.addAll(getRelevantValues(templateNode));
        }
        return result;
    }

    private List<ITemplatedValue> getRelevantValues(Node<ITemplatedValueContainer> templateNode) {
        ITemplatedValue templateValue = findTemplatedValue(templateNode.getElement());
        if (templateValue.isConcreteValue()) {
            // Include the template's value as it is a concrete value. Product components using the
            // template do not have to be included as their value depends on the template.
            return Lists.newArrayList(templateValue);
        } else {
            // If the template does not define a concrete value, the values of all product
            // components (and templates) based on it have to be included as their values actually
            // depend on the value from the template this PMO uses.
            return findTemplatedValuesBasedOnTemplate(templateNode);
        }
    }

    /**
     * Returns the children of the given node that hold containers that are not templates (i.e.
     * "normal" product components or generations).
     */
    private List<Node<ITemplatedValueContainer>> getContainerNodes(Node<ITemplatedValueContainer> node) {
        return FluentIterable.from(node.getChildren()).filter(Predicates.not(isTemplate())).toList();
    }

    /** Returns the children of the given node that hold product templates. */
    private List<Node<ITemplatedValueContainer>> getTemplateNodes(Node<ITemplatedValueContainer> node) {
        return FluentIterable.from(node.getChildren()).filter(isTemplate()).toList();
    }

    /** Returns the identifier for the templated values used in this PMO. */
    private ITemplatedValueIdentifier getIdentifier() {
        return getTemplatedValue().getIdentifier();
    }

    /** Function to transform an IIpsSrcFile to the ITemplatedValueContainer enclosed in it. */
    private Function<IIpsSrcFile, ITemplatedValueContainer> srcFileToContainer() {
        return new Function<IIpsSrcFile, ITemplatedValueContainer>() {
            @Override
            public ITemplatedValueContainer apply(IIpsSrcFile srcFile) {
                // FindBugs does not like Preconditions.checkState...
                if (srcFile == null) {
                    throw new IllegalStateException();
                }
                return (ITemplatedValueContainer)srcFile.getIpsObject();
            }

        };
    }

    /** Function to transform a node to the ITemplatedValue enclosed in it. */
    private Function<Node<ITemplatedValueContainer>, ITemplatedValue> nodeToTemplatedValue() {
        return new Function<Node<ITemplatedValueContainer>, ITemplatedValue>() {

            @Override
            public ITemplatedValue apply(Node<ITemplatedValueContainer> node) {
                // FindBugs does not like Preconditions.checkState...
                if (node == null) {
                    throw new IllegalStateException();
                }
                return findTemplatedValue(node.getElement());
            }
        };
    }

    /** Returns the templated value from the given product component (or its generation). */
    private ITemplatedValue findTemplatedValue(ITemplatedValueContainer container) {
        IProductCmpt productCmpt = container.getProductCmpt();
        ITemplatedValue templatedValue = getIdentifier().getValueFrom(productCmpt);

        if (templatedValue != null) {
            return templatedValue;
        }

        // TODO FIPS-4433
        // IProductCmptGeneration gen = productCmpt.getGenerationEffectiveOn(effectiveDate);
        IProductCmptGeneration gen = productCmpt.getLatestProductCmptGeneration();
        if (gen != null) {
            return getIdentifier().getValueFrom(gen);
        } else {
            return null;
        }
    }

    /**
     * Predicate that matches an ITemplatedValue that has the given TemplateValueStatus.
     */
    private Predicate<ITemplatedValue> valueStatus(final TemplateValueStatus t) {
        return new Predicate<ITemplatedValue>() {

            @Override
            public boolean apply(ITemplatedValue value) {
                return value != null && value.getTemplateValueStatus() == t;
            }

        };
    }

    /** Predicate that matches a node that encloses an ITemplatedValueContainer that is a template. */
    private Predicate<Node<ITemplatedValueContainer>> isTemplate() {
        return new Predicate<Node<ITemplatedValueContainer>>() {
            @Override
            public boolean apply(Node<ITemplatedValueContainer> node) {
                return node != null && node.getElement().isProductTemplate();
            }
        };
    }

    /**
     * Returns a comparator to compare the "actual" values of templated value objects. See
     * {@link #valueFunction()} for a definition of "actual" value.
     */
    public Comparator<Object> getValueComparator() {
        return getTemplatedValue().getValueComparator();
    }

    @Override
    protected boolean isAffected(ContentChangeEvent event) {
        return false;
    }

    /**
     * Returns a function to obtain the "actual" value of a templated value.
     * <p>
     * For an {@link org.faktorips.devtools.core.model.productcmpt.IAttributeValue} the actual value
     * is the String/Decimal/... that is defined in the product component (or generation), for an
     * {@link org.faktorips.devtools.core.model.productcmpt.ITableContentUsage} the actual value is
     * the name of the table etc.
     * <p>
     * For an {@link org.faktorips.devtools.core.model.productcmpt.IProductCmptLink} the actual
     * value (in this context) is its cardinality.
     */
    @SuppressWarnings("unchecked")
    private Function<ITemplatedValue, Object> valueFunction() {
        return (Function<ITemplatedValue, Object>)getTemplatedValue().getValueGetter();
    }

    @Override
    protected void partHasChanged() {
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_INHERITED_VALUES_LABEL_TEXT, null, null));
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_DEFINED_VALUES_LABEL_TEXT, null, null));
    }

}
