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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.util.Histogram;
import org.faktorips.devtools.core.util.Tree;
import org.faktorips.devtools.core.util.Tree.Node;

public class TemplatePropertyUsagePmo extends IpsObjectPartPmo {

    private final IProductCmpt template;
    private final GregorianCalendar effectiveDate;

    // lazily loaded
    private List<IProductCmpt> productCmptsBasedOnTemplate;

    public TemplatePropertyUsagePmo(IPropertyValue propertyValue, GregorianCalendar effectiveDate) {
        super(propertyValue);
        this.template = findTemplate(propertyValue);
        this.effectiveDate = effectiveDate;
    }

    /** Returns the product components that inherit the value from the template. */
    public Collection<IProductCmpt> getInheritingProductCmpts() {
        return Collections2
                .filter(getProductCmptsBasedOnTemplate(), propertyValueStatus(TemplateValueStatus.INHERITED));
    }

    /** Returns all product components that define a custom value. */
    public Collection<IProductCmpt> getDefiningProductCmpts() {
        return Collections2.filter(getProductCmptsBasedOnTemplate(), propertyValueStatus(TemplateValueStatus.DEFINED));
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
    public Histogram<Object, IProductCmpt> getDefinedValuesHistogram() {
        Collection<IProductCmpt> definingProductCmpts = getDefiningProductCmpts();
        return new Histogram<Object, IProductCmpt>(valueFunction(), valueComparator(), definingProductCmpts);
    }

    /** Returns the property value with which this PMO was initialized. */
    private IPropertyValue getInitialPropertyValue() {
        IPropertyValue propertyValue = (IPropertyValue)getIpsObjectPartContainer();
        return propertyValue;
    }

    /** Returns the product components that reference this PMO's template. */
    private List<IProductCmpt> getProductCmptsBasedOnTemplate() {
        if (productCmptsBasedOnTemplate == null) {
            productCmptsBasedOnTemplate = findProductCmptsBasedOnTemplate(template);
        }
        return productCmptsBasedOnTemplate;
    }

    @Override
    protected void partHasChanged() {
        // reset state to force update
        productCmptsBasedOnTemplate = null;
    }

    /**
     * Finds the template of the property value container to which the given property value belongs.
     * Returns <code>null</code> if the property value does not have a template value.
     */
    private IProductCmpt findTemplate(IPropertyValue p) {
        if (isDefinedTemplateProperty(p)) {
            return (IProductCmpt)p.getIpsObject();
        }
        IPropertyValue templateValue = p.findTemplateProperty(p.getIpsProject());
        if (templateValue == null) {
            return null;
        } else {
            return templateValue.getPropertyValueContainer().getProductCmpt();
        }
    }

    private boolean isDefinedTemplateProperty(IPropertyValue p) {
        return p.getIpsObject().getIpsObjectType() == IpsObjectType.PRODUCT_TEMPLATE
                && p.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    /**
     * Find product components that are based on the given template. This list includes
     * <ul>
     * <li>product components that use the given template</li>
     * <li>product templates that use the given template and define a value (i.e. do not inherit the
     * value from the given template)</li>
     * <li>product components and templates that use templates inheriting their values from the
     * given template</li>
     * </ul>
     */
    private List<IProductCmpt> findProductCmptsBasedOnTemplate(IProductCmpt template) {

        Tree<IIpsSrcFile> srcFileHierarchy = getIpsProject().findTemplateHierarchy(template);
        if (srcFileHierarchy.isEmpty()) {
            return Collections.emptyList();
        }

        Tree<IProductCmpt> productCmptHierarchy = srcFileHierarchy.transform(srcFileToProductCmpt());

        return findProductCmptsBasedOnTemplate(productCmptHierarchy.getRoot());
    }

    private List<IProductCmpt> findProductCmptsBasedOnTemplate(Node<IProductCmpt> node) {
        List<IProductCmpt> result = Lists.newArrayList();
        result.addAll(Lists.transform(getProductNodes(node), nodeToProductCmpt()));

        List<Node<IProductCmpt>> templateNodes = getTemplateNodes(node);
        for (Node<IProductCmpt> templateNode : templateNodes) {
            IProductCmpt t = templateNode.getElement();
            if (definesValue(t)) {
                // Include t as it defines a custom value. Product components using t do not have to
                // be included as their value depends on t and not this PMO's template.
                result.add(t);
            } else {
                // If t does not define a value all product components using t should be included as
                // their values actually depend on this PMO's template.
                result.addAll(findProductCmptsBasedOnTemplate(templateNode));
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
    private IProductCmptProperty getProperty() {
        try {
            IPropertyValue propertyValue = getInitialPropertyValue();
            return propertyValue.findProperty(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns whether or not the given product component defines a custom values (i.e. does not
     * inherit the value from its template).
     */
    private boolean definesValue(IProductCmpt p) {
        IPropertyValue value = findPropertyValue(p);
        return value != null && value.getTemplateValueStatus() == TemplateValueStatus.DEFINED;
    }

    /**
     * Returns the value for this PMO's property from the given product component (or its
     * generation).
     */
    private IPropertyValue findPropertyValue(IProductCmpt productCmpt) {
        IProductCmptProperty property = getProperty();
        IPropertyValue propertyValue = productCmpt.getPropertyValue(property);
        if (propertyValue != null) {
            return propertyValue;
        }

        IProductCmptGeneration gen = productCmpt.getGenerationEffectiveOn(effectiveDate);
        return gen.getPropertyValue(property);
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

    /** Function to transform a node to the IProductCmpt enclosed in it. */
    private Function<Node<IProductCmpt>, IProductCmpt> nodeToProductCmpt() {
        return new Function<Node<IProductCmpt>, IProductCmpt>() {

            @Override
            public IProductCmpt apply(Node<IProductCmpt> node) {
                // FindBugs does not like Preconditions.checkState...
                if (node == null) {
                    throw new IllegalStateException();
                }
                return node.getElement();
            }
        };
    }

    /**
     * Predicate that matches an IProductCmpt whose property value (for this PMO's property) has the
     * given TemplateValueStatus.
     */
    private Predicate<IProductCmpt> propertyValueStatus(final TemplateValueStatus t) {
        return new Predicate<IProductCmpt>() {

            @Override
            public boolean apply(IProductCmpt productCmpt) {
                if (productCmpt == null) {
                    return false;
                }
                IPropertyValue propertyValue = findPropertyValue(productCmpt);
                return propertyValue.getTemplateValueStatus() == t;
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
     * Returns a function to obtain the value of a product components property value (for this PMO's
     * property).
     */
    private Function<IProductCmpt, Object> valueFunction() {
        final Function<IPropertyValue, Object> valueFunction = getInitialPropertyValue().getPropertyValueType()
                .getValueFunction();
        return new Function<IProductCmpt, Object>() {

            @Override
            public Object apply(IProductCmpt p) {
                IPropertyValue propertyValue = findPropertyValue(p);
                return valueFunction.apply(propertyValue);
            }
        };
    }

}
