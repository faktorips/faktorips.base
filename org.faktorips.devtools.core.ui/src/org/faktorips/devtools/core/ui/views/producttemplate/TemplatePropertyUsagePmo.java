/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplatedValueFormatter;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.util.Tree;
import org.faktorips.devtools.model.util.Tree.Node;

public class TemplatePropertyUsagePmo extends IpsObjectPartPmo {

    public static final String PROPERTY_INHERITED_VALUES_LABEL_TEXT = "inheritedValuesLabelText"; //$NON-NLS-1$
    public static final String PROPERTY_DEFINED_VALUES_LABEL_TEXT = "definedValuesLabelText"; //$NON-NLS-1$

    /**
     * Predicate that matches a node that encloses an ITemplatedValueContainer that is a template.
     */
    private final Predicate<Node<ITemplatedValueContainer>> isTemplate = node -> node != null
            && node.getElement().isProductTemplate();

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
        if (showValues()) {
            return NLS.bind(Messages.TemplatePropertyUsageView_InheritedValue_label,
                    new Object[] { propertyName, formattedValue, inheritedCount, inheritedPercent });
        } else {
            return NLS.bind(Messages.TemplatePropertyUsageView_InheritedValue_labelWithoutValue,
                    new Object[] { propertyName, inheritedCount, inheritedPercent });
        }
    }

    /**
     * Whether or not the "actual" values of the templated values should be displayed.
     * <p>
     * For {@link org.faktorips.devtools.model.productcmpt.IPropertyValue property values} the value
     * should always be displayed. For {@link IProductCmptLink links} the value (i.e. the
     * cardinality) should only be displayed if there is a meaningful cardinality, i.e. if the link
     * is configuring a policy association.
     * <p>
     * See {@link #valueFunction()} for a definition of "actual" value.
     */
    protected boolean showValues() {
        if (getTemplatedValue() instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)getTemplatedValue();
            return link.isConfiguringPolicyAssociation();
        }
        return true;
    }

    private BigDecimal getInheritPercent(int inheritedCount) {
        int count = getCount();
        if (count == 0) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(inheritedCount).multiply(new BigDecimal(100))
                    .divide(new BigDecimal(count), 1, RoundingMode.HALF_UP);
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
        return IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getTemplatedValue());
    }

    /** Returns the template whose property usage is displayed. */
    public ITemplatedValueContainer getTemplate() {
        return getTemplatedValue().getTemplatedValueContainer();
    }

    /** Returns the product components that inherit the value from the template. */
    public Collection<ITemplatedValue> getInheritingTemplatedValues() {
        if (hasData()) {
            return findTemplatedValuesBasedOnTemplate().stream().filter(valueStatus(TemplateValueStatus.INHERITED))
                    .collect(Collectors.toList());
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
        return new Histogram<>(valueFunction(), getValueComparator(),
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
        if (templatePropertyValue != null) {
            return valueFunction().apply(templatePropertyValue);
        } else {
            return null;
        }
    }

    /**
     * Returns all templated values that define a custom value.
     * <p>
     * Unlike the name suggests, these templated values may contain some with
     * {@code TemplateValueStatus.UNDEFINED}, namely product component links that were deleted.
     */
    protected Collection<ITemplatedValue> getDefiningTemplatedValues() {
        return findTemplatedValuesBasedOnTemplate().stream()
                .filter(valueStatus(TemplateValueStatus.DEFINED, TemplateValueStatus.UNDEFINED))
                .collect(Collectors.toList());
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
        Tree<IIpsSrcFile> templateSrcFileHierarchy = getIpsProject()
                .findTemplateHierarchy(getTemplate().getProductCmpt());
        if (templateSrcFileHierarchy.isEmpty()) {
            return Collections.emptyList();
        }
        Tree<ITemplatedValueContainer> templateHierarchy = templateSrcFileHierarchy
                .transform(srcFile -> (ITemplatedValueContainer)nonNull(srcFile).getIpsObject());
        return findTemplatedValuesBasedOnTemplate(templateHierarchy.getRoot());
    }

    private List<ITemplatedValue> findTemplatedValuesBasedOnTemplate(Node<ITemplatedValueContainer> node) {
        List<ITemplatedValue> result = new ArrayList<>();
        result.addAll(getContainerNodes(node).stream().map(n -> findTemplatedValue(nonNull(n).getElement()))
                .filter(Objects::nonNull).collect(Collectors.toList()));

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
            return List.of(templateValue);
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
        return node.getChildren().stream().filter(isTemplate.negate()).collect(Collectors.toList());
    }

    /** Returns the children of the given node that hold product templates. */
    private List<Node<ITemplatedValueContainer>> getTemplateNodes(Node<ITemplatedValueContainer> node) {
        return node.getChildren().stream().filter(isTemplate).collect(Collectors.toList());
    }

    /** Returns the identifier for the templated values used in this PMO. */
    private ITemplatedValueIdentifier getIdentifier() {
        return getTemplatedValue().getIdentifier();
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
     * Predicate that matches an ITemplatedValue with a TemplateValueStatus contained in the given
     * TemplateValueStatus.
     */
    private Predicate<ITemplatedValue> valueStatus(final TemplateValueStatus... t) {
        final Set<TemplateValueStatus> states = Set.of(t);
        return value -> value != null && states.contains(value.getTemplateValueStatus());
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
     * For an {@link org.faktorips.devtools.model.productcmpt.IAttributeValue} the actual value is
     * the String/Decimal/... that is defined in the product component (or generation), for an
     * {@link org.faktorips.devtools.model.productcmpt.ITableContentUsage} the actual value is the
     * name of the table etc.
     * <p>
     * For an {@link org.faktorips.devtools.model.productcmpt.IProductCmptLink} the actual value (in
     * this context) is its cardinality.
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

    /**
     * Does the same as {@link Objects#requireNonNull(Object)}, but throws an
     * {@link IllegalStateException} instead of a {@link NullPointerException} to keep the behavior
     * unchanged.
     */
    private static final <T> T nonNull(T t) {
        if (t == null) {
            throw new IllegalStateException();
        }
        return t;
    }

}
