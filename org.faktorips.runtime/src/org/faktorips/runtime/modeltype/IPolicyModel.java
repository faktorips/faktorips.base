/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype;

/**
 * This interface extends {@link IModelType} with policy specific information.
 */
public interface IPolicyModel extends IModelType {

    /**
     * Returns whether this policy component type is configured by a product component type. If this
     * method returns <code>true</code> you could use {@link #getProductCmptType()} to get the model
     * type of the configuring product.
     * 
     * @return <code>true</code> if this policy component type is configured else <code>false</code>
     */
    public boolean isConfiguredByPolicyCmptType();

    /**
     * Returns the {@link IProductModel} that configures this policy component type. Throws an
     * {@link IllegalArgumentException} if this policy component type is not configured. Use
     * {@link #isConfiguredByPolicyCmptType()} to check whether it is configured or not.
     * 
     * @return the {@link IProductModel} that configures this policy component model type
     * @throws NullPointerException if this policy component type is not configured
     * 
     */
    public IProductModel getProductCmptType();

    @Override
    public IPolicyModel getSuperType();

}