/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IpsObjectPartTest extends AbstractIpsPluginTest {

    private IIpsProject project;

    private IProductCmpt productCmpt;

    private IIpsObjectPart part;

    private IIpsObjectPart subpart;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        productCmpt = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "Product");
        productCmpt.getIpsSrcFile();
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        part = generation;
        subpart = generation.newAttributeValue();
    }

    @Test
    public void testGetIpsObject() {
        assertEquals(productCmpt, part.getIpsObject());
        assertEquals(productCmpt, subpart.getIpsObject());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEquals() {
        assertFalse(part.equals(null));
        assertFalse(part.equals("abc"));

        // different id
        IIpsObjectGeneration gen2 = productCmpt.newGeneration();
        assertFalse(part.equals(gen2));

        IProductCmpt productCmpt2 = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "Product2");
        IIpsObjectGeneration gen3 = productCmpt2.newGeneration();

        // same id, different parent
        assertFalse(part.equals(gen3));

        assertTrue(part.equals(part));
    }

    @Test
    public void testCopyFrom() {
        TestIpsObjectPart part = new TestIpsObjectPart();
        TestIpsObjectPart source = new TestIpsObjectPart();

        String idBeforeCopy = part.getId();
        // Can't use Mockito as the mocked class will be recognized as a different class
        part.copyFrom(source);

        assertEquals(part.xml, source.copyXml);
        assertEquals(idBeforeCopy, source.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyFromIllegalTargetClass() {
        TestIpsObjectPart part = new TestIpsObjectPart();
        part.copyFrom(mock(IIpsObjectPartContainer.class));
    }

    private static class TestIpsObjectPart extends IpsObjectPart {

        private Element xml;

        private Element copyXml;

        public TestIpsObjectPart() {
            super(null, "foo");
        }

        @Override
        protected IIpsElement[] getChildrenThis() {
            return new IIpsElement[0];
        }

        @Override
        protected Element createElement(Document doc) {
            xml = doc.createElement("TestPart");
            return xml;
        }

        @Override
        protected void reinitPartCollectionsThis() {

        }

        @Override
        protected boolean addPartThis(IIpsObjectPart part) {
            return false;
        }

        @Override
        protected boolean removePartThis(IIpsObjectPart part) {
            return false;
        }

        @Override
        protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
            return null;
        }

        @Override
        protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
            return null;
        }

        @Override
        protected void initFromXml(Element element, String id) {
            super.initFromXml(element, id);
            copyXml = element;
        }

    }

}
