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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ValueSetNullIncompatibleValidator;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplateValueFinder;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.core.internal.model.valueset.DelegatingValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfigElement extends AbstractSimplePropertyValue implements IConfigElement {

    public static final String TAG_NAME = ValueToXmlHelper.XML_TAG_CONFIG_ELEMENT;

    private String pcTypeAttribute;

    private IValueSet valueSet;

    private String value = ""; //$NON-NLS-1$

    private final TemplateValueSettings templateValueSettings;

    public ConfigElement(IPropertyValueContainer parent, String id) {
        this(parent, id, ""); //$NON-NLS-1$
    }

    public ConfigElement(IPropertyValueContainer parent, String id, String pcTypeAttribute) {
        super(parent, id);
        this.pcTypeAttribute = pcTypeAttribute;
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
        this.templateValueSettings = new TemplateValueSettings(this);
    }

    /**
     * Returns the product component generation this configuration element belongs to.
     * 
     * @deprecated Config-elements can be used in a context other than product component
     *             generations. This method will then yield unexpected and or erroneous results. Use
     *             {@link #getParent()} instead.
     */
    @Override
    @Deprecated
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }

    @Override
    public String getPropertyName() {
        return pcTypeAttribute;
    }

    @Override
    public IProductCmptProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findPcTypeAttribute(ipsProject);
    }

    @Override
    public PropertyValueType getPropertyValueType() {
        return PropertyValueType.CONFIG_ELEMENT;
    }

    @Override
    public String getPropertyValue() {
        return getValueSet().toShortString();
    }

    @Override
    public String getPolicyCmptTypeAttribute() {
        return pcTypeAttribute;
    }

    @Override
    public void setPolicyCmptTypeAttribute(String newName) {
        String oldName = pcTypeAttribute;
        pcTypeAttribute = newName;
        name = pcTypeAttribute;
        valueChanged(oldName, pcTypeAttribute);
    }

    @Override
    public String getValue() {
        // TODO FIPS-4556 at the moment only value set is inherited from template
        // if (getTemplateValueStatus() == TemplateValueStatus.INHERITED) {
        // return findTemplateValue();
        // }
        // if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
        //            return ""; //$NON-NLS-1$
        // }
        return value;
    }

    // TODO FIPS-4556
    // private String findTemplateValue() {
    // IConfigElement templateConfigElement = findTemplateProperty(getIpsProject());
    // if (templateConfigElement == null) {
    // // Template should exist but does not. Use the "last known" value as a more or less
    // // helpful fallback while some validation hopefully addresses the missing template...
    // return value;
    // }
    // return templateConfigElement.getValue();
    // }

    @Override
    public void setValue(String newValue) {
        String oldValue = value;
        value = newValue;
        valueChanged(oldValue, value);
    }

    @Override
    public IPolicyCmptTypeAttribute findPcTypeAttribute(IIpsProject ipsProject) {
        IPolicyCmptType pcType = getPropertyValueContainer().findPolicyCmptType(ipsProject);
        if (pcType == null) {
            return null;
        }
        return pcType.findPolicyCmptTypeAttribute(pcTypeAttribute, ipsProject);
    }

    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) {
        IPolicyCmptTypeAttribute a = findPcTypeAttribute(ipsProject);
        if (a != null) {
            return a.findDatatype(ipsProject);
        }
        return null;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IPolicyCmptTypeAttribute attribute = validateReferenceToAttribute(list, ipsProject);
        if (attribute == null) {
            return;
        }
        list.add(templateValueSettings.validate(this, ipsProject));

        if (shouldValidateValueAndValueSet(attribute)) {
            validateValueAndValueSet(list, ipsProject, attribute);
        }
    }

    private boolean shouldValidateValueAndValueSet(IPolicyCmptTypeAttribute attribute) {
        if (getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
            return false;
        }
        return attribute.getAttributeType() == AttributeType.CHANGEABLE
                || attribute.getAttributeType() == AttributeType.CONSTANT;
    }

    private IPolicyCmptTypeAttribute validateReferenceToAttribute(MessageList list, IIpsProject ipsProject) {

        IPolicyCmptTypeAttribute attribute = findPcTypeAttribute(ipsProject);
        if (attribute == null) {
            IPolicyCmptType policyCmptType = getPropertyValueContainer().findPolicyCmptType(ipsProject);
            if (policyCmptType == null) {
                String text = NLS.bind(Messages.ConfigElement_policyCmptTypeNotFound, pcTypeAttribute);
                list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this,
                        PROPERTY_VALUE));
            } else {
                String policyCmptTypeLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(policyCmptType);
                String text = NLS.bind(Messages.ConfigElement_msgAttrNotDefined, pcTypeAttribute, policyCmptTypeLabel);
                list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this,
                        PROPERTY_VALUE));
            }
        }
        return attribute;
    }

    private void validateValueAndValueSet(MessageList list, IIpsProject ipsProject, IPolicyCmptTypeAttribute attribute)
            throws CoreException {
        ValueDatatype valueDatatype = attribute.findDatatype(ipsProject);
        validateValueVsDatatype(valueDatatype, list);
        if (valueDatatype != null) {
            validateValueVsValueSet(valueDatatype, ipsProject, list);
            validateValueSetVsAttributeValueSet(attribute, ipsProject, list);
        }
    }

    private void validateValueVsDatatype(ValueDatatype valueDatatype, MessageList list) throws CoreException {

        String valueToValidate = getValue();
        if (valueDatatype == null) {
            if (!StringUtils.isEmpty(valueToValidate)) {
                String text = Messages.ConfigElement_msgUndknownDatatype;
                list.add(new Message(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE, text, Message.WARNING, this,
                        PROPERTY_VALUE));
            }
            return;
        }
        try {
            if (valueDatatype.checkReadyToUse().containsErrorMsg()) {
                String text = Messages.ConfigElement_msgInvalidDatatype;
                list.add(new Message(IConfigElement.MSGCODE_INVALID_DATATYPE, text, Message.ERROR, this, PROPERTY_VALUE));
            }
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new CoreException(new IpsStatus(e));
        }

        if (!valueDatatype.isParsable(valueToValidate)) {
            addNotParsableMessage(valueToValidate, valueDatatype, list);
        }
    }

    private void validateValueSetVsAttributeValueSet(IPolicyCmptTypeAttribute attribute,
            IIpsProject ipsProject,
            MessageList list) throws CoreException {

        IValueSet valueSetToValidate = getValueSet();
        IValueSet modelValueSet = attribute.getValueSet();

        if (modelValueSet.validate(ipsProject).containsErrorMsg()) {
            String text = Messages.ConfigElement_msgInvalidAttributeValueset;
            list.add(new Message(IConfigElement.MSGCODE_UNKNWON_VALUESET, text, Message.WARNING, this, PROPERTY_VALUE));
            return;
        }
        if (valueSetToValidate.isAbstract()) {
            String text = Messages.ConfigElement_error_msg_abstractValueSet;
            list.add(new Message("", text, Message.ERROR, this, IConfigElement.PROPERTY_VALUE_SET)); //$NON-NLS-1$
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
            msgCode = IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH;
            text = NLS.bind(Messages.ConfigElement_msgTypeMismatch, new String[] {
                    modelValueSet.getValueSetType().getName(), valueSetToValidate.getValueSetType().getName() });
        } else if (!modelValueSet.containsValueSet(valueSetToValidate)) {
            msgCode = IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET;
            text = NLS.bind(Messages.ConfigElement_valueSetIsNotASubset, valueSetToValidate.toShortString(),
                    modelValueSet.toShortString());
        } else {
            // should never happen
            throw new RuntimeException();
        }
        // determine invalid property (usage e.g. to display problem marker on correct ui control)
        List<ObjectProperty> invalidObjectProperties = new ArrayList<ObjectProperty>();
        invalidObjectProperties.add(new ObjectProperty(this, IConfigElement.PROPERTY_VALUE_SET));
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

    private void validateValueVsValueSet(ValueDatatype valueDatatype, IIpsProject ipsProject, MessageList list)
            throws CoreException {
        String valueToValidate = getValue();
        IValueSet valueSetToValidate = getValueSet();

        if (StringUtils.isNotEmpty(valueToValidate)) {
            if (!valueSetToValidate.containsValue(valueToValidate, ipsProject)) {
                String formattedValue = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter()
                        .formatValue(valueDatatype, valueToValidate);
                list.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET, NLS.bind(
                        Messages.ConfigElement_msgValueNotInValueset, formattedValue), Message.ERROR, this,
                        PROPERTY_VALUE));
            }
        }
    }

    private void addNotParsableMessage(String value, ValueDatatype valueDatatype, MessageList msgList) {
        String valueInMsg = value;
        if (value == null) {
            valueInMsg = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else if (value.length() == 0) {
            valueInMsg = Messages.ConfigElement_msgValueIsEmptyString;
        }
        String text = NLS.bind(Messages.ConfigElement_msgValueNotParsable, valueInMsg, valueDatatype.getName());
        msgList.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, this, PROPERTY_VALUE));
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

    private IValueSet findTemplateValueSet() {
        IConfigElement templateConfigElement = findTemplateProperty(getIpsProject());
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
    public void setValueSetType(ValueSetType type) {
        IValueSet oldset = valueSet;
        valueSet = type.newValueSet(this, getNextPartId());
        valueChanged(oldset, valueSet);
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
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);

        value = ValueToXmlHelper.getValueFromElement(element, ValueToXmlHelper.XML_TAG_VALUE);

        pcTypeAttribute = element.getAttribute(ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE);
        name = pcTypeAttribute;
        templateValueSettings.initPropertiesFromXml(element);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE, pcTypeAttribute);
        ValueToXmlHelper.addValueToElement(getValue(), element, ValueToXmlHelper.XML_TAG_VALUE);
        templateValueSettings.propertiesToXml(element);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        if (getValueSet() != null) {
            return new IIpsElement[] { getValueSet() };
        }
        return new IIpsElement[0];
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
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // Nothing to do
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
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = null;
            return true;
        }
        return false;
    }

    public ValueDatatype getValueDatatype() {
        return findValueDatatype(getIpsProject());
    }

    @Override
    public boolean isValueSetUpdateable() {
        return true;
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);

        String caption = null;
        IAttribute attribute = findPcTypeAttribute(getIpsProject());
        if (attribute != null) {
            caption = attribute.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(pcTypeAttribute);
    }

    @Override
    public void setTemplateValueStatus(TemplateValueStatus newStatus) {
        if (newStatus == TemplateValueStatus.DEFINED) {
            // Copy value/value set from template (if present)
            // TODO FIPS-4556 at the moment only value set is inherited from template
            // this.value = getValue();
            copyValueSet();
        }
        TemplateValueStatus oldValue = templateValueSettings.getStatus();
        templateValueSettings.setStatus(newStatus);
        objectHasChanged(new PropertyChangeEvent(this, PROPERTY_TEMPLATE_VALUE_STATUS, oldValue, newStatus));
    }

    private void copyValueSet() {
        this.valueSet = getValueSet().copy(this, getNextPartId());
    }

    @Override
    public TemplateValueStatus getTemplateValueStatus() {
        return templateValueSettings.getStatus();
    }

    @Override
    public IConfigElement findTemplateProperty(IIpsProject ipsProject) {
        return TemplateValueFinder.findTemplateValue(this, IConfigElement.class);
    }

}
