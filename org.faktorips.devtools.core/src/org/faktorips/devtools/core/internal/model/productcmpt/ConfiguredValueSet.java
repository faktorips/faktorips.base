/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValueSetNullIncompatibleValidator;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.core.internal.model.valueset.DelegatingValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The allowed values for a {@link IPolicyCmptTypeAttribute} as configured in a
 * {@link IPropertyValueContainer}.
 */
public class ConfiguredValueSet extends ConfigElement implements IConfiguredValueSet {

    public static final String LEGACY_TAG_NAME = ValueToXmlHelper.XML_TAG_VALUE_SET;

    public static final String TAG_NAME = ValueToXmlHelper.XML_TAG_CONFIGURED_VALUE_SET;

    private IValueSet valueSet;

    public ConfiguredValueSet(IPropertyValueContainer parent, String policyAttribute, String id) {
        super(parent, policyAttribute, id);
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
    }

    @Override
    public IConfiguredValueSet findTemplateProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.findTemplateValue(this, IConfiguredValueSet.class);
    }

    @Override
    public boolean hasTemplateForProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.hasTemplateForValue(this, IConfiguredValueSet.class);
    }

    @Override
    public PropertyValueType getPropertyValueType() {
        return PropertyValueType.CONFIGURED_VALUESET;
    }

    @Override
    public IValueSet getPropertyValue() {
        return getValueSet();
    }

    @Override
    protected void validateContent(MessageList list, IIpsProject ipsProject, IPolicyCmptTypeAttribute attribute)
            throws CoreException {
        IValueSet valueSetToValidate = getValueSet();
        IValueSet modelValueSet = attribute.getValueSet();

        if (modelValueSet.validate(ipsProject).containsErrorMsg()) {
            String text = Messages.ConfiguredValueSet_msgInvalidAttributeValueset;
            list.add(new Message(IConfiguredValueSet.MSGCODE_UNKNWON_VALUESET, text, Message.WARNING, this,
                    PROPERTY_VALUE_SET));
            return;
        }
        if (valueSetToValidate.isAbstract()) {
            String text = Messages.ConfiguredValueSet_error_msg_abstractValueSet;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_VALUE_SET)); //$NON-NLS-1$
            return;
        }
        if (valueSetToValidate.isDetailedSpecificationOf(modelValueSet)) {
            // situations like model value set is unrestricted, and this value set is a range
            // are ok.
            return;
        }
        validateNullIncompatible(list, modelValueSet, valueSetToValidate);
        String msgCode;
        String text;
        if (!valueSetToValidate.isSameTypeOfValueSet(modelValueSet)) {
            msgCode = IConfiguredValueSet.MSGCODE_VALUESET_TYPE_MISMATCH;
            text = NLS.bind(Messages.ConfigElement_msgTypeMismatch, new String[] {
                    modelValueSet.getValueSetType().getName(), valueSetToValidate.getValueSetType().getName() });
        } else if (!modelValueSet.containsValueSet(valueSetToValidate)) {
            msgCode = IConfiguredValueSet.MSGCODE_VALUESET_IS_NOT_A_SUBSET;
            text = NLS.bind(Messages.ConfigElement_valueSetIsNotASubset, valueSetToValidate.toShortString(),
                    modelValueSet.toShortString());
        } else {
            // should never happen
            throw new RuntimeException();
        }
        // determine invalid property (usage e.g. to display problem marker on correct ui control)
        List<ObjectProperty> invalidObjectProperties = new ArrayList<ObjectProperty>();
        invalidObjectProperties.add(new ObjectProperty(this, PROPERTY_VALUE_SET));
        if (valueSetToValidate.isRange()) {
            invalidObjectProperties.add(new ObjectProperty(valueSetToValidate, IRangeValueSet.PROPERTY_LOWERBOUND));
            invalidObjectProperties.add(new ObjectProperty(valueSetToValidate, IRangeValueSet.PROPERTY_UPPERBOUND));
            invalidObjectProperties.add(new ObjectProperty(valueSetToValidate, IRangeValueSet.PROPERTY_STEP));
        }
        ObjectProperty[] invalidOP = invalidObjectProperties
                .toArray(new ObjectProperty[invalidObjectProperties.size()]);
        list.add(new Message(msgCode, text, Message.ERROR, invalidOP));
    }

    private void validateNullIncompatible(MessageList list, IValueSet modelValueSet, IValueSet valueSetToValidate) {
        new ValueSetNullIncompatibleValidator(modelValueSet, valueSetToValidate).validateAndAppendMessages(list);
    }

    private IValueSet findTemplateValueSet() {
        IConfiguredValueSet templateConfigElement = findTemplateProperty(getIpsProject());
        if (templateConfigElement == null) {
            // Template should exist but does not. Use the "last known" value as a more or less
            // helpful fallback while some validation hopefully addresses the missing template...
            return valueSet;
        }
        return new DelegatingValueSet((ValueSet)templateConfigElement.getValueSet(), this);
    }

    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptTypeAttribute attribute = findPcTypeAttribute(ipsProject);
        if (attribute == null) {
            ArrayList<ValueSetType> types = new ArrayList<ValueSetType>();
            types.add(valueSet.getValueSetType());
            return types;
        }
        if (attribute.getValueSet().isUnrestricted()) {
            return ipsProject.getValueSetTypes(attribute.findDatatype(ipsProject));
        }
        ArrayList<ValueSetType> types = new ArrayList<ValueSetType>();
        types.add(attribute.getValueSet().getValueSetType());
        return types;
    }

    @Override
    public IEnumValueSet convertValueSetToEnumType() {
        ValueSetType newValueSetType = ValueSetType.ENUM;
        if (valueSet.getValueSetType().equals(newValueSetType)) {
            // unchanged
            return (IEnumValueSet)valueSet;
        }
        setValueSetType(newValueSetType);
        IEnumValueSet newValueSet = (IEnumValueSet)valueSet;
        ValueDatatype valueSetDatatype = findValueDatatype(getIpsProject());
        if (Datatype.BOOLEAN.equals(valueSetDatatype) || Datatype.PRIMITIVE_BOOLEAN.equals(valueSetDatatype)) {
            newValueSet.addValue(Boolean.TRUE.toString());
            newValueSet.addValue(Boolean.FALSE.toString());
            if (!valueSetDatatype.isPrimitive()) {
                newValueSet.addValue(null);
            }
        }
        return newValueSet;
    }

    @Override
    public IValueSet getValueSet() {
        if (getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
            return findTemplateValueSet();
        }
        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return new UnrestrictedValueSet(this, getNextPartId());
        }
        return valueSet;
    }

    @Override
    public void setValueSetType(ValueSetType type) {
        IValueSet oldset = valueSet;
        valueSet = type.newValueSet(this, getNextPartId());
        valueChanged(oldset, valueSet);
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

    @Override
    public void setValueSet(IValueSet source) {
        setValueSetCopy(source);
    }

    @Override
    public boolean isValueSetUpdateable() {
        return true;
    }

    @Override
    protected void templateValueChanged() {
        this.valueSet = getValueSet().copy(this, getNextPartId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (ValueSet.XML_TAG.equals(xmlTagName)) {
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;
        }
        return null;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        if (LEGACY_TAG_NAME.equals(element.getNodeName())) {
            // overwrite ID in legacy mode
            super.initPropertiesFromXml(element, getNextPartId());
            // the legacy part only have one XML element for the ConfiguredVallueSet and nested
            // ValueSet. So we create the a child with the same XML element
            IIpsObjectPart newPart = newPart(element, id);
            newPart.initFromXml(element);
        } else {
            super.initPropertiesFromXml(element, id);
        }
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = null;
            return true;
        }
        return false;
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
    protected IIpsElement[] getChildrenThis() {
        if (getValueSet() != null) {
            return new IIpsElement[] { getValueSet() };
        }
        return new IIpsElement[0];
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        return NLS.bind(Messages.ConfiguredValueSet_caption, getAttributeLabel(locale));
    }

    @Override
    public String getLastResortCaption() {
        return NLS.bind(Messages.ConfiguredValueSet_caption, getAttributeLabel(null));
    }

}
