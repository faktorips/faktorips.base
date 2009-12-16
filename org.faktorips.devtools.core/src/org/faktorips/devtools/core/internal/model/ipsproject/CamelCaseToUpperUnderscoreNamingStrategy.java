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

package org.faktorips.devtools.core.internal.model.ipsproject;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Roman Grutza
 */
public class CamelCaseToUpperUnderscoreNamingStrategy implements ITableColumnNamingStrategy, ITableNamingStrategy {

    public final static String EXTENSION_ID = "org.faktorips.devtools.core.CamelCaseToUpperUnderscoreNamingStrategy";
    private IIpsProject ipsProject;

    // /**
    // * @param ipsProject
    // */
    // public CamelCaseToUpperUnderscoreNamingStrategy(IIpsProject ipsProject) {
    // this.ipsProject = ipsProject;
    // }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public String getTableName(String baseName) {
        if (StringUtils.isEmpty(baseName)) {
            // throw new
            // RuntimeException("Empty base name is not allowed to derive a table identifier from.");
        }
        return toUnderscoreUppercase(baseName);
    }

    public String getTableName(String baseName, int maxLength) {
        if (StringUtils.isEmpty(baseName)) {
            // throw new
            // RuntimeException("Empty base name is not allowed to derive a table identifier from.");
        }
        if (maxLength <= 0) {
            throw new RuntimeException("Negative length given for table identifier size.");
        }
        String derivedName = toUnderscoreUppercase(baseName);
        return derivedName.substring(0, Math.min(derivedName.length(), maxLength));
    }

    public String getTableColumnName(String baseName) {
        return getTableName(baseName);
    }

    public String getTableColumnName(String baseName, int maxLength) {
        return getTableName(baseName, maxLength);
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(Element el) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    public void setIpsProject(IIpsProject project) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    public Element toXml(Document doc) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void validate(String name) {
        // TODO Auto-generated method stub

    }

    /**
     * @param baseName
     * @return
     */
    private String toUnderscoreUppercase(String baseName) {
        String underscoredName = StringUtil.camelCaseToUnderscore(baseName, false);
        return underscoredName.toUpperCase();
    }

}
