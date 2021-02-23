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

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ITableNamingStrategy;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Strategy for deriving table names from Strings.
 * <p>
 * This strategy converts CamelCased Strings to uppercase with an underscore in between where a
 * lowercase character is followed by an uppercased character.
 * <p>
 * Example:<br>
 * CamelCasedIdentifier42 &rarr; CAMEL_CASED_IDENTIFIER42
 * 
 * @author Roman Grutza
 */
public class CamelCaseToUpperUnderscoreTableNamingStrategy implements ITableNamingStrategy {

    public static final String EXTENSION_ID = "org.faktorips.devtools.model.CamelCaseToUpperUnderscoreTableNamingStrategy"; //$NON-NLS-1$

    private IIpsProject ipsProject;

    @Override
    public String getTableName(String baseName) {
        if (baseName == null) {
            throw new NullPointerException("Cannot derive a table identifier from null."); //$NON-NLS-1$
        }
        return toUnderscoreUppercase(baseName);
    }

    @Override
    public String getTableName(String baseName, int maxLength) {
        if (baseName == null) {
            throw new NullPointerException("Cannot derive a table identifier from null."); //$NON-NLS-1$
        }
        if (maxLength <= 0) {
            throw new RuntimeException("Maximum length must be positive."); //$NON-NLS-1$
        }
        String derivedName = toUnderscoreUppercase(baseName);
        return derivedName.substring(0, Math.min(derivedName.length(), maxLength));
    }

    @Override
    public void setIpsProject(IIpsProject project) {
        if (project == null) {
            throw new NullPointerException();
        }
        ipsProject = project;
    }

    @Override
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public void initFromXml(Element el) {
        // nothing to do, this strategy has no sub-elements
    }

    @Override
    public Element toXml(Document doc) {
        Element strategyEl = doc.createElement(ITableNamingStrategy.XML_TAG_NAME);
        strategyEl.setAttribute("id", EXTENSION_ID); //$NON-NLS-1$
        return strategyEl;
    }

    @Override
    public void validate(String name) {
        // nothing to do, this strategy has no state
    }

    private String toUnderscoreUppercase(String baseName) {
        String underscoredName = StringUtil.camelCaseToUnderscore(baseName, false);
        return underscoredName.toUpperCase();
    }

}
