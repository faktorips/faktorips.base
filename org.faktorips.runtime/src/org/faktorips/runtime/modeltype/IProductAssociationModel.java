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

import java.util.Calendar;
import java.util.List;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;

/**
 * 
 * @author Daniel Hohenberger
 */
public interface IProductAssociationModel extends IModelTypeAssociation {

    /**
     * Returns the model type this association belongs to.
     */
    @Override
    public IProductModel getModelType();

    /**
     * Returns the target model type object of this association.
     * 
     */
    @Override
    public IProductModel getTarget();

    /**
     * Returns a list of the target(s) of the given product component's association identified by
     * this model type association. If this association is changing over time (resides in the
     * generation) the date is used to retrieve the correct generation. If the date is
     * <code>null</code> the latest generation is used. If the association is not changing over time
     * the date will be ignored.
     * 
     * @param productComponentSource a product object corresponding to the {@link IProductModel}
     *            this association belongs to
     * @param effectiveDate The date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * @return a list of the target(s) of the given model object's association identified by this
     *         model type association
     * @throws IllegalArgumentException if the model object does not have an association fitting
     *             this model type association or that association is not accessible for any reason
     */
    public List<IProductComponent> getTargetObjects(IProductComponent productComponentSource, Calendar effectiveDate);

    /**
     * Returns the {@link IModelType} identified by {@link #getMatchingAssociationSource()}
     * 
     * @see #getMatchingAssociationSource()
     * 
     * @return The model type object of the matching association source
     */
    @Override
    public IPolicyModel getMatchingAssociationSourceType();

    /**
     * Checks whether this association is changing over time (resides in the generation) or not
     * (resides in the product component).
     * 
     * @return <code>true</code> if this association is changing over time, else <code>false</code>
     */
    boolean isChangingOverTime();

}
