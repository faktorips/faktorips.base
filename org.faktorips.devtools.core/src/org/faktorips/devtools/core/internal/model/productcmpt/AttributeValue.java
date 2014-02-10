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

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.runtime.internal.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class AttributeValue extends AtomicIpsObjectPart implements IAttributeValue {

    public static final String TAG_NAME = "AttributeValue"; //$NON-NLS-1$

    private String attribute;

    private IValueHolder<?> valueHolder;

    public AttributeValue(IPropertyValueContainer parent, String id) {
        this(parent, id, ""); //$NON-NLS-1$ 
    }

    public AttributeValue(IPropertyValueContainer parent, String id, String attribute) {
        super(parent, id);
        ArgumentCheck.notNull(attribute);
        this.attribute = attribute;
    }

    @Override
    public final IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public void setAttribute(String newAttribute) {
        String oldAttr = attribute;
        attribute = newAttribute;
        name = attribute;
        valueChanged(oldAttr, attribute);
    }

    /**
     * {@inheritDoc}
     * 
     * * @deprecated Since 3.7 we support multi valued attributes. You should use
     * {@link #getValueHolder()} instead.
     */
    @Override
    @Deprecated
    public String getValue() {
        return getPropertyValue();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 
     * @deprecated Since 3.7 we support multi valued attributes. You should use
     *             {@link #setValueHolder(IValueHolder)} instead, for example:
     *             <code>attributeValue.setValueHolder(AttributeValueType.SINGLE_VALUE.newHolderInstance(attributeValue, myValue));</code>
     *             while <code>myValue</code> is a new value of type {@link IValue}
     */
    @Override
    @Deprecated
    public void setValue(String value) {
        if (value == null) {
            valueHolder = null;
        }
        if (valueHolder == null) {
            valueHolder = new SingleValueHolder(this, new StringValue(value));
        } else {
            this.valueHolder.setStringValue(value);
        }
        objectHasChanged();
    }

    @Override
    public IValueHolder<?> getValueHolder() {
        return valueHolder;
    }

    @Override
    public void setValueHolder(IValueHolder<?> newValue) {
        IValueHolder<?> oldValue = valueHolder;
        setValueHolderInternal(newValue);
        valueChanged(oldValue, newValue);
    }

    public void setValueHolderInternal(IValueHolder<?> newValue) {
        valueHolder = newValue;
    }

    @Override
    public String getPropertyName() {
        return attribute;
    }

    @Override
    public IProductCmptProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findAttribute(ipsProject);
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE;
    }

    @Override
    public String getPropertyValue() {
        if (valueHolder != null) {
            return valueHolder.getStringValue();
        } else {
            return null;
        }
    }

    @Override
    public IProductCmptTypeAttribute findAttribute(IIpsProject ipsProject) throws CoreException {
        IProductCmptType type = getPropertyValueContainer().findProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        return type.findProductCmptTypeAttribute(attribute, ipsProject);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        attribute = element.getAttribute(PROPERTY_ATTRIBUTE);
        Element valueEl = XmlUtil.getFirstElement(element, ValueToXmlHelper.XML_TAGNAME_VALUE);
        valueHolder = AbstractValueHolder.initValueHolder(this, valueEl);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ATTRIBUTE, attribute);
        Document ownerDocument = element.getOwnerDocument();
        if (valueHolder != null) {
            Element valueElement = valueHolder.toXml(ownerDocument);
            element.appendChild(valueElement);
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptTypeAttribute attr = findAttribute(ipsProject);
        if (attr == null) {
            String typeLabel = getPropertyValueContainer().getProductCmptType();
            IProductCmptType productCmptType = getPropertyValueContainer().findProductCmptType(ipsProject);
            if (productCmptType != null) {
                typeLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(productCmptType);
            }
            String text = NLS.bind(Messages.AttributeValue_attributeNotFound, attribute, typeLabel);
            list.add(new Message(MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this, PROPERTY_ATTRIBUTE));
            return;
        }
        if (attr.isMultiValueAttribute() != (valueHolder instanceof MultiValueHolder)) {
            String text;
            String hint = Messages.AttributeValue_msg_validateValueHolder_hint;
            if (attr.isMultiValueAttribute()) {
                text = Messages.AttributeValue_msg_validateValueHolder_multiValue + hint;
            } else {
                text = Messages.AttributeValue_msg_validateValueHolder_singleValue + hint;
            }
            list.add(new Message(MSGCODE_INVALID_VALUE_HOLDER, text, Message.ERROR, this, PROPERTY_VALUE_HOLDER));
        }
        MessageList validateValue = valueHolder.validate(ipsProject);
        list.add(validateValue);
    }

    @Override
    public String getName() {
        return attribute;
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);

        String caption = null;
        IAttribute foundAttribute = findAttribute(getIpsProject());
        if (foundAttribute != null) {
            caption = foundAttribute.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(attribute);
    }

    @Override
    public String toString() {
        return attribute + "=" + getPropertyValue(); //$NON-NLS-1$
    }

}
