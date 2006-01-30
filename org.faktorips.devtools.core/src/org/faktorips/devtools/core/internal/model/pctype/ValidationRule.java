package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
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

	final static String TAG_NAME = "ValidationRuleDef";

	private String msgText = "";

	private String msgCode = "";

	private List validatedAttributes = new ArrayList();

	private MessageSeverity msgSeverity = MessageSeverity.ERROR;

	// the qualified name of the business functions this rule is used in
	private ArrayList functions = new ArrayList(0);

	private boolean applyInAll;

	private boolean validatedAttrSpecifiedInSrc = false;

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
	 * @inheritDoc
	 */
	public void delete() {
		((PolicyCmptType) getIpsObject()).removeRule(this);
		updateSrcFile();
	}

	/**
	 * @inheritDoc
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		valueChanged(oldName, newName);
	}

	/**
	 * @inheritDoc
	 */
	public Image getImage() {
		return IpsPlugin.getDefault().getImage("ValidationRuleDef.gif");
	}

	/**
	 * @inheritDoc
	 */
	public String[] getBusinessFunctions() {
		return (String[]) functions.toArray(new String[functions.size()]);
	}

	/**
	 * @inheritDoc
	 */
	public void setBusinessFunctions(String[] functionNames) {
		functions.clear();
		for (int i = 0; i < functionNames.length; i++) {
			functions.add(functionNames[i]);
		}
		updateSrcFile();
	}

	/**
	 * @inheritDoc
	 */
	public int getNumOfBusinessFunctions() {
		return functions.size();
	}

	/**
	 * @inheritDoc
	 */
	public void addBusinessFunction(String functionName) {
		ArgumentCheck.notNull(functionName);
		functions.add(functionName);
		updateSrcFile();
	}

	/**
	 * @inheritDoc
	 */
	public void removeBusinessFunction(int index) {
		functions.remove(index);
		updateSrcFile();
	}

	/**
	 * @inheritDoc
	 */
	public String getBusinessFunction(int index) {
		return (String) functions.get(index);
	}

	/**
	 * @inheritDoc
	 */
	public void setBusinessFunctions(int index, String functionName) {
		ArgumentCheck.notNull(functionName);
		String oldName = getBusinessFunction(index);
		functions.set(index, functionName);
		valueChanged(oldName, functionName);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isAppliedInAllBusinessFunctions() {
		return applyInAll;
	}

	/**
	 * @inheritDoc
	 */
	public void setAppliedInAllBusinessFunctions(boolean newValue) {
		boolean oldValue = applyInAll;
		applyInAll = newValue;
		valueChanged(oldValue, newValue);
	}

	/**
	 * @inheritDoc
	 */
	protected void validate(MessageList list) throws CoreException {
		super.validate(list);
		ValidationUtils.checkStringPropertyNotEmpty(name, "name", this,
				PROPERTY_NAME, list);
		IIpsProject project = getIpsProject();
		for (Iterator it = functions.iterator(); it.hasNext();) {
			String function = (String) it.next();
			if (StringUtils.isNotEmpty(function)) {
				if (project.findIpsObject(IpsObjectType.BUSINESS_FUNCTION,
						function) == null) {
					String text = function + " does not exists.";
					list.add(new Message("", text, Message.ERROR, function,
							"name"));
				} else {
					if (isAppliedInAllBusinessFunctions()) {
						String text = "The rule is applied in all business functions, this information is ignored.";
						list.add(new Message("", text, Message.WARNING,
								function, "name"));
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
				String text = "The specified attribute is not defined for the policy class of this rule.";
				list.add(new Message("", text, Message.ERROR,
						new ObjectProperty(this, "validatedAttributes", i)));
			}
		}

		for (int i = 0; i < validatedAttributes.size() - 1; i++) {

			for (int r = i + 1; r < validatedAttributes.size(); r++) {
				if (validatedAttributes.get(i).equals(
						validatedAttributes.get(r))) {
					String text = "Duplicate entries.";
					list.add(new Message("", text, Message.WARNING,
							new ObjectProperty[] {
									new ObjectProperty(this,
											"validatedAttributes", i),
									new ObjectProperty(this,
											"validatedAttributes", r) }));
				}
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public String getMessageText() {
		return msgText;
	}

	/**
	 * @inheritDoc
	 */
	public void setMessageText(String newText) {
		String oldText = msgText;
		msgText = newText;
		valueChanged(oldText, msgText);
	}

	/**
	 * @inheritDoc
	 */
	public String getMessageCode() {
		return msgCode;
	}

	/**
	 * @inheritDoc
	 */
	public void setMessageCode(String newCode) {
		String oldCode = msgCode;
		msgCode = newCode;
		valueChanged(oldCode, msgCode);
	}

	/**
	 * @inheritDoc
	 */
	public MessageSeverity getMessageSeverity() {
		return msgSeverity;
	}

	/**
	 * @inheritDoc
	 */
	public void setMessageSeverity(MessageSeverity newSeverity) {
		MessageSeverity oldSeverity = msgSeverity;
		msgSeverity = newSeverity;
		valueChanged(oldSeverity, msgSeverity);
	}

	/**
	 * @inheritDoc
	 */
	protected Element createElement(Document doc) {
		return doc.createElement(TAG_NAME);
	}

	/**
	 * @inheritDoc
	 */
	protected void initPropertiesFromXml(Element element) {
		super.initPropertiesFromXml(element);
		name = element.getAttribute(PROPERTY_NAME);
		applyInAll = Boolean.valueOf(
				element.getAttribute(PROPERTY_APPLIED_IN_ALL_FUNCTIONS))
				.booleanValue();
		msgCode = element.getAttribute(PROPERTY_MESSAGE_CODE);
		msgText = element.getAttribute(PROPERTY_MESSAGE_TEXT);
		msgSeverity = MessageSeverity.getMessageSeverity(element
				.getAttribute(PROPERTY_MESSAGE_SEVERITY));
		validatedAttrSpecifiedInSrc = Boolean.valueOf(
				element.getAttribute(PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC))
				.booleanValue();

		NodeList nl = element.getChildNodes();
		functions.clear();
		validatedAttributes.clear();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element subElement = (Element) nl.item(i);
				if (subElement.getNodeName().equals("BusinessFunction")) {
					functions.add(subElement.getAttribute("name"));
				}
				if (subElement.getNodeName().equals("ValidatedAttribute")) {
					validatedAttributes.add(subElement.getAttribute("name"));
				}
			}
		}
		functions.trimToSize();
	}

	/**
	 * @inheritDoc
	 */
	protected void propertiesToXml(Element newElement) {
		super.propertiesToXml(newElement);
		newElement.setAttribute(PROPERTY_NAME, name);
		newElement.setAttribute(PROPERTY_APPLIED_IN_ALL_FUNCTIONS, String
				.valueOf(applyInAll));
		newElement.setAttribute(PROPERTY_MESSAGE_CODE, msgCode);
		newElement.setAttribute(PROPERTY_MESSAGE_TEXT, msgText);
		newElement.setAttribute(PROPERTY_MESSAGE_SEVERITY, msgSeverity.getId());
		newElement.setAttribute(PROPERTY_VALIDATIED_ATTR_SPECIFIED_IN_SRC,
				String.valueOf(validatedAttrSpecifiedInSrc));
		Document doc = newElement.getOwnerDocument();
		for (int i = 0; i < functions.size(); i++) {
			Element fctElement = doc.createElement("BusinessFunction");
			fctElement.setAttribute("name", (String) functions.get(i));
			newElement.appendChild(fctElement);
		}
		for (int i = 0; i < validatedAttributes.size(); i++) {
			Element attrElement = doc.createElement("ValidatedAttribute");
			attrElement.setAttribute("name", (String) validatedAttributes
					.get(i));
			newElement.appendChild(attrElement);
		}
	}

	/**
	 * @inheritDoc
	 */
	public String addValidatedAttribute(String attributeName) {
		ArgumentCheck.notNull(this, attributeName);
		validatedAttributes.add(attributeName);
		updateSrcFile();
		return attributeName;
	}

	/**
	 * @inheritDoc
	 */
	public String[] getValidatedAttributes() {
		return (String[]) validatedAttributes
				.toArray(new String[validatedAttributes.size()]);
	}

	/**
	 * @inheritDoc
	 */
	public void removeValidatedAttribute(int index) {
		validatedAttributes.remove(index);
		updateSrcFile();
	}

	/**
	 * @inheritDoc
	 */
	public String getValidatedAttributeAt(int index) {
		return (String) validatedAttributes.get(index);
	}

	/**
	 * @inheritDoc
	 */
	public void setValidatedAttributeAt(int index, String attributeName) {
		String oldValue = getValidatedAttributeAt(index);
		validatedAttributes.set(index, attributeName);
		valueChanged(oldValue, attributeName);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isValidatedAttrSpecifiedInSrc() {
		return validatedAttrSpecifiedInSrc;
	}

	/**
	 * @inheritDoc
	 */
	public void setValidatedAttrSpecifiedInSrc(
			boolean validatedAttrSpecifiedInSrc) {
		boolean oldValue = this.validatedAttrSpecifiedInSrc;
		this.validatedAttrSpecifiedInSrc = validatedAttrSpecifiedInSrc;
		valueChanged(oldValue, validatedAttrSpecifiedInSrc);
	}
}
