/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static final String SEPARATOR = "|"; //$NON-NLS-1$

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
        if (values == null) {
            return Collections.emptyList();
        }
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
        return new MultiValueHolderValidator(this, getParent(), ipsProject).validate();
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

    @Override
    public boolean isMultiValue() {
        return true;
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

    @Override
    protected MultiValueHolderValidator newValidator(IAttributeValue parent, IIpsProject ipsProject) {
        return new MultiValueHolderValidator(this, parent, ipsProject);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((values == null) ? 0 : values.hashCode());
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
        MultiValueHolder other = (MultiValueHolder)obj;
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    /**
     * This factory creates {@link MultiValueHolder} objects
     * 
     * @author dirmeier
     */
    public static class Factory implements IAttributeValueHolderFactory<List<SingleValueHolder>> {

        private static final String MULTI_VALUE_SPLIT_REGEX = "\\s*\\" + SEPARATOR + "\\s*"; //$NON-NLS-1$ //$NON-NLS-2$

        @Override
        public IValueHolder<List<SingleValueHolder>> createValueHolder(IAttributeValue parent) {
            return new MultiValueHolder(parent);
        }

        @Override
        public IValueHolder<List<SingleValueHolder>> createValueHolder(IAttributeValue parent, IValue<?> defaultValue) {
            ArrayList<SingleValueHolder> values;
            if (defaultValue instanceof StringValue) {
                values = splitMultiDefaultValues(parent, (StringValue)defaultValue);
            } else {
                values = new ArrayList<SingleValueHolder>();
                if (defaultValue.getContent() != null) {
                    SingleValueHolder singleValueHolder = new SingleValueHolder(parent, defaultValue);
                    values.add(singleValueHolder);
                }
            }
            return new MultiValueHolder(parent, values);
        }

        /**
         * For {@link StringValue string values} we try to split the content using " | ". This is a
         * kind of workaround defined in FIPS-1864 to avoid the need of fully refactoring the meta
         * model to always use {@link IValueHolder} for default values instead of a single String
         * field.
         */
        ArrayList<SingleValueHolder> splitMultiDefaultValues(IAttributeValue parent, StringValue defaultValue) {
            ArrayList<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
            String content = defaultValue.getContent();
            if (content != null) {
                String[] splittedMultiValues = getSplitMultiValue(content);
                for (String string : splittedMultiValues) {
                    values.add(new SingleValueHolder(parent, new StringValue(string)));
                }
            }
            return values;
        }

        public static String[] getSplitMultiValue(String content) {
            return content.split(MULTI_VALUE_SPLIT_REGEX);
        }

    }

}
