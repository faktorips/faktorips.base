/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
     * Returns the product component this policy component is based on.
     */
    public IProductComponent getProductComponent();

    /**
     * Sets the current product component.
     */
    public void setProductComponent(IProductComponent productComponent);

    /**
     * Returns the date since when this model object is effective.
     */
    public Calendar getEffectiveFromAsCalendar();

    /**
     * Initializes the model object with the defaults from it's product component generation.
     */
    public void initialize();

}
