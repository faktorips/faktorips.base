/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.extproperties;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.model.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IExtensionPropertyDefinition</code> for extension properties of type
 * {@link Enum}.
 * <p>
 * If this class is used directly the literal name of the enum values will be persisted in the xml.
 * It is better to extend this class and override the methods {@link #valueToXml(Element, Object)}
 * and {@link #getValueFromXml(Element)} so that they persist an ID which should the enum values
 * provide.
 */

public abstract class EnumExtensionPropertyDefinition2<E extends Enum<E>> extends ExtensionPropertyDefinition {

    private final Class<E> enumType;

    public EnumExtensionPropertyDefinition2(Class<E> enumType) {
        super();
        this.enumType = enumType;
    }

    @Override
    public void setDefaultValue(String s) {
        defaultValue = Enum.valueOf(enumType, s);
    }

    @Override
    public Object getValueFromString(String value) {
        return Enum.valueOf(enumType, value);
    }

    @Override
    public Object getValueFromXml(Element valueElement) {
        String content = XmlUtil.getCDATAorTextContent(valueElement);
        if (content == null) {
            return defaultValue;
        }
        int qualifiedNameDelimiterPos = content.lastIndexOf('.');
        String id = IpsStringUtils.EMPTY;
        if (qualifiedNameDelimiterPos < 0) {
            id = content;
        } else {
            id = content.substring(qualifiedNameDelimiterPos + 1);
        }
        return Enum.valueOf(enumType, id);
    }
}
