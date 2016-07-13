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

import java.util.List;

import org.faktorips.runtime.IModelObject;

public interface IPolicyAssociationModel extends IModelTypeAssociation {

    /**
     * Returns the model type this association belongs to.
     */
    @Override
    public IPolicyModel getModelType();

    /**
     * Returns the target model type object of this association.
     * 
     */
    @Override
    public IPolicyModel getTarget();

    /**
     * Returns a list of the target(s) of the given model object's association identified by this
     * model type association.
     * 
     * @param source a model object corresponding to the {@link IPolicyModel} this association
     *            belongs to
     * @return a list of the target(s) of the given model object's association identified by this
     *         model type association
     * @throws IllegalArgumentException if the model object does not have an association fitting
     *             this model type association or that association is not accessible for any reason
     */
    public List<IModelObject> getTargetObjects(IModelObject source);

    /**
     * Returns the {@link IModelType} identified by {@link #getMatchingAssociationSource()}
     * 
     * @see #getMatchingAssociationSource()
     * 
     * @return The model type object of the matching association source
     */
    @Override
    public IProductModel getMatchingAssociationSourceType();

}
