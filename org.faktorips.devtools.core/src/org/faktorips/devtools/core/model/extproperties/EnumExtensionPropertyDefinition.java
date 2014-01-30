/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.extproperties;

import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * Implementation of </code>IExtensionPropertyDefinition</code> for extension properties of type
 * EnumType.
 * 
 * @author Faktor Zehn AG, Juergen Niedernhuber
 */
@SuppressWarnings("deprecation")
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
        String content = XmlUtil.getCDATAorTextContent(valueElement);
        if (content == null) {
            return defaultValue;
        }
        int qualifiedNameDelimiterPos = content.lastIndexOf("."); //$NON-NLS-1$
        String id = ""; //$NON-NLS-1$
        if (qualifiedNameDelimiterPos < 0) {
            id = content;
        } else {
            id = content.substring(qualifiedNameDelimiterPos + 1);
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
