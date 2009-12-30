/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class TestAbstractProductCmptNamingStrategyTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private AbstractProductCmptNamingStrategy namingStrategy;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        namingStrategy = new TestNamingStrategy();
        namingStrategy.setIpsProject(ipsProject);
    }

    public void testGetReplacedCharacters() {
        assertEquals(2, namingStrategy.getReplacedCharacters().length);

        namingStrategy.putSpecialCharReplacement('#', "xx");
        assertEquals(3, namingStrategy.getReplacedCharacters().length);
    }

    /*
     * Test method for
     * 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getProductCmptName(String,
     * String)'
     */
    public void testGetProductCmptName() {
        assertEquals("abc - id", namingStrategy.getProductCmptName("abc", "id"));
    }

    /*
     * Test method for
     * 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getConstantPart(String)'
     */
    public void testGetKindId() {
        assertEquals("abc", namingStrategy.getKindId("abc - id"));
    }

    /*
     * Test method for
     * 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getVersionId(String)'
     */
    public void testGetVersionId() {
        assertEquals("id", namingStrategy.getVersionId("abc - id"));
    }

    public void testGetNextName() throws CoreException {
        IProductCmpt pc = newProductCmpt(ipsProject, "TestProduct - id");
        assertEquals("TestProduct - nextId", namingStrategy.getNextName(pc));
    }

    /*
     * Test method for
     * 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.validate(String)'
     */
    public void testValidate() {
        MessageList list = namingStrategy.validate("abc");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_MISSING_VERSION_SEPARATOR));

        list = namingStrategy.validate("abc% - 123");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_CHARACTERS));

        list = namingStrategy.validate("abc - qwe - 123"); // two version separators strings are ok,
        // the first one is taken
        assertFalse(list.containsErrorMsg());

        list = namingStrategy.validate("abc - d123");
        assertFalse(list.containsErrorMsg());
    }

    /*
     * Test method for
     * 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.validateConstantPart(String)'
     */
    public void testValidateKindId() {
        MessageList list = namingStrategy.validateKindId("abc%");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_CHARACTERS));

        list = namingStrategy.validateKindId("");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_KIND_ID_IS_EMPTY));

        list = namingStrategy.validateKindId("abc");
        assertFalse(list.containsErrorMsg());
    }

    public void testValidateRuntimeId() {
        MessageList list = namingStrategy.validateRuntimeId("x");
        assertNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_INVALID_RUNTIME_ID_FORMAT));

        list = namingStrategy.validateRuntimeId("");
        assertNotNull(list.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_INVALID_RUNTIME_ID_FORMAT));
    }

    /*
     * Test method for
     * 'org.faktorips.devtools.core.internal.model.product.AbstractProductCmptNamingStrategy.getJavaClassIdentifier(String)'
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
        public boolean supportsVersionId() {
            return true;
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
        @Override
        protected void initSubclassFromXml(Element el) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Element toXmlSubclass(Document doc) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getExtensionId() {
            return "TestStrategy";
        }

        /**
         * {@inheritDoc}
         */
        public String getUniqueRuntimeId(IIpsProject project, String productCmptName) throws CoreException {
            throw new UnsupportedOperationException();
        }
    }
}
