/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.GregorianCalendar;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
public class DeepCopyOperationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmpt product;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IPolicyCmptType pctype = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        product = newProductCmpt(pctype.findProductCmptType(ipsProject), "Product");
    }

    /**
     * For this test, the comfort-product of the default test content is copied completely. After
     * that, the new files are expected to be existant and not dirty.
     */
    public void testCopyAll() throws Exception {
        createTestContent();

        IProductCmpt productCmpt = ipsProject.findProductCmpt("products.ComfortMotorProduct");
        assertNotNull(productCmpt);

        IProductCmptTreeStructure structure = productCmpt.getStructure(ipsProject);
        IProductCmptStructureReference[] toCopy = structure.toArray(true);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(element, ipsObject.getIpsPackageFragment().getIpsSrcFile(
                    "DeepCopyOf" + ipsObject.getName() + "." + ipsObject.getIpsObjectType().getFileExtension()));
            assertFalse(handles.get(element).exists());
        }

        DeepCopyOperation dco = new DeepCopyOperation(toCopy, new IProductCmptReference[0], handles);
        dco.setIpsPackageFragmentRoot(productCmpt.getIpsPackageFragment().getRoot());
        dco.run(null);

        for (IProductCmptStructureReference element : toCopy) {
            IIpsSrcFile src = handles.get(element);
            assertTrue(src.exists());

            // we have a race condition, because files are written async. So loop for some times...
            int count = 0;
            if (src.isDirty() && count < 100) {
                count++;
            }

            assertFalse(src.isDirty());
        }
    }

    /**
     * For this test, the comfort-product of the default test content is copied only in part. After
     * that, the new files are expected to be existant and not dirty. Some relations from the new
     * objects link now the the not copied old objects.
     */
    public void testCopySome() throws Exception {
        createTestContent();

        IProductCmptReference[] toCopy = new IProductCmptReference[3];
        IProductCmptReference[] toRefer = new IProductCmptReference[2];
        int copyCount = 0;
        int refCount = 0;

        IProductCmpt comfortMotorProduct = ipsProject.findProductCmpt("products.ComfortMotorProduct");
        assertNotNull(comfortMotorProduct);

        IProductCmpt standardVehicle = ipsProject.findProductCmpt("products.StandardVehicle");
        assertNotNull(standardVehicle);

        IProductCmpt comfortCollisionCoverageA = ipsProject.findProductCmpt("products.ComfortCollisionCoverageA");
        assertNotNull(comfortCollisionCoverageA);

        IProductCmpt comfortCollisionCoverageB = ipsProject.findProductCmpt("products.ComfortCollisionCoverageB");
        assertNotNull(comfortCollisionCoverageB);

        IProductCmpt standardTplCoverage = ipsProject.findProductCmpt("products.StandardTplCoverage");
        assertNotNull(standardTplCoverage);

        IProductCmptTreeStructure structure = comfortMotorProduct.getStructure(ipsProject);
        IProductCmptReference node = structure.getRoot();
        IProductCmptReference[] children = structure.getChildProductCmptReferences(node);
        for (IProductCmptReference element : children) {
            if (element.getProductCmpt().equals(comfortMotorProduct)
                    || element.getProductCmpt().equals(standardVehicle)
                    || element.getProductCmpt().equals(comfortCollisionCoverageA)) {
                toCopy[copyCount] = element;
                copyCount++;
            } else if (element.getProductCmpt().equals(comfortCollisionCoverageB)
                    || element.getProductCmpt().equals(standardTplCoverage)) {
                toRefer[refCount] = element;
                refCount++;
            }
        }
        toCopy[copyCount] = node;
        copyCount++;

        assertEquals(3, copyCount);
        assertEquals(2, refCount);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();

        for (IProductCmptReference element : toCopy) {
            IProductCmpt cmpt = element.getProductCmpt();
            handles.put(element, cmpt.getIpsPackageFragment().getIpsSrcFile(
                    "DeepCopyOf" + cmpt.getName() + ".ipsproduct"));
            assertFalse(handles.get(element).exists());
        }

        DeepCopyOperation dco = new DeepCopyOperation(toCopy, toRefer, handles);
        dco.setIpsPackageFragmentRoot(comfortMotorProduct.getIpsPackageFragment().getRoot());
        dco.run(null);
        for (IProductCmptReference element : toCopy) {
            IIpsSrcFile src = handles.get(element);
            assertTrue(src.exists());

            // we have a race condition, because files are written async. So loop for some times...
            int count = 0;
            if (src.isDirty() && count < 100) {
                count++;
            }
            assertFalse(src.isDirty());
        }

        IProductCmpt base = (IProductCmpt)handles.get(toCopy[toCopy.length - 1]).getIpsObject();
        IProductCmptGeneration gen = (IProductCmptGeneration)base.getGenerationsOrderedByValidDate()[0];
        IProductCmptLink[] rels = gen.getLinks("TplCoverageType");
        assertEquals(1, rels.length);
        assertEquals("products.StandardTplCoverage", rels[0].getName());

        rels = gen.getLinks("VehicleType");
        assertEquals(1, rels.length);
        assertEquals("products.DeepCopyOfStandardVehicle", rels[0].getName());
    }

    public void testCopyWithNoGeneration() throws Exception {
        product = newProductCmpt(ipsProject, "EmptyProduct");
        IProductCmptTreeStructure structure = product.getStructure(ipsProject);
        IProductCmptStructureReference[] toCopy = structure.toArray(true);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(element, ipsObject.getIpsPackageFragment().getIpsSrcFile("DeepCopy2Of" + ipsObject.getName(),
                    ipsObject.getIpsObjectType()));
            assertFalse(handles.get(element).exists());
        }

        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar(1990, 1, 1));

        DeepCopyOperation dco = new DeepCopyOperation(toCopy, new IProductCmptReference[0], handles);
        dco.setIpsPackageFragmentRoot(product.getIpsPackageFragment().getRoot());
        dco.run(null);

        for (IProductCmptStructureReference element : toCopy) {
            IIpsSrcFile src = handles.get(element);
            assertTrue(src.exists());

            // we have a race condition, because files are written async. So loop for some times...
            int count = 0;
            if (src.isDirty() && count < 100) {
                count++;
            }

            assertFalse(src.isDirty());
        }
    }

    private void createTestContent() throws CoreException {
        createModel();
        createProducts();
    }

    private IPolicyCmptType contract;
    private IPolicyCmptType motorContract;

    private IPolicyCmptType coverage;
    private IPolicyCmptType collisionCoverage;
    private IPolicyCmptType tplCoverage;

    private IPolicyCmptType vehicle;

    private IProductCmpt comfortMotorProduct;
    private IProductCmpt standardVehicle;
    private IProductCmpt comfortCollisionCoverageA;
    private IProductCmpt comfortCollisionCoverageB;
    private IProductCmpt standardTplCoverage;

    private void createModel() throws CoreException {
        contract = newPolicyAndProductCmptType(ipsProject, "independant.Contract", "independant.Product");
        motorContract = newPolicyAndProductCmptType(ipsProject, "motor.MotorContract", "motor.MotorProduct");

        coverage = newPolicyAndProductCmptType(ipsProject, "independant.Coverage", "independant.CoverageType");
        collisionCoverage = newPolicyAndProductCmptType(ipsProject, "motor.CollisionCoverage",
                "motor.CollisionCoverageType");
        tplCoverage = newPolicyAndProductCmptType(ipsProject, "motor.TplCoverage", "motor.TplCoverageType");

        vehicle = newPolicyAndProductCmptType(ipsProject, "motor.Vehicle", "motor.VehicleType");

        // create association: Contract to Coverage
        createPolicyCmptTypeAssociation(contract, coverage, AssociationType.COMPOSITION_MASTER_TO_DETAIL, "Coverage",
                "Coverages", 0, Integer.MAX_VALUE);
        createProductCmptTypeAssociation(contract.findProductCmptType(ipsProject), coverage
                .findProductCmptType(ipsProject), "CoverageType", "CoverageTypes", 0, 1);

        // create association: MotorContract to Vehicle
        createPolicyCmptTypeAssociation(motorContract, vehicle, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "Vehicle", "Vehicles", 0, 1);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject), vehicle
                .findProductCmptType(ipsProject), "VehicleType", "VehicleTypes", 0, 1);

        // create association: MotorContract to CollisionCoverage
        createPolicyCmptTypeAssociation(motorContract, collisionCoverage, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "CollisionCoverage", "CollisionCoverages", 0, 2);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject), collisionCoverage
                .findProductCmptType(ipsProject), "CollisionCoverageType", "CollisionCoverageTypes", 0, 2);

        // create association: MotorContract to TplCoverage
        createPolicyCmptTypeAssociation(motorContract, tplCoverage, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "TplCoverage", "TplCoverages", 0, 1);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject), tplCoverage
                .findProductCmptType(ipsProject), "TplCoverageType", "TplCoverageTypes", 0, 1);
    }

    private void createProducts() throws CoreException {
        comfortMotorProduct = newProductCmpt(motorContract.findProductCmptType(ipsProject),
                "products.ComfortMotorProduct");
        comfortMotorProduct.setProductCmptType(motorContract.getProductCmptType());

        standardVehicle = newProductCmpt(vehicle.findProductCmptType(ipsProject), "products.StandardVehicle");
        standardVehicle.setProductCmptType(vehicle.getProductCmptType());

        comfortCollisionCoverageA = newProductCmpt(collisionCoverage.findProductCmptType(ipsProject),
                "products.ComfortCollisionCoverageA");
        comfortCollisionCoverageA.setProductCmptType(collisionCoverage.getProductCmptType());

        comfortCollisionCoverageB = newProductCmpt(collisionCoverage.findProductCmptType(ipsProject),
                "products.ComfortCollisionCoverageB");
        comfortCollisionCoverageB.setProductCmptType(collisionCoverage.getProductCmptType());

        standardTplCoverage = newProductCmpt(tplCoverage.findProductCmptType(ipsProject),
                "products.StandardTplCoverage");
        standardTplCoverage.setProductCmptType(tplCoverage.getProductCmptType());

        // link products
        IProductCmptGeneration generation = (IProductCmptGeneration)comfortMotorProduct
                .findGenerationEffectiveOn(new GregorianCalendar());
        IProductCmptLink link = generation.newLink("VehicleType");
        link.setTarget("products.StandardVehicle");

        link = generation.newLink("CollisionCoverageType");
        link.setTarget("products.ComfortCollisionCoverageA");
        link = generation.newLink("CollisionCoverageType");
        link.setTarget("products.ComfortCollisionCoverageB");

        link = generation.newLink("TplCoverageType");
        link.setTarget("products.StandardTplCoverage");
    }

    private void createProductCmptTypeAssociation(IProductCmptType source,
            IProductCmptType target,
            String roleNameSingular,
            String roleNamePlural,
            int minCardinality,
            int maxCardinality) {

        IProductCmptTypeAssociation coverageTypeAssoc = source.newProductCmptTypeAssociation();
        coverageTypeAssoc.setTarget(target.getQualifiedName());
        coverageTypeAssoc.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        coverageTypeAssoc.setTargetRoleSingular(roleNameSingular);
        coverageTypeAssoc.setTargetRolePlural(roleNamePlural);
        coverageTypeAssoc.setMinCardinality(minCardinality);
        coverageTypeAssoc.setMaxCardinality(maxCardinality);
    }

    private void createPolicyCmptTypeAssociation(IPolicyCmptType source,
            IPolicyCmptType target,
            AssociationType assocType,
            String roleNameSingular,
            String roleNamePlural,
            int minCardinality,
            int maxCardinality) {

        IPolicyCmptTypeAssociation assoc = source.newPolicyCmptTypeAssociation();
        assoc.setTarget(target.getQualifiedName());
        assoc.setAssociationType(assocType);
        assoc.setTargetRoleSingular(roleNameSingular);
        assoc.setTargetRolePlural(roleNamePlural);
        assoc.setMinCardinality(minCardinality);
        assoc.setMaxCardinality(maxCardinality);
    }
}
