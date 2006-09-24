/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @inheritDoc
 */
public class ValidationRule extends IpsObjectPart implements IValidationRule {

	final static String TAG_NAME = "ValidationRuleDef"; //$NON-NLS-1$

	private String msgText = ""; //$NON-NLS-1$

	private String msgCode = ""; //$NON-NLS-1$

	private List validatedAttributes = new ArrayList();

	private MessageSeverity msgSeverity = MessageSeverity.ERROR;

	// the qualified name of the business functions this rule is used in
	private ArrayList functions = new ArrayList(0);

	private boolean applyInAll;

	private boolean validatedAttrSpecifiedInSrc = false;

	/**
	 * Flag which is <code>true</code> if this rule is a default rule for validating the value of an attribute
	 * against the value set defined for the attribute. Default means, that the rule is not a manually 
	 * build rule - it is an automatically created rule. The creation of this rule has to be allowed by 
	 * the user.
	 */
	private boolean checkValueAgainstValueSetRule = false;
	
	/**
	 * Creates a new validation rule definition.
	 * 
	 * @param pcType
	 *            The type the rule belongs to.
	 * @param id
	 *            The rule's unique id within the type.
	 */
	public ValidationRule(IPolicyCmptType pcType, int id) {
		super(pcType, id);
	}

	/**
	 * Constructor for testing purposes.
	 */
	public ValidationRule() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete() {
		((PolicyCmptType) getIpsObject()).removeRule(this);
		objectHasChanged();
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


	/**
	 * {@inheritDoc}
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		valueChanged(oldName, newName);
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return IpsPlugin.getDefault().getImage("ValidationRuleDef.gif"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getBusinessFunctions() {
		return (String[]) functions.toArray(new String[functions.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBusinessFunctions(String[] functionNames) {
		functions.clear();
		for (int i = 0; i < functionNames.length; i++) {
			functions.add(functionNames[i]);
		}
		objectHasChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNumOfBusinessFunctions() {
		return functions.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addBusinessFunction(String functionName) {
		ArgumentCheck.notNull(functionName);
		functions.add(functionName);
		objectHasChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeBusinessFunction(int index) {
		functions.remove(index);
		objectHasChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBusinessFunction(int index) {
		return (String) functions.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBusinessFunctions(int index, String functionName) {
		ArgumentCheck.notNull(functionName);
		String oldName = getBusinessFunction(index);
		functions.set(index, functionName);
		valueChanged(oldName, functionName);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAppliedInAllBusinessFunctions() {
		return applyInAll;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAppliedInAllBusinessFunctions(boolean newValue) {
		boolean oldValue = applyInAll;
		applyInAll = newValue;
		valueChanged(oldValue, newValue);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList list) throws CoreException {
		super.validateThis(list);
		ValidationUtils.checkStringPropertyNotEmpty(name, "name", this, //$NON-NLS-1$
				PROPERTY_NAME, "", list); //$NON-NLS-1$
		IIpsProject project = getIpsProject();
		for (Iterator it = functions.iterator(); it.hasNext();) {
			String function = (String) it.next();
			if (StringUtils.isNotEmpty(function)) {
				if (project.findIpsObject(IpsObjectType.BUSINESS_FUNCTION,
						function) == null) {
					String text = NLS.bind(Messages.ValidationRule_msgFunctionNotExists, function);
					list.add(new Message("", text, Message.ERROR, function, //$NON-NLS-1$
							"name")); //$NON-NLS-1$
				} else {
					if (isAppliedInAllBusinessFunctions()) {
						String text = Messages.ValidationRule_msgIgnored;
						list.add(new Message("", text, Message.WARNING, //$NON-NLS-1$
								function, "name")); //$NON-NLS-1$
					}
				}
			}
		}
		validateValidatedAttribute(list);
	}

	private PolicyCmptType getPolicyCmptType() {
		return (PolicyCmptType) getIpsObject();
	}

	private void validateValidatedAttribute(MessageList list)
			throws CoreException {

		IAttribute[] attributes = getPolicyCmptType().getSupertypeHierarchy()
				.getAllAttributes(getPolicyCmptType());
		List attributeNames = new ArrayList(attributes.length);
		for (int i = 0; i < attributes.length; i++) {
			attributeNames.add(attributes[i].getName());
		}
		for (int i = 0; i < validatedAttributes.size(); i++) {
			String validatedAttribute = (String) validatedAttributes.get(i);
			if (!attributeNames.contains(validatedAttribute)) {
				String text = Messages.ValidationRule_msgUndefinedAttribute;
				list.add(new Message("", text, Message.ERROR, //$NON-NLS-1$
						new ObjectProperty(this, "validatedAttributes", i))); //$NON-NLS-1$
			}
		}

		for (int i = 0; i < validatedAttributes.size() - 1; i++) {

			for (int r = i + 1; r < validatedAttributes.size(); r++) {
				if (validatedAttributes.get(i).equals(
						validatedAttributes.get(r))) {
					String text = Messages.ValidationRule_msgDuplicateEntries;
					list.add(new Message("", text, Message.WARNING, //$NON-NLS-1$
							new ObjectProperty[] {
									new ObjectProperty(this,
											"validatedAttributes", i), //$NON-NLS-1$
									new ObjectProperty(this,
											"validatedAttributes", r) })); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessageText() {
		return msgText;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMessageText(String newText) {
		String oldText = msgText;
		msgText = newText;
		valueChanged(oldText, msgText);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessageCode() {
		return msgCode;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMessageCode(String newCode) {
		String oldCode = msgCode;
		msgCode = newCode;
		valueChanged(oldCode, msgCode);
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageSeverity getMessageSeverity() {
		return msgSeverity;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMessageSeverity(MessageSeverity newSeverity) {
		MessageSeverity oldSeverity = msgSeverity;
		msgSeverity = newSeverity;
		valueChanged(oldSeverity, msgSeverity);
	}

	
	
	/**
	 * {@inheritDoc}
	 */
	protected Element createElement(Document doc) {
		return doc.createElement(TAG_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		name = element.getAttribute(PROPERTY_NAME);
		applyInAll = Boolean.valueOf(
				element.getAttribute(PROPERTY_APPLIED_IN_ALL_FUNCTIONS))
				.booleanValue();
		msgCode = element.getAttribute(PROPERTY_MESSAGE_CODE);
		msgText = element.getAttribute(PROPERTY_MESSAGE_TEXT);
		msgSeverity = MessageSeverity.getMessageSeverity(element
				.getAttribute(PROPERTY_MESSAGE_SEVERITY));
		checkValueAgainstValueSetRule = Boolean.valueOf(
				element.getAttribute(PROPERTY_VALIDATES_ATTR_VALUE_AGAINST_VALUESET))
				.booleanValue();
		validatedAttrSpecifiedInSrc = Boolean.valueOf(
				element.getAttribute(PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC))
				.booleanValue();

		NodeList nl = element.getChildNodes();
		functions.clear();
		validatedAttributes.clear();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element subElement = (Element) nl.item(i);
				if (subElement.getNodeName().equals("BusinessFunction")) { //$NON-NLS-1$
					functions.add(subElement.getAttribute("name")); //$NON-NLS-1$
				}
				if (subElement.getNodeName().equals("ValidatedAttribute")) { //$NON-NLS-1$
					validatedAttributes.add(subElement.getAttribute("name")); //$NON-NLS-1$
				}
			}
		}
		functions.trimToSize();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element newElement) {
		super.propertiesToXml(newElement);
		newElement.setAttribute(PROPERTY_NAME, name);
		newElement.setAttribute(PROPERTY_APPLIED_IN_ALL_FUNCTIONS, String
				.valueOf(applyInAll));
		newElement.setAttribute(PROPERTY_MESSAGE_CODE, msgCode);
		newElement.setAttribute(PROPERTY_MESSAGE_TEXT, msgText);
		newElement.setAttribute(PROPERTY_MESSAGE_SEVERITY, msgSeverity.getId());
		newElement.setAttribute(PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC, String.valueOf(validatedAttrSpecifiedInSrc));
		newElement.setAttribute(PROPERTY_VALIDATES_ATTR_VALUE_AGAINST_VALUESET,
                String.valueOf(checkValueAgainstValueSetRule));
		Document doc = newElement.getOwnerDocument();
		for (int i = 0; i < functions.size(); i++) {
			Element fctElement = doc.createElement("BusinessFunction"); //$NON-NLS-1$
			fctElement.setAttribute("name", (String) functions.get(i)); //$NON-NLS-1$
			newElement.appendChild(fctElement);
		}
		for (int i = 0; i < validatedAttributes.size(); i++) {
			Element attrElement = doc.createElement("ValidatedAttribute"); //$NON-NLS-1$
			attrElement.setAttribute("name", (String) validatedAttributes //$NON-NLS-1$
					.get(i));
			newElement.appendChild(attrElement);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String addValidatedAttribute(String attributeName) {
		ArgumentCheck.notNull(this, attributeName);
		validatedAttributes.add(attributeName);
		objectHasChanged();
		return attributeName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getValidatedAttributes() {
		return (String[]) validatedAttributes
				.toArray(new String[validatedAttributes.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeValidatedAttribute(int index) {
		validatedAttributes.remove(index);
		objectHasChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValidatedAttributeAt(int index) {
		return (String) validatedAttributes.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValidatedAttributeAt(int index, String attributeName) {
		String oldValue = getValidatedAttributeAt(index);
		validatedAttributes.set(index, attributeName);
		valueChanged(oldValue, attributeName);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isValidatedAttrSpecifiedInSrc() {
		return validatedAttrSpecifiedInSrc;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValidatedAttrSpecifiedInSrc(
			boolean validatedAttrSpecifiedInSrc) {
		boolean oldValue = this.validatedAttrSpecifiedInSrc;
		this.validatedAttrSpecifiedInSrc = validatedAttrSpecifiedInSrc;
		valueChanged(oldValue, validatedAttrSpecifiedInSrc);
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCheckValueAgainstValueSetRule() {
		return checkValueAgainstValueSetRule;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCheckValueAgainstValueSetRule(boolean isAttributeValueValidationRule) {
		boolean oldValue = isCheckValueAgainstValueSetRule();
		this.checkValueAgainstValueSetRule = isAttributeValueValidationRule;
		valueChanged(oldValue, isAttributeValueValidationRule);
		
	}
	

}
