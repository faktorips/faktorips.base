/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import org.faktorips.devtools.model.ipsproject.ITableColumnNamingStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Strategy for deriving table column names from Strings.
 * <p>
 * This strategy converts CamelCased Strings to uppercase with an underscore in between where a
 * lowercase character is followed by an uppercased character.
 * <p>
 * Example:<br>
 * CamelCasedIdentifier42 &rarr; CAMEL_CASED_IDENTIFIER42
 * 
 * @author Roman Grutza
 */
public class CamelCaseToUpperUnderscoreColumnNamingStrategy extends CamelCaseToUpperUnderscoreTableNamingStrategy
        implements ITableColumnNamingStrategy {

    @SuppressWarnings("hiding")
    public static final String EXTENSION_ID = "org.faktorips.devtools.model.CamelCaseToUpperUnderscoreColumnNamingStrategy"; //$NON-NLS-1$

    @Override
    public String getTableColumnName(String baseName) {
        return getTableName(baseName);
    }

    @Override
    public String getTableColumnName(String baseName, int maxLength) {
        return getTableName(baseName, maxLength);
    }

    @Override
    public Element toXml(Document doc) {
        Element strategyEl = doc.createElement(ITableColumnNamingStrategy.XML_TAG_NAME);
        strategyEl.setAttribute("id", EXTENSION_ID); //$NON-NLS-1$
        return strategyEl;
    }

}
