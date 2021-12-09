/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class NoVersionIdProductCmptNamingStrategyTest extends AbstractIpsPluginTest {

    private IIpsProject project;

    private NoVersionIdProductCmptNamingStrategy strategy = new NoVersionIdProductCmptNamingStrategy();

    private String prefix;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject();
        prefix = project.getRuntimeIdPrefix();
        strategy.setIpsProject(project);
    }

    @Test
    public void testGetNextVersionId() throws CoreRuntimeException {
        IProductCmpt pc = newProductCmpt(project, "TestProduct");
        assertEquals("", strategy.getNextVersionId(pc, null));
    }

    @Test
    public void testGetConstantPart() {
        assertEquals("abc", strategy.getKindId("abc"));
    }

    @Test
    public void testGetVersionId() {
        assertEquals("", strategy.getVersionId("abc.- 123"));
    }

    @Test
    public void testGetNextName() {
        assertEquals("abc", strategy.getKindId("abc"));
    }

    @Test
    public void testValidate() {
        MessageList list = strategy.validate("abc");
        assertFalse(list.containsErrorMsg());
    }

    @Test
    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        strategy.initFromXml(el); // should not throw an exception
        assertEquals("", strategy.getVersionIdSeparator());
    }

    @Test
    public void testToXml() {
        Document doc = newDocument();
        Element el = strategy.toXml(doc);
        assertEquals(IProductCmptNamingStrategy.XML_TAG_NAME, el.getNodeName());
    }

    @Test
    public void testGetUniqueRuntimeId() throws Exception {
        String id = strategy.getUniqueRuntimeId(project, "TestProductCmpt");
        assertEquals(prefix + "TestProductCmpt", id);

        newProductCmpt(project, "TestProductCmpt");
        id = strategy.getUniqueRuntimeId(project, "TestProductCmpt");
        assertEquals(prefix + "TestProductCmpt1", id);

        newProductCmpt(project, "test.TestProductCmpt");
        id = strategy.getUniqueRuntimeId(project, "TestProductCmpt");
        assertEquals(prefix + "TestProductCmpt2", id);

        id = strategy.getUniqueRuntimeId(project, "TestProductCmpt1");
        assertEquals(prefix + "TestProductCmpt11", id);
    }
}
