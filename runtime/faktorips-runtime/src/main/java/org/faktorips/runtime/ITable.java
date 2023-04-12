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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
}
