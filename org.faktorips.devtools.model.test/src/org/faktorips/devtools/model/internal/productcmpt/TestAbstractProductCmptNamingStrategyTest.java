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
import static org.junit.Assert.assertNull;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
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
public class TestAbstractProductCmptNamingStrategyTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private AbstractProductCmptNamingStrategy namingStrategy;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        namingStrategy = new TestNamingStrategy();
        namingStrategy.setIpsProject(ipsProject);
    }

    @Test
    public void testGetReplacedCharacters() {
        assertEquals(2, namingStrategy.getReplacedCharacters().length);

        namingStrategy.putSpecialCharReplacement('#', "xx");
        assertEquals(3, namingStrategy.getReplacedCharacters().length);
    }

    @Test
    public void testGetProductCmptName() {
        assertEquals("abc - id", namingStrategy.getProductCmptName("abc", "id"));
    }

    @Test
    public void testGetKindId() {
        assertEquals("abc", namingStrategy.getKindId("abc - id"));
        assertEquals("abc - def", namingStrategy.getKindId("abc - def - id"));
    }

    @Test
    public void testGetVersionId() {
        assertEquals("id", namingStrategy.getVersionId("abc - id"));
        assertEquals("id", namingStrategy.getVersionId("abc - def - id"));
    }

    @Test
    public void testGetNextName() {
        IProductCmpt pc = newProductCmpt(ipsProject, "TestProduct - id");
        assertEquals("TestProduct - nextId", namingStrategy.getNextName(pc, null));
    }

    @Test
    public void testValidate() {
        MessageList list = namingStrategy.validate("abc");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_MISSING_VERSION_SEPARATOR));

        list = namingStrategy.validate("abc% - 123");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_CHARACTERS));

        list = namingStrategy.validate("abc - qwe - 123"); // two version separator strings are ok,
        // the last one is taken
        assertFalse(list.containsErrorMsg());

        list = namingStrategy.validate("abc - d123");
        assertFalse(list.containsErrorMsg());
    }

    @Test
    public void testValidateKindId() {
        MessageList list = namingStrategy.validateKindId("abc%");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_CHARACTERS));

        list = namingStrategy.validateKindId("");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_KIND_ID_IS_EMPTY));

        list = namingStrategy.validateKindId("abc");
        assertFalse(list.containsErrorMsg());
    }

    @Test
    public void testValidateRuntimeId() {
        MessageList list = namingStrategy.validateRuntimeId("x");
        assertNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_INVALID_RUNTIME_ID_FORMAT));

        list = namingStrategy.validateRuntimeId("");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_INVALID_RUNTIME_ID_FORMAT));
    }

    @Test
    public void testGetJavaClassIdentifier() {
        assertEquals("abc", namingStrategy.getJavaClassIdentifier("abc"));
        assertEquals("abc___def__", namingStrategy.getJavaClassIdentifier("abc def-"));
    }

    class TestNamingStrategy extends AbstractProductCmptNamingStrategy {

        public TestNamingStrategy() {
            super(" - ");
        }

        @Override
        public boolean supportsVersionId() {
            return true;
        }

        @Override
        public String getNextVersionId(IProductCmpt pc, GregorianCalendar validFrom) {
            return "nextId";
        }

        @Override
        public MessageList validateVersionId(String versionId) {
            return new MessageList();
        }

        @Override
        protected void initSubclassFromXml(Element el) {

        }

        @Override
        protected Element toXmlSubclass(Document doc) {
            return null;
        }

        @Override
        public String getExtensionId() {
            return "TestStrategy";
        }

        @Override
        public String getUniqueRuntimeId(IIpsProject project, String productCmptName) {
            throw new UnsupportedOperationException();
        }
    }
}
