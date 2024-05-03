/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.InternationalStringXmlHelper;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.productcmpttype.ChangingOverTimePropertyValidator;
import org.faktorips.devtools.model.internal.type.TypePart;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.IValidationRuleMessageText;
import org.faktorips.devtools.model.pctype.MessageSeverity;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.util.MarkerEnumUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.LocalizedString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ValidationRule extends TypePart implements IValidationRule {

    private static final String XML_TAG_VALIDATED_ATTRIBUTE = "ValidatedAttribute"; //$NON-NLS-1$
    private static final String XML_TAG_MARKERS = "Markers"; //$NON-NLS-1$
    private static final String XML_TAG_MARKER = "Marker"; //$NON-NLS-1$

    private final ValidationRuleMessageText msgText;

    private String msgCode = ""; //$NON-NLS-1$

    private List<String> validatedAttributes = new ArrayList<>();

    private MessageSeverity msgSeverity = MessageSeverity.ERROR;

    private boolean validatedAttrSpecifiedInSrc = false;

    private boolean configurableByProductComponent = false;

    private boolean activatedByDefault = true;

    private boolean changingOverTime = true;

    /** The markers that are applied to this rule. */
    private List<String> markers = new ArrayList<>();

    /**
     * Flag which is <code>true</code> if this rule is a default rule for validating the value of an
     * attribute against the value set defined for the attribute. Default means, that the rule is
     * not a manually build rule - it is an automatically created rule. The creation of this rule
     * has to be allowed by the user.
     */
    private boolean checkValueAgainstValueSetRule = false;

    private boolean overwrites;

    /**
     * Creates a new validation rule definition.
     * 
     * @param pcType The type the rule belongs to.
     * @param id The rule's unique id within the type.
     */
    public ValidationRule(IPolicyCmptType pcType, String id) {
        super(pcType, id);
        msgText = new ValidationRuleMessageText($ -> objectHasChanged());
        initDefaultChangingOverTime();
    }

    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        ValidationUtils.checkStringPropertyNotEmpty(name, "name", this, //$NON-NLS-1$
                PROPERTY_NAME, "", list); //$NON-NLS-1$
        if (IpsStringUtils.isEmpty(msgCode)) {
            String text = Messages.ValidationRule_msgCodeShouldBeProvided;
            Message msg = new Message(IValidationRule.MSGCODE_MSGCODE_SHOULDNT_BE_EMPTY, text, Message.ERROR, this,
                    PROPERTY_MESSAGE_CODE);
            list.add(msg);
        }

        validateValidatedAttribute(list, ipsProject);
        validateCheckValueAgainstValueSet(list);

        validateNoLineSeperators(list);
        validateReplacementParameters(ipsProject, list);

        validateMarker(list, ipsProject);

        validateChangingOverTimeFlag(list);

        validateOverwritingValidationRule(list, ipsProject);
    }

    private void validateOverwritingValidationRule(MessageList result, IIpsProject ipsProject) {
        if (overwrites) {
            IValidationRule superRule = findOverwrittenValidationRule(ipsProject);
            if (superRule == null) {
                String text = MessageFormat.format(Messages.ValidationRule_msgNothingToOverwrite, getName());
                result.add(new Message(MSGCODE_NOTHING_TO_OVERWRITE, text, Message.ERROR, this,
                        PROPERTY_OVERWRITES, PROPERTY_NAME));
            } else {
                validateAgainstOverwrittenValidationRule(result, superRule);
            }
        }
    }

    private void validateAgainstOverwrittenValidationRule(MessageList result, IValidationRule superRule) {
        if (isChangingOverTime() != superRule.isChangingOverTime()) {
            result.add(new Message(MSGCODE_OVERWRITTEN_RULE_HAS_DIFFERENT_CHANGE_OVER_TIME,
                    Messages.ValidationRule_msgOverwritten_ChangingOverTimeAttribute_different, Message.ERROR, this,
                    PROPERTY_CHANGING_OVER_TIME));
        }
    }

    private void validateChangingOverTimeFlag(MessageList result) {
        if (!isConfigurableByProductComponent()) {
            return;
        }
        ChangingOverTimePropertyValidator propertyValidator = new ChangingOverTimePropertyValidator(this);
        propertyValidator.validateTypeDoesNotAcceptChangingOverTime(result);
    }

    private void validateMarker(MessageList list, IIpsProject ipsProject) {
        MarkerEnumUtil markerEnumUtil = new MarkerEnumUtil(ipsProject);
        Set<String> definedMarkerIds = markerEnumUtil.getDefinedMarkerIds();
        Set<String> usedMarkerIds = getUsedMarkerIds();
        usedMarkerIds.removeAll(definedMarkerIds);
        if (!usedMarkerIds.isEmpty()) {
            String text = MessageFormat.format(Messages.ValidationRule_msg_InvalidMarkerId, usedMarkerIds,
                    markerEnumUtil.getMarkerEnumTypeName());
            Message msg = new Message(IValidationRule.MSGCODE_INVALID_MARKER_ID, text, Message.ERROR, this,
                    PROPERTY_MESSAGE_CODE);
            list.add(msg);
        }
    }

    private Set<String> getUsedMarkerIds() {
        Set<String> usedMarkerIds = new LinkedHashSet<>();
        for (String usedMarker : markers) {
            usedMarkerIds.add(usedMarker);
        }
        return usedMarkerIds;
    }

    private void validateCheckValueAgainstValueSet(MessageList msgList) {
        if (isCheckValueAgainstValueSetRule()) {
            String attributeName = getValidatedAttributeAt(0);
            IPolicyCmptTypeAttribute attribute = getPolicyCmptType().getPolicyCmptTypeAttribute(attributeName);
            if (attribute == null) {
                return;
            }
            if (ValueSetType.UNRESTRICTED.equals(attribute.getValueSet().getValueSetType())
                    && attribute.getValueSet().isContainsNull() && !attribute.isProductRelevant()) {
                String text = Messages.ValidationRule_msgValueSetRule;
                msgList.add(new Message("", text, Message.ERROR, this, //$NON-NLS-1$
                        IValidationRule.PROPERTY_CHECK_AGAINST_VALUE_SET_RULE));
            }
        }
    }

    private PolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }

    private void validateValidatedAttribute(MessageList list, IIpsProject ipsProject) {
        List<IAttribute> attributes = getPolicyCmptType().getSupertypeHierarchy().getAllAttributes(getPolicyCmptType());
        Set<String> attributeNames = new HashSet<>(attributes.size());
        for (IAttribute attribute : attributes) {
            attributeNames.add(attribute.getName());
        }
        for (int i = 0; i < validatedAttributes.size(); i++) {
            String validatedAttribute = validatedAttributes.get(i);
            if (!attributeNames.contains(validatedAttribute)) {
                String text = Messages.ValidationRule_msgUndefinedAttribute;
                list.add(new Message(MSGCODE_UNDEFINED_ATTRIBUTE, text, Message.ERROR,
                        new ObjectProperty(this, "validatedAttributes", i))); //$NON-NLS-1$
            } else {
                IPolicyCmptTypeAttribute attribute = getPolicyCmptType().findPolicyCmptTypeAttribute(validatedAttribute,
                        ipsProject);
                if (attribute.getAttributeType() == AttributeType.CONSTANT) {
                    list.add(new Message(IValidationRule.MSGCODE_CONSTANT_ATTRIBUTES_CANT_BE_VALIDATED,
                            Messages.ValidationRule_ConstantAttributesCantBeValidated, Message.ERROR,
                            new ObjectProperty(this, "validatedAttributes", i))); //$NON-NLS-1$
                }
            }
        }

        for (int i = 0; i < validatedAttributes.size() - 1; i++) {
            for (int r = i + 1; r < validatedAttributes.size(); r++) {
                if (validatedAttributes.get(i).equals(validatedAttributes.get(r))) {
                    String text = Messages.ValidationRule_msgDuplicateEntries;
                    list.add(new Message("", text, Message.WARNING, //$NON-NLS-1$
                            new ObjectProperty(this, "validatedAttributes", i), //$NON-NLS-1$
                            new ObjectProperty(this, "validatedAttributes", r))); //$NON-NLS-1$
                }
            }
        }
    }

    private void validateNoLineSeperators(MessageList list) {
        for (LocalizedString localizedString : msgText.values()) {
            String message = localizedString.getValue();
            if (IpsStringUtils.isNotEmpty(System.lineSeparator())
                    && message.indexOf(System.lineSeparator()) != -1) {
                String text = MessageFormat.format(Messages.ValidationRule_msgNoNewlineAllowed,
                        localizedString.getLocale().getDisplayLanguage());
                list.add(new Message(IValidationRule.MSGCODE_NO_NEWLINE, text, Message.ERROR, this,
                        IValidationRule.PROPERTY_MESSAGE_TEXT));
            }
        }

    }

    private void validateReplacementParameters(IIpsProject ipsProject, MessageList list) {
        msgText.validateReplacementParameters(ipsProject, list);
    }

    @Override
    public IValidationRuleMessageText getMessageText() {
        return msgText;
    }

    @Override
    public String getMessageCode() {
        return msgCode;
    }

    @Override
    public void setMessageCode(String newCode) {
        String oldCode = msgCode;
        msgCode = newCode;
        valueChanged(oldCode, msgCode);
    }

    @Override
    public MessageSeverity getMessageSeverity() {
        return msgSeverity;
    }

    @Override
    public void setMessageSeverity(MessageSeverity newSeverity) {
        MessageSeverity oldSeverity = msgSeverity;
        msgSeverity = newSeverity;
        valueChanged(oldSeverity, msgSeverity);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        initDefaultChangingOverTime();
        name = element.getAttribute(PROPERTY_NAME);
        msgCode = element.getAttribute(PROPERTY_MESSAGE_CODE);
        msgSeverity = MessageSeverity.getMessageSeverity(element.getAttribute(PROPERTY_MESSAGE_SEVERITY));
        checkValueAgainstValueSetRule = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_CHECK_AGAINST_VALUE_SET_RULE);
        validatedAttrSpecifiedInSrc = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC);
        configurableByProductComponent = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT);
        overwrites = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_OVERWRITES);
        if (element.hasAttribute(PROPERTY_CHANGING_OVER_TIME)) {
            changingOverTime = Boolean.parseBoolean(element.getAttribute(PROPERTY_CHANGING_OVER_TIME));
        }
        if (element.hasAttribute(PROPERTY_ACTIVATED_BY_DEFAULT)) {
            activatedByDefault = Boolean.parseBoolean(element.getAttribute(PROPERTY_ACTIVATED_BY_DEFAULT));
        } else {
            /*
             * Preserve true as default value if attribute does not exist. This maintains
             * compatibility to rules that have not yet been migrated.
             */
            activatedByDefault = true;
        }

        NodeList nl = element.getChildNodes();
        validatedAttributes.clear();
        markers.clear();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element subElement = (Element)nl.item(i);
                initChildrenFor(XML_TAG_VALIDATED_ATTRIBUTE, validatedAttributes, subElement);
                initChildrenForMarkers(subElement);
                if (subElement.getNodeName().equals(XML_TAG_MSG_TXT)) {
                    InternationalStringXmlHelper.initFromXml(msgText, subElement);
                }
            }
        }
    }

    private void initChildrenFor(String elementType, List<String> childElements, Element subElement) {
        if (subElement.getNodeName().equals(elementType)) {
            childElements.add(subElement.getAttribute("name")); //$NON-NLS-1$
        }
    }

    private void initChildrenForMarkers(Element subElement) {
        if (subElement.getNodeName().equals(XML_TAG_MARKERS)) {
            NodeList childNodes = subElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i) instanceof Element) {
                    initChildrenFor(XML_TAG_MARKER, markers, (Element)childNodes.item(i));
                }
            }
        }
    }

    private void initDefaultChangingOverTime() {
        IProductCmptType productCmptType = findProductCmptType(getIpsProject());
        changingOverTime = productCmptType != null && productCmptType.isChangingOverTime();
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_NAME, name);
        newElement.setAttribute(PROPERTY_MESSAGE_CODE, msgCode);
        newElement.setAttribute(PROPERTY_MESSAGE_SEVERITY, msgSeverity.getId());
        if (overwrites) {
            newElement.setAttribute(PROPERTY_OVERWRITES, "" + overwrites); //$NON-NLS-1$
        }
        if (checkValueAgainstValueSetRule) {
            newElement.setAttribute(PROPERTY_CHECK_AGAINST_VALUE_SET_RULE,
                    String.valueOf(checkValueAgainstValueSetRule));
        }
        if (validatedAttrSpecifiedInSrc) {
            newElement.setAttribute(PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC,
                    String.valueOf(validatedAttrSpecifiedInSrc));
        }
        if (configurableByProductComponent) {
            newElement.setAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCT_COMPONENT,
                    String.valueOf(configurableByProductComponent));
        }
        newElement.setAttribute(PROPERTY_ACTIVATED_BY_DEFAULT, String.valueOf(activatedByDefault));
        newElement.setAttribute(PROPERTY_CHANGING_OVER_TIME, String.valueOf(changingOverTime));
        appendChildrenFor(XML_TAG_VALIDATED_ATTRIBUTE, validatedAttributes, newElement);
        appendChildrenForMarkers(newElement);

        InternationalStringXmlHelper.toXml(msgText, newElement, XML_TAG_MSG_TXT);
    }

    private void appendChildrenFor(String elementType, List<String> childElements, Element newElement) {
        Document doc = newElement.getOwnerDocument();
        for (String childElement : childElements) {
            Element element = doc.createElement(elementType);
            element.setAttribute("name", childElement); //$NON-NLS-1$
            newElement.appendChild(element);
        }
    }

    private void appendChildrenForMarkers(Element newElement) {
        if (!markers.isEmpty()) {
            Document doc = newElement.getOwnerDocument();
            Element markersRootElement = doc.createElement(XML_TAG_MARKERS);
            newElement.appendChild(markersRootElement);
            appendChildrenFor(XML_TAG_MARKER, markers, markersRootElement);
        }
    }

    @Override
    public String addValidatedAttribute(String attributeName) {
        ArgumentCheck.notNull(this, attributeName);
        validatedAttributes.add(attributeName);
        objectHasChanged();
        return attributeName;
    }

    @Override
    public String[] getValidatedAttributes() {
        return validatedAttributes.toArray(new String[validatedAttributes.size()]);
    }

    @Override
    public void removeValidatedAttribute(int index) {
        validatedAttributes.remove(index);
        objectHasChanged();
    }

    @Override
    public String getValidatedAttributeAt(int index) {
        return validatedAttributes.get(index);
    }

    @Override
    public void setValidatedAttributeAt(int index, String attributeName) {
        String oldValue = getValidatedAttributeAt(index);
        validatedAttributes.set(index, attributeName);
        valueChanged(oldValue, attributeName);
    }

    @Override
    public boolean isValidatedAttrSpecifiedInSrc() {
        return validatedAttrSpecifiedInSrc;
    }

    @Override
    public void setValidatedAttrSpecifiedInSrc(boolean validatedAttrSpecifiedInSrc) {
        boolean oldValue = this.validatedAttrSpecifiedInSrc;
        this.validatedAttrSpecifiedInSrc = validatedAttrSpecifiedInSrc;
        valueChanged(oldValue, validatedAttrSpecifiedInSrc);
    }

    @Override
    public boolean isCheckValueAgainstValueSetRule() {
        return checkValueAgainstValueSetRule;
    }

    @Override
    public void setCheckValueAgainstValueSetRule(boolean isAttributeValueValidationRule) {
        boolean oldValue = isCheckValueAgainstValueSetRule();
        checkValueAgainstValueSetRule = isAttributeValueValidationRule;
        valueChanged(oldValue, isAttributeValueValidationRule);
    }

    @Override
    public boolean isConfigurableByProductComponent() {
        IPolicyCmptType type = (IPolicyCmptType)getIpsObject();
        if (type == null || !type.isConfigurableByProductCmptType()) {
            return false;
        }
        return configurableByProductComponent;
    }

    @Override
    public void setConfigurableByProductComponent(boolean configurable) {
        boolean oldValue = isConfigurableByProductComponent();
        configurableByProductComponent = configurable;
        valueChanged(oldValue, configurable);
    }

    @Override
    public boolean isActivatedByDefault() {
        return activatedByDefault;
    }

    @Override
    public void setActivatedByDefault(boolean activated) {
        boolean oldValue = isActivatedByDefault();
        activatedByDefault = activated;
        valueChanged(oldValue, activated);
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return ProductCmptPropertyType.VALIDATION_RULE;
    }

    @Override
    public List<PropertyValueType> getPropertyValueTypes() {
        return Arrays.asList(PropertyValueType.VALIDATION_RULE_CONFIG);
    }

    @Override
    public String getPropertyName() {
        return getName();
    }

    @Override
    public String getQualifiedRuleName() {
        IIpsObject ipsObject = getIpsObject();
        String qualifiedName = ipsObject.getQualifiedName();
        String ruleName = getName();
        return qualifiedName + QNAME_SEPARATOR + ruleName;
    }

    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    @Override
    public void setChangingOverTime(boolean changingOverTime) {
        boolean oldVal = this.changingOverTime;
        this.changingOverTime = changingOverTime;
        valueChanged(oldVal, changingOverTime, PROPERTY_CHANGING_OVER_TIME);
    }

    @Override
    public String getPropertyDatatype() {
        return ValueDatatype.BOOLEAN.getName();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        return getPolicyCmptType().findProductCmptType(ipsProject);
    }

    @Override
    public boolean isPolicyCmptTypeProperty() {
        return true;
    }

    @Override
    public boolean isPropertyFor(IPropertyValue propertyValue) {
        return getProductCmptPropertyType().isMatchingPropertyValue(getPropertyName(), propertyValue);
    }

    @Override
    public List<String> getMarkers() {
        return markers;
    }

    @Override
    public void setMarkers(List<String> newMarkers) {
        List<String> oldMarkers = markers;
        markers = newMarkers;
        valueChanged(oldMarkers, newMarkers, PROPERTY_MARKERS);
    }

    @Override
    public boolean overrides(IValidationRule other) {
        if (equals(other) || !getType().isSubtypeOf(other.getType(), other.getIpsProject())) {
            return false;
        }
        return true;
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
    public IValidationRule findOverwrittenValidationRule(IIpsProject ipsProject) throws IpsException {
        IPolicyCmptType supertype = (IPolicyCmptType)((IType)getIpsObject()).findSupertype(ipsProject);
        if (supertype == null) {
            return null;
        }
        IValidationRule candidate = supertype.findValidationRule(name, ipsProject);
        if (candidate == this) {
            // can happen if we have a cycle in the type hierarchy!
            return null;
        }
        return candidate;
    }
}
