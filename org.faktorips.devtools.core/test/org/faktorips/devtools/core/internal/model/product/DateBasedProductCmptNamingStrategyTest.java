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

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Jan Ortmann
 */
public class DateBasedProductCmptNamingStrategyTest extends IpsPluginTest  {

	private DateBasedProductCmptNamingStrategy strategy;
	
	
	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		strategy = new DateBasedProductCmptNamingStrategy(" ", df, false, 10);
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.DateBasedProductCmptNamingStrategy.validateVersionId(String)'
	 */
	public void testValidateVersionId() {
		MessageList list = new MessageList();
		list = strategy.validateVersionId("a2006-01-31");
		assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));

		list = strategy.validateVersionId("2006-01-31x");
		assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));

		list = strategy.validateVersionId("2006-01-31");
		assertFalse(list.containsErrorMsg());
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		strategy = new DateBasedProductCmptNamingStrategy(" ", df, true, 10);
		list = strategy.validateVersionId("2006-01-31a");
		assertFalse(list.containsErrorMsg());
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.product.DateBasedProductCmptNamingStrategy.getNextVersionId(IProductCmpt)'
	 */
	public void testGetNextVersionId() throws CoreException {
		IpsPreferences.setWorkingDate(new GregorianCalendar(2006, 0, 31));
		IIpsProject project = newIpsProject("TestProject");
		IProductCmpt pc = newProductCmpt(project, "TestProduct 2005-01-01");
		assertEquals("2006-01-31", strategy.getNextVersionId(pc));
	}

}
