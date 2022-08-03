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

/**
 * Base interface for all model objects that are configurable by a product component with the
 * ability to change over time via product component generations.
 */
public interface ITimedConfigurableModelObject extends IConfigurableModelObject {

    /**
     * The name of the property 'productCmptGeneration'.
     */
    String PROPERTY_PRODUCT_CMPT_GENERATION = "productCmptGeneration";

    /**
     * Returns the product component generation this policy component is based on.
     */
    IProductComponentGeneration getProductCmptGeneration();

}
