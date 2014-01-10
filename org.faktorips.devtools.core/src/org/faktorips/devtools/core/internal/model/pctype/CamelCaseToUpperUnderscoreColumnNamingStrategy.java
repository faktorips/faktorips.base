/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Strategy for deriving table column names from Strings.
 * <p>
 * This strategy converts CamelCased Strings to uppercase with an underscore in between where a
 * lowercase character is followed by an uppercased character.
 * <p>
 * Example:<br />
 * CamelCasedIdentifier42 -> CAMEL_CASED_IDENTIFIER42
 * 
 * @author Roman Grutza
 */
public class CamelCaseToUpperUnderscoreColumnNamingStrategy extends CamelCaseToUpperUnderscoreTableNamingStrategy
        implements ITableColumnNamingStrategy {

    public final static String EXTENSION_ID = "org.faktorips.devtools.core.CamelCaseToUpperUnderscoreColumnNamingStrategy"; //$NON-NLS-1$

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
