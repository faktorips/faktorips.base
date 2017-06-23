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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.faktorips.runtime.internal.DateTime;

/**
 * Interface for all product components.
 * 
 * @author Jan Ortmann
 */
public interface IProductComponent extends IRuntimeObject, IProductComponentLinkSource {

    /**
     * Returns the repository this product component belongs to. This method never returns
     * <code>null</code>.
     */
    @Override
    public IRuntimeRepository getRepository();

    /**
     * Returns the product component's id that uniquely identifies it in the repository it belongs
     * to.
     */
    public String getId();

    /**
     * Returns the id of the product component kind that this product component belongs to.
     */
    public String getKindId();

    /**
     * Returns the version id that identifies this product component in its kind.
     */
    public String getVersionId();

    /**
     * @return whether this is a variant of another {@link IProductComponent}
     */
    public boolean isVariant();

    /**
     * @return the {@link IProductComponent} this {@link IProductComponent} is based on or
     *         {@code null} if it is not a variant or the {@link IProductComponent variedBase} can
     *         not be found in the {@link IRuntimeRepository}.
     * 
     * @see #isVariant()
     * @see IRuntimeRepository#getProductComponent(String)
     */
    public IProductComponent getVariedBase();

    /**
     * Returns the date from which this product component is valid. If this product component
     * supports generations this is the same valid from date as the first generation.
     * 
     * @return The valid from date of this product component
     */
    public DateTime getValidFrom();

    /**
     * Returns the date from which this product component is valid as a {@link Date}. If this
     * product component supports generations this is the same valid from date as the first
     * generation.
     * 
     * @param timeZone The time zone which is used to calculate the returned valid from date.
     * @return The valid from date of this product component
     */
    public Date getValidFrom(TimeZone timeZone);

    /**
     * Returns the date when this product component expires. Returning <code>null</code> means no
     * end of the validity period.
     */
    public DateTime getValidTo();

    /**
     * Returns the generation that is effective on the given date or <code>null</code> if no
     * generation is effective on that date.
     * 
     * @throws UnsupportedOperationException if this product component has no product component
     *             generations.
     * @throws NullPointerException if effective date is <code>null</code>.
     */
    public IProductComponentGeneration getGenerationBase(Calendar effectiveDate);

    /**
     * Returns the latest product component generation of the provided product component or
     * <code>null</code> if non available.
     * 
     * @throws UnsupportedOperationException if this product component has no product component
     *             generations.
     */
    public IProductComponentGeneration getLatestProductComponentGeneration();

    /**
     * Creates a new policy component that is configured by this product component.
     */
    public IConfigurableModelObject createPolicyComponent();

    /**
     * Returns the <code>IProductComponentLink</code> for the association with the given role name
     * to the given product component or <code>null</code> if no such association exists.
     * 
     * @since 3.8
     */
    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target);

    /**
     * Returns a <code>List</code> of all the <code>IProductComponentLink</code>s from this product
     * component generation to other product components.
     * 
     * @since 3.8
     */
    public List<IProductComponentLink<? extends IProductComponent>> getLinks();

    /**
     * Returns <code>true</code> if this product component has {@link IProductComponentGeneration
     * product component generations}.
     * 
     * @since 3.15
     */
    public boolean isChangingOverTime();

    /**
     * Returns the description for this product component in the specified locale. If there is no
     * description in the specified locale, it tries to find the description in the locale's
     * language. If there is also no description in the locale's language it returns the empty
     * string.
     * 
     * @return the description for the given locale/language or an empty string if no description
     *         exists for the given locale
     */
    public String getDescription(Locale locale);

}
