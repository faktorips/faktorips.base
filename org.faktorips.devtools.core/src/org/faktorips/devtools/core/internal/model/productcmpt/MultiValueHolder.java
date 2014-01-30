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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValueHolderFactory;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A multi value holder used for multi value attributes.
 * <p>
 * This holder just contains a list of {@link SingleValueHolder}. The list validation and XML
 * handling is delegated to the internal string value holders.
 * 
 * @since 3.7
 * @author dirmeier
 */
public class MultiValueHolder extends AbstractValueHolder<List<SingleValueHolder>> {

    public static final String XML_TYPE_NAME = "MultiValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "MULTIVALUEHOLDER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there an error in any value of this multi value
     * holder
     */
    public static final String MSGCODE_CONTAINS_INVALID_VALUE = MSGCODE_PREFIX + "ContainsInvalidValue"; //$NON-NLS-1$
    /**
     * Validation message code to indicate that this {@link MultiValueHolder value holder's} values
     * are not unique. At least one value has a duplicate.
     */
    public static final String MSGCODE_CONTAINS_DUPLICATE_VALUE = MSGCODE_PREFIX + "ContainsDuplicateValue"; //$NON-NLS-1$

    private List<SingleValueHolder> values;

    /**
     * Default constructor only needs the attribute value used as parent object.
     * 
     * @param attributeValue The parent attribute value
     */
    public MultiValueHolder(IAttributeValue attributeValue) {
        super(attributeValue);
        values = new ArrayList<SingleValueHolder>();
    }

    /**
     * A constructor to directly set a default value after creating the value holder.
     * 
     * @param attributeValue the parent attribute value
     * @param defaultValue a default value
     */
    public MultiValueHolder(IAttributeValue attributeValue, List<SingleValueHolder> defaultValue) {
        this(attributeValue);
        values = defaultValue;
    }

    @Override
    public IAttributeValue getParent() {
        return (IAttributeValue)super.getParent();
    }

    /**
     * Returns {@link AttributeValueType#MULTI_VALUE}
     */
    @Override
    protected AttributeValueType getType() {
        return AttributeValueType.MULTI_VALUE;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The returned list is a defense copy of the internal list.
     */
    @Override
    public List<SingleValueHolder> getValue() {
        return new ArrayList<SingleValueHolder>(values);
    }

    @Override
    public void setValue(List<SingleValueHolder> values) {
        List<SingleValueHolder> oldValue = this.values;
        this.values = values;
        objectHasChanged(oldValue, values);
    }

    @Override
    protected void contentToXml(Element valueEl, Document doc) {
        Element multiValueElement = doc.createElement(AttributeValueType.MULTI_VALUE.getXmlTypeName());
        for (SingleValueHolder value : values) {
            Element valueElement = value.toXml(doc);
            multiValueElement.appendChild(valueElement);
        }
        valueEl.appendChild(multiValueElement);
    }

    @Override
    public void initFromXml(Element element) {
        NodeList multiValueElementList = element.getElementsByTagName(AttributeValueType.MULTI_VALUE.getXmlTypeName());
        if (multiValueElementList.getLength() > 0 && multiValueElementList.item(0) instanceof Element) {
            Element multiValueElement = (Element)multiValueElementList.item(0);
            values = new ArrayList<SingleValueHolder>();
            NodeList childNodes = multiValueElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i) instanceof Element) {
                    Element child = (Element)childNodes.item(i);
                    SingleValueHolder stringValueHolder = new SingleValueHolder(getParent());
                    stringValueHolder.initFromXml(child);
                    values.add(stringValueHolder);
                }
            }
        }
    }

    /**
     * This validates every {@link SingleValueHolder} in the list of values.
     */
    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        MessageList messageList = new MessageList();
        if (values == null) {
            return messageList;
        }
        Set<SingleValueHolder> duplicateValueHolders = getDuplicateValueHolders();
        for (SingleValueHolder duplicateValueHolder : duplicateValueHolders) {
            messageList.add(Message.newError(MSGCODE_CONTAINS_DUPLICATE_VALUE,
                    Messages.MultiValueHolder_DuplicateValueMessageText, duplicateValueHolder, PROPERTY_VALUE));
        }
        for (SingleValueHolder valueHolder : values) {
            messageList.add(valueHolder.validate(ipsProject));
        }
        if (messageList.containsErrorMsg()) {
            ObjectProperty[] invalidObjectProperties = new ObjectProperty[] {
                    new ObjectProperty(getParent(), IAttributeValue.PROPERTY_VALUE_HOLDER),
                    new ObjectProperty(this, PROPERTY_VALUE) };
            messageList
                    .add(new Message(MSGCODE_CONTAINS_INVALID_VALUE,
                            Messages.MultiValueHolder_AtLeastOneInvalidValueMessageText, Message.ERROR,
                            invalidObjectProperties));
        }
        return messageList;
    }

    private Set<SingleValueHolder> getDuplicateValueHolders() {
        Set<SingleValueHolder> duplicates = new HashSet<SingleValueHolder>();
        Set<SingleValueHolder> processedValues = new HashSet<SingleValueHolder>();
        for (SingleValueHolder element : values) {
            if (processedValues.contains(element)) {
                duplicates.add(element);
            } else {
                processedValues.add(element);
            }
        }
        return duplicates;
    }

    @Override
    public int compareTo(IValueHolder<List<SingleValueHolder>> o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a string representation of the list of values. For example
     * <code>[value1, value2, value3]
     */
    @Override
    public String getStringValue() {
        List<String> stringList = new ArrayList<String>();
        for (SingleValueHolder holder : values) {
            stringList.add(holder.getStringValue());
        }
        return stringList.toString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation set the value as first and single element in the list of values. It is
     * not recommended to use this method because it is not symmetric to {@link #getStringValue()}
     * 
     * @deprecated Use {@link #setValue(List)}, because we have a list of SingleValues
     */
    @Deprecated
    @Override
    public void setStringValue(String value) {
        values = new ArrayList<SingleValueHolder>();
        values.add(new SingleValueHolder(getParent(), new StringValue(value)));
    }

    /**
     * {@inheritDoc}
     * <p>
     * MultiValueHolder Is never null, because the list can be empty but never null.
     */
    @Override
    public boolean isNullValue() {
        return false;
    }

    @Override
    public ValueType getValueType() {
        if (!values.isEmpty()) {
            return getValueTypeFromFirstEntry();
        } else {
            return getValueTypeForNoEntries();
        }
    }

    private ValueType getValueTypeForNoEntries() {
        try {
            IProductCmptTypeAttribute attribute = getParent().findAttribute(getIpsProject());
            if (attribute != null) {
                return getValueTypeFromAttribute(attribute);
            } else {
                return ValueType.STRING;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private ValueType getValueTypeFromFirstEntry() {
        return values.get(0).getValueType();
    }

    private ValueType getValueTypeFromAttribute(IProductCmptTypeAttribute attribute) {
        if (attribute.isMultilingual()) {
            return ValueType.INTERNATIONAL_STRING;
        } else {
            return ValueType.STRING;
        }
    }

    /**
     * This factory creates {@link MultiValueHolder} objects
     * 
     * @author dirmeier
     */
    public static class Factory implements IAttributeValueHolderFactory<List<SingleValueHolder>> {

        @Override
        public IValueHolder<List<SingleValueHolder>> createValueHolder(IAttributeValue parent) {
            return new MultiValueHolder(parent);
        }

        @Override
        public IValueHolder<List<SingleValueHolder>> createValueHolder(IAttributeValue parent, IValue<?> defaultValue) {
            ArrayList<SingleValueHolder> values;
            if (defaultValue instanceof StringValue) {
                values = splitMultiDefaultValues(parent, defaultValue);
            } else if (defaultValue.getContent() != null) {
                values = new ArrayList<SingleValueHolder>();
                SingleValueHolder singleValueHolder = new SingleValueHolder(parent, defaultValue);
                values.add(singleValueHolder);
            } else {
                values = new ArrayList<SingleValueHolder>();
            }
            return new MultiValueHolder(parent, values);
        }

        private ArrayList<SingleValueHolder> splitMultiDefaultValues(IAttributeValue parent, IValue<?> defaultValue) {
            ArrayList<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
            StringValue stringValue = (StringValue)defaultValue;
            String content = stringValue.getContent();
            if (content != null) {
                String[] split = content.split(","); //$NON-NLS-1$
                for (String string : split) {
                    values.add(new SingleValueHolder(parent, new StringValue(string.trim())));
                }
            }
            return values;
        }
    }

}
