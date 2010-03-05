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

package org.faktorips.devtools.stdbuilder.formulatest;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

public class FormulaTestBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmpt productCmpt;
    private IProductCmptType productCmptType;
    private IFormula formula;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(0);
        formula = generation.newFormula();
        formula.newFormulaTestCase();
    }

    public void testDelete() throws CoreException {
        productCmpt.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertTrue(productCmpt.getIpsSrcFile().exists());

        productCmpt.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertFalse(productCmpt.getIpsSrcFile().exists());

        IProductCmpt productCmpt2 = newProductCmpt(productCmptType, "productCmptWithoutFormula");
        productCmpt2.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        productCmpt2.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
