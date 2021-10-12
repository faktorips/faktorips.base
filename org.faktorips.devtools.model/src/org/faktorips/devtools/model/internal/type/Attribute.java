/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import java.util.EnumSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ValueSetNullIncompatibleValidator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.AttributeProperty;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.DatatypeUtil;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 * 
 * @author Jan Ortmann
 */
public abstract class Attribute extends TypePart implements IAttribute {

    static final String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$

    private String defaultValue = null;

    private boolean overwrites;

    private final EnumSet<AttributeProperty> properties = EnumSet.noneOf(AttributeProperty.class);

    public Attribute(IType parent, String id) {
        super(parent, id);
        name = ""; //$NON-NLS-1$
        initPropertyDefaultChangingOverTime();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }

    @Override
    public ValueDatatype findDatatype(IIpsProject project) {
        return project.findValueDatatype(datatype);
    }

    public ValueDatatype findValueDatatype(IIpsProject project) {
        return project.findValueDatatype(datatype);
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(String newValue) {
        String oldValue = defaultValue;
        defaultValue = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    public boolean isOverwrite() {
        return overwrites;
    }

    @Override
    public void setOverwrite(boolean overwrites) {
        boolean old = this.overwrites;
        this.overwrites = overwrites;
        valueChanged(old, overwrites, PROPERTY_OVERWRITES);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        initPropertyDefaultChangingOverTime();
        if (element.hasAttribute(PROPERTY_CHANGING_OVER_TIME)) {
            String changingOverTimeAttribute = element.getAttribute(PROPERTY_CHANGING_OVER_TIME);
            setProperty(AttributeProperty.CHANGING_OVER_TIME, Boolean.parseBoolean(changingOverTimeAttribute));
        }
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        defaultValue = ValueToXmlHelper.getValueFromElement(element, "DefaultValue"); //$NON-NLS-1$
        overwrites = ValueToXmlHelper.isAttributeTrue(element, PROPERTY_OVERWRITES);
    }

    protected abstract void initPropertyDefaultChangingOverTime();

    protected void setProperty(AttributeProperty property, boolean state) {
        if (state) {
            properties.add(property);
        } else {
            properties.remove(property);
        }
    }

    protected boolean isPropertySet(AttributeProperty property) {
        return properties.contains(property);
    }

    @Override
    public boolean isChangingOverTime() {
        return isPropertySet(AttributeProperty.CHANGING_OVER_TIME);
    }

    @Override
    public void setChangingOverTime(boolean changesOverTime) {
        boolean oldValue = isPropertySet(AttributeProperty.CHANGING_OVER_TIME);
        setProperty(AttributeProperty.CHANGING_OVER_TIME, changesOverTime);
        valueChanged(oldValue, changesOverTime);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_CHANGING_OVER_TIME, String.valueOf(isChangingOverTime()));
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        ValueToXmlHelper.addValueToElement(defaultValue, element, "DefaultValue"); //$NON-NLS-1$
        element.setAttribute(PROPERTY_OVERWRITES, "" + overwrites); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        IStatus status = ValidationUtils.validateFieldName(name, ipsProject);
        if (!status.isOK()) {
            String invalidName = (StringUtils.isNotEmpty(name)) ? " " + name : ""; //$NON-NLS-1$ //$NON-NLS-2$
            result.add(new Message(MSGCODE_INVALID_ATTRIBUTE_NAME, NLS.bind(
                    Messages.Attribute_msg_InvalidAttributeName, invalidName), Message.ERROR, this, PROPERTY_NAME)); // $NON-NLS-1$
        }
        ValueDatatype datatypeObject = ValidationUtils.checkValueDatatypeReference(getDatatype(), false, this,
                PROPERTY_DATATYPE, "", result); //$NON-NLS-1$
        if (datatypeObject != null) {
            validateDefaultValue(datatypeObject, result, ipsProject);
        } else {
            if (!StringUtils.isEmpty(defaultValue)) {
                String text = NLS.bind(Messages.Attribute_msg_DefaultNotParsable_UnknownDatatype, defaultValue);
                result.add(new Message(MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE, text, Message.WARNING, this,
                        PROPERTY_DEFAULT_VALUE));
            }
        }

        validateOverwritingAttribute(result, ipsProject);
        validateAbstractDatatype(result, ipsProject);
    }

    private void validateOverwritingAttribute(MessageList result, IIpsProject ipsProject) throws CoreException {
        if (overwrites) {
            IAttribute superAttr = findOverwrittenAttribute(ipsProject);
            if (superAttr == null) {
                String text = NLS.bind(Messages.Attribute_msgNothingToOverwrite, getName());
                result.add(new Message(MSGCODE_NOTHING_TO_OVERWRITE, text, Message.ERROR, this,
                        PROPERTY_OVERWRITES, PROPERTY_NAME));
            } else {
                validateAgainstOverwrittenAttribute(result, superAttr);
            }
        }
    }

    private void validateAgainstOverwrittenAttribute(MessageList result, IAttribute superAttr) {
        validateOverwrittenDatatype(superAttr, result);
        if (!getValueSet().isDetailedSpecificationOf(superAttr.getValueSet())) {
            String text = NLS.bind(Messages.Attribute_ValueSet_not_SubValueSet_of_the_overridden_attribute,
                    getParent().getName() + '.' + getName(),
                    superAttr.getParent().getName() + '.' + superAttr.getName());
            String code = MSGCODE_OVERWRITTEN_ATTRIBUTE_INCOMPAIBLE_VALUESET;
            result.newError(code, text, getValueSet(), IEnumValueSet.PROPERTY_VALUES);
        }
        validateNullIncompatible(result, superAttr.getValueSet());
        if (isChangingOverTimeValidationNecessary() && ((Attribute)superAttr).isChangingOverTimeValidationNecessary()
                && hasSuperAttributeDifferentChangingOverTime(superAttr)) {
            result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME,
                    Messages.Attribute_msgOverwritten_ChangingOverTimeAttribute_different, Message.ERROR, this,
                    PROPERTY_CHANGING_OVER_TIME));
        }
        if (!getModifier().equals(superAttr.getModifier())) {
            result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER,
                    Messages.Attribute_msg_Overwritten_modifier_different, Message.ERROR, this, PROPERTY_MODIFIER));
        }
    }

    protected abstract boolean isChangingOverTimeValidationNecessary();

    private boolean hasSuperAttributeDifferentChangingOverTime(IAttribute superAttr) {
        return isChangingOverTime() != superAttr.isChangingOverTime();
    }

    protected void validateOverwrittenDatatype(IAttribute superAttr, MessageList result) {
        if (!DatatypeUtil.isCovariant(findDatatype(getIpsProject()), superAttr.findDatatype(getIpsProject()))) {
            result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE,
                    NLS.bind(Messages.Attribute_msg_Overwritten_type_incompatible, getName()), Message.ERROR, this,
                    PROPERTY_DATATYPE));
        }
    }

    protected void validateAbstractDatatype(MessageList result, IIpsProject ipsProject) {
        if (!getType().isAbstract()) {
            new AttributeAbstractDatatypeValidator(this, ipsProject).validateNotAbstractDatatype(result);
        }
    }

    private void validateNullIncompatible(MessageList list, IValueSet modelValueSet) {
        new ValueSetNullIncompatibleValidator(modelValueSet, getValueSet()).validateAndAppendMessages(list);
    }

    @Override
    public IAttribute findOverwrittenAttribute(IIpsProject ipsProject) throws CoreException {
        IType supertype = ((IType)getIpsObject()).findSupertype(ipsProject);
        if (supertype == null) {
            return null;
        }
        IAttribute candidate = supertype.findAttribute(name, ipsProject);
        if (candidate == this) {
            // can happen if we have a cycle in the type hierarchy!
            return null;
        }
        return candidate;
    }

    protected void validateDefaultValue(ValueDatatype valueDatatype, MessageList result, IIpsProject ipsProject)
            throws CoreException {
        validateDefaultValue(defaultValue, valueDatatype, result, ipsProject);
    }

    protected void validateDefaultValue(String defaultValueToValidate,
            ValueDatatype valueDatatype,
            MessageList result,
            IIpsProject ipsProject) throws CoreException {
        if (!isValueParsable(defaultValueToValidate, valueDatatype)) {
            addMessageDatatypeMissmatch(defaultValueToValidate, result);
        } else if (!isValueInValueSet(defaultValueToValidate, ipsProject)) {
            addMessageDefaultValueNotInValueSet(defaultValueToValidate, result);
        }
    }

    private boolean isValueParsable(String defaultValueToValidate, ValueDatatype valueDatatype) {
        return valueDatatype.isParsable(defaultValueToValidate);
    }

    private boolean isValueInValueSet(String defaultValueToValidate, IIpsProject ipsProject) throws CoreException {
        IValueSet valueSet = getValueSet();
        return valueSet == null || defaultValueToValidate == null
                || valueSet.containsValue(defaultValueToValidate, ipsProject);
    }

    private void addMessageDatatypeMissmatch(String defaultValueToValidate, MessageList result) {
        String defaultValueInMsg = defaultValueToValidate;
        if (defaultValueToValidate == null) {
            defaultValueInMsg = IIpsModelExtensions.get().getModelPreferences().getNullPresentation();
        } else if (StringUtils.isEmpty(defaultValueToValidate)) {
            defaultValueInMsg = Messages.Attribute_msg_DefaultValueIsEmptyString;
        }
        String text = NLS.bind(Messages.Attribute_msg_ValueTypeMismatch, defaultValueInMsg, getDatatype());
        result.newError(MSGCODE_VALUE_NOT_PARSABLE, text, this, PROPERTY_DEFAULT_VALUE);
    }

    private void addMessageDefaultValueNotInValueSet(String defaultValueToValidate, MessageList result) {
        result.add(new Message(MSGCODE_DEFAULT_NOT_IN_VALUESET,
                NLS.bind(Messages.Attribute_msg_DefaultNotInValueset, defaultValueToValidate), Message.WARNING, this,
                PROPERTY_DEFAULT_VALUE));
    }

}
