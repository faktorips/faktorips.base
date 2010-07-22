/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.extproperties;

import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

/**
 * Implementation of </code>IExtensionPropertyDefinition</code> for extension properties of type
 * EnumType.
 * 
 * @author Faktor Zehn AG, Juergen Niedernhuber
 */
public abstract class EnumExtensionPropertyDefinition extends ExtensionPropertyDefinition {

    private EnumType enumType;

    public EnumExtensionPropertyDefinition(EnumType enumType) {
        super();
        this.enumType = enumType;
    }

    @Override
    public void setDefaultValue(String s) {
        defaultValue = enumType.getEnumValue(s);
    }

    @Override
    public Object getValueFromString(String value) {
        return enumType.getEnumValue(value);
    }

    @Override
    public Object getValueFromXml(Element valueElement) {
        CDATASection cdata = XmlUtil.getFirstCDataSection(valueElement);
        if (cdata == null) {
            return defaultValue;
        }
        int qualifiedNameDelimiterPos = cdata.getData().lastIndexOf("."); //$NON-NLS-1$
        String id = ""; //$NON-NLS-1$
        if (qualifiedNameDelimiterPos < 0) {
            id = cdata.getData();
        } else {
            id = cdata.getData().substring(qualifiedNameDelimiterPos + 1);
        }
        return enumType.getEnumValue(id);
    }

    /**
     * Returns this ExtensionProperty's {@link EnumType}.
     * 
     * @return the {@link EnumType}
     */
    public EnumType getEnumType() {
        return enumType;
    }

}
