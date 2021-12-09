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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
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
public class DateBasedProductCmptNamingStrategyTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private DateBasedProductCmptNamingStrategy strategy;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        IIpsProjectProperties props = ipsProject.getProperties();
        strategy = new DateBasedProductCmptNamingStrategy();
        strategy.setIpsProject(ipsProject);
        strategy.setVersionIdSeparator(" ");
        strategy.setDateFormatPattern("yyyy-MM-dd");
        strategy.setPostfixAllowed(false);
        props.setProductCmptNamingStrategy(strategy);
        ipsProject.setProperties(props);
    }

    @Test
    public void testValidateVersionId() {
        MessageList list = new MessageList();
        list = strategy.validateVersionId("a2006-01-31");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));

        list = strategy.validateVersionId("2006-01-31x");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));

        list = strategy.validateVersionId("2006");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));

        list = strategy.validateVersionId("");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));

        list = strategy.validateVersionId("2006-01-31");
        assertFalse(list.containsErrorMsg());

        strategy.setPostfixAllowed(true);
        list = strategy.validateVersionId("2006-01-31a");
        assertFalse(list.containsErrorMsg());

        list = strategy.validateVersionId("2006");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_VERSION_ID));
    }

    @Test
    public void testGetNextVersionId() throws CoreRuntimeException {
        GregorianCalendar workingDate = new GregorianCalendar(2006, 0, 31);
        IProductCmpt pc = newProductCmpt(ipsProject, "TestProduct 2005-01-01");
        assertEquals("2006-01-31", strategy.getNextVersionId(pc, workingDate));
    }

    @Test
    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        strategy.setPostfixAllowed(false);
        strategy.setDateFormatPattern("MM-yyyy");
        strategy.initFromXml(el);
        assertEquals("-", strategy.getVersionIdSeparator());
        assertEquals("yyyy-MM", strategy.getDateFormatPattern());
        assertTrue(strategy.isPostfixAllowed());
        assertFalse(strategy.validateVersionId("2006-12").containsErrorMsg());
        assertFalse(strategy.validateVersionId("2006-12b").containsErrorMsg());
        assertTrue(strategy.validateVersionId("12-2006").containsErrorMsg());

        assertEquals(2, strategy.getReplacedCharacters().length);
        assertEquals(' ', strategy.getReplacedCharacters()[0]);
        assertEquals('-', strategy.getReplacedCharacters()[1]);
        assertEquals("x", strategy.getReplacement('-'));
        assertEquals("y", strategy.getReplacement(' '));
    }

    @Test
    public void testToXml() {
        Document doc = newDocument();
        strategy.putSpecialCharReplacement('#', "zzz");
        Element el = strategy.toXml(doc);
        assertEquals(IProductCmptNamingStrategy.XML_TAG_NAME, el.getNodeName());
        strategy = new DateBasedProductCmptNamingStrategy();
        strategy.initFromXml(el);
        assertEquals(" ", strategy.getVersionIdSeparator());
        assertEquals("yyyy-MM-dd", strategy.getDateFormatPattern());
        assertEquals(3, strategy.getReplacedCharacters().length);
    }

    @Test
    public void testGetUniqueRuntimeId() throws Exception {
        String prefix = ipsProject.getIpsProject().getRuntimeIdPrefix();

        String id = strategy.getUniqueRuntimeId(ipsProject, "TestProduct 2005-01-01");
        assertEquals(prefix + "TestProduct 2005-01-01", id);

        newProductCmpt(ipsProject, "TestProduct 2005-01-01");
        id = strategy.getUniqueRuntimeId(ipsProject, "TestProduct 2005-01-01");
        assertEquals(prefix + "TestProduct1 2005-01-01", id);

        newProductCmpt(ipsProject, "pack2.TestProduct 2005-01-01");
        id = strategy.getUniqueRuntimeId(ipsProject, "TestProduct 2005-01-01");
        assertEquals(prefix + "TestProduct2 2005-01-01", id);

        id = strategy.getUniqueRuntimeId(ipsProject, "Test Product 2005-01-01");
        assertEquals(prefix + "Test Product 2005-01-01", id);

        newProductCmpt(ipsProject, "pack2.Test Product 2005-01-01");

        id = strategy.getUniqueRuntimeId(ipsProject, "Test Product 2005-01-01");
        assertEquals(prefix + "Test Product1 2005-01-01", id);
    }

}
