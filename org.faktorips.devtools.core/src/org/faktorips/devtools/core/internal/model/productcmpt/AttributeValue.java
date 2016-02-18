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

import java.beans.PropertyChangeEvent;
import java.util.Comparator;
import java.util.Locale;

import com.google.common.base.Function;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.HiddenAttributeMismatchEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplatePropertyFinder;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITemplatedValueIdentifier;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.runtime.internal.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.functional.BiConsumer;
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

    private final TemplateValueSettings templateValueSettings;

    public AttributeValue(IPropertyValueContainer parent, String id) {
        this(parent, id, ""); //$NON-NLS-1$
    }

    public AttributeValue(IPropertyValueContainer parent, String id, String attribute) {
        super(parent, id);
        ArgumentCheck.notNull(attribute);
        this.attribute = attribute;
        this.templateValueSettings = new TemplateValueSettings(this);
    }

    @Override
    public final IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    @Override
    public IPropertyValueContainer getTemplatedValueContainer() {
        return getPropertyValueContainer();
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
        if (getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            return findTemplateValueHolder();
        }
        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return getUndefinedValueHolder();
        }
        return valueHolder;
    }

    private IValueHolder<?> findTemplateValueHolder() {
        IAttributeValue templateAttribute = findTemplateProperty(getIpsProject());
        if (templateAttribute == null || templateAttribute.getValueHolder() == null) {
            // Template should have an attribute value but does not. Use the "last known" value
            // holder as a more or less helpful fallback while some validation hopefully addresses
            // the missing attribute value in the template...
            return valueHolder;
        }
        return DelegatingValueHolder.of(this, templateAttribute.getValueHolder());
    }

    private IValueHolder<?> getUndefinedValueHolder() {
        IProductCmptTypeAttribute typeAttribute = findAttribute(getIpsProject());
        if (typeAttribute == null) {
            return new SingleValueHolder(this);
        } else {
            return AttributeValueType.getTypeFor(typeAttribute).newHolderInstance(this);
        }
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
    public PropertyValueType getPropertyValueType() {
        return PropertyValueType.ATTRIBUTE_VALUE;
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return getProductCmptPropertyType();
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return getPropertyValueType().getCorrespondingPropertyType();
    }

    @Override
    public String getPropertyValue() {
        if (getValueHolder() != null) {
            return getValueHolder().getStringValue();
        } else {
            return null;
        }
    }

    @Override
    public IProductCmptTypeAttribute findAttribute(IIpsProject ipsProject) {
        IProductCmptType type = getPropertyValueContainer().findProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        return type.findProductCmptTypeAttribute(attribute, ipsProject);
    }

    @Override
    public IAttributeValue findTemplateProperty(IIpsProject ipsProject) {
        return TemplatePropertyFinder.findTemplatePropertyValue(this, IAttributeValue.class);
    }

    @Override
    public TemplateValueStatus getTemplateValueStatus() {
        return templateValueSettings.getStatus();
    }

    @Override
    public boolean isPartOfTemplateHierarchy() {
        return getTemplatedValueContainer().isPartOfTemplateHierarchy();
    }

    @Override
    public void setTemplateValueStatus(TemplateValueStatus newStatus) {
        if (newStatus == TemplateValueStatus.DEFINED) {
            valueHolder = getValueHolder().copy(this);
        }
        TemplateValueStatus oldValue = templateValueSettings.getStatus();
        templateValueSettings.setStatus(newStatus);
        objectHasChanged(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldValue, newStatus));
    }

    @Override
    public void switchTemplateValueStatus() {
        setTemplateValueStatus(getTemplateValueStatus().getNextStatus(this));
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        attribute = element.getAttribute(PROPERTY_ATTRIBUTE);
        Element valueEl = XmlUtil.getFirstElement(element, ValueToXmlHelper.XML_TAG_VALUE);
        valueHolder = AbstractValueHolder.initValueHolder(this, valueEl);
        templateValueSettings.initPropertiesFromXml(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ATTRIBUTE, attribute);
        Document ownerDocument = element.getOwnerDocument();
        if (getValueHolder() != null) {
            Element valueElement = getValueHolder().toXml(ownerDocument);
            element.appendChild(valueElement);
        }
        templateValueSettings.propertiesToXml(element);
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
        if (attr.isMultiValueAttribute() != (getValueHolder().isMultiValue())) {
            String text;
            String hint = Messages.AttributeValue_msg_validateValueHolder_hint;
            if (attr.isMultiValueAttribute()) {
                text = Messages.AttributeValue_msg_validateValueHolder_multiValue + hint;
            } else {
                text = Messages.AttributeValue_msg_validateValueHolder_singleValue + hint;
            }
            list.add(new Message(MSGCODE_INVALID_VALUE_HOLDER, text, Message.ERROR, this, PROPERTY_VALUE_HOLDER));
        }
        if (getTemplateValueStatus() != TemplateValueStatus.UNDEFINED) {
            list.add(getValueHolder().validate(ipsProject));
            attrIsHiddenMismatch(attr, list);
        }
        list.add(templateValueSettings.validate(this, ipsProject));
    }

    /**
     * This method validates entries of the {@link HiddenAttributeMismatchEntry}. If more than one
     * {@link DeltaType} Entrys have to be validated, it is better to introduce Validator classes
     * where needed.
     */
    private void attrIsHiddenMismatch(IProductCmptTypeAttribute attr, MessageList list) {
        HiddenAttributeMismatchEntry attributeEntry = new HiddenAttributeMismatchEntry(this, attr);
        if (attributeEntry.isMismatch()) {
            String text = NLS.bind(
                    Messages.AttributeValue_HiddenAttributeMismatch,
                    new String[] { attr.getDefaultValue(), attributeEntry.getPropertyName(),
                            attributeEntry.getCurrentAttributeValue() });
            list.add(new Message(MSGCODE_HIDDEN_ATTRIBUTE, text, Message.ERROR));
        }
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

    @Override
    public Comparator<Object> getValueComparator() {
        return getPropertyValueType().getValueComparator();
    }

    @Override
    public Function<IPropertyValue, Object> getValueGetter() {
        return getPropertyValueType().getValueGetter();
    }

    @Override
    public BiConsumer<IPropertyValue, Object> getValueSetter() {
        return getPropertyValueType().getValueSetter();
    }

    @Override
    public ITemplatedValueIdentifier getIdentifier() {
        return new PropertyValueIdentifier(this);
    }

}
