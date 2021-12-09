/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.faktorips.abstracttest.matcher.IpsElementNamesMatcher.containsInOrder;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.abstraction.AFile;
import org.faktorips.devtools.model.abstraction.AFolder;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.type.AssociationType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for product component structure.
 * 
 * @author Thorsten Guenther
 */
@RunWith(MockitoJUnitRunner.class)
public class DeepCopyOperationTest extends AbstractIpsPluginTest {

    private static final String TABLE_STRUCTURE = "TableStructure";
    private static final String TABLE_CONTENT = "TableContent";

    private IIpsProject ipsProject;
    private IProductCmpt product;

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
    private ITableContents tableContent;
    private IPolicyCmptTypeAttribute salesNameAttribute;

    @Mock
    private IProductCmptReference structureMock;
    @Mock
    private IpsPackageFragment sourcePackageFragment;
    @Mock
    private IpsPackageFragment targetPackageFragment;
    @Mock
    private AFile sortOrderFile;
    @Mock
    private AFile targetSortOrderFile;
    @Mock
    private IProgressMonitor progressMonitor;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IPolicyCmptType pctype = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        product = newProductCmpt(pctype.findProductCmptType(ipsProject), "Product");
    }

    /**
     * For this test, the comfort-product of the default test content is copied completely. After
     * that, the new files are expected to be existent and not dirty.
     */
    @Test
    public void testCopyAll() throws Exception {
        createTestContent();

        IProductCmpt productCmpt = ipsProject.findProductCmpt("products.ComfortMotorProduct");
        assertNotNull(productCmpt);

        IProductCmptTreeStructure structure = productCmpt.getStructure(productCmpt.getFirstGeneration().getValidFrom(),
                ipsProject);
        Set<IProductCmptStructureReference> toCopy = structure.toSet(false);
        removeReferences(toCopy);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = prependDeepCopyOf(toCopy);

        IpsPackageFragment packageFragment = (IpsPackageFragment)productCmpt.getIpsPackageFragment();
        deepCopy(structure, toCopy, handles, packageFragment, packageFragment);

        for (IProductCmptStructureReference element : toCopy) {
            IIpsSrcFile src = handles.get(element);
            assertTrue(src.exists());

            // we have a race condition, because files are written async. So loop for some times...
            int count = 0;
            while (src.isDirty() && count < 100) {
                count++;
            }

            assertFalse(src.isDirty());

        }

    }

    @Test
    public void testExtPropertyCopy() throws Exception {
        createTestContent();
        IProductCmpt productCmpt = ipsProject.findProductCmpt("products.ComfortMotorProduct");
        IProductCmptTreeStructure structure = productCmpt
                .getStructure((GregorianCalendar)GregorianCalendar.getInstance(), ipsProject);
        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<>();
        Set<IProductCmptStructureReference> toCopy = structure.toSet(true);

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(element, ipsObject.getIpsPackageFragment().getIpsSrcFile(
                    "DeepCopyOf" + ipsObject.getName() + "." + ipsObject.getIpsObjectType().getFileExtension()));
        }

        String expPropValue = (String)standardVehicle.getExtPropertyValue("StringExtPropForProdCmpts");
        assertEquals("standardVehicleExtPropValue", expPropValue);

        IpsPackageFragment packageFragment = (IpsPackageFragment)productCmpt.getIpsPackageFragment();
        deepCopy(structure, toCopy, handles, packageFragment, packageFragment);

        IProductCmptStructureReference srcProdCmptRef = structure.getRoot()
                .findProductCmptReference(standardVehicle.getQualifiedName());
        ProductCmpt copiedProductCmpt = (ProductCmpt)handles.get(srcProdCmptRef).getIpsObject();
        String actPropValue = (String)copiedProductCmpt.getExtPropertyValue("StringExtPropForProdCmpts");

        expPropValue = (String)standardVehicle.getExtPropertyValue("StringExtPropForProdCmpts");
        assertEquals("standardVehicleExtPropValue", expPropValue);

        assertEquals(expPropValue, actPropValue);

    }

    /**
     * For this test, the comfort-product of the default test content is copied only in part. After
     * that, the new files are expected to be existent and not dirty. Some relations from the new
     * objects link now the the not copied old objects.
     */
    @Test
    public void testCopySome() throws Exception {
        createTestContent();

        IProductCmptReference[] toCopy = new IProductCmptReference[4];
        IProductCmptReference[] toRefer = new IProductCmptReference[3];
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

        IProductCmptTreeStructure structure = comfortMotorProduct
                .getStructure(comfortMotorProduct.getFirstGeneration().getValidFrom(), ipsProject);
        IProductCmptReference node = structure.getRoot();
        IProductCmptReference[] children = structure.getChildProductCmptReferences(node);
        for (IProductCmptReference element : children) {
            if (element.getProductCmpt().equals(comfortMotorProduct) || element.getProductCmpt().equals(standardVehicle)
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

        assertEquals(4, copyCount);
        assertEquals(3, refCount);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<>();

        for (IProductCmptReference element : toCopy) {
            IProductCmpt cmpt = element.getProductCmpt();
            handles.put(element,
                    cmpt.getIpsPackageFragment().getIpsSrcFile("DeepCopyOf" + cmpt.getName() + ".ipsproduct"));
            assertFalse(handles.get(element).exists());
        }

        DeepCopyOperation dco = new DeepCopyOperation(structure.getRoot(),
                new HashSet<IProductCmptStructureReference>(Arrays.asList(toCopy)),
                new HashSet<IProductCmptStructureReference>(Arrays.asList(toRefer)), handles, new GregorianCalendar(),
                new GregorianCalendar());
        dco.setIpsPackageFragmentRoot(comfortMotorProduct.getIpsPackageFragment().getRoot());
        dco.setSourceIpsPackageFragment(comfortMotorProduct.getIpsPackageFragment());
        dco.setTargetIpsPackageFragment(comfortMotorProduct.getIpsPackageFragment());
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

        List<IProductCmptLink> allLinks = base.getLinksAsList();
        assertEquals(2, allLinks.size());
        List<IProductCmptLink> linksAsList = base.getLinksAsList("VehicleTypeStatic");
        assertEquals(1, linksAsList.size());
        assertEquals("products.DeepCopyOfStandardVehicle", linksAsList.get(0).getName());

        linksAsList = base.getLinksAsList("TplCoverageTypeStatic");
        assertEquals(1, linksAsList.size());
        assertEquals("products.StandardTplCoverage", linksAsList.get(0).getName());

        IProductCmptGeneration gen = (IProductCmptGeneration)base.getGenerationsOrderedByValidDate()[0];
        linksAsList = gen.getLinksAsList("VehicleType");
        assertEquals(1, linksAsList.size());
        assertEquals("products.DeepCopyOfStandardVehicle", linksAsList.get(0).getName());

        linksAsList = gen.getLinksAsList("CollisionCoverageType");
        assertEquals(2, linksAsList.size());
        assertEquals("products.DeepCopyOfComfortCollisionCoverageA", linksAsList.get(0).getName());
        assertEquals("products.ComfortCollisionCoverageB", linksAsList.get(1).getName());

        IProductCmptLink[] rels = gen.getLinks("TplCoverageType");
        assertEquals(1, rels.length);
        assertEquals("products.StandardTplCoverage", rels[0].getName());

        rels = gen.getLinks("VehicleType");
        assertEquals(1, rels.length);
        assertEquals("products.DeepCopyOfStandardVehicle", rels[0].getName());
    }

    @Test
    public void testCopyWithNoGeneration() throws Exception {
        product = newProductCmpt(ipsProject, "EmptyProduct");
        IProductCmptTreeStructure structure = product.getStructure(new GregorianCalendar(), ipsProject);
        Set<IProductCmptStructureReference> toCopy = structure.toSet(true);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<>();

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(element, ipsObject.getIpsPackageFragment().getIpsSrcFile("DeepCopy2Of" + ipsObject.getName(),
                    ipsObject.getIpsObjectType()));
            assertFalse(handles.get(element).exists());
        }

        DeepCopyOperation dco = new DeepCopyOperation(structure.getRoot(), toCopy,
                new HashSet<IProductCmptStructureReference>(), handles, new GregorianCalendar(),
                new GregorianCalendar(1990, 1, 1));
        dco.setIpsPackageFragmentRoot(product.getIpsPackageFragment().getRoot());
        dco.setSourceIpsPackageFragment(product.getIpsPackageFragment());
        dco.setTargetIpsPackageFragment(product.getIpsPackageFragment());
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

    @Test
    public void testDeepCopyOperationFixups() throws CoreRuntimeException, CycleInProductStructureException {
        createTestContent();
        IProductCmptTreeStructure structure = comfortMotorProduct
                .getStructure((GregorianCalendar)GregorianCalendar.getInstance(), ipsProject);
        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<>();
        Set<IProductCmptStructureReference> toCopy = structure.toSet(true);

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(element, ipsObject.getIpsPackageFragment().getIpsSrcFile(
                    "DeepCopyOf" + ipsObject.getName() + "." + ipsObject.getIpsObjectType().getFileExtension()));
        }

        IProductCmptGeneration generation = comfortMotorProduct.getGenerationEffectiveOn(new GregorianCalendar());
        IConfiguredDefault configElement = generation.newPropertyValue(salesNameAttribute, IConfiguredDefault.class);
        configElement.setValue("Foo");

        IDeepCopyOperationFixup testDeepCopyOperationFixup = mock(IDeepCopyOperationFixup.class);
        try (TestIpsModelExtensions testIpsModelExtensions = new TestIpsModelExtensions()) {
            testIpsModelExtensions.setDeepCopyOperationFixups(Arrays.asList(testDeepCopyOperationFixup));

            IpsPackageFragment packageFragment = (IpsPackageFragment)comfortMotorProduct.getIpsPackageFragment();
            deepCopy(structure, toCopy, handles, packageFragment, packageFragment);

            IProductCmptStructureReference srcProdCmptRef = structure.getRoot()
                    .findProductCmptReference(comfortMotorProduct.getQualifiedName());
            ProductCmpt copiedProductCmpt = (ProductCmpt)handles.get(srcProdCmptRef).getIpsObject();
            generation = copiedProductCmpt.getGenerationEffectiveOn(new GregorianCalendar());
            configElement = generation.getConfiguredDefault("salesName");

            verify(testDeepCopyOperationFixup, times(5)).fix(any(IProductCmpt.class), any(IProductCmpt.class));
            // comfortMotorProduct + 4 Links
        }
    }

    private void createTestContent() throws CoreRuntimeException {
        createModel();
        createProducts();
        createTables();
    }

    private void createModel() throws CoreRuntimeException {

        // set up extension properties
        // IExtensionPropertyDefinition extProp = mock(IExtensionPropertyDefinition.class);
        StringExtensionPropertyDefinition extProp = new StringExtensionPropertyDefinition();
        extProp.setPropertyId("StringExtPropForProdCmpts");
        extProp.setExtendedType(ProductCmpt.class);
        extProp.setDefaultValue("defaultValue");
        ((IpsModel)ipsProject.getIpsModel()).addIpsObjectExtensionProperty(extProp);

        StringExtensionPropertyDefinition extPropPart = new StringExtensionPropertyDefinition();
        extPropPart.setPropertyId("StringExtPropForAttributeValues");
        extPropPart.setExtendedType(AttributeValue.class);
        extPropPart.setDefaultValue("defaultValuePart");
        ((IpsModel)ipsProject.getIpsModel()).addIpsObjectExtensionProperty(extPropPart);

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
        createProductCmptTypeAssociation(contract.findProductCmptType(ipsProject),
                coverage.findProductCmptType(ipsProject), "CoverageType", "CoverageTypes", 0, 1, true);

        // create association: MotorContract to Vehicle
        createPolicyCmptTypeAssociation(motorContract, vehicle, AssociationType.COMPOSITION_MASTER_TO_DETAIL, "Vehicle",
                "Vehicles", 0, 1);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject),
                vehicle.findProductCmptType(ipsProject), "VehicleType", "VehicleTypes", 0, 1, true);

        // create association: MotorContract to CollisionCoverage
        createPolicyCmptTypeAssociation(motorContract, collisionCoverage, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "CollisionCoverage", "CollisionCoverages", 0, 2);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject),
                collisionCoverage.findProductCmptType(ipsProject), "CollisionCoverageType", "CollisionCoverageTypes", 0,
                2, true);

        // create association: MotorContract to TplCoverage
        createPolicyCmptTypeAssociation(motorContract, tplCoverage, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "TplCoverage", "TplCoverages", 0, 1);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject),
                tplCoverage.findProductCmptType(ipsProject), "TplCoverageType", "TplCoverageTypes", 0, 1, true);

        // create static associations
        createPolicyCmptTypeAssociation(motorContract, vehicle, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "VehicleStatic", "VehicleStatics", 0, 1);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject),
                vehicle.findProductCmptType(ipsProject), "VehicleTypeStatic", "VehicleTypeStatics", 0, 1, false);
        createPolicyCmptTypeAssociation(motorContract, tplCoverage, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "TplCoverageStatic", "TplCoverageStatics", 0, 1);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject),
                tplCoverage.findProductCmptType(ipsProject), "TplCoverageTypeStatic", "TplCoverageTypeStatics", 0, 1,
                false);

        ITableStructureUsage newTableStructureUsage = tplCoverage.findProductCmptType(ipsProject)
                .newTableStructureUsage();
        newTableStructureUsage.setRoleName(TABLE_STRUCTURE);
        newTableStructureUsage.addTableStructure(TABLE_STRUCTURE);

        salesNameAttribute = motorContract.newPolicyCmptTypeAttribute("salesName");
        salesNameAttribute.setValueSetConfiguredByProduct(true);
    }

    private void createProducts() throws CoreRuntimeException {
        comfortMotorProduct = newProductCmpt(motorContract.findProductCmptType(ipsProject),
                "products.ComfortMotorProduct");
        comfortMotorProduct.setProductCmptType(motorContract.getProductCmptType());

        sourcePackageFragment = (IpsPackageFragment)comfortMotorProduct.getIpsPackageFragment();

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
        IProductCmptGeneration generation = comfortMotorProduct.getGenerationEffectiveOn(new GregorianCalendar());
        IProductCmptLink link = generation.newLink("VehicleType");
        link.setTarget("products.StandardVehicle");

        link = generation.newLink("CollisionCoverageType");
        link.setTarget("products.ComfortCollisionCoverageA");
        link = generation.newLink("CollisionCoverageType");
        link.setTarget("products.ComfortCollisionCoverageB");

        link = generation.newLink("TplCoverageType");
        link.setTarget("products.StandardTplCoverage");

        link = comfortMotorProduct.newLink("VehicleTypeStatic");
        link.setTarget("products.StandardVehicle");
        link = comfortMotorProduct.newLink("TplCoverageTypeStatic");
        link.setTarget("products.StandardTplCoverage");

        standardVehicle.setExtPropertyValue("StringExtPropForProdCmpts", "standardVehicleExtPropValue");
    }

    private void createTables() throws CoreRuntimeException {
        tableContent = newTableContents(ipsProject, "TableContent");
        ITableContentUsage newTableContentUsage = standardTplCoverage.getLatestProductCmptGeneration()
                .newTableContentUsage();
        newTableContentUsage.setStructureUsage(TABLE_STRUCTURE);
        newTableContentUsage.setTableContentName(TABLE_CONTENT);
        tableContent.getIpsSrcFile().save(true, progressMonitor);
    }

    private void createProductCmptTypeAssociation(IProductCmptType source,
            IProductCmptType target,
            String roleNameSingular,
            String roleNamePlural,
            int minCardinality,
            int maxCardinality,
            boolean changeOverTime) {

        IProductCmptTypeAssociation assoc = source.newProductCmptTypeAssociation();
        assoc.setTarget(target.getQualifiedName());
        assoc.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assoc.setTargetRoleSingular(roleNameSingular);
        assoc.setTargetRolePlural(roleNamePlural);
        assoc.setMinCardinality(minCardinality);
        assoc.setMaxCardinality(maxCardinality);
        assoc.setChangingOverTime(changeOverTime);
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

    @Test
    public void testCopySortOrder_empty() throws Exception {
        when(sourcePackageFragment.getCorrespondingResource()).thenReturn(mock(AFolder.class));
        when(sourcePackageFragment.getChildIpsPackageFragments()).thenReturn(new IIpsPackageFragment[] {});
        DeepCopyOperation deepCopyOperation = mockSortOrderDependencies();

        deepCopyOperation.copySortOrder(Collections.<IProductCmpt, IProductCmpt> emptyMap(), null);

        verifyNoMoreInteractions(targetSortOrderFile);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCopySortOrder_defaultPackage() throws Exception {
        when(targetPackageFragment.getName()).thenReturn(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE);
        when(targetPackageFragment.isDefaultPackage()).thenReturn(true);
        IIpsPackageFragment subPackage = mock(IIpsPackageFragment.class);
        when(subPackage.getLastSegmentName()).thenReturn("abc");
        mockSourcePackage(subPackage);
        mockTargetPackage(subPackage);
        when(targetPackageFragment.getSubPackage("abc")).thenReturn(subPackage);

        DeepCopyOperation deepCopyOperation = mockSortOrderDependencies();

        DefinedOrderComparator sourceOrder = mockDefinedOrderComparator();
        when(sourcePackageFragment.getChildOrderComparator()).thenReturn(sourceOrder);
        when(targetPackageFragment.getChildOrderComparator()).thenReturn(sourceOrder);

        deepCopyOperation.copySortOrder(Collections.<IProductCmpt, IProductCmpt> emptyMap(), progressMonitor);

        verify(subPackage).exists();
        verify(targetPackageFragment, never()).setChildOrderComparator(any(Comparator.class));
    }

    private DefinedOrderComparator mockDefinedOrderComparator(IIpsElement... elements) {
        DefinedOrderComparator definedOrderComparator = mock(DefinedOrderComparator.class);
        when(definedOrderComparator.getElements()).thenReturn(elements);
        return definedOrderComparator;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCopySortOrder_targetAlreadyHasSortOrder() throws Exception {
        when(targetPackageFragment.getName()).thenReturn(IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE);
        when(targetPackageFragment.isDefaultPackage()).thenReturn(true);
        IIpsPackageFragment subPackage = mock(IIpsPackageFragment.class);
        when(subPackage.getLastSegmentName()).thenReturn("abc");
        mockSourcePackage(subPackage);
        mockTargetPackage(subPackage);
        when(targetPackageFragment.getSubPackage("abc")).thenReturn(subPackage);

        DeepCopyOperation deepCopyOperation = mockSortOrderDependencies();

        DefinedOrderComparator existingOrder = mockDefinedOrderComparator();
        when(sourcePackageFragment.getChildOrderComparator()).thenReturn(existingOrder);
        when(targetPackageFragment.getChildOrderComparator()).thenReturn(existingOrder);

        deepCopyOperation.copySortOrder(Collections.<IProductCmpt, IProductCmpt> emptyMap(), progressMonitor);

        verify(subPackage).exists();
        verify(targetPackageFragment, never()).setChildOrderComparator(any(Comparator.class));
    }

    @Test
    public void testCopySortOrder_renamedProductsInNewPackage() throws Exception {
        createTestContent();

        IProductCmptTreeStructure structure = comfortMotorProduct
                .getStructure(comfortMotorProduct.getFirstGeneration().getValidFrom(), ipsProject);
        Set<IProductCmptStructureReference> toCopy = structure.toSet(false);
        removeReferences(toCopy);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = prependDeepCopyOf(toCopy);

        sourcePackageFragment.setChildOrderComparator(new DefinedOrderComparator(standardTplCoverage.getIpsSrcFile(),
                comfortCollisionCoverageB.getIpsSrcFile(), comfortCollisionCoverageA.getIpsSrcFile()));

        IIpsPackageFragment targetPackageFragment = comfortMotorProduct.getIpsPackageFragment().getRoot()
                .createPackageFragment("targetpack", true, null);
        deepCopy(structure, toCopy, handles, sourcePackageFragment, targetPackageFragment);

        Comparator<IIpsElement> targetChildOrderComparator = targetPackageFragment.getChildOrderComparator();
        assertThat(targetChildOrderComparator, is(instanceOf(DefinedOrderComparator.class)));

        IIpsElement[] elements = ((DefinedOrderComparator)targetChildOrderComparator).getElements();
        assertThat(Arrays.asList(elements), containsInOrder("products.DeepCopyOfStandardTplCoverage",
                "products.DeepCopyOfComfortCollisionCoverageB", "products.DeepCopyOfComfortCollisionCoverageA"));
    }

    protected Hashtable<IProductCmptStructureReference, IIpsSrcFile> prependDeepCopyOf(
            Set<IProductCmptStructureReference> toCopy) {
        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<>();

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(element, ipsObject.getIpsPackageFragment().getIpsSrcFile(
                    "DeepCopyOf" + ipsObject.getName() + "." + ipsObject.getIpsObjectType().getFileExtension()));
            assertFalse(handles.get(element).exists());
        }
        return handles;
    }

    @Test
    public void testCopySortOrder_withPackages() throws Exception {
        createTestContent();

        IProductCmptTreeStructure structure = comfortMotorProduct
                .getStructure(comfortMotorProduct.getFirstGeneration().getValidFrom(), ipsProject);
        Set<IProductCmptStructureReference> toCopy = structure.toSet(false);
        removeReferences(toCopy);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = prependDeepCopyOf(toCopy);

        IpsPackageFragment sourcePackageFoo = (IpsPackageFragment)sourcePackageFragment.createSubPackage("foo", true,
                null);
        IIpsPackageFragment sourcePackageFooBar = sourcePackageFoo.createSubPackage("bar", true, null);
        IIpsPackageFragment sourcePackageFooBaz = sourcePackageFoo.createSubPackage("baz", true, null);
        IIpsPackageFragment sourcePackageBar = sourcePackageFragment.createSubPackage("bar", true, null);

        sourcePackageFragment.setChildOrderComparator(new DefinedOrderComparator(sourcePackageFoo, sourcePackageBar));
        sourcePackageFoo.setChildOrderComparator(new DefinedOrderComparator(sourcePackageFooBaz, sourcePackageFooBar));

        IIpsPackageFragment targetPackageFragment = sourcePackageFragment.getRoot().createPackageFragment("targetpack",
                true, null);
        IpsPackageFragment targetPackageFoo = (IpsPackageFragment)targetPackageFragment.createSubPackage("foo", true,
                null);
        targetPackageFoo.createSubPackage("bar", true, null);
        targetPackageFoo.createSubPackage("baz", true, null);
        targetPackageFragment.createSubPackage("bar", true, null);
        deepCopy(structure, toCopy, handles, sourcePackageFragment, targetPackageFragment);

        Comparator<IIpsElement> targetChildOrderComparator = targetPackageFragment.getChildOrderComparator();
        assertThat(targetChildOrderComparator, is(instanceOf(DefinedOrderComparator.class)));

        IIpsElement[] elements = ((DefinedOrderComparator)targetChildOrderComparator).getElements();
        assertThat(Arrays.asList(elements), containsInOrder("targetpack.foo", "targetpack.bar"));

        Comparator<IIpsElement> targetSubChildOrderComparator = targetPackageFoo.getChildOrderComparator();
        assertThat(targetSubChildOrderComparator, is(instanceOf(DefinedOrderComparator.class)));

        elements = ((DefinedOrderComparator)targetSubChildOrderComparator).getElements();
        assertThat(Arrays.asList(elements), containsInOrder("targetpack.foo.baz", "targetpack.foo.bar"));
    }

    @Test
    public void testCopySortOrder_dontCreateEmptySortOrder() throws Exception {
        createTestContent();

        IProductCmptTreeStructure structure = comfortMotorProduct
                .getStructure(comfortMotorProduct.getFirstGeneration().getValidFrom(), ipsProject);
        Set<IProductCmptStructureReference> toCopy = structure.toSet(false);
        removeReferences(toCopy);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = prependDeepCopyOf(toCopy);

        IpsPackageFragment sourcePackageFoo = (IpsPackageFragment)sourcePackageFragment.createSubPackage("foo", true,
                null);
        IIpsPackageFragment sourcePackageFooBar = sourcePackageFoo.createSubPackage("bar", true, null);
        IIpsPackageFragment sourcePackageFooBaz = sourcePackageFoo.createSubPackage("baz", true, null);
        IIpsPackageFragment sourcePackageBar = sourcePackageFragment.createSubPackage("bar", true, null);

        sourcePackageFragment.setChildOrderComparator(new DefinedOrderComparator(sourcePackageFoo, sourcePackageBar));
        sourcePackageFoo.setChildOrderComparator(new DefinedOrderComparator(sourcePackageFooBaz, sourcePackageFooBar));

        IIpsPackageFragment targetPackageFragment = sourcePackageFragment.getRoot().createPackageFragment("targetpack",
                true, null);
        IIpsPackageFragment targetPackageFoo = targetPackageFragment.createSubPackage("foo", true, null);
        targetPackageFragment.createSubPackage("bar", true, null);
        deepCopy(structure, toCopy, handles, sourcePackageFragment, targetPackageFragment);

        Comparator<IIpsElement> targetChildOrderComparator = targetPackageFragment.getChildOrderComparator();
        assertThat(targetChildOrderComparator, is(instanceOf(DefinedOrderComparator.class)));

        IIpsElement[] elements = ((DefinedOrderComparator)targetChildOrderComparator).getElements();
        assertThat(Arrays.asList(elements), containsInOrder("targetpack.foo", "targetpack.bar"));

        Comparator<IIpsElement> targetSubChildOrderComparator = targetPackageFoo.getChildOrderComparator();
        assertThat(targetSubChildOrderComparator, is(not(instanceOf(DefinedOrderComparator.class))));
    }

    @Test
    public void testCopySortOrder_onlyExistingElements() throws Exception {
        createTestContent();

        IProductCmptTreeStructure structure = comfortMotorProduct
                .getStructure(comfortMotorProduct.getFirstGeneration().getValidFrom(), ipsProject);
        Set<IProductCmptStructureReference> toCopy = structure.toSet(false);
        removeReferences(toCopy);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = prependDeepCopyOf(toCopy);

        IIpsPackageFragment sourcePackageFoo = sourcePackageFragment.createSubPackage("foo", true, null);
        IIpsPackageFragment sourcePackageBar = sourcePackageFragment.createSubPackage("bar", true, null);
        ProductCmpt anotherCoverage = newProductCmpt(tplCoverage.findProductCmptType(ipsProject),
                "products.AnotherCoverage");
        anotherCoverage.setProductCmptType(tplCoverage.getProductCmptType());

        sourcePackageFragment.setChildOrderComparator(new DefinedOrderComparator(sourcePackageFoo, sourcePackageBar,
                standardTplCoverage.getIpsSrcFile(), comfortCollisionCoverageB.getIpsSrcFile(),
                comfortCollisionCoverageA.getIpsSrcFile(), anotherCoverage.getIpsSrcFile()));

        IIpsPackageFragment targetPackageFragment = sourcePackageFragment.getRoot().createPackageFragment("targetpack",
                true, null);
        targetPackageFragment.createSubPackage("foo", true, null);
        deepCopy(structure, toCopy, handles, sourcePackageFragment, targetPackageFragment);

        Comparator<IIpsElement> targetChildOrderComparator = targetPackageFragment.getChildOrderComparator();
        assertThat(targetChildOrderComparator, is(instanceOf(DefinedOrderComparator.class)));

        IIpsElement[] elements = ((DefinedOrderComparator)targetChildOrderComparator).getElements();
        assertThat(Arrays.asList(elements), containsInOrder("targetpack.foo", "products.DeepCopyOfStandardTplCoverage",
                "products.DeepCopyOfComfortCollisionCoverageB", "products.DeepCopyOfComfortCollisionCoverageA"));
    }

    private void deepCopy(IProductCmptTreeStructure structure,
            Set<IProductCmptStructureReference> toCopy,
            Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles,
            IpsPackageFragment sourcePackageFragment,
            IIpsPackageFragment targetPackageFragment) throws CoreRuntimeException {
        DeepCopyOperation dco = new DeepCopyOperation(structure.getRoot(), toCopy,
                new HashSet<IProductCmptStructureReference>(), handles, new GregorianCalendar(),
                new GregorianCalendar());
        dco.setIpsPackageFragmentRoot(sourcePackageFragment.getRoot());
        dco.setSourceIpsPackageFragment(sourcePackageFragment);
        dco.setTargetIpsPackageFragment(targetPackageFragment);
        dco.setCreateEmptyTableContents(true);
        dco.run(null);
    }

    protected void removeReferences(Set<IProductCmptStructureReference> toCopy) {
        for (Iterator<IProductCmptStructureReference> iterator = toCopy.iterator(); iterator.hasNext();) {
            IProductCmptStructureReference productCmptStructureReference = iterator.next();
            if (!(productCmptStructureReference instanceof IProductCmptReference
                    || productCmptStructureReference instanceof IProductCmptStructureTblUsageReference)) {
                iterator.remove();
            }
        }
    }

    private DeepCopyOperation mockSortOrderDependencies() {
        Set<IProductCmptStructureReference> copyElements = new HashSet<>();
        Set<IProductCmptStructureReference> linkElements = new HashSet<>();
        Map<IProductCmptStructureReference, IIpsSrcFile> handleMap = new HashMap<>();
        GregorianCalendar oldValidFrom = (GregorianCalendar)Calendar.getInstance();
        GregorianCalendar newValidFrom = (GregorianCalendar)Calendar.getInstance();
        DeepCopyOperation deepCopyOperation = new DeepCopyOperation(structureMock, copyElements, linkElements,
                handleMap, oldValidFrom, newValidFrom);
        deepCopyOperation.setSourceIpsPackageFragment(sourcePackageFragment);
        deepCopyOperation.setTargetIpsPackageFragment(targetPackageFragment);
        return deepCopyOperation;
    }

    private void mockSourcePackage(IIpsPackageFragment... childPackages) throws Exception {
        AFolder sourceFolder = mock(AFolder.class);
        when(sourceFolder.getFile(IIpsPackageFragment.SORT_ORDER_FILE_NAME)).thenReturn(sortOrderFile);
        when(sourcePackageFragment.getCorrespondingResource()).thenReturn(sourceFolder);
        when(sourcePackageFragment.getChildIpsPackageFragments()).thenReturn(childPackages);

    }

    private void mockTargetPackage(IIpsPackageFragment... childPackages) throws Exception {
        AFolder targetFolder = mock(AFolder.class);
        when(targetFolder.getFile(IIpsPackageFragment.SORT_ORDER_FILE_NAME)).thenReturn(targetSortOrderFile);
        when(targetPackageFragment.getCorrespondingResource()).thenReturn(targetFolder);
        when(targetPackageFragment.getChildIpsPackageFragments()).thenReturn(childPackages);
        IIpsPackageFragmentRoot parent = mock(IIpsPackageFragmentRoot.class);
        when(targetPackageFragment.getRoot()).thenReturn(parent);
    }

}
