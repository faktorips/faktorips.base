/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StructuredSelection;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class StructuredSelectionHelperTest extends AbstractIpsPluginTest {

    private IProductCmpt prodCmpt;
    private IPolicyCmptType polType;
    private IProductCmptType prodType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject = newIpsProject("TestProject");
        prodType = newProductCmptType(ipsProject, "TestProdCmptType");
        polType = newPolicyCmptType(ipsProject, "TestPolicyCmptType");
        prodType.setPolicyCmptType(polType.getQualifiedName());
        polType.setProductCmptType(prodType.getQualifiedName());

        prodCmpt = newProductCmpt(prodType, "TestCmpt");
    }

    @Test
    public void testConstructor() {
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection());
        IProductCmptType foundType = helper.getFirstElementAsIpsObject(IProductCmptType.class);
        assertNull(foundType);

        helper = new StructuredSelectionHelper(null);
        foundType = helper.getFirstElementAsIpsObject(IProductCmptType.class);
        assertNull(foundType);
    }

    @Test
    public void testProdCmpt() {
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(prodCmpt));
        assertProdCmpt(helper);
    }

    @Test
    public void testProdCmptSrcFile() {
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(
                prodCmpt.getIpsSrcFile()));
        assertProdCmpt(helper);
    }

    @Test
    public void testProdCmptResource() {
        IResource resource = prodCmpt.getIpsSrcFile().getCorrespondingResource().unwrap();
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(resource));
        assertProdCmpt(helper);
    }

    @Test
    public void testProdCmptFirstElement() {
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(new Object[] {
                prodCmpt.getIpsSrcFile().getCorrespondingResource().unwrap(), new String(), new String() }));
        assertProdCmpt(helper);
    }

    @Test
    public void testProdCmptIllegalType() {
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(new Object[] {
                new String(), prodCmpt.getIpsSrcFile().getCorrespondingResource().unwrap(), new String() }));
        IProductCmpt foundCmpt = helper.getFirstElementAsIpsObject(IProductCmpt.class);
        assertNull(foundCmpt);
    }

    protected void assertProdCmpt(StructuredSelectionHelper helper) {
        IProductCmpt foundCmpt = helper.getFirstElementAsIpsObject(IProductCmpt.class);
        assertNotNull(foundCmpt);
        assertEquals(prodCmpt, foundCmpt);
    }

    @Test
    public void testProdType() {
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(prodType));
        IProductCmptType foundType = helper.getFirstElementAsIpsObject(IProductCmptType.class);
        assertNotNull(foundType);
        assertEquals(prodType, foundType);
    }
}
