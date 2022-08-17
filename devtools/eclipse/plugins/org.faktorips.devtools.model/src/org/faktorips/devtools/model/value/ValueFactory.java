/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.value;

import org.faktorips.devtools.model.internal.InternationalStringXmlHelper;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Element;

/**
 * Factory to build {@link StringValue} or {@link InternationalStringValue}
 * 
 * @author frank
 * @since 3.9
 */
public final class ValueFactory {

    private ValueFactory() {
        // only static
    }

    /**
     * Reads the XML and creates a new {@link IValue}.
     * 
     * @param valueEl Element
     */
    public static IValue<?> createValue(Element valueEl) {
        if (valueEl == null || Boolean.parseBoolean(valueEl.getAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL))) {
            return new StringValue(null);
        }
        if (InternationalStringXmlHelper.isInternationalStringElement(valueEl)) {
            return InternationalStringValue.createFromXml(valueEl);
        }
        String content = XmlUtil.getCDATAorTextContent(valueEl);
        return new StringValue(content == null ? "" : content); //$NON-NLS-1$
    }

    /**
     * Return the new {@link IValue}. If {@code isMultilingual} is {@code true}, then {@link IValue}
     * is {@link InternationalStringValue}. If {@code false}, then {@link IValue} is
     * {@link StringValue}.
     * 
     * @param isMultilingual whether the value should be a multilingual
     *            {@link InternationalStringValue} or simple {@link StringValue}
     * @param value the value to set (used only in {@link StringValue})
     */
    public static IValue<?> createValue(boolean isMultilingual, String value) {
        if (isMultilingual) {
            return new InternationalStringValue();
        } else {
            return new StringValue(value);
        }
    }

    /**
     * Returns a new {@link StringValue}
     * 
     * @param value the value to set
     */
    public static IValue<String> createStringValue(String value) {
        return new StringValue(value);
    }
}
