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

package org.faktorips.devtools.core.internal.model.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 * 
 * @author Jan Ortmann
 */
public abstract class Attribute extends TypePart implements IAttribute {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$

    private String defaultValue = null;

    private boolean overwrites;

    public Attribute(IType parent, String id) {
        super(parent, id);
        name = ""; //$NON-NLS-1$
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
    public ValueDatatype findDatatype(IIpsProject project) throws CoreException {
        return project.findValueDatatype(datatype);
    }

    public ValueDatatype findValueDatatype(IIpsProject project) throws CoreException {
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
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        defaultValue = ValueToXmlHelper.getValueFromElement(element, "DefaultValue"); //$NON-NLS-1$
        overwrites = Boolean.valueOf(element.getAttribute(PROPERTY_OVERWRITES)).booleanValue();
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
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
            result.add(new Message(MSGCODE_INVALID_ATTRIBUTE_NAME, Messages.Attribute_msg_InvalidAttributeName + name
                    + "!", Message.ERROR, this, PROPERTY_NAME)); //$NON-NLS-1$ 
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
        if (overwrites) {
            IAttribute superAttr = findOverwrittenAttribute(ipsProject);
            if (superAttr == null) {
                String text = NLS.bind(Messages.Attribute_msgNothingToOverwrite, getName());
                result.add(new Message(MSGCODE_NOTHING_TO_OVERWRITE, text, Message.ERROR, this, new String[] {
                        PROPERTY_OVERWRITES, PROPERTY_NAME }));
            } else {
                if (!getValueSet().isDetailedSpecificationOf(superAttr.getValueSet())) {
                    String text = Messages.Attribute_ValueSet_not_SubValueSet_of_the_overridden_attribute;
                    result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_INCOMPAIBLE_VALUESET, text, Message.ERROR, getValueSet()));
                }
                if (!getDatatype().equals(superAttr.getDatatype())) {
                    result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_DATATYPE,
                            Messages.Attribute_msg_Overwritten_datatype_different, Message.ERROR, this,
                            PROPERTY_DATATYPE));
                }
                if (!getModifier().equals(superAttr.getModifier())) {
                    result.add(new Message(MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER,
                            Messages.Attribute_msg_Overwritten_modifier_different, Message.ERROR, this,
                            PROPERTY_MODIFIER));
                }
            }
        }
    }

    @Override
    public IAttribute findOverwrittenAttribute(IIpsProject ipsProject) throws CoreException {
        IType supertype = ((IType)getIpsObject()).findSupertype(ipsProject);
        if (supertype == null) {
            return null;
        }
        IAttribute candidate = supertype.findAttribute(name, ipsProject);
        if (candidate == this) {
            return null; // can happen if we have a cycle in the type hierarchy!
        }
        return candidate;
    }

    private void validateDefaultValue(ValueDatatype valueDatatype, MessageList result, IIpsProject ipsProject)
            throws CoreException {

        if (!valueDatatype.isParsable(defaultValue)) {
            String defaultValueInMsg = defaultValue;
            if (defaultValue == null) {
                defaultValueInMsg = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            } else if (defaultValue.equals("")) { //$NON-NLS-1$
                defaultValueInMsg = Messages.Attribute_msg_DefaultValueIsEmptyString;
            }
            String text = NLS.bind(Messages.Attribute_msg_ValueTypeMismatch, defaultValueInMsg, getDatatype());
            result.add(new Message(MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, this, PROPERTY_DEFAULT_VALUE));
            return;
        }
        IValueSet valueSet = getValueSet();
        if (valueSet != null) {
            if (defaultValue != null && !valueSet.containsValue(defaultValue, ipsProject)) {
                result.add(new Message(MSGCODE_DEFAULT_NOT_IN_VALUESET, NLS.bind(
                        Messages.Attribute_msg_DefaultNotInValueset, defaultValue), Message.WARNING, this,
                        PROPERTY_DEFAULT_VALUE));
            }
        }
    }

}
