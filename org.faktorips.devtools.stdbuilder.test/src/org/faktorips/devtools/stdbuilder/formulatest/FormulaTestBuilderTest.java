/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.formulatest;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;

public class FormulaTestBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmpt productCmpt;
    private IProductCmptType productCmptType;
    private IFormula formula;
    
    /**
     * {@inheritDoc}
     */
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

    public void testDelete() throws CoreException{
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
