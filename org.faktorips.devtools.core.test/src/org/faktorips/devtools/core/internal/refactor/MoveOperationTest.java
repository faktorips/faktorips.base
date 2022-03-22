/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for move-operation
 */
public class MoveOperationTest extends AbstractIpsPluginTest {

    private static final String PRODUCT_A_QNAME = "data.products.ProductA"; //$NON-NLS-1$
    private static final String PRODUCT_B_QNAME = "data.products.ProductB"; //$NON-NLS-1$
    private static final String PRODUCT_C_QNAME = "data.products.ProductC"; //$NON-NLS-1$
    private static final String COVERAGE_QNAME = "data.coverages.Coverage"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_NAME = "CoverageType"; //$NON-NLS-1$
    private static final String COVERAGE_TYPE_QNAME = "model." + COVERAGE_TYPE_NAME; //$NON-NLS-1$
    private static final String PRODUCT_QNAME = "model.Product"; //$NON-NLS-1$

    private IIpsProject ipsProject;
    private IProductCmpt productA;
    private IProductCmptGeneration productAGen;
    private IProductCmpt productB;
    private IProductCmptGeneration productBGen;
    private IProductCmpt coverage;
    private IIpsProject ipsProject2;
    private IProductCmpt productC;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        ipsProject2 = newIpsProject();
        IProductCmptType productCmptType1 = newProductCmptType(ipsProject, PRODUCT_QNAME);
        IProductCmptType productCmptType2 = newProductCmptType(ipsProject, COVERAGE_TYPE_QNAME);
        IProductCmptType productCmptType3 = newProductCmptType(ipsProject2, PRODUCT_QNAME);
        IProductCmptTypeAssociation association = productCmptType1.newProductCmptTypeAssociation();
        association.setTarget(productCmptType2.getQualifiedName());
        association.setTargetRoleSingular(COVERAGE_TYPE_NAME);
        IProductCmptTypeAssociation staticAssociation = productCmptType1.newProductCmptTypeAssociation();
        staticAssociation.setTarget(productCmptType2.getQualifiedName());
        staticAssociation.setTargetRoleSingular(COVERAGE_TYPE_NAME);
        staticAssociation.setChangingOverTime(false);

        productCmptType1.getIpsSrcFile().save(true, null);

        coverage = newProductCmpt(productCmptType2, COVERAGE_QNAME);

        productA = newProductCmpt(productCmptType1, PRODUCT_A_QNAME);
        productAGen = productA.getProductCmptGeneration(0);
        productAGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productA.getIpsSrcFile().save(true, null);

        productB = newProductCmpt(productCmptType1, PRODUCT_B_QNAME);
        productBGen = productB.getProductCmptGeneration(0);
        productBGen.newLink(COVERAGE_TYPE_NAME).setTarget(coverage.getQualifiedName());
        productB.getIpsSrcFile().save(true, null);

        productC = newProductCmpt(productCmptType3, PRODUCT_C_QNAME);

    }

    @Test
    public void testCanMove_WithReferencedProject() throws Exception {
        createProjectReference(ipsProject2, ipsProject);
        AFolder folderTarget = ipsProject2.getProject().getFolder("target");
        IIpsObjectPath path = ipsProject2.getIpsObjectPath();
        IIpsSrcFolderEntry newEntry = path.newSourceFolderEntry(folderTarget);

        assertTrue(ipsProject2.isReferencing(ipsProject));
        assertTrue(MoveOperation.canMove(new Object[] { productA }, newEntry.getIpsPackageFragmentRoot()));
    }

    /**
     * Makes a project referencing another project.
     * 
     * @param referencingProject The project that is referencing the referencedProject
     * @param referencedProject The project that is referenced by the referencingProject
     */
    private void createProjectReference(IIpsProject referencingProject, IIpsProject referencedProject) {
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencedProject);
        referencingProject.setIpsObjectPath(path);
    }

    @Test
    public void testCanMove_WithReferencingProject() throws Exception {
        createProjectReference(ipsProject2, ipsProject);
        AFolder folderTarget = ipsProject.getProject().getFolder("target");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry newEntry = path.newSourceFolderEntry(folderTarget);

        assertTrue(ipsProject2.isReferencing(ipsProject));
        assertTrue(MoveOperation.canMove(new Object[] { productC }, newEntry.getIpsPackageFragmentRoot()));
    }

    @Test
    public void testCanMove_WithoutReferencedProject() throws Exception {
        AFolder folderTarget = ipsProject2.getProject().getFolder("target");
        IIpsObjectPath path = ipsProject2.getIpsObjectPath();
        IIpsSrcFolderEntry newEntry = path.newSourceFolderEntry(folderTarget);

        assertFalse(ipsProject2.isReferencing(ipsProject));
        assertFalse(MoveOperation.canMove(new Object[] { productA }, newEntry.getIpsPackageFragmentRoot()));
    }

    @Test
    public void testCanMove_OtherProductCmpt() throws Exception {
        assertFalse(MoveOperation.canMove(new Object[] { productA }, productB));
    }

    @Test
    public void testCanMove_SameProject() throws Exception {
        AFolder folderTarget = ipsProject.getProject().getFolder("target");
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        IIpsSrcFolderEntry newEntry = path.newSourceFolderEntry(folderTarget);

        assertTrue(MoveOperation.canMove(new Object[] { productA }, newEntry.getIpsPackageFragmentRoot()));
    }
}
