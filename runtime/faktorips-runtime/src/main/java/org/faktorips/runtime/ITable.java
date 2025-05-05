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

import java.util.List;
import java.util.Locale;

import org.faktorips.values.InternationalString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Base interface for table contents
 *
 * @param <R> the class representing a row of this table
 */
public interface ITable<R> {

    String TAG_NAME = "TableContents";

    /**
     * Returns the qualified name of this table.
     */
    String getName();

    /**
     * Returns all rows of this table.
     */
    List<R> getAllRows();

    /**
     * Creates an XML {@link Element} that represents this table's data.
     * <p>
     * Throws an {@link UnsupportedOperationException} if the support for toXml ("Generate toXml
     * Support") is not activated in the Faktor-IPS standard builder.
     *
     * @param document a document, that can be used to create XML elements.
     */
    Element toXml(Document document);

    /**
     * Sets or updates the description for the given {@link Locale}.
     * <p>
     * If the description for the locale already exists, it will be replaced. If no description
     * exists, a new localized description entry will be added. If the table belongs to a read-only
     * {@link IRuntimeRepository}, this method will throw an
     * {@link IllegalRepositoryModificationException}.
     *
     * @param locale the locale for which the description is to be set (must not be {@code null})
     * @param newDescriptionText the new description text for the given locale (must not be
     *            {@code null})
     * @param repository the runtime repository containing the table (must not be {@code null})
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws IllegalRepositoryModificationException if the table is part of a read-only repository
     *
     * @since 25.7
     */
    void setDescription(@NonNull Locale locale,
            @NonNull String newDescriptionText,
            @NonNull IRuntimeRepository repository);

    /**
     * Replaces the current multi-language description with the given {@link InternationalString}.
     * <p>
     * This method will overwrite all existing localized descriptions. If the table belongs to a
     * read-only {@link IRuntimeRepository}, this method will throw an
     * {@link IllegalRepositoryModificationException}.
     *
     * @param newDescription the new internationalized description to set (must not be {@code null})
     * @param repository the runtime repository containing the table (must not be {@code null})
     *
     * @throws NullPointerException if {@code newDescription} is {@code null}
     * @throws IllegalRepositoryModificationException if the table is part of a read-only repository
     *
     * @since 25.7
     */
    void setDescription(@NonNull InternationalString newDescription, @NonNull IRuntimeRepository repository);
}
