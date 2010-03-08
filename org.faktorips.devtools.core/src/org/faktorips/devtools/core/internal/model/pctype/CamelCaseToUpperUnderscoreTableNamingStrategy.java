/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Strategy for deriving table names from Strings.
 * <p/>
 * This strategy converts CamelCased Strings to uppercase with an underscore in between where a
 * lowercase character is followed by an uppercased character.
 * <p/>
 * Example:<br/>
 * CamelCasedIdentifier42 -> CAMEL_CASED_IDENTIFIER42
 * 
 * @author Roman Grutza
 */
public class CamelCaseToUpperUnderscoreTableNamingStrategy implements ITableNamingStrategy {

    public final static String EXTENSION_ID = "org.faktorips.devtools.core.CamelCaseToUpperUnderscoreTableNamingStrategy";

    private IIpsProject ipsProject;

    public String getTableName(String baseName) {
        if (baseName == null) {
            throw new NullPointerException("Cannot derive a table identifier from null.");
        }
        return toUnderscoreUppercase(baseName);
    }

    public String getTableName(String baseName, int maxLength) {
        if (baseName == null) {
            throw new NullPointerException("Cannot derive a table identifier from null.");
        }
        if (maxLength <= 0) {
            throw new RuntimeException("Maximum length must be positive.");
        }
        String derivedName = toUnderscoreUppercase(baseName);
        return derivedName.substring(0, Math.min(derivedName.length(), maxLength));
    }

    public void setIpsProject(IIpsProject project) {
        if (project == null) {
            throw new NullPointerException();
        }
        ipsProject = project;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public void initFromXml(Element el) {
        // nothing to do, this strategy has no sub-elements
    }

    public Element toXml(Document doc) {
        Element strategyEl = doc.createElement(ITableNamingStrategy.XML_TAG_NAME);
        strategyEl.setAttribute("id", EXTENSION_ID); //$NON-NLS-1$
        return strategyEl;
    }

    public void validate(String name) {
        // nothing to do, this strategy has no state
    }

    private String toUnderscoreUppercase(String baseName) {
        String underscoredName = StringUtil.camelCaseToUnderscore(baseName, false);
        return underscoredName.toUpperCase();
    }
}
