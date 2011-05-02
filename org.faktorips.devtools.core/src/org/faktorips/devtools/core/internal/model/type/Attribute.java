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

package org.faktorips.devtools.core.internal.model.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.runtime.internal.ValueToXmlHelper;
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
public abstract class Attribute extends IpsObjectPart implements IAttribute {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private String datatype = ""; //$NON-NLS-1$
    private Modifier modifier = Modifier.PUBLISHED;
    private String defaultValue = null;

    public Attribute(IIpsObject parent, String id) {
        super(parent, id);
        name = ""; //$NON-NLS-1$
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public IType getType() {
        return (IType)getParent();
    }

    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    @Override
    public Modifier getModifier() {
        return modifier;
    }

    @Override
    public void setModifier(Modifier newModifer) {
        ArgumentCheck.notNull(newModifer);
        Modifier oldModifier = modifier;
        modifier = newModifer;
        valueChanged(oldModifier, newModifer);
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
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        if (modifier == null) {
            modifier = Modifier.PUBLISHED;
        }
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        defaultValue = ValueToXmlHelper.getValueFromElement(element, "DefaultValue"); //$NON-NLS-1$    
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        ValueToXmlHelper.addValueToElement(defaultValue, element, "DefaultValue"); //$NON-NLS-1$
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
