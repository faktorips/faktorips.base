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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class NoVersionIdProductCmptNamingStrategyTest extends IpsPluginTest {

	private NoVersionIdProductCmptNamingStrategy strategy = new NoVersionIdProductCmptNamingStrategy();
	
	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.NoVersionIdProductCmptNamingStrategy.getNextVersionId(IProductCmpt)'
	 */
	public void testGetNextVersionId() throws CoreException {
		IIpsProject project = newIpsProject("TestProject");
		IProductCmpt pc = newProductCmpt(project, "TestProduct");
		assertEquals("", strategy.getNextVersionId(pc));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getConstantPart(String)'
	 */
	public void testGetConstantPart() {
		assertEquals("abc", strategy.getConstantPart("abc"));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getVersionId(String)'
	 */
	public void testGetVersionId() {
		assertEquals("", strategy.getVersionId("abc.- 123"));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getNextName(IProductCmpt)'
	 */
	public void testGetNextName() {
		
		assertEquals("abc", strategy.getConstantPart("abc"));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.validate(String)'
	 */
	public void testValidate() {
		MessageList list = strategy.validate("abc");
		assertFalse(list.containsErrorMsg());
	}

	public void testInitFromXml() {
		Element el = getTestDocument().getDocumentElement();
		strategy.initFromXml(el); // should not throw an exception
		assertEquals("", strategy.getVersionIdSeparator());
	}

	public void testToXml() {
		Document doc = newDocument();
		Element el = strategy.toXml(doc);
		assertEquals(IProductCmptNamingStrategy.XML_TAG_NAME, el.getNodeName());
	}
}
