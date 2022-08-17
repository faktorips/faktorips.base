/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.devtools.model.internal.ipsobject.DescriptionHelper;
import org.faktorips.devtools.model.util.XmlUtil;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class DescriptionHelperTest extends XmlAbstractTestCase {

    @Test
    public void testSetDescription() {
        Document doc = newDocument();
        Element el = doc.createElement("Test");

        assertEquals("", DescriptionHelper.getDescription(el));

        DescriptionHelper.setDescription(el, "abc");
        assertEquals("abc", DescriptionHelper.getDescription(el));

        DescriptionHelper.setDescription(el, "�������");
        assertEquals("�������", DescriptionHelper.getDescription(el));

        DescriptionHelper.setDescription(el, "<>;");
        assertEquals("<>;", DescriptionHelper.getDescription(el));

        DescriptionHelper.setDescription(el, "l1" + System.lineSeparator() + "l2");
        assertEquals("l1" + System.lineSeparator() + "l2", DescriptionHelper.getDescription(el));
    }

    @Test
    public void testGetDescription() {
        Element rootEl = getTestDocument().getDocumentElement();

        Element obj = XmlUtil.getFirstElement(rootEl, "Object0");
        assertEquals("bla", DescriptionHelper.getDescription(obj));

        obj = XmlUtil.getFirstElement(rootEl, "Object1");
        assertEquals("blabla", DescriptionHelper.getDescription(obj));

        obj = XmlUtil.getFirstElement(rootEl, "Object2");
        assertEquals("", DescriptionHelper.getDescription(obj));

        obj = XmlUtil.getFirstElement(rootEl, "Object3");
        assertEquals("", DescriptionHelper.getDescription(obj));
    }

    @Test
    public void testGetFirstNoneDescriptionElement() {
        Element rootEl = getTestDocument().getDocumentElement();

        Element obj = XmlUtil.getFirstElement(rootEl, "Object1");
        assertEquals("Child1", DescriptionHelper.getFirstNoneDescriptionElement(obj).getNodeName());

        obj = XmlUtil.getFirstElement(rootEl, "Object2");
        assertNull(DescriptionHelper.getFirstNoneDescriptionElement(obj));

        obj = XmlUtil.getFirstElement(rootEl, "Object3");
        assertEquals("Child3", DescriptionHelper.getFirstNoneDescriptionElement(obj).getNodeName());

        obj = XmlUtil.getFirstElement(rootEl, "Object4");
        assertNull(DescriptionHelper.getFirstNoneDescriptionElement(obj));
    }

}
