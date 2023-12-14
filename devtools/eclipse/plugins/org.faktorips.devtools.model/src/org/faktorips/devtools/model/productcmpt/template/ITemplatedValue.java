/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt.template;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.model.type.ProductCmptType;

/**
 * An interface for product component properties ({@link IPropertyValue} or {@link IProductCmptLink}
 * ) that could be configured using templates.
 */
public interface ITemplatedValue extends IIpsObjectPart {

    /** The name of the template value status property. */
    String PROPERTY_TEMPLATE_VALUE_STATUS = "templateValueStatus"; //$NON-NLS-1$

    /** Validation message code to indicate that this property's template status is invalid. */
    String MSGCODE_INVALID_TEMPLATE_VALUE_STATUS = "TEMPLATEDPROPERTY-InvalidTemplateValueStatus"; //$NON-NLS-1$

    /**
     * Sets this property's template status (e.g. whether it is inherited from a parent template or
     * not).
     *
     * @param status the new template status
     */
    void setTemplateValueStatus(TemplateValueStatus status);

    /**
     * Returns the current template status of this property. It specifies whether a property is is
     * defined in this object or inherited from a template.
     *
     * @return this property's template status (e.g. whether it is inherited from a parent template
     *             or not).
     *
     * @see TemplateValueStatus
     */
    TemplateValueStatus getTemplateValueStatus();

    /**
     * Sets the next valid template value status. The template value status order is defined in
     * {@link TemplateValueStatus}.
     *
     */
    void switchTemplateValueStatus();

    /**
     * Finds the property in the template hierarchy (parent or grand*-parent template) that has the
     * status {@link TemplateValueStatus#DEFINED} and thus is used as a template property for this
     * property.
     *
     * If there is no template or no parent template defines such a property, this method returns
     * <code>null</code>.
     *
     * Note: This method does <em>not</em> find the property that provides the actual value. Instead
     * it finds the closest template property. E.g. in case this property overrides a property from
     * its template, this method still finds the template property (even though the property is
     * overridden).
     *
     * @param ipsProject The {@link IIpsProject} used to search the template hierarchy
     * @return the property that should be used as template or <code>null</code> if there is no such
     *             property.
     */
    ITemplatedValue findTemplateProperty(IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if somewhere in the template hierarchy (parent or grand*-parent
     * template) there is a template that knows about this property (because it configures a
     * {@link IProductCmptType} that includes this property).
     *
     * If the corresponding container uses no template or the templates are only configuring
     * parent-product-types that don't include the property, <code>false</code> is returned.
     *
     * @return <code>true</code> if the corresponding container uses a template that could define a
     *             value for this property.
     */
    boolean hasTemplateForProperty(IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if this property is part of a template hierarchy, by either acting
     * as a template value, overwriting a value from a template or defining a new value. This is the
     * case if its container uses a template or if the container itself is a product template.
     * Accordingly the property has a template value status (or can "configure" its status).
     *
     * If the property's parent is a regular product component that does not use templates, the
     * template value status should always be {@link TemplateValueStatus#DEFINED}. In that case this
     * method returns <code>false</code>.
     *
     * If the property's parent is using a template, the property is only considered part of that
     * template hierarchy if the {@link ProductCmptType} configured by the template also includes
     * the property (a template could configure a super-{@link ProductCmptType} and the property
     * could be added in the sub-{@link ProductCmptType} this property's parent configures).
     *
     * @return <code>true</code> if the corresponding container is using a template or if itself is
     *             a template.
     */
    boolean isPartOfTemplateHierarchy();

    /**
     * Returns the {@link ITemplatedValueContainer} which is the parent of this object
     *
     * @return the container this object belongs to
     */
    ITemplatedValueContainer getTemplatedValueContainer();

    Comparator<Object> getValueComparator();

    BiConsumer<? extends ITemplatedValue, Object> getValueSetter();

    Function<? extends ITemplatedValue, Object> getValueGetter();

    /**
     * Returns the getter for the internal value, independent of the template settings.
     *
     * @since 24.1.1
     */
    Function<? extends ITemplatedValue, Object> getInternalValueGetter();

    ITemplatedValueIdentifier getIdentifier();

    /**
     * A concrete {@link ITemplatedValue} has its value used as it is. Whether a value is concrete
     * depends on its template value status and the kind of property.
     * <ul>
     * <li>Defined values are concrete values, naturally.</li>
     * <li>Inherited values are never concrete values, as their actual value is never used, the
     * corresponding value from a template is used instead.</li>
     * <li>For undefined values the result depends on the kind of templated property:
     * <ul>
     * <li>For PropertyValues undefined means, well, undefined, and thus not concrete.</li>
     * <li>For links however, undefined means something like "deleted". Deleted (or cardinality 0
     * for that matter) is effectively used, and thus is viewed as a concrete value.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @return whether this value defines a concrete value.
     */
    boolean isConcreteValue();

}
