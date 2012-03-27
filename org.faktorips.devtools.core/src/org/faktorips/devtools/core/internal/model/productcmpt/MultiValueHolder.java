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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValueHolderFactory;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
    public final static String MSGCODE_PREFIX = "MULTIVALUEHOLDER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there an error in any value of this multi value
     * holder
     */
    public final static String MSGCODE_CONTAINS_INVALID_VALUE = MSGCODE_PREFIX + "ContainsInvalidValue"; //$NON-NLS-1$

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
        for (SingleValueHolder valueHolder : values) {
            messageList.add(valueHolder.validate(ipsProject));
        }
        if (messageList.containsErrorMsg()) {
            messageList.add(Message.newError(MSGCODE_CONTAINS_INVALID_VALUE, "There is at least one invalid value.",
                    this, PROPERTY_VALUE));
        }
        return messageList;
    }

    @Override
    public int compareTo(IValueHolder<List<SingleValueHolder>> o) {
        // TODO Auto-generated method stub
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
     */
    @Override
    @Deprecated
    public void setStringValue(String value) {
        values = new ArrayList<SingleValueHolder>();
        values.add(new SingleValueHolder(getParent(), value));
    }

    @Override
    public String toString() {
        return getStringValue();
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
        public IValueHolder<List<SingleValueHolder>> createValueHolder(IAttributeValue parent,
                List<SingleValueHolder> defaultValue) {
            return new MultiValueHolder(parent, defaultValue);
        }

    }

}
