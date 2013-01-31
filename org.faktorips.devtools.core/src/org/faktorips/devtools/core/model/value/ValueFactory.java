/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.value;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
        NodeList childNodes = valueEl.getChildNodes();
        if (childNodes.getLength() == 1) {
            Node node = childNodes.item(0);
            if (node instanceof Text) {
                return StringValue.createFromXml((Text)node);
            }
        } else {
            return InternationalStringValue.createFromXml(valueEl);
        }
        CDATASection cdata = XmlUtil.getFirstCDataSection(valueEl);
        // if no cdata-section was found, the value stored was an empty string.
        // In this case, the cdata-section get lost during transformation of the
        // xml-document to a string.
        String result = ""; //$NON-NLS-1$
        if (cdata != null) {
            result = cdata.getData();
        }
        return new StringValue(result);
    }

    /**
     * Finds the {@link IProductCmptTypeAttribute} and returns the new created IValue<T>.
     * 
     * @param attributeValue the {@link IAttributeValue}
     */
    public static IValue<?> createValue(IAttributeValue attributeValue) {
        try {
            IProductCmptTypeAttribute prodCmptTypeAttribute = null;
            if (attributeValue != null && attributeValue.getIpsProject() != null) {
                prodCmptTypeAttribute = attributeValue.findAttribute(attributeValue.getIpsProject());
            }
            return createValue(prodCmptTypeAttribute, null);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Creates depending on the attribute multilingual Setting a new
     * {@link InternationalStringValue} if <code>true</code> or an {@link StringValue} if
     * <code>false</code>
     * 
     * @param attribute the {@link IProductCmptTypeAttribute}
     * @param value the value for the StringValue
     */
    public static IValue<?> createValue(IProductCmptTypeAttribute attribute, String value) {
        if (attribute == null) {
            return createStringValue(value);
        } else {
            if (attribute.isMultilingual()) {
                return new InternationalStringValue();
            } else {
                return createStringValue(value);
            }
        }
    }

    /**
     * Creates the IValue with the default Value defined in the attribute or if the attribute is
     * multilingual defined a new {@link InternationalString}
     * 
     * @param attribute the {@link IProductCmptTypeAttribute}
     */
    public static IValue<?> createDefaultValue(IProductCmptTypeAttribute attribute) {
        String defaultValue = null;
        if (attribute != null) {
            defaultValue = attribute.getDefaultValue();
            if (attribute.isMultilingual()) {
                return new InternationalStringValue();
            }
        }
        return createStringValue(defaultValue);
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
