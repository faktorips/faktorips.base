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

package org.faktorips.devtools.core.internal.model;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.util.XmlAbstractTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class DescriptionHelperTest extends XmlAbstractTestCase {

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.DescriptionHelper.setDescription(Element, String)'
	 */
	public void testSetDescription() {
		Document doc = newDocument();
		Element el = doc.createElement("Test");
		
		assertEquals("", DescriptionHelper.getDescription(el));

		DescriptionHelper.setDescription(el, "abc");
		assertEquals("abc", DescriptionHelper.getDescription(el));
		
		DescriptionHelper.setDescription(el, "Ã¶Ã¤Ã¼ÃÃÃÃ");
		assertEquals("Ã¶Ã¤Ã¼ÃÃÃÃ", DescriptionHelper.getDescription(el));
		
		DescriptionHelper.setDescription(el, "<>;");
		assertEquals("<>;", DescriptionHelper.getDescription(el));
		
		DescriptionHelper.setDescription(el, "l1" + SystemUtils.LINE_SEPARATOR + "l2");
		assertEquals("l1" + SystemUtils.LINE_SEPARATOR + "l2", DescriptionHelper.getDescription(el));
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.internal.model.DescriptionHelper.getDescription(Element)'
	 */
	public void testGetDescription() {

	}

}
