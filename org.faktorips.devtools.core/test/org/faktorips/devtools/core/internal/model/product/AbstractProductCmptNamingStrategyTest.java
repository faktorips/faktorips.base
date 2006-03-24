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

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class AbstractProductCmptNamingStrategyTest extends IpsPluginTest {

	private AbstractProductCmptNamingStrategy namingStrategy;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		namingStrategy = new TestNamingStrategy();
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getProductCmptName(String, String)'
	 */
	public void testGetProductCmptName() {
		assertEquals("abc - id", namingStrategy.getProductCmptName("abc", "id"));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getConstantPart(String)'
	 */
	public void testGetConstantPart() {
		assertEquals("abc", namingStrategy.getConstantPart("abc - id"));

	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getVersionId(String)'
	 */
	public void testGetVersionId() {
		assertEquals("id", namingStrategy.getVersionId("abc - id"));
	}

	public void testGetNextName() throws CoreException {
		IIpsProject project = newIpsProject("TestProject");
		IProductCmpt pc = newProductCmpt(project, "TestProduct - id");
		assertEquals("TestProduct - nextId", namingStrategy.getNextName(pc));
	}
	
	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.validate(String)'
	 */
	public void testValidate() {
		MessageList list = namingStrategy.validate("abc");
		assertNotNull(list.getMessageByCode(AbstractProductCmptNamingStrategy.MSGCODE_MISSING_VERSION_SEPARATOR));

		list = namingStrategy.validate("abc - qwe - 123");
		assertNotNull(list.getMessageByCode(AbstractProductCmptNamingStrategy.MSGCODE_ONYL_1_OCCURENCE_OF_SEPARATOR_ALLOWED));
		
		list = namingStrategy.validate("abc% - 123");
		assertNotNull(list.getMessageByCode(AbstractProductCmptNamingStrategy.MSGCODE_ILLEGAL_CHARACTERS));	
	
		list = namingStrategy.validate("abc - d123");
		assertFalse(list.containsErrorMsg());	
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.validateConstantPart(String)'
	 */
	public void testValidateConstantPart() {
		MessageList list = namingStrategy.validateConstantPart("abc%");
		assertNotNull(list.getMessageByCode(AbstractProductCmptNamingStrategy.MSGCODE_ILLEGAL_CHARACTERS));	

		list = namingStrategy.validateConstantPart("abc");
		assertFalse(list.containsErrorMsg());	
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getJavaClassIdentifier(String)'
	 */
	public void testGetJavaClassIdentifier() {
		assertEquals("abc", namingStrategy.getJavaClassIdentifier("abc"));
		assertEquals("abc___def__", namingStrategy.getJavaClassIdentifier("abc def-"));
		
	}

	class TestNamingStrategy extends AbstractProductCmptNamingStrategy {

		public TestNamingStrategy() {
			super(" - ");
		}

		/**
		 * {@inheritDoc}
		 */
		public String getName(Locale locale) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getNextVersionId(IProductCmpt pc) {
			return "nextId";
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
		protected void initSubclassFromXml(Element el) {
		}

		/**
		 * {@inheritDoc}
		 */
		protected Element toXmlSubclass(Document doc) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getExtensionId() {
			return "TestStrategy";
		}

		
	}
}
