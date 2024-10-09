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
import java.util.Locale;

import org.faktorips.runtime.internal.DateTime;

/**
 * Interface for all product components.
 *
 * @author Jan Ortmann
 */
public interface IProductComponent extends IRuntimeObject, IProductObject {

    String PROPERTY_VALID_TO = "validTo";

    /**
     * Returns the repository this product component belongs to. This method never returns
     * <code>null</code>.
     */
    @Override
    IRuntimeRepository getRepository();

    /**
     * Returns the product component's id that uniquely identifies it in the repository it belongs
     * to.
     */
    String getId();

    /**
     * Returns the id of the product component kind that this product component belongs to.
     */
    String getKindId();

    /**
     * Returns the version id that identifies this product component in its kind.
     */
    String getVersionId();

    /**
     * A variant is a product component that is based on another product component (hence varied
     * base). It uses the varied base as a template and "changes" specific values.
     *
     * @return <code>true</code> if this is a variant of another {@link IProductComponent},
     *             <code>false</code> for regular product components.
     *
     * @see #getVariedBase()
     */
    boolean isVariant();

    /**
     * Allows distinguishing variants not only by their product component type but also their varied
     * base. {@link #isVariant()} allows determining whether a product component is a variant.
     *
     * @return the {@link IProductComponent} this {@link IProductComponent} is based on. Returns
     *             {@code null} if it is a regular product component or the {@link IProductComponent
     *             variedBase} can not be found in the {@link IRuntimeRepository}.
     *
     * @see #isVariant()
     * @see IRuntimeRepository#getProductComponent(String)
     */
    IProductComponent getVariedBase();

    /**
     * Returns the date when this product component expires. Returning <code>null</code> means no
     * end of the validity period.
     */
    DateTime getValidTo();

    /**
     * Returns the generation that is effective on the given date or <code>null</code> if no
     * generation is effective on that date.
     *
     * @throws UnsupportedOperationException if this product component has no product component
     *             generations.
     * @throws NullPointerException if effective date is <code>null</code>.
     */
    IProductComponentGeneration getGenerationBase(Calendar effectiveDate);

    /**
     * Returns the latest product component generation of the provided product component or
     * <code>null</code> if non available.
     *
     * @throws UnsupportedOperationException if this product component has no product component
     *             generations.
     */
    IProductComponentGeneration getLatestProductComponentGeneration();

    /**
     * Returns <code>true</code> if this product component has {@link IProductComponentGeneration
     * product component generations}.
     *
     * @since 3.15
     */
    boolean isChangingOverTime();

    /**
     * Returns the description for this product component in the specified locale. If there is no
     * description in the specified locale, it tries to find the description in the locale's
     * language. If there is also no description in the locale's language it returns the empty
     * string.
     *
     * @return the description for the given locale/language or an empty string if no description
     *             exists for the given locale
     */
    String getDescription(Locale locale);

    /**
     * Returns the qualified name of this product component. The qualified name is the name of the
     * file this product component is read from, relative to the repository's root, if the
     * repository uses files - otherwise the value's definition depends on the repository
     * implementation.
     * <p>
     * The qualified name may be {@code null} for newly created product components.
     *
     * @return the qualified name of this product component
     *
     * @since 24.1
     */
    default String getQualifiedName() {
        // fallback for old repository implementations and mocks
        return getId();
    }

}
