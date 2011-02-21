/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.util.message.MessageList;
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
        project = newIpsProject("TestProject");
        prefix = project.getRuntimeIdPrefix();
        strategy.setIpsProject(project);
    }

    @Test
    public void testGetNextVersionId() throws CoreException {
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
