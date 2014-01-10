/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    public final static String PROPERTY_PRODUCT_COMPONENT = "productComponent";

    /**
     * The name of the property 'productCmptGeneration'.
     */
    public final static String PROPERTY_PRODUCT_CMPT_GENERATION = "productCmptGeneration";

    /**
     * Returns the product component this policy component is based on.
     */
    public IProductComponent getProductComponent();

    /**
     * Returns the product component generation this policy component is based on.
     */
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
