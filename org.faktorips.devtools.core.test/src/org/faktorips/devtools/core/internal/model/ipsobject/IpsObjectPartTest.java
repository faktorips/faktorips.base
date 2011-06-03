/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
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
        subpart = generation.newConfigElement();
    }

    @Test
    public void testGetIpsObject() {
        assertEquals(productCmpt, part.getIpsObject());
        assertEquals(productCmpt, subpart.getIpsObject());
    }

    @Test
    public void testEquals() throws CoreException {
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
    public void testCopy() {
        TestIpsObjectPart part = new TestIpsObjectPart();
        TestIpsObjectPart target = new TestIpsObjectPart();
        TestIpsObjectPart spyTarget = spy(target);

        part.copy(spyTarget);

        verify(spyTarget).initFromXml(part.xml, target.getId());
    }

    // Public so it can be accessed by Mockito
    public static class TestIpsObjectPart extends IpsObjectPart {

        private Element xml;

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

    }

}
