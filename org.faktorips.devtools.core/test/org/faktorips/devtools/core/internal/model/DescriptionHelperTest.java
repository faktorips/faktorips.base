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
		
		DescriptionHelper.setDescription(el, "öäüÖÄÜß");
		assertEquals("öäüÖÄÜß", DescriptionHelper.getDescription(el));
		
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
