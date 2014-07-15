/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.extproperties;

import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IExtensionPropertyDefinition</code> for extension properties of type
 * EnumType.
 * 
 * @deprecated Since 3.13 use {@link EnumExtensionPropertyDefinition2} instead as this class
 *             represents an extension property for the deprecated type {@link EnumType}.
 * 
 * @see EnumType How to migrate to new Java enum.
 */
@Deprecated
public abstract class EnumExtensionPropertyDefinition extends ExtensionPropertyDefinition {

    private EnumType enumType;

    /**
     * @deprecated Since 3.13 as this class is designed for an {@link EnumType} and as the type
     *             {@link EnumType} is deprecated the instantiation of this class is deprecated,
     *             too.
     */
    @Deprecated
    public EnumExtensionPropertyDefinition(EnumType enumType) {
        super();
        this.enumType = enumType;
    }

    /**
     * @deprecated Since 3.13 as this class is designed for an {@link EnumType} and as the type
     *             {@link EnumType} is deprecated this method is deprecated, too. Use
     *             {@link EnumExtensionPropertyDefinition2}. {@link #setDefaultValue(String s)}
     *             instead.
     */
    @Deprecated
    @Override
    public void setDefaultValue(String s) {
        defaultValue = enumType.getEnumValue(s);
    }

    /**
     * @deprecated Since 3.13 as this class is designed for an {@link EnumType} and as the type
     *             {@link EnumType} is deprecated this method is deprecated, too. Use
     *             {@link EnumExtensionPropertyDefinition2}.
     *             {@link #getValueFromString(String value)} instead.
     */
    @Deprecated
    @Override
    public Object getValueFromString(String value) {
        return enumType.getEnumValue(value);
    }

    /**
     * @deprecated Since 3.13 as this class is designed for an {@link EnumType} and as the type
     *             {@link EnumType} is deprecated this method is deprecated, too. Use
     *             {@link EnumExtensionPropertyDefinition2}.
     *             {@link #getValueFromXml(Element valueElement)} instead.
     */
    @Deprecated
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
     * @deprecated Since 3.13 as this class is designed for an {@link EnumType} and as the type
     *             {@link EnumType} is deprecated this method is deprecated, too.
     */
    @Deprecated
    public EnumType getEnumType() {
        return enumType;
    }

}
