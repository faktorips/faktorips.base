/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
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

    public final static String TAG_NAME = "AttributeValue"; //$NON-NLS-1$

    private String attribute;
    private String value;

    public AttributeValue(IPropertyValueContainer parent, String id) {
        this(parent, id, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public AttributeValue(IPropertyValueContainer parent, String id, String attributeName) {
        this(parent, id, attributeName, ""); //$NON-NLS-1$
    }

    public AttributeValue(IPropertyValueContainer parent, String id, String attribute, String value) {
        super(parent, id);
        ArgumentCheck.notNull(attribute);
        this.attribute = attribute;
        this.value = value;
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

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String newValue) {
        String oldValue = value;
        value = newValue;
        valueChanged(oldValue, newValue);
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
        return value;
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
        value = ValueToXmlHelper.getValueFromElement(element, "Value"); //$NON-NLS-1$
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ATTRIBUTE, attribute);
        ValueToXmlHelper.addValueToElement(value, element, "Value"); //$NON-NLS-1$
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
        if (!ValidationUtils.checkValue(attr.getDatatype(), value, this, PROPERTY_VALUE, list)) {
            return;
        }
        if (!attr.getValueSet().containsValue(value, ipsProject)) {
            String text;
            if (attr.getValueSet().getValueSetType() == ValueSetType.RANGE) {
                text = NLS.bind(Messages.AttributeValue_AllowedValuesAre, value, attr.getValueSet().toShortString());
            } else {
                text = NLS.bind(Messages.AttributeValue_ValueNotAllowed, value);
            }
            list.add(new Message(MSGCODE_VALUE_NOT_IN_SET, text, Message.ERROR, this, PROPERTY_VALUE));
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
        IAttribute attribute = findAttribute(getIpsProject());
        if (attribute != null) {
            caption = attribute.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(attribute);
    }

    @Override
    public String toString() {
        return attribute + "=" + value; //$NON-NLS-1$
    }

}
