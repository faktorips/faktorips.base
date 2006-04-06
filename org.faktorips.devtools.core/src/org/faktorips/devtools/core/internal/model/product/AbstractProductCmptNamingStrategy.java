/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An abstract implementation that uses a special character to separate the
 * constant part and the version id. 
 * <p>
 * When transforming a name to a Java identifier, special characters like 
 * blank and hypen (-) can be replaced with a String that is allowed
 * in a Java identifiers. The special characters can be registered along with
 * their replacement via the addSpecialCharReplacement() method.
 * <p>
 * Note that two special characters can't have the same replacement, as otherwise
 * two product component names (so unlikely) could result in the same Java class name.
 * <p>
 * The dot (.) is prohibited in names as it is use to separate the name from the
 * package information in qualified names.
 * <p>
 * By default the following replacement is used:<p>
 *  hypen (-) => two underscores
 *  blank => three underscores
 *
 * @author Jan Ortmann
 */
public abstract class AbstractProductCmptNamingStrategy implements
		IProductCmptNamingStrategy {

	/**
     * Validation message code to indicate that the name contains illegal characters.
     */
    public final static String MSGCODE_ILLEGAL_CHARACTERS = MSGCODE_PREFIX + "IllegalCharacters"; //$NON-NLS-1$

	private String separator;
	private HashMap specialCharReplacements = new HashMap();
	
	public AbstractProductCmptNamingStrategy() {
		this(""); //$NON-NLS-1$
	}

	public AbstractProductCmptNamingStrategy(String separator) {
		this.separator = separator;
		putSpecialCharReplacement('-', "__"); //$NON-NLS-1$
		putSpecialCharReplacement(' ', "___"); //$NON-NLS-1$
	}
	
	/**
	 * Sets the String that separates the version id from the constant part of the product
	 * component name. 
	 */
	public void setVersionIdSeparator(String separator) {
		this.separator = separator;
	}
	
	/**
	 * Returns the String that separates the version id from the constant part of the product
	 * component name. 
	 */
	public String getVersionIdSeparator() {
		return separator;
	}
	
	protected void putSpecialCharReplacement(char specialChar, String replacement) {
		if (specialChar=='.') {
			throw new IllegalArgumentException("The dot (.) is is prohibited in names, as it is used to separate name and package information in qualified names."); //$NON-NLS-1$
		}
		if (replacement==null) {
			specialCharReplacements.remove(new Character(specialChar));
		} else {
			specialCharReplacements.put(new Character(specialChar), replacement);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getProductCmptName(String constantPart, String versionId) {
		return constantPart + separator + versionId;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getKindId(String productCmptName) {
		int index = productCmptName.indexOf(separator);
		if (index==-1) {
			throw new IllegalArgumentException("Can't get constant part from " + productCmptName + ", separator not found!"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return productCmptName.substring(0, index);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionId(String productCmptName) {
		int index = productCmptName.indexOf(separator);
		if (index==-1) {
			throw new IllegalArgumentException("Can't get constant part from " + productCmptName + ", separator not found!"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return productCmptName.substring(index+separator.length());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextName(IProductCmpt productCmpt) {
		String part = getKindId(productCmpt.getName());
		return part + separator + getNextVersionId(productCmpt);
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageList validate(String name) {
		MessageList list = new MessageList();
		if (separator.length() > 0) {
			int separatorCount = StringUtils.countMatches(name, separator); 
			if ( separatorCount == 0) {
				String text = NLS.bind(Messages.AbstractProductCmptNamingStrategy_msgNoVersionSeparator, name);
				Message msg = Message.newError(MSGCODE_MISSING_VERSION_SEPARATOR, text);
				list.add(msg);
				return list;
			}
		}
		list.add(validateKindId(getKindId(name)));
		list.add(validateVersionId(getVersionId(name)));
		return list;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MessageList validateKindId(String kindId) {
		MessageList list = new MessageList();
		if (StringUtils.isEmpty(kindId)) {
			Message msg = Message.newError(MSGCODE_KIND_ID_IS_EMPTY, Messages.AbstractProductCmptNamingStrategy_emptyKindId);
			list.add(msg);
			return list;
		}
		try {
			getJavaClassIdentifier(kindId);
		} catch (IllegalArgumentException e) {
			Message msg = Message.newError(MSGCODE_ILLEGAL_CHARACTERS, Messages.AbstractProductCmptNamingStrategy_msgIllegalChar);
			list.add(msg);
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJavaClassIdentifier(String name) {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<name.length(); i++) {
			char c = name.charAt(i);
			if (isSpecialChar(c)) {
				buffer.append(getReplacement(c));
			} else {
				buffer.append(c);
			}
		}
		String identifier = buffer.toString();
		IStatus status = JavaConventions.validateJavaTypeName(identifier); 
		if (status.isOK() || status.getSeverity()==IStatus.WARNING) {
			return identifier;
		}
		throw new IllegalArgumentException("Name " + name + " can't be transformed to a valid Java class name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private boolean isSpecialChar(char c) {
		return specialCharReplacements.containsKey(new Character(c));
	}
	
	private String getReplacement(char c) {
		return (String)specialCharReplacements.get(new Character(c));
	}

	/**
	 * {@inheritDoc}
	 */
	public final void initFromXml(Element el) {
		Element subEl = XmlUtil.getFirstElement(el);
		separator = subEl.getAttribute("versionIdSeparator"); //$NON-NLS-1$
		initSubclassFromXml(subEl);
	}
	
	/**
	 * Subclasses must init their state from the given element.
	 * 
	 * @param The xml element containing the data specific for the subclass.
	 */
	protected abstract void initSubclassFromXml(Element el);

	/**
	 * Appends the data from this abstract stratgey to the given element.
	 * Should be used by subclasses to add the date.
	 */
	public final Element toXml(Document doc) {
		Element el = doc.createElement(XML_TAG_NAME);
		el.setAttribute("id", getExtensionId()); //$NON-NLS-1$
		Element subEl = toXmlSubclass(doc); 
		subEl.setAttribute("versionIdSeparator", separator); //$NON-NLS-1$
		el.appendChild(subEl);
		return el;
	}
	
	/**
	 * Subclasses must create an xml element, copy their state to the element
	 * and return it. 
	 * 
	 * @param doc Xml document to create elements.
	 */
	protected abstract Element toXmlSubclass(Document doc);
	
	
}
