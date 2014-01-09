/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.value;

import org.faktorips.devtools.core.internal.model.InternationalStringXmlHelper;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Element;

/**
 * Factory to build StringValue or InternationalStringValue
 * 
 * @author frank
 * @since 3.9
 */
public final class ValueFactory {

    private ValueFactory() {
        // only static
    }

    /**
     * Read the xml and creates a new IValue<?>.
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
     * Return the new {@link IValue}. If isMultilingual is <code>true</code>, then {@link IValue} is
     * {@link InternationalStringValue}. If <code>false</code>, then {@link IValue} is
     * {@link StringValue}.
     * 
     * @param isMultilingual <code>true</code> or <code>false</code>
     * @param value the value to set only in {@link StringValue}
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
