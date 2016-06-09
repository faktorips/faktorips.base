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
public interface IProductModel extends IModelType {

    /**
     * Returns whether this product component type is changing over time.
     * 
     * @return <code>true</code> if it has generations else <code>false</code>
     */
    public boolean isChangingOverTime();

    /**
     * Returns whether this product component type is a configuration for a policy component type.
     * 
     * @return <code>true</code> if this type configures a policy component type, <code>false</code>
     *         if not
     */
    public boolean isConfigurationForPolicyCmptType();

    /**
     * Returns the model type of the policy component type for which is configured by this type. If
     * this product component class has no configuration it throws a {@link NullPointerException}.
     * 
     * @see #isConfigurationForPolicyCmptType()
     * 
     * @return The model type of the configured policy component type
     * @throws NullPointerException if the product component type is not a configuration for a
     *             policy component type
     */
    public IPolicyModel getPolicyCmptType();

    @Override
    public IProductModel getSuperType();

}