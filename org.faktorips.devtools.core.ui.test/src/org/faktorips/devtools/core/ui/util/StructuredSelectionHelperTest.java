/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.jface.viewers.StructuredSelection;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class StructuredSelectionHelperTest extends AbstractIpsPluginTest {

    private IProductCmpt prodCmpt;
    private IPolicyCmptType polType;
    private IProductCmptType prodType;

    @Override
    @Before
    public void setUp() throws Exception {
        IIpsProject ipsProject = newIpsProject("TestProject");
        prodType = newProductCmptType(ipsProject, "TestProdCmptType");
        polType = newPolicyCmptType(ipsProject, "TestPolicyCmptType");
        prodType.setPolicyCmptType(polType.getQualifiedName());
        polType.setProductCmptType(prodType.getQualifiedName());

        prodCmpt = newProductCmpt(prodType, "TestCmpt");
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
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(prodCmpt
                .getIpsSrcFile().getCorrespondingResource()));
        assertProdCmpt(helper);
    }

    @Test
    public void testProdCmptFirstElement() {
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(new Object[] {
                prodCmpt.getIpsSrcFile().getCorrespondingResource(), new String(), new String() }));
        assertProdCmpt(helper);
    }

    @Test
    public void testProdCmptIllegalType() {
        StructuredSelectionHelper helper = new StructuredSelectionHelper(new StructuredSelection(new Object[] {
                new String(), prodCmpt.getIpsSrcFile().getCorrespondingResource(), new String() }));
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
