/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
