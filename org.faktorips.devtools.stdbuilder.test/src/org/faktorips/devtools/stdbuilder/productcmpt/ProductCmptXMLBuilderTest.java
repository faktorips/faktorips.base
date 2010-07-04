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

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptXMLBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        IIpsProjectProperties props = project.getProperties();
        project.setProperties(props);
        policyCmptType = newPolicyAndProductCmptType(project, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(project);

        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.setName("age");
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("AgeCalculation");

        assertTrue(productCmptType.isValid());

        IProductCmptTypeAssociation rel = productCmptType.newProductCmptTypeAssociation();
        rel.setTargetRoleSingular("role");
        rel.setTargetRolePlural("roles");
        rel.setTarget(productCmptType.getQualifiedName());

        productCmptType.getIpsSrcFile().save(true, null);

        productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration gen = productCmpt.getProductCmptGeneration(0);
        gen.setValidFrom(new GregorianCalendar(2006, 0, 1));
        IFormula ce = gen.newFormula();
        ce.setFormulaSignature(method.getFormulaName());
        ce.setExpression("42");

        IProductCmpt refTarget = newProductCmpt(productCmptType, "RefProduct");
        refTarget.newGeneration(gen.getValidFrom());
        refTarget.setRuntimeId("RefProductRuntimeId");

        IProductCmptLink link = gen.newLink("role");
        link.setTarget(refTarget.getQualifiedName());

        productCmpt.getIpsSrcFile().save(true, null);
        refTarget.getIpsSrcFile().save(true, null);

        assertFalse(productCmpt.validate(productCmpt.getIpsProject()).containsErrorMsg());
    }

    public void testBuild() throws CoreException {
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    public void testDelete() throws CoreException {
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        productCmpt.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
