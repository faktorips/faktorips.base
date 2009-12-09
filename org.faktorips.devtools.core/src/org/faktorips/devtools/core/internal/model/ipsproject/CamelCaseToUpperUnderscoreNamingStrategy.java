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

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Roman Grutza
 */
public class CamelCaseToUpperUnderscoreNamingStrategy implements ITableColumnNamingStrategy, ITableNamingStrategy {

    public final static String EXTENSION_ID = "org.faktorips.devtools.core.CamelCaseToUpperUnderscoreNamingStrategy";

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getTableColumnName(String baseName) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getTableColumnName(String baseName, int maxLength) {
        // TODO Auto-generated method stub
        return null;
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
     * {@inheritDoc}
     */
    public String getTableName(String baseName) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getTableName(String baseName, int maxLength) {
        // TODO Auto-generated method stub
        return null;
    }

}
