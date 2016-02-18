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

    public void setTemplatedValue(ITemplatedValue propertyValue) {
        setIpsObjectPartContainer(propertyValue);
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

    private ITemplatedValue getTemplatedValue() {
        return (ITemplatedValue)getIpsObjectPartContainer();
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

    /** Returns the value for the templated value in the template. */
    protected Object getTemplateValue() {
        ITemplatedValue templatePropertyValue = findTemplatedValue(getTemplate());
        return valueFunction().apply(templatePropertyValue);
    }

    /** Returns all templated values that define a custom value. */
    /* private */protected Collection<ITemplatedValue> getDefiningTemplatedValues() {
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
            ITemplatedValue templateValue = findTemplatedValue(templateNode.getElement());
            if (definesValue(templateValue)) {
                // Include template as it defines a custom value. Product components using the
                // template do not have to be included as their value depends on template and not
                // the initial template.
                result.add(templateValue);
            } else {
                // If the template does not define a value all product components using the template
                // should be included as their values actually depend on this PMO's template.
                result.addAll(findTemplatedValuesBasedOnTemplate(templateNode));
            }
        }
        return result;
    }

    /** Returns the children of the given node that hold product components (i.e. not templates). */
    private List<Node<ITemplatedValueContainer>> getContainerNodes(Node<ITemplatedValueContainer> node) {
        return FluentIterable.from(node.getChildren()).filter(Predicates.not(isTemplate())).toList();
    }

    /** Returns the children of the given node that hold product templates. */
    private List<Node<ITemplatedValueContainer>> getTemplateNodes(Node<ITemplatedValueContainer> node) {
        return FluentIterable.from(node.getChildren()).filter(isTemplate()).toList();
    }

    /** Returns this PMO's property. */
    private ITemplatedValueIdentifier getIdentifier() {
        return getTemplatedValue().getIdentifier();
    }

    /**
     * Returns whether or not the given property value defines a custom values (i.e. does not
     * inherit the value from its template).
     */
    private boolean definesValue(ITemplatedValue value) {
        return value != null && value.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    /** Function to transform an IIpsSrcFile to the IProductCmpt enclosed in it. */
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

    /** Function to transform a node to the IPropertyValue enclosed in it. */
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

    /**
     * Returns the value for this PMO's property from the given product component (or its
     * generation).
     */
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
     * Predicate that matches an IProductCmpt whose property value (for this PMO's property) has the
     * given TemplateValueStatus.
     */
    private Predicate<ITemplatedValue> valueStatus(final TemplateValueStatus t) {
        return new Predicate<ITemplatedValue>() {

            @Override
            public boolean apply(ITemplatedValue value) {
                return value != null && value.getTemplateValueStatus() == t;
            }

        };
    }

    /** Predicate that matches a node that encloses an IProductCmpt that is a template. */
    private Predicate<Node<ITemplatedValueContainer>> isTemplate() {
        return new Predicate<Node<ITemplatedValueContainer>>() {
            @Override
            public boolean apply(Node<ITemplatedValueContainer> node) {
                return node != null && node.getElement().isProductTemplate();
            }
        };
    }

    /** Returns a comparator to compare the value of property values. */
    public Comparator<Object> getValueComparator() {
        return getTemplatedValue().getValueComparator();
    }

    @Override
    protected boolean isAffected(ContentChangeEvent event) {
        return false;
    }

    /**
     * Returns a function to obtain the value of a property value
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
