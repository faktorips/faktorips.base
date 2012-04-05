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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValueHolderFactory;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The default value holder for attribute values holding a single value. The String value does not
 * mean that the element in the model needs to be of type String but that it is persisted as a
 * String value.
 * 
 * @since 3.7
 * @author dirmeier
 */
public class SingleValueHolder extends AbstractValueHolder<String> {

    public static final String DEFAULT_XML_TYPE_NAME = "SingleValue"; //$NON-NLS-1$

    private final IAttributeValue attributeValue;

    private String value;

    public SingleValueHolder(IAttributeValue parent) {
        super(parent);
        this.attributeValue = parent;
    }

    public SingleValueHolder(IAttributeValue parent, String value) {
        this(parent);
        this.value = value;
    }

    @Override
    protected AttributeValueType getType() {
        return AttributeValueType.SINGLE_VALUE;
    }

    /**
     * @return Returns the value.
     */
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        String oldValue = this.value;
        this.value = value;
        objectHasChanged(oldValue, value);
    }

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        MessageList list = new MessageList();
        IProductCmptTypeAttribute attribute = attributeValue.findAttribute(attributeValue.getIpsProject());
        if (attribute == null) {
            return list;
        }
        if (!ValidationUtils.checkValue(attribute.findDatatype(attributeValue.getIpsProject()), getValue(), this,
                PROPERTY_VALUE, list)) {
            return list;
        }
        if (!attribute.getValueSet().containsValue(getValue(), ipsProject)) {
            String text;
            if (attribute.getValueSet().getValueSetType() == ValueSetType.RANGE) {
                text = NLS.bind(Messages.AttributeValue_AllowedValuesAre, getValue(), attribute.getValueSet()
                        .toShortString());
            } else {
                text = NLS.bind(Messages.AttributeValue_ValueNotAllowed, getValue());
            }
            list.add(new Message(AttributeValue.MSGCODE_VALUE_NOT_IN_SET, text, Message.ERROR, this, PROPERTY_VALUE));
        }
        return list;
    }

    @Override
    protected void contentToXml(Element valueEl, Document doc) {
        if (value != null) {
            valueEl.appendChild(doc.createTextNode(value));
        }
    }

    @Override
    public void initFromXml(Element element) {
        value = ValueToXmlHelper.getValueFromElement(element);
    }

    @Override
    public String getStringValue() {
        return value;
    }

    @Override
    public void setStringValue(String value) {
        setValue(value);
    }

    @Override
    public int compareTo(IValueHolder<String> o) {
        return value.compareTo(o.getValue());
    }

    @Override
    public String toString() {
        return getStringValue();
    }

    /**
     * This factory creates {@link SingleValueHolder} objects.
     * 
     * @author dirmeier
     */
    public static class Factory implements IAttributeValueHolderFactory<String> {

        @Override
        public IValueHolder<String> createValueHolder(IAttributeValue parent) {
            return new SingleValueHolder(parent);
        }

        @Override
        public IValueHolder<String> createValueHolder(IAttributeValue parent, String defaultValue) {
            return new SingleValueHolder(parent, defaultValue);
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributeValue == null) ? 0 : attributeValue.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SingleValueHolder other = (SingleValueHolder)obj;
        if (attributeValue == null) {
            if (other.attributeValue != null) {
                return false;
            }
        } else if (!attributeValue.equals(other.attributeValue)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
