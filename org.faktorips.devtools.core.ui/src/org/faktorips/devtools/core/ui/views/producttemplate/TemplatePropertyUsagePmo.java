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

import static com.google.common.collect.Collections2.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.util.Histogram;
import org.faktorips.devtools.core.util.TemplatePropertyValueUtil;
import org.faktorips.devtools.core.util.Tree;
import org.faktorips.devtools.core.util.Tree.Node;

public class TemplatePropertyUsagePmo extends IpsObjectPartPmo {

    private final IProductCmpt template;
    private final GregorianCalendar effectiveDate;

    // lazily loaded
    private List<IPropertyValue> propertyValuesBasedOnTemplate;

    // lazily loaded
    private Histogram<Object, IPropertyValue> histogram;

    public TemplatePropertyUsagePmo(IPropertyValue propertyValue) {
        super(propertyValue);
        this.template = findTemplate();
        this.effectiveDate = getEffectiveDate();
    }

    /**
     * Finds the template of the property value container to which the given property value belongs.
     * Returns <code>null</code> if the property value does not have a template value.
     */
    private IProductCmpt findTemplate() {
        if (TemplatePropertyValueUtil.isDefinedTemplatePropertyValue(getInitialPropertyValue())) {
            return (IProductCmpt)getInitialPropertyValue().getIpsObject();
        }
        IPropertyValue templateValue = getInitialPropertyValue().findTemplateProperty(getIpsProject());
        if (templateValue == null) {
            return null;
        } else {
            return templateValue.getPropertyValueContainer().getProductCmpt();
        }
    }

    /**
     * Get the effective date based on the initial property value
     */
    private GregorianCalendar getEffectiveDate() {
        IPropertyValueContainer propertyValueContainer = getInitialPropertyValue().getPropertyValueContainer();
        if (propertyValueContainer instanceof IProductCmptGeneration) {
            return ((IProductCmptGeneration)propertyValueContainer).getValidFrom();
        } else if (propertyValueContainer instanceof IProductCmpt) {
            return ((IProductCmpt)propertyValueContainer).getValidFrom();
        } else {
            return null;
        }
    }

    /** Returns the property value with which this PMO was initialized. */
    private IPropertyValue getInitialPropertyValue() {
        IPropertyValue propertyValue = (IPropertyValue)getIpsObjectPartContainer();
        return propertyValue;
    }

    /** Returns the template whose property usage is displayed. */
    public IProductCmpt getTemplate() {
        return template;
    }

    /** Returns the product components that inherit the value from the template. */
    public Collection<IPropertyValue> getInheritingPropertyValues() {
        return filter(getPropertyValuesBasedOnTemplate(), propertyValueStatus(TemplateValueStatus.INHERITED));
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
    public Histogram<Object, IPropertyValue> getDefinedValuesHistogram() {
        if (histogram == null) {
            histogram = new Histogram<Object, IPropertyValue>(valueFunction(), valueComparator(),
                    getDefiningPropertyValues());
        }
        return histogram;
    }

    /** Returns all product components that define a custom value. */
    /* private */protected Collection<IPropertyValue> getDefiningPropertyValues() {
        return filter(getPropertyValuesBasedOnTemplate(), propertyValueStatus(TemplateValueStatus.DEFINED));
    }

    /** Returns the product components that reference this PMO's template. */
    private List<IPropertyValue> getPropertyValuesBasedOnTemplate() {
        if (propertyValuesBasedOnTemplate == null) {
            propertyValuesBasedOnTemplate = findPropertyValuesBasedOnTemplate(template);
        }
        return propertyValuesBasedOnTemplate;
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
    private List<IPropertyValue> findPropertyValuesBasedOnTemplate(IProductCmpt template) {
        Tree<IIpsSrcFile> srcFileHierarchy = getIpsProject().findTemplateHierarchy(template);
        if (srcFileHierarchy.isEmpty()) {
            return Collections.emptyList();
        }
        Tree<IProductCmpt> productCmptHierarchy = srcFileHierarchy.transform(srcFileToProductCmpt());
        return findPropertyValuesBasedOnTemplate(productCmptHierarchy.getRoot());
    }

    private List<IPropertyValue> findPropertyValuesBasedOnTemplate(Node<IProductCmpt> node) {
        List<IPropertyValue> result = Lists.newArrayList();
        result.addAll(Lists.transform(getProductNodes(node), nodeToPropertyValue()));

        List<Node<IProductCmpt>> templateNodes = getTemplateNodes(node);
        for (Node<IProductCmpt> templateNode : templateNodes) {
            IPropertyValue templateValue = findPropertyValue(templateNode.getElement());
            if (definesValue(templateValue)) {
                // Include template as it defines a custom value. Product components using the
                // template do not have to be included as their value depends on template and not
                // the initial template.
                result.add(templateValue);
            } else {
                // If the template does not define a value all product components using the template
                // should be included as their values actually depend on this PMO's template.
                result.addAll(findPropertyValuesBasedOnTemplate(templateNode));
            }
        }
        return result;
    }

    /** Returns the children of the given node that hold product components (i.e. not templates). */
    private List<Node<IProductCmpt>> getProductNodes(Node<IProductCmpt> node) {
        return FluentIterable.from(node.getChildren()).filter(Predicates.not(isTemplate())).toImmutableList();
    }

    /** Returns the children of the given node that hold product templates. */
    private List<Node<IProductCmpt>> getTemplateNodes(Node<IProductCmpt> node) {
        return FluentIterable.from(node.getChildren()).filter(isTemplate()).toImmutableList();
    }

    /** Returns this PMO's property. */
    private String getPropertyName() {
        return getInitialPropertyValue().getPropertyName();
    }

    /**
     * Returns whether or not the given property value defines a custom values (i.e. does not
     * inherit the value from its template).
     */
    private boolean definesValue(IPropertyValue value) {
        return value != null && value.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    /** Function to transform an IIpsSrcFile to the IProductCmpt enclosed in it. */
    private Function<IIpsSrcFile, IProductCmpt> srcFileToProductCmpt() {
        return new Function<IIpsSrcFile, IProductCmpt>() {
            @Override
            public IProductCmpt apply(IIpsSrcFile srcFile) {
                // FindBugs does not like Preconditions.checkState...
                if (srcFile == null) {
                    throw new IllegalStateException();
                }
                return (IProductCmpt)srcFile.getIpsObject();
            }

        };
    }

    /** Function to transform a node to the IPropertyValue enclosed in it. */
    private Function<Node<IProductCmpt>, IPropertyValue> nodeToPropertyValue() {
        return new Function<Node<IProductCmpt>, IPropertyValue>() {

            @Override
            public IPropertyValue apply(Node<IProductCmpt> node) {
                // FindBugs does not like Preconditions.checkState...
                if (node == null) {
                    throw new IllegalStateException();
                }
                return findPropertyValue(node.getElement());
            }
        };
    }

    /**
     * Returns the value for this PMO's property from the given product component (or its
     * generation).
     */
    private IPropertyValue findPropertyValue(IProductCmpt productCmpt) {
        IPropertyValue propertyValue = productCmpt.getPropertyValue(getPropertyName());
        if (propertyValue != null) {
            return propertyValue;
        }

        IProductCmptGeneration gen = productCmpt.getGenerationEffectiveOn(effectiveDate);
        return gen.getPropertyValue(getPropertyName());
    }

    /**
     * Predicate that matches an IProductCmpt whose property value (for this PMO's property) has the
     * given TemplateValueStatus.
     */
    private Predicate<IPropertyValue> propertyValueStatus(final TemplateValueStatus t) {
        return new Predicate<IPropertyValue>() {

            @Override
            public boolean apply(IPropertyValue propertyValue) {
                return propertyValue != null && propertyValue.getTemplateValueStatus() == t;
            }

        };
    }

    /** Predicate that matches a node that encloses an IProductCmpt that is a template. */
    private Predicate<Node<IProductCmpt>> isTemplate() {
        return new Predicate<Node<IProductCmpt>>() {
            @Override
            public boolean apply(Node<IProductCmpt> node) {
                return node != null && node.getElement().isProductTemplate();
            }
        };
    }

    /** Returns a comparator to compare the value of property values. */
    private Comparator<Object> valueComparator() {
        return getInitialPropertyValue().getPropertyValueType().getValueComparator();
    }

    /**
     * Returns a function to obtain the value of a property value
     */
    private Function<IPropertyValue, Object> valueFunction() {
        return getInitialPropertyValue().getPropertyValueType().getValueFunction();
    }

    @Override
    protected void partHasChanged() {
        // reset state to force update
        propertyValuesBasedOnTemplate = null;
        histogram = null;
    }

}
