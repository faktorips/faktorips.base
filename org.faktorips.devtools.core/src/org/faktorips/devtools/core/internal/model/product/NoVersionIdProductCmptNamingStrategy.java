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

import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A naming strategy for product components that allows to use only the constant
 * part, the version is always the empty string.
 * The next version is determined by appending a 1 to the given name.
 * 
 * @author Jan Ortmann
 */
public class NoVersionIdProductCmptNamingStrategy extends
		AbstractProductCmptNamingStrategy {

	public final static String EXTENSION_ID = "org.faktorips.devtools.core.NoVersionIdProductCmptNamingStrategy"; //$NON-NLS-1$
	
	public final static String XML_TAG_NAME = "NoVersionIdProductCmptNamingStrategy"; //$NON-NLS-1$
	
	public NoVersionIdProductCmptNamingStrategy() {
		super();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getExtensionId() {
		return EXTENSION_ID;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean supportsVersionId() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConstantPart(String productCmptName) {
		return productCmptName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getVersionId(String productCmptName) {
		return ""; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextVersionId(IProductCmpt productCmpt) {
		return ""; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextName(IProductCmpt productCmpt) {
		return productCmpt.getName() + "1"; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageList validateVersionId(String versionId) {
		return new MessageList();
	}

	/**
	 * {@inheritDoc}
	 */
	public void initSubclassFromXml(Element el) {
		setVersionIdSeparator(""); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public Element toXmlSubclass(Document doc) {
		return doc.createElement(XML_TAG_NAME);
	}

}
