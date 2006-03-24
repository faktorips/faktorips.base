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

/**
 * A naming strategy for product components that allows to use only the constant
 * part, the version is always the empty string.
 * The next version is determined by appending a 1 to the given name.
 * 
 * @author Jan Ortmann
 */
public class NoVersionIdProductCmptNamingStrategy extends
		AbstractProductCmptNamingStrategy {

	/**
	 * @param id
	 * @param separator
	 */
	public NoVersionIdProductCmptNamingStrategy() {
		super("NoVersionIdNamingStrategy", "");
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
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextVersionId(IProductCmpt productCmpt) {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNextName(IProductCmpt productCmpt) {
		return productCmpt.getName() + "1";
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageList validateVersionId(String versionId) {
		return new MessageList();
	}

}
