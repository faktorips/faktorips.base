/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.abstracttest.TestExtensionRegistry;
import org.faktorips.abstracttest.TestMockingUtils;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
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
    private IPolicyCmptTypeAttribute salesNameAttribute;

    @Mock
    private IProductCmptReference structureMock;
    @Mock
    private IIpsPackageFragment sourcePackageFragment;
    @Mock
    private IIpsPackageFragment targetPackageFragment;
    @Mock
    private IFile sortOrderFile;
    @Mock
    private IFile targetSortOrderFile;
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
     * that, the new files are expected to be existant and not dirty.
     */
    @Test
    public void testCopyAll() throws Exception {
        createTestContent();

        IProductCmpt productCmpt = ipsProject.findProductCmpt("products.ComfortMotorProduct");
        assertNotNull(productCmpt);

        IProductCmptTreeStructure structure = productCmpt.getStructure(productCmpt.getFirstGeneration().getValidFrom(),
                ipsProject);
        Set<IProductCmptStructureReference> toCopy = structure.toSet(true);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(
                    element,
                    ipsObject.getIpsPackageFragment().getIpsSrcFile(
                            "DeepCopyOf" + ipsObject.getName() + "." + ipsObject.getIpsObjectType().getFileExtension()));
            assertFalse(handles.get(element).exists());
        }

        DeepCopyOperation dco = new DeepCopyOperation(structure.getRoot(), toCopy,
                new HashSet<IProductCmptStructureReference>(), handles, new GregorianCalendar(),
                new GregorianCalendar());
        dco.setIpsPackageFragmentRoot(productCmpt.getIpsPackageFragment().getRoot());
        dco.setSourceIpsPackageFragment(productCmpt.getIpsPackageFragment());
        dco.setTargetIpsPackageFragment(productCmpt.getIpsPackageFragment());
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
    public void testExtPropertyCopy() throws Exception {
        createTestContent();
        IProductCmpt productCmpt = ipsProject.findProductCmpt("products.ComfortMotorProduct");
        IProductCmptTreeStructure structure = productCmpt.getStructure(
                (GregorianCalendar)GregorianCalendar.getInstance(), ipsProject);
        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();
        Set<IProductCmptStructureReference> toCopy = structure.toSet(true);

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(
                    element,
                    ipsObject.getIpsPackageFragment().getIpsSrcFile(
                            "DeepCopyOf" + ipsObject.getName() + "." + ipsObject.getIpsObjectType().getFileExtension()));
        }

        String expPropValue = (String)standardVehicle.getExtPropertyValue("StringExtPropForProdCmpts");
        assertEquals("standardVehicleExtPropValue", expPropValue);

        DeepCopyOperation dco = new DeepCopyOperation(structure.getRoot(), toCopy,
                new HashSet<IProductCmptStructureReference>(), handles, new GregorianCalendar(),
                new GregorianCalendar());
        dco.setIpsPackageFragmentRoot(productCmpt.getIpsPackageFragment().getRoot());
        dco.setSourceIpsPackageFragment(productCmpt.getIpsPackageFragment());
        dco.setTargetIpsPackageFragment(productCmpt.getIpsPackageFragment());
        dco.run(null);

        IProductCmptStructureReference srcProdCmptRef = structure.getRoot().findProductCmptReference(
                standardVehicle.getQualifiedName());
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

        IProductCmptTreeStructure structure = comfortMotorProduct.getStructure(comfortMotorProduct.getFirstGeneration()
                .getValidFrom(), ipsProject);
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

        assertEquals(4, copyCount);
        assertEquals(3, refCount);

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();

        for (IProductCmptReference element : toCopy) {
            IProductCmpt cmpt = element.getProductCmpt();
            handles.put(element,
                    cmpt.getIpsPackageFragment().getIpsSrcFile("DeepCopyOf" + cmpt.getName() + ".ipsproduct"));
            assertFalse(handles.get(element).exists());
        }

        DeepCopyOperation dco = new DeepCopyOperation(structure.getRoot(), new HashSet<IProductCmptStructureReference>(
                Arrays.asList(toCopy)), new HashSet<IProductCmptStructureReference>(Arrays.asList(toRefer)), handles,
                new GregorianCalendar(), new GregorianCalendar());
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

        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(
                    element,
                    ipsObject.getIpsPackageFragment().getIpsSrcFile("DeepCopy2Of" + ipsObject.getName(),
                            ipsObject.getIpsObjectType()));
            assertFalse(handles.get(element).exists());
        }

        DeepCopyOperation dco = new DeepCopyOperation(structure.getRoot(), toCopy,
                new HashSet<IProductCmptStructureReference>(), handles, new GregorianCalendar(), new GregorianCalendar(
                        1990, 1, 1));
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
    public void testDeepCopyOperationFixups() throws CoreException, CycleInProductStructureException {
        createTestContent();
        IProductCmptTreeStructure structure = comfortMotorProduct.getStructure(
                (GregorianCalendar)GregorianCalendar.getInstance(), ipsProject);
        Hashtable<IProductCmptStructureReference, IIpsSrcFile> handles = new Hashtable<IProductCmptStructureReference, IIpsSrcFile>();
        Set<IProductCmptStructureReference> toCopy = structure.toSet(true);

        for (IProductCmptStructureReference element : toCopy) {
            IIpsObject ipsObject = element.getWrappedIpsObject();
            handles.put(
                    element,
                    ipsObject.getIpsPackageFragment().getIpsSrcFile(
                            "DeepCopyOf" + ipsObject.getName() + "." + ipsObject.getIpsObjectType().getFileExtension()));
        }

        IProductCmptGeneration generation = comfortMotorProduct.getGenerationEffectiveOn(new GregorianCalendar());
        IConfigElement configElement = generation.newConfigElement(salesNameAttribute);
        configElement.setValue("Foo");

        IpsPlugin ipsPlugin = IpsPlugin.getDefault();
        ipsPlugin = spy(ipsPlugin);
        SingletonMockHelper singletonMockHelper = new SingletonMockHelper();
        try {
            singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);
            IDeepCopyOperationFixup testDeepCopyOperationFixup = mock(IDeepCopyOperationFixup.class);
            Map<String, Object> executableExtensionMap = new HashMap<String, Object>();
            executableExtensionMap.put("class", testDeepCopyOperationFixup);
            IExtension extension = TestMockingUtils.mockExtension("TestDeepCopyOperationFixup",
                    new TestConfigurationElement(IDeepCopyOperationFixup.CONFIG_ELEMENT_ID_FIXUP,
                            new HashMap<String, String>(), null, new IConfigurationElement[0], executableExtensionMap));
            IExtensionPoint extensionPoint = TestMockingUtils.mockExtensionPoint(IpsPlugin.PLUGIN_ID,
                    IDeepCopyOperationFixup.EXTENSION_POINT_ID_DEEP_COPY_OPERATION, extension);
            TestExtensionRegistry extensionRegistry = new TestExtensionRegistry(
                    new IExtensionPoint[] { extensionPoint });
            doReturn(extensionRegistry).when(ipsPlugin).getExtensionRegistry();

            DeepCopyOperation dco = new DeepCopyOperation(structure.getRoot(), toCopy,
                    new HashSet<IProductCmptStructureReference>(), handles, new GregorianCalendar(),
                    new GregorianCalendar());
            dco.setIpsPackageFragmentRoot(comfortMotorProduct.getIpsPackageFragment().getRoot());
            dco.setSourceIpsPackageFragment(comfortMotorProduct.getIpsPackageFragment());
            dco.setTargetIpsPackageFragment(comfortMotorProduct.getIpsPackageFragment());
            dco.run(null);

            IProductCmptStructureReference srcProdCmptRef = structure.getRoot().findProductCmptReference(
                    comfortMotorProduct.getQualifiedName());
            ProductCmpt copiedProductCmpt = (ProductCmpt)handles.get(srcProdCmptRef).getIpsObject();
            generation = copiedProductCmpt.getGenerationEffectiveOn(new GregorianCalendar());
            configElement = generation.getConfigElement("salesName");

            verify(testDeepCopyOperationFixup, times(5)).fix(any(IProductCmpt.class), any(IProductCmpt.class)); // comfortMotorProduct
                                                                                                                // +
                                                                                                                // 4
                                                                                                                // Links
        } finally {
            singletonMockHelper.reset();
        }
    }

    private void createTestContent() throws CoreException {
        createModel();
        createProducts();
    }

    private void createModel() throws CoreException {

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
        createPolicyCmptTypeAssociation(motorContract, vehicle, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "Vehicle", "Vehicles", 0, 1);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject),
                vehicle.findProductCmptType(ipsProject), "VehicleType", "VehicleTypes", 0, 1, true);

        // create association: MotorContract to CollisionCoverage
        createPolicyCmptTypeAssociation(motorContract, collisionCoverage, AssociationType.COMPOSITION_MASTER_TO_DETAIL,
                "CollisionCoverage", "CollisionCoverages", 0, 2);
        createProductCmptTypeAssociation(motorContract.findProductCmptType(ipsProject),
                collisionCoverage.findProductCmptType(ipsProject), "CollisionCoverageType", "CollisionCoverageTypes",
                0, 2, true);

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

        salesNameAttribute = motorContract.newPolicyCmptTypeAttribute("salesName");
        salesNameAttribute.setProductRelevant(true);
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
        mockSourcePackage();
        DeepCopyOperation deepCopyOperation = mockSortOrderDependencies();

        deepCopyOperation.copySortOrder(null);

        verify(sortOrderFile).exists();
        verifyNoMoreInteractions(sortOrderFile);
    }

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
        when(sortOrderFile.exists()).thenReturn(true);
        InputStream inputStream = mock(InputStream.class);
        when(sortOrderFile.getContents(true)).thenReturn(inputStream);

        deepCopyOperation.copySortOrder(progressMonitor);

        verify(subPackage).exists();
        verify(sortOrderFile).exists();
        verify(targetSortOrderFile).create(sortOrderFile.getContents(true), true, progressMonitor);
    }

    @Test
    public void testCopySortOrder_noChild() throws Exception {
        mockSourcePackage();
        mockTargetPackage();
        DeepCopyOperation deepCopyOperation = mockSortOrderDependencies();
        when(sortOrderFile.exists()).thenReturn(true);
        InputStream inputStream = mock(InputStream.class);
        when(sortOrderFile.getContents(true)).thenReturn(inputStream);

        deepCopyOperation.copySortOrder(progressMonitor);

        verify(sortOrderFile).exists();
        verify(targetSortOrderFile).create(sortOrderFile.getContents(true), true, progressMonitor);
    }

    @Test
    public void testCopySortOrder_targetNotExists() throws Exception {
        final IIpsPackageFragment subPackage = mock(IIpsPackageFragment.class);
        mockSourcePackage(subPackage);
        mockTargetPackage();
        DeepCopyOperation deepCopyOperation = mockSortOrderDependencies();
        when(sortOrderFile.exists()).thenReturn(true);
        InputStream inputStream = mock(InputStream.class);
        when(sortOrderFile.getContents(true)).thenReturn(inputStream);
        when(targetPackageFragment.getSubPackage(anyString())).thenReturn(subPackage);

        deepCopyOperation.copySortOrder(progressMonitor);

        verify(sortOrderFile).exists();
        verify(targetSortOrderFile).create(sortOrderFile.getContents(true), true, progressMonitor);
    }

    private DeepCopyOperation mockSortOrderDependencies() {
        Set<IProductCmptStructureReference> copyElements = new HashSet<IProductCmptStructureReference>();
        Set<IProductCmptStructureReference> linkElements = new HashSet<IProductCmptStructureReference>();
        Map<IProductCmptStructureReference, IIpsSrcFile> handleMap = new HashMap<IProductCmptStructureReference, IIpsSrcFile>();
        GregorianCalendar oldValidFrom = (GregorianCalendar)Calendar.getInstance();
        GregorianCalendar newValidFrom = (GregorianCalendar)Calendar.getInstance();
        DeepCopyOperation deepCopyOperation = new DeepCopyOperation(structureMock, copyElements, linkElements,
                handleMap, oldValidFrom, newValidFrom);
        deepCopyOperation.setSourceIpsPackageFragment(sourcePackageFragment);
        deepCopyOperation.setTargetIpsPackageFragment(targetPackageFragment);
        return deepCopyOperation;
    }

    private void mockSourcePackage(IIpsPackageFragment... childPackages) throws Exception {
        when(sourcePackageFragment.getSortOrderFile()).thenReturn(sortOrderFile);
        when(sourcePackageFragment.getChildIpsPackageFragments()).thenReturn(childPackages);

    }

    private void mockTargetPackage(IIpsPackageFragment... childPackages) throws Exception {
        when(targetPackageFragment.getSortOrderFile()).thenReturn(targetSortOrderFile);
        when(targetPackageFragment.getChildIpsPackageFragments()).thenReturn(childPackages);
        IIpsPackageFragmentRoot parent = mock(IIpsPackageFragmentRoot.class);
        when(targetPackageFragment.getRoot()).thenReturn(parent);
    }

}
