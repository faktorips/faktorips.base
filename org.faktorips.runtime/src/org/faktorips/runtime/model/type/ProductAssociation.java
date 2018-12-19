/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.type;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;

public class ProductAssociation extends Association {

    private boolean changingOverTime;

    public ProductAssociation(Type type, Method getterMethod, boolean changingOverTime) {
        super(type, getterMethod);
        this.changingOverTime = changingOverTime;
    }

    @Override
    public ProductAssociation createOverwritingAssociationFor(Type subType) {
        return new ProductAssociation(subType, getGetterMethod(), changingOverTime);
    }

    /**
     * Returns the {@link ProductCmptType} this association belongs to.
     */
    @Override
    public ProductCmptType getType() {
        return (ProductCmptType)super.getType();
    }

    /**
     * Returns the model type this association belongs to.
     * 
     * @deprecated Use {@link #getType()}
     */
    @Deprecated
    @Override
    public ProductCmptType getModelType() {
        return getType();
    }

    /**
     * Returns the target type of this association.
     * 
     */
    @Override
    public ProductCmptType getTarget() {
        return (ProductCmptType)super.getTarget();
    }

    /**
     * Returns a list of the target(s) of the given product component's association identified by
     * this model type association. If this association is changing over time (resides in the
     * generation) the date is used to retrieve the correct generation. If the date is
     * <code>null</code> the latest generation is used. If the association is not changing over time
     * the date will be ignored.
     * 
     * @param productComponentSource a product object corresponding to the {@link ProductCmptType}
     *            this association belongs to
     * @param effectiveDate The date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * @return a list of the target(s) of the given model object's association identified by this
     *         model type association
     * @throws IllegalArgumentException if the model object does not have an association fitting
     *             this model type association or that association is not accessible for any reason
     */
    public List<IProductComponent> getTargetObjects(IProductComponent productComponentSource, Calendar effectiveDate) {
        List<IProductComponent> targets = new ArrayList<IProductComponent>();
        Object source = getRelevantProductObject(productComponentSource, effectiveDate, isChangingOverTime());
        Object returnValue = invokeMethod(getGetterMethod(), source);
        if (returnValue instanceof Iterable<?>) {
            for (Object target : (Iterable<?>)returnValue) {
                targets.add((IProductComponent)target);
            }
        } else if (returnValue instanceof IProductComponent) {
            targets.add((IProductComponent)returnValue);
        }
        return targets;
    }

    /**
     * Returns the {@link PolicyCmptType} identified by {@link #getMatchingAssociationSource()}
     * 
     * @see #getMatchingAssociationSource()
     * 
     * @return The policy component type of the matching association source
     */
    @Override
    public PolicyCmptType getMatchingAssociationSourceType() {
        return (PolicyCmptType)super.getMatchingAssociationSourceType();
    }

    /**
     * Checks whether this association is changing over time (resides in the generation) or not
     * (resides in the product component).
     * 
     * @return <code>true</code> if this association is changing over time, else <code>false</code>
     */
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

}
