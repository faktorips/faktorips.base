/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.type.Attribute;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.AttributeProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttribute extends Attribute implements IProductCmptTypeAttribute {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private IValueSet valueSet;

    private EnumSet<AttributeProperty> properties = EnumSet.of(AttributeProperty.CHANGING_OVER_TIME,
            AttributeProperty.VISIBLE);

    public ProductCmptTypeAttribute(IProductCmptType parent, String id) {
        super(parent, id);
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        // setting defaults
        properties = EnumSet.of(AttributeProperty.CHANGING_OVER_TIME, AttributeProperty.VISIBLE);

        if (element.hasAttribute(PROPERTY_CHANGING_OVER_TIME)) {
            String changingOverTimeAttribute = element.getAttribute(PROPERTY_CHANGING_OVER_TIME);
            setProperty(AttributeProperty.CHANGING_OVER_TIME, Boolean.parseBoolean(changingOverTimeAttribute));
        }
        if (element.hasAttribute(PROPERTY_MULTI_VALUE_ATTRIBUTE)) {
            String multiValueAttributeElement = element.getAttribute(PROPERTY_MULTI_VALUE_ATTRIBUTE);
            setProperty(AttributeProperty.MULTI_VALUE_ATTRIBUTE, Boolean.parseBoolean(multiValueAttributeElement));
        }
        if (element.hasAttribute(PROPERTY_VISIBLE)) {
            String visibleElement = element.getAttribute(PROPERTY_VISIBLE);
            setProperty(AttributeProperty.VISIBLE, Boolean.parseBoolean(visibleElement));
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_CHANGING_OVER_TIME,
                "" + properties.contains(AttributeProperty.CHANGING_OVER_TIME)); //$NON-NLS-1$
        element.setAttribute(PROPERTY_MULTI_VALUE_ATTRIBUTE,
                "" + properties.contains(AttributeProperty.MULTI_VALUE_ATTRIBUTE)); //$NON-NLS-1$
        element.setAttribute(PROPERTY_VISIBLE, "" + properties.contains(AttributeProperty.VISIBLE)); //$NON-NLS-1$
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public String getPropertyName() {
        return getName();
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE;
    }

    @Override
    public boolean isDerived() {
        return false;
    }

    public ValueDatatype getValueDatatype() {
        try {
            return findDatatype(getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException {
        return ipsProject.getValueSetTypes(findDatatype(ipsProject));
    }

    @Override
    public boolean isValueSetUpdateable() {
        return true;
    }

    @Override
    public void setValueSetType(ValueSetType newType) {
        ArgumentCheck.notNull(newType);
        if (newType == valueSet.getValueSetType()) {
            return;
        }
        valueSet = newType.newValueSet(this, getNextPartId());
        objectHasChanged();
    }

    @Override
    public IValueSet changeValueSetType(ValueSetType newType) {
        setValueSetType(newType);
        return valueSet;
    }

    @Override
    public void setValueSetCopy(IValueSet source) {
        IValueSet oldset = valueSet;
        valueSet = source.copy(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    private void setProperty(AttributeProperty property, boolean state) {
        if (state) {
            properties.add(property);
        } else {
            properties.remove(property);
        }
    }

    private boolean isPropertySet(AttributeProperty property) {
        return properties.contains(property);
    }

    @Override
    public void setChangingOverTime(boolean changesOverTime) {
        boolean oldValue = isPropertySet(AttributeProperty.CHANGING_OVER_TIME);
        setProperty(AttributeProperty.CHANGING_OVER_TIME, changesOverTime);
        valueChanged(oldValue, changesOverTime);
    }

    @Override
    public boolean isChangingOverTime() {
        return isPropertySet(AttributeProperty.CHANGING_OVER_TIME);
    }

    @Override
    public boolean isMultiValueAttribute() {
        return isPropertySet(AttributeProperty.MULTI_VALUE_ATTRIBUTE);
    }

    @Override
    public void setMultiValueAttribute(boolean multiValueAttribute) {
        boolean oldValue = isPropertySet(AttributeProperty.MULTI_VALUE_ATTRIBUTE);
        setProperty(AttributeProperty.MULTI_VALUE_ATTRIBUTE, multiValueAttribute);
        valueChanged(oldValue, multiValueAttribute);
    }

    @Override
    public String getPropertyDatatype() {
        return getDatatype();
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        if (valueSet != null) {
            return new IIpsElement[] { valueSet };
        }
        return new IIpsElement[0];
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = (IValueSet)part;
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        if (xmlTag.getNodeName().equals(ValueSet.XML_TAG)) {
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = new UnrestrictedValueSet(this, getNextPartId());
            return true;
        }
        return false;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // Nothing to do
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmptType();
    }

    @Override
    public boolean isPolicyCmptTypeProperty() {
        return false;
    }

    @Override
    public boolean isPropertyFor(IPropertyValue propertyValue) {
        return getProductCmptPropertyType().equals(propertyValue.getPropertyType())
                && getPropertyName().equals(propertyValue.getPropertyName());
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        if (isOverwrite()) {
            IProductCmptTypeAttribute superAttr = (IProductCmptTypeAttribute)findOverwrittenAttribute(ipsProject);
            if (superAttr != null) {
                if (isMultiValueAttribute() != superAttr.isMultiValueAttribute()) {
                    result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES,
                            Messages.ProductCmptTypeAttribute_msgOverwritten_singleValueMultipleValuesDifference,
                            Message.ERROR, this, PROPERTY_MULTI_VALUE_ATTRIBUTE));
                }
                if (isChangingOverTime() != superAttr.isChangingOverTime()) {
                    result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME,
                            Messages.ProductCmptTypeAttribute_msgOverwritten_ChangingOverTimeAttribute_different,
                            Message.ERROR, this, PROPERTY_CHANGING_OVER_TIME));
                }
            }
        }
    }

    @Override
    public boolean isVisible() {
        return isPropertySet(AttributeProperty.VISIBLE);
    }

    @Override
    public void setVisible(boolean visible) {
        boolean old = isPropertySet(AttributeProperty.VISIBLE);
        setProperty(AttributeProperty.VISIBLE, visible);
        valueChanged(old, visible, PROPERTY_VISIBLE);
    }
}
