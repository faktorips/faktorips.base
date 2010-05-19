/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfigElement extends IpsObjectPart implements IConfigElement {

    final static String TAG_NAME = "ConfigElement"; //$NON-NLS-1$

    private String pcTypeAttribute = ""; //$NON-NLS-1$

    private IValueSet valueSet;

    private String value = ""; //$NON-NLS-1$

    public ConfigElement(ProductCmptGeneration parent, String id) {
        super(parent, id);
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
    }

    public ConfigElement(ProductCmptGeneration parent, String id, String pcTypeAttribute) {
        super(parent, id);
        this.pcTypeAttribute = pcTypeAttribute;
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
    }

    @Override
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent().getParent();
    }

    @Override
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }

    @Override
    public String getPropertyName() {
        return pcTypeAttribute;
    }

    @Override
    public IProdDefProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findPcTypeAttribute(ipsProject);
    }

    @Override
    public ProdDefPropertyType getPropertyType() {
        return ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET;
    }

    @Override
    public String getPropertyValue() {
        return value;
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
        return value;
    }

    @Override
    public void setValue(String newValue) {
        String oldValue = value;
        value = newValue;
        valueChanged(oldValue, value);
    }

    @Override
    public IPolicyCmptTypeAttribute findPcTypeAttribute(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType pcType = getProductCmpt().findPolicyCmptType(ipsProject);
        if (pcType == null) {
            return null;
        }
        return pcType.findPolicyCmptTypeAttribute(pcTypeAttribute, ipsProject);
    }

    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException {
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
        if (attribute.getAttributeType() == AttributeType.CHANGEABLE
                || attribute.getAttributeType() == AttributeType.CONSTANT) {

            validateValueAndValueSet(list, ipsProject, attribute);
        }
    }

    private IPolicyCmptTypeAttribute validateReferenceToAttribute(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        IPolicyCmptTypeAttribute attribute = findPcTypeAttribute(ipsProject);
        if (attribute == null) {
            IPolicyCmptType policyCmptType = getProductCmpt().findPolicyCmptType(ipsProject);
            if (policyCmptType == null) {
                String text = NLS.bind(Messages.ConfigElement_policyCmptTypeNotFound, pcTypeAttribute);
                list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this,
                        PROPERTY_VALUE));
            } else {
                String text = NLS.bind(Messages.ConfigElement_msgAttrNotDefined, pcTypeAttribute, policyCmptType
                        .getName());
                list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this,
                        PROPERTY_VALUE));
            }
        }
        return attribute;
    }

    private void validateValueAndValueSet(MessageList list, IIpsProject ipsProject, IPolicyCmptTypeAttribute attribute)
            throws CoreException {

        ValueDatatype valueDatatype = validateValueVsDatatype(attribute, ipsProject, list);
        if (valueDatatype != null) {
            validateValueVsValueSet(ipsProject, list);
            validateValueSetVsAttributeValueSet(attribute, ipsProject, list);
        }
    }

    private ValueDatatype validateValueVsDatatype(IPolicyCmptTypeAttribute attribute,
            IIpsProject ipsProject,
            MessageList list) throws CoreException {

        ValueDatatype valueDatatype = attribute.findDatatype(ipsProject);
        if (valueDatatype == null) {
            if (!StringUtils.isEmpty(value)) {
                String text = Messages.ConfigElement_msgUndknownDatatype;
                list.add(new Message(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE, text, Message.WARNING, this,
                        PROPERTY_VALUE));
            }
            return null;
        }
        try {
            if (valueDatatype.checkReadyToUse().containsErrorMsg()) {
                String text = Messages.ConfigElement_msgInvalidDatatype;
                list
                        .add(new Message(IConfigElement.MSGCODE_INVALID_DATATYPE, text, Message.ERROR, this,
                                PROPERTY_VALUE));
                return null;
            }
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }

        if (!valueDatatype.isParsable(value)) {
            addNotParsableMessage(value, valueDatatype, list);
            return null;
        }
        return valueDatatype;
    }

    private void validateValueSetVsAttributeValueSet(IPolicyCmptTypeAttribute attribute,
            IIpsProject ipsProject,
            MessageList list) throws CoreException {

        IValueSet modelValueSet = attribute.getValueSet();
        if (modelValueSet.validate(ipsProject).containsErrorMsg()) {
            String text = Messages.ConfigElement_msgInvalidAttributeValueset;
            list.add(new Message(IConfigElement.MSGCODE_UNKNWON_VALUESET, text, Message.WARNING, this, PROPERTY_VALUE));
            return;
        }
        if (valueSet.isAbstract()) {
            String text = "Must specify a concrete set of values!";
            list.add(new Message("", text, Message.ERROR, this, IConfigElement.PROPERTY_VALUE_SET));
            return;
        }
        if (valueSet.isDetailedSpecificationOf(modelValueSet)) {
            // situations like model value set is unrestricted, and this value set is a range
            // are ok.
            return;
        }
        String msgCode;
        String text;
        if (!valueSet.isSameTypeOfValueSet(modelValueSet)) {
            msgCode = IConfigElement.MSGCODE_VALUESET_TYPE_MISMATCH;
            text = NLS.bind(Messages.ConfigElement_msgTypeMismatch, new String[] {
                    modelValueSet.getValueSetType().getName(), valueSet.getValueSetType().getName() });
        } else if (!modelValueSet.containsValueSet(valueSet)) {
            msgCode = IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET;
            text = NLS.bind(Messages.ConfigElement_valueSetIsNotASubset, valueSet.toShortString(), modelValueSet
                    .toShortString());
        } else {
            throw new RuntimeException(); // should never happen
        }
        // determine invalid property (usage e.g. to display problem marker on correct ui control)
        String[] invalidProperties = null;
        Object invalidObject = this;
        if (valueSet instanceof IEnumValueSet) {
            invalidProperties = new String[] { IConfigElement.PROPERTY_VALUE_SET };
        } else if (valueSet instanceof IRangeValueSet) {
            invalidObject = valueSet;
            invalidProperties = new String[] { IRangeValueSet.PROPERTY_LOWERBOUND, IRangeValueSet.PROPERTY_UPPERBOUND,
                    IRangeValueSet.PROPERTY_STEP };
        } else {
            invalidProperties = new String[] { PROPERTY_VALUE };
        }
        list.add(new Message(msgCode, text, Message.ERROR, invalidObject, invalidProperties));
    }

    private void validateValueVsValueSet(IIpsProject ipsProject, MessageList list) throws CoreException {

        if (StringUtils.isNotEmpty(value)) {
            if (!valueSet.containsValue(value, ipsProject)) {
                list.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET, NLS.bind(
                        Messages.ConfigElement_msgValueNotInValueset, value), Message.ERROR, this, PROPERTY_VALUE));
            }
        }
    }

    private void addNotParsableMessage(String value, ValueDatatype valueDatatype, MessageList msgList) {
        String valueInMsg = value;
        if (value == null) {
            valueInMsg = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else if (value.equals("")) {
            valueInMsg = Messages.ConfigElement_msgValueIsEmptyString;
        }
        String text = NLS.bind(Messages.ConfigElement_msgValueNotParsable, valueInMsg, valueDatatype.getName());
        msgList.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, this, PROPERTY_VALUE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueSetType(ValueSetType type) {
        IValueSet oldset = valueSet;
        valueSet = type.newValueSet(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueSet changeValueSetType(ValueSetType newType) {
        setValueSetType(newType);
        return valueSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueSetCopy(IValueSet source) {
        IValueSet oldset = valueSet;
        valueSet = source.copy(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reinitPartCollections() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = (IValueSet)part;
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = null;
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);

        value = ValueToXmlHelper.getValueFromElement(element, "Value"); //$NON-NLS-1$

        pcTypeAttribute = element.getAttribute("attribute");
        name = pcTypeAttribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("attribute", pcTypeAttribute); //$NON-NLS-1$
        ValueToXmlHelper.addValueToElement(value, element, "Value");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type" + partType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsElement[] getChildren() {
        List<IIpsElement> childrenList = new ArrayList<IIpsElement>(1);
        if (valueSet != null) {
            childrenList.add(valueSet);
        }
        return childrenList.toArray(new IIpsElement[0]);
    }

    @Override
    protected IIpsObjectPart newPart(Element partEl, String id) {
        String xmlTagName = partEl.getNodeName();
        if (ValueSet.XML_TAG.equals(xmlTagName)) {
            valueSet = ValueSetType.newValueSet(partEl, this, id);
            return valueSet;
        } else if (PROPERTY_VALUE.equalsIgnoreCase(xmlTagName)) {
            // ignore value nodes, will be parsed in the this#initPropertiesFromXml method
            return null;
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName);
    }

    public ValueDatatype getValueDatatype() {
        try {
            return findValueDatatype(getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        return null;
    }

    @Override
    public boolean isValueSetUpdateable() {
        return true;
    }

}
