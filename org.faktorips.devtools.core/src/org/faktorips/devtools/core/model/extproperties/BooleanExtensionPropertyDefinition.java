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

package org.faktorips.devtools.core.model.extproperties;

import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

/**
 * Implementation of </code>IExtensionPropertyDefinition</code> for extension properties of type
 * Boolean.
 * 
 * @author Jan Ortmann
 */
public class BooleanExtensionPropertyDefinition extends ExtensionPropertyDefinition {

    /**
     * Empty constructor needed because of Eclipse extension point mechanism.
     */
    public BooleanExtensionPropertyDefinition() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueFromXml(Element valueElement) {
        CDATASection cdata = XmlUtil.getFirstCDataSection(valueElement);
        if (cdata == null) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(cdata.getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueFromString(String value) {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultValue(String s) {
        defaultValue = Boolean.valueOf(s);
    }

}
