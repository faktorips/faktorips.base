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

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.faktorips.runtime.internal.ProductComponent;

/**
 * A product component generation represents the state of a product component valid for a period of
 * time. The period's begins is defined by the generation's valid from date. The period ends at the
 * next generation's valid from date. A product component's generation periods are none overlapping.
 * For a given point in time exactly one (or none) generation is found.
 * 
 * @author Jan Ortmann
 */
public interface IProductComponentGeneration extends IRuntimeObject, IProductComponentLinkSource {

    /**
     * Creates a new policy component that is configured by this product component generation. After
     * creating the policy component it is automatically initialized. The new policy component is
     * not added to any parent structure.
     * <p>
     * 
     * @throws RuntimeException if this product component does not configure a policy component.
     */
    public IConfigurableModelObject createPolicyComponent();

    /**
     * Returns the repository this product component generation belongs to. This method never
     * returns <code>null</code>.
     */
    public IRuntimeRepository getRepository();

    /**
     * Returns the product component this generation belongs to. This method never returns
     * <code>null</code>.
     */
    IProductComponent getProductComponent();

    /**
     * Returns the previous generation if available if not <code>null</code> will be returned.
     */
    public IProductComponentGeneration getPreviousGeneration();

    /**
     * Returns the next generation if available if not <code>null</code> will be returned.
     */
    public IProductComponentGeneration getNextGeneration();

    /**
     * Returns the point in time this generation is valid from in the given time zone. This method
     * never returns <code>null</code>.
     * 
     * @throws NullPointerException if zone is <code>null</code>.
     */
    Date getValidFrom(TimeZone zone);

    /**
     * Returns the <code>IProductComponentLink</code> for the association with the given role name
     * to the given product component or <code>null</code> if no such association exists.
     */
    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target);

    /**
     * Returns a <code>List</code> of all the <code>IProductComponentLink</code>s from this product
     * component generation to other product components.
     */
    public List<IProductComponentLink<? extends IProductComponent>> getLinks();

    /**
     * Returns whether the validation rule with the given name is configured as active in this
     * {@link ProductComponent}.
     * 
     * @param ruleName the name of the rule in question
     * @return <code>true</code> if the rule was activated, <code>false</code> else.
     */
    public boolean isValidationRuleActivated(String ruleName);

}
