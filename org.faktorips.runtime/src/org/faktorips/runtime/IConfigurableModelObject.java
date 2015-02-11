/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.Calendar;

/**
 * Base interface for all model objects that are configurable by a product component.
 * 
 * @author Jan Ortmann
 */
public interface IConfigurableModelObject extends IModelObject {

    /**
     * The name of the property 'productComponent'.
     */
    public static final String PROPERTY_PRODUCT_COMPONENT = "productComponent";

    /**
     * The name of the property 'productCmptGeneration'.
     * 
     * @deprecated since 3.15, this interface describes configurable model objects that are not
     *             changing over time, meaning that they do not support generations. On the other
     *             hand, model objects that support this concept are now represented by the new
     *             interface {@link ITimedConfigurableModelObject}. Use
     *             {@link ITimedConfigurableModelObject#PROPERTY_PRODUCT_CMPT_GENERATION} instead.
     */
    @Deprecated
    public static final String PROPERTY_PRODUCT_CMPT_GENERATION = "productCmptGeneration";

    /**
     * Returns the product component this policy component is based on.
     */
    public IProductComponent getProductComponent();

    /**
     * Returns the product component generation this policy component is based on.
     * 
     * @deprecated since 3.15, this interface describes configurable model objects that are not
     *             changing over time, meaning that they do not support generations. On the other
     *             hand, model objects that support this concept are now represented by the new
     *             interface {@link ITimedConfigurableModelObject}. Calling this operation on an
     *             object that does not implement {@link ITimedConfigurableModelObject} will result
     *             in an {@link UnsupportedOperationException}.
     * 
     * @throws UnsupportedOperationException if called upon an object that does not implement
     *             {@link ITimedConfigurableModelObject}
     */
    @Deprecated
    public IProductComponentGeneration getProductCmptGeneration();

    /**
     * Returns the date since when this model object is effective.
     */
    public Calendar getEffectiveFromAsCalendar();

    /**
     * Initializes the model object with the defaults from it's product component generation.
     */
    public void initialize();

}
