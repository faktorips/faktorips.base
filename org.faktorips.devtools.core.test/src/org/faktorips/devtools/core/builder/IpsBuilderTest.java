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

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestArtefactBuilderSetInfo;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArchiveEntry;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

/**
 * A common base class for builder tests.
 * 
 * @author Jan Ortmann
 */
// IMPORTANT: in the test methods the test builder set has to be set to the model after the
// properties have been set otherwise the builder set will be removed since the setProperties method
// causes a clean to the builder set map of the ips model. Hence when the model is requested
// for the builder set it will look registered builder sets at the extension point and
// won't find the test builder set
public class IpsBuilderTest extends AbstractIpsPluginTest {

    protected IIpsProject ipsProject;
    protected IIpsPackageFragmentRoot root;

    public IpsBuilderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        root = ipsProject.getIpsPackageFragmentRoots()[0];
    }

    @Test
    public void testCleanBuildNonDerivedFiles() throws CoreException {
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        IProductCmpt productCmpt = newProductCmpt(type, "Product");

        IFolder folder = productCmpt.getIpsPackageFragment().getRoot().getArtefactDestination(true);
        // the artefact destination is expected to be there right from the beginning
        assertTrue(folder.exists());

        // after an incremental build the base package and the generated xml file for the product
        // cmpt
        // is expected to be there
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        IFolder baseDir = folder.getFolder(new Path("/org/faktorips/sample/model"));
        assertTrue(baseDir.exists());
        // TODO little dirty here. Better to ask the builder for its package
        IFile productFile = folder.getFile(new Path("/org/faktorips/sample/model/internal/Product.xml"));
        assertTrue(productFile.exists());

        // a clean build is expected to remove the base directory and the product xml file.
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        assertFalse(productFile.exists());
        assertFalse(baseDir.exists());

        // a full build creates the base directory and the product component xml file again.
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(baseDir.exists());
        assertTrue(productFile.exists());

        // Putting an arbitrary file into a sub folder of the derived destination folder.
        IFile file = baseDir.getFile("keep.txt");
        file.create(new ByteArrayInputStream("".getBytes()), true, null);
        file.setDerived(false);
        assertTrue(file.exists());

        // after the clean build the non derived file in the destinations sub folder is expected to
        // stay
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        assertTrue(file.exists());
        assertTrue(baseDir.exists());
    }

    private void setTestArtefactBuilder(IIpsProject project, IIpsArtefactBuilder builder) throws CoreException {
        IIpsProjectProperties props = project.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        project.setProperties(props);
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet(new IIpsArtefactBuilder[] { builder });
        builderSet.setIpsProject(project);
        IIpsArtefactBuilderSetInfo[] builderSetInfos = new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(
                builderSet) };
        ((IpsModel)project.getIpsModel()).setIpsArtefactBuilderSetInfos(builderSetInfos);
    }

    class AssertThatFullBuildIsTriggeredBuilder extends AbstractArtefactBuilder {

        boolean called = false;
        int buildKind = -1;

        public AssertThatFullBuildIsTriggeredBuilder() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

        @Override
        public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
            called = true;
            this.buildKind = buildKind;
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {

        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {

        }

        @Override
        public String getName() {
            return "AssertThatFullBuildIsTriggeredBuilder";
        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
            return false;
        }

    }

    private static class TestRemoveIpsArtefactBuilder extends AbstractArtefactBuilder {

        public TestRemoveIpsArtefactBuilder() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

        private boolean buildCalled = false;

        private boolean deleteCalled = false;

        @Override
        public String getName() {
            return "TestRemoveIpsArtefactBuilder";
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
            buildCalled = true;
        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
            return true;
        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
            deleteCalled = true;
        }

    }

    private static class TestDependencyIpsArtefactBuilder extends AbstractArtefactBuilder {

        private List<IIpsObject> builtIpsObjects = new ArrayList<IIpsObject>();
        private IIpsProject ipsProjectOfBeforeBuildProcess;
        private IIpsProject ipsProjectOfAfterBuildProcess;

        public TestDependencyIpsArtefactBuilder() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

        @Override
        public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
            ipsProjectOfBeforeBuildProcess = project;
        }

        @Override
        public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
            ipsProjectOfAfterBuildProcess = project;
        }

        public List<IIpsObject> getBuiltIpsObjects() {
            return builtIpsObjects;
        }

        public void clear() {
            builtIpsObjects.clear();
            ipsProjectOfBeforeBuildProcess = null;
            ipsProjectOfAfterBuildProcess = null;
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
            builtIpsObjects.add(ipsSrcFile.getIpsObject());
        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
            return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE)
                    || ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)
                    || ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT);
        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        }

        @Override
        public String getName() {
            return "TestDependencyIpsArtefactBuilder";
        }

    }

    @Test
    public void testMarkerHandling() throws Exception {
        IPolicyCmptType pcType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "TestPolicy");
        pcType.setSupertype("UnknownSupertype");
        pcType.getIpsSrcFile().save(true, null);
        MessageList msgList = pcType.validate(pcType.getIpsProject());
        int numOfMsg = msgList.getNoOfMessages();
        assertTrue(numOfMsg > 0);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        IResource resource = pcType.getEnclosingResource();
        IMarker[] markers = resource.findMarkers(IpsPlugin.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertTrue(markers.length > 0);
        assertEquals(msgList.getNoOfMessages(), markers.length);
        Map<String, Integer> msgTexts = new HashMap<String, Integer>();
        for (Object name : msgList) {
            Message msg = (Message)name;
            if (msg.getSeverity() == Message.ERROR) {
                msgTexts.put(msg.getText(), new Integer(IMarker.SEVERITY_ERROR));
            }
            if (msg.getSeverity() == Message.WARNING) {
                msgTexts.put(msg.getText(), new Integer(IMarker.SEVERITY_WARNING));
            }
        }
        for (IMarker marker : markers) {
            assertTrue(msgTexts.keySet().contains(marker.getAttribute(IMarker.MESSAGE)));
            assertEquals(msgTexts.get(marker.getAttribute(IMarker.MESSAGE)), marker.getAttribute(IMarker.SEVERITY));
        }

        // test if marker got's deleted if the problem is fixed.
        pcType.setSupertype("");
        pcType.getIpsSrcFile().save(true, null);
        msgList = pcType.validate(ipsProject);
        assertEquals(0, msgList.getNoOfMessages());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        resource = pcType.getEnclosingResource();
        markers = resource.findMarkers(IpsPlugin.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals(msgList.getNoOfMessages(), markers.length);
    }

    @Test
    public void testDependencyGraphInstanceOfDependency() throws Exception {
        IProductCmptType a = newProductCmptType(root, "A");
        IProductCmptType b = newProductCmptType(root, "B");
        IAssociation aToB = a.newAssociation();
        aToB.setTarget(b.getQualifiedName());

        IProductCmpt aProduct = newProductCmpt(a, "AProduct");

        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, false);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        List<IIpsObject> builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(b));
        assertTrue(builtIpsObjects.contains(aProduct));

        builtIpsObjects.clear();
        assertTrue(builtIpsObjects.isEmpty());

        IProductCmptTypeAttribute bAttr = b.newProductCmptTypeAttribute("bAttr");
        bAttr.setDatatype("String");
        b.getIpsSrcFile().save(true, null);

        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(b));
        assertTrue(builtIpsObjects.contains(aProduct));
    }

    @Test
    public void testDependencyGraphDatatypeAndInstanceOfDependency() throws Exception {
        IPolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "A", "AConfigType");
        IAttribute aAttr = a.newAttribute();
        aAttr.setName("aAttr");
        aAttr.setDatatype("Integer");
        aAttr.setModifier(Modifier.PUBLIC);

        IProductCmptType aConfigType = a.findProductCmptType(ipsProject);
        IProductCmptTypeMethod formula = aConfigType.newFormulaSignature("formula");
        formula.setDatatype("Integer");
        formula.setModifier(Modifier.PUBLIC);
        formula.setName("calculateValue");

        formula.newParameter(a.getQualifiedName(), "pA");

        IProductCmpt aProduct = newProductCmpt(aConfigType, "AProduct");
        IProductCmptGeneration aProductGeneration = (IProductCmptGeneration)aProduct.getFirstGeneration();
        IFormula productFormula = aProductGeneration.newFormula(formula);
        productFormula.setExpression("pA.aAttr");

        a.getIpsSrcFile().save(true, null);
        aConfigType.getIpsSrcFile().save(true, null);
        aProduct.getIpsSrcFile().save(true, null);

        final TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, false);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        List<IIpsObject> builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(aConfigType));
        assertTrue(builtIpsObjects.contains(aProduct));

        builtIpsObjects.clear();
        assertTrue(builtIpsObjects.isEmpty());

        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        assertTrue(builtIpsObjects.isEmpty());

        aAttr.setDatatype("String");
        final IPolicyCmptType aFinal = a;
        final IProductCmptType aConfigTypeFinal = aConfigType;
        final IProductCmpt aProductFinal = aProduct;

        /*
         * to ensure that the build has finished before the results are checked the assertions are
         * done within this resource change listener. The listener is registered for post build
         * events it is necessary to remove the listener after assertion sind the workspace will be
         * the same for all test cases that are executed in one test suite
         */
        IResourceChangeListener listener = new IResourceChangeListener() {
            @Override
            public void resourceChanged(IResourceChangeEvent event) {
                getIpsModel().getWorkspace().removeResourceChangeListener(this);
                List<IIpsObject> builtIpsObjects = builder.getBuiltIpsObjects();
                assertTrue(builtIpsObjects.contains(aFinal));
                assertTrue(builtIpsObjects.contains(aConfigTypeFinal));
                assertTrue(builtIpsObjects.contains(aProductFinal));
            }
        };
        getIpsModel().getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_BUILD);
        a.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
    }

    @Test
    public void testDependencyGraphWithAggregateRootBuilderNoComposits() throws Exception {
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.C");
        IPolicyCmptTypeAssociation rel = a.newPolicyCmptTypeAssociation();
        rel.setTarget(b.getQualifiedName());
        rel.setAssociationType(AssociationType.ASSOCIATION);
        rel = b.newPolicyCmptTypeAssociation();
        rel.setTarget(c.getQualifiedName());
        rel.setAssociationType(AssociationType.ASSOCIATION);

        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, true);
        // initial build: all ipsobjects will be touched
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        IPolicyCmptTypeAttribute cAttr = c.newPolicyCmptTypeAttribute();
        cAttr.setName("cAttr");
        c.getIpsSrcFile().save(true, null);

        builder.getBuiltIpsObjects().clear();
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        /*
         * list is expected to be empty since only master to detail compositions will be build when
         * the the builder set is an aggregate root builder set
         */
        List<IIpsObject> builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(c));
    }

    @Test
    public void testDependencyGraphWithAggregateRootBuilderWithMasterToChildComposits() throws Exception {

        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.C");
        IPolicyCmptTypeAssociation rel = a.newPolicyCmptTypeAssociation();
        rel.setTarget(b.getQualifiedName());
        rel.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        rel = b.newPolicyCmptTypeAssociation();
        rel.setTarget(c.getQualifiedName());
        rel.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, true);
        // initial build: all ipsobjects will be touched
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        IPolicyCmptTypeAttribute cAttr = c.newPolicyCmptTypeAttribute();
        cAttr.setName("cAttr");
        c.getIpsSrcFile().save(true, null);

        builder.getBuiltIpsObjects().clear();
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        /*
         * all dependent objects are expected to be in the list since all relations are composite
         * master to detail relations
         */
        List<IIpsObject> builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(b));
        assertTrue(builtIpsObjects.contains(c));
    }

    @Test
    public void testDependencyGraphWithAggregateRootBuilderWithChildToMasterComposits() throws Exception {

        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.C");
        IPolicyCmptTypeAssociation rel = a.newPolicyCmptTypeAssociation();
        rel.setTarget(b.getQualifiedName());
        rel.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        rel = b.newPolicyCmptTypeAssociation();
        rel.setTarget(c.getQualifiedName());
        rel.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, true);
        // initial build: all ipsobjects will be touched
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        IPolicyCmptTypeAttribute cAttr = c.newPolicyCmptTypeAttribute();
        cAttr.setName("cAttr");
        c.getIpsSrcFile().save(true, null);

        builder.getBuiltIpsObjects().clear();
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        /*
         * all dependent objects are expected to be in the list since all relations are composite
         * master to detail relations
         */
        List<IIpsObject> builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(c));
    }

    @Test
    public void testDependencyGraph() throws CoreException {
        IProductCmptType a = newProductCmptType(root, "A");
        IProductCmptType b = newProductCmptType(root, "B");
        b.setSupertype(a.getQualifiedName());
        IProductCmptType c = newProductCmptType(root, "C");
        c.newAssociation().setTarget(a.getQualifiedName());
        IProductCmpt aProduct = newProductCmpt(a, "AProduct");
        IProductCmptType d = newProductCmptType(root, "D");
        a.newAssociation().setTarget(d.getQualifiedName());

        // dependencies: b->a, c->a, aProduct->a, a->d

        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, false);

        // after this incremental build the TestDependencyIpsArtefactBuilder is expected to contain
        // all new IpsObjects in its build list.
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        List<IIpsObject> builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(b));
        assertTrue(builtIpsObjects.contains(c));
        assertTrue(builtIpsObjects.contains(d));
        assertTrue(builtIpsObjects.contains(aProduct));

        builder.clear();
        // after this second build no IpsObjects are expected in the build list since nothing has
        // changed since the last build
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        assertTrue(builder.getBuiltIpsObjects().isEmpty());

        // since the ProductCmptType d has been deleted after this build the
        // TestDependencyIpsArtefactBuilder is expected to contain all dependent IpsObjects
        d.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(aProduct));

        // recreate d. All dependants are expected to be rebuilt
        d = newProductCmptType(root, "D");
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(aProduct));

        // delete d and dependants. The IpsBuilder has to make sure to only build the existing
        // IpsObjects though the graph still contains the dependency chain of the deleted IpsOjects
        // during the build cycle
        d.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        a.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        b.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        c.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
    }

    @Test
    public void testIsFullBuildTriggeredAfterChangesToIpsArchiveOnObjectPath() throws CoreException {
        IFile archiveFile = ipsProject.getProject().getFile("archive.ipsar");
        IPath archivePath = archiveFile.getLocation();
        IIpsProject project2 = newIpsProject("Project2");
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project2, archiveFile.getLocation().toFile());
        op.run(null);
        archiveFile.refreshLocal(1, null);
        assertTrue(archiveFile.exists());

        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newArchiveEntry(archivePath);
        ipsProject.setIpsObjectPath(path);

        AssertThatFullBuildIsTriggeredBuilder builder = new AssertThatFullBuildIsTriggeredBuilder();
        setTestArtefactBuilder(ipsProject, builder);
        builder.called = false;
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(builder.called);

        builder.buildKind = -1;
        builder.called = false;
        archiveFile.touch(null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(builder.called);
        assertEquals(IncrementalProjectBuilder.FULL_BUILD, builder.buildKind);
    }

    @Test
    public void testIsFullBuildTriggeredAfterChangesToIpsProjectFile() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        AssertThatFullBuildIsTriggeredBuilder builder = new AssertThatFullBuildIsTriggeredBuilder();
        setTestArtefactBuilder(ipsProject, builder); // this changes the properties file!
        builder.buildKind = -1;
        builder.called = false;
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(builder.called);
        assertEquals(IncrementalProjectBuilder.FULL_BUILD, builder.buildKind);
    }

    @Test
    public void testMarkerForNotParsableIpsSrcFiles() throws CoreException, UnsupportedEncodingException {
        IFile file = ((IContainer)root.getCorrespondingResource()).getFile(new Path("test."
                + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension()));
        String xml = "invalid xml";
        suppressLoggingDuringExecutionOfThisTestCase();
        file.create(new ByteArrayInputStream(xml.getBytes(ipsProject.getXmlFileCharset())), true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        IMarker[] markers = file.findMarkers(IMarker.PROBLEM, true, 0);
        boolean isMessageThere = false;
        for (IMarker marker : markers) {
            String msg = (String)marker.getAttribute(IMarker.MESSAGE);
            if (msg.equals(Messages.IpsBuilder_ipsSrcFileNotParsable)) {
                isMessageThere = true;
            }
        }
        assertTrue("The expected message could not be found", isMessageThere);
    }

    @Test
    public void testRemoveResource() throws CoreException {
        TestRemoveIpsArtefactBuilder builder = new TestRemoveIpsArtefactBuilder();

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        ipsProject.setProperties(props);
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet(new IIpsArtefactBuilder[] { builder });
        builderSet.setIpsProject(ipsProject);
        ((IpsModel)ipsProject.getIpsModel())
                .setIpsArtefactBuilderSetInfos(new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(
                        builderSet) });

        IIpsObject ipsObject = this.newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "IpsObjectToRemove");
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        assertTrue(builder.buildCalled);
        ipsObject.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        assertTrue(builder.deleteCalled);
    }

    // TODO pk
    @Test
    public void testDependencyGraphWithMultipleDelete() {

    }

    @Test
    public void testDependencyGraphWithReferencingProjects() throws Exception {
        IIpsProject projectB = createSubProject(ipsProject, "projectB");
        IIpsProject projectC = createSubProject(projectB, "projectC");

        IPolicyCmptType a = newPolicyCmptType(ipsProject, "A");

        IPolicyCmptType b = newPolicyCmptType(projectB, "B");
        b.setSupertype(a.getQualifiedName());

        IPolicyCmptType c = newPolicyCmptType(projectC, "C");
        c.setSupertype(b.getQualifiedName());

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        ipsProject.setProperties(props);
        TestDependencyIpsArtefactBuilder builderProjectA = new TestDependencyIpsArtefactBuilder();
        TestIpsArtefactBuilderSet builderSetProjectA = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { builderProjectA });
        builderSetProjectA.setIpsProject(ipsProject);
        builderSetProjectA.setAggregateRootBuilder(false);

        // the project needs to have its own builder set otherwise the project is considered
        // invalid since there is no builder set found for the builder set id defined in the
        // project properties
        props = projectB.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        projectB.setProperties(props);
        TestDependencyIpsArtefactBuilder builderProjectB = new TestDependencyIpsArtefactBuilder();
        TestIpsArtefactBuilderSet builderSetProjectB = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { builderProjectB });
        builderSetProjectB.setIpsProject(projectB);
        builderSetProjectB.setAggregateRootBuilder(false);

        props = projectC.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        projectC.setProperties(props);
        TestDependencyIpsArtefactBuilder builderProjectC = new TestDependencyIpsArtefactBuilder();
        TestIpsArtefactBuilderSet builderSetProjectC = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { builderProjectC });
        builderSetProjectC.setIpsProject(projectC);
        builderSetProjectC.setAggregateRootBuilder(false);

        IIpsArtefactBuilderSetInfo[] builderSetInfos = new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(
                TestIpsArtefactBuilderSet.ID, new IIpsArtefactBuilderSet[] { builderSetProjectA, builderSetProjectB,
                        builderSetProjectC }) };
        ((IpsModel)ipsProject.getIpsModel()).setIpsArtefactBuilderSetInfos(builderSetInfos);

        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        projectB.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        projectC.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        List<IIpsObject> buildObjects = builderProjectA.getBuiltIpsObjects();
        assertTrue(buildObjects.contains(a));

        List<IIpsObject> buildObjectsB = builderProjectB.getBuiltIpsObjects();
        assertTrue(buildObjectsB.contains(b));

        List<IIpsObject> buildObjectsC = builderProjectC.getBuiltIpsObjects();
        assertTrue(buildObjectsC.contains(c));

        builderProjectA.clear();
        builderProjectB.clear();
        builderProjectC.clear();

        IPolicyCmptTypeAttribute attrA = a.newPolicyCmptTypeAttribute();
        attrA.setName("AttrA");
        attrA.setAttributeType(AttributeType.CHANGEABLE);
        attrA.setDatatype("String");
        a.getIpsSrcFile().save(true, null);

        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        buildObjects = builderProjectA.getBuiltIpsObjects();
        assertTrue(buildObjects.contains(a));

        buildObjectsB = builderProjectB.getBuiltIpsObjects();
        assertTrue(buildObjectsB.contains(b));

        buildObjectsC = builderProjectC.getBuiltIpsObjects();
        assertTrue(buildObjectsC.contains(c));

        builderProjectA.clear();
        builderProjectB.clear();
        builderProjectC.clear();

        attrA = a.newPolicyCmptTypeAttribute();
        attrA.setName("attrB");
        attrA.setAttributeType(AttributeType.CHANGEABLE);
        attrA.setDatatype("String");
        a.getIpsSrcFile().save(true, null);

        ((IpsProject)projectC).getIpsProjectPropertiesFile().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        buildObjects = builderProjectA.getBuiltIpsObjects();
        assertTrue(buildObjects.contains(a));

        buildObjectsB = builderProjectB.getBuiltIpsObjects();
        assertTrue(buildObjectsB.contains(b));

        buildObjectsC = builderProjectC.getBuiltIpsObjects();
        assertFalse(buildObjectsC.contains(c));
    }

    @Test
    public void testDependencyGraphWithProductsInReferencingProjects() throws Exception {
        IIpsProject projectB = createSubProject(ipsProject, "projectB");
        IIpsProject projectC = createSubProject(projectB, "projectC");

        IPolicyCmptType a = newPolicyCmptType(ipsProject, "A");
        IProductCmptType aProductType = newProductCmptType(ipsProject, "aProductType");
        IProductCmpt aProduct = newProductCmpt(aProductType, "aProduct");
        a.setProductCmptType(aProductType.getQualifiedName());
        aProductType.setPolicyCmptType(a.getQualifiedName());

        IPolicyCmptType b = newPolicyCmptType(projectB, "B");
        b.setSupertype(a.getQualifiedName());

        IProductCmptType bProductType = newProductCmptType(projectB, "bProductType");
        bProductType.setPolicyCmptType(b.getQualifiedName());
        bProductType.setSupertype(aProductType.getQualifiedName());

        IProductCmpt bProduct = newProductCmpt(bProductType, "bProduct");
        b.setProductCmptType(bProductType.getQualifiedName());

        IPolicyCmptType c = newPolicyCmptType(projectC, "C");
        c.setSupertype(b.getQualifiedName());

        IProductCmptType cProductType = newProductCmptType(projectC, "cProductType");
        c.setProductCmptType(cProductType.getQualifiedName());
        cProductType.setPolicyCmptType(c.getQualifiedName());

        cProductType.setSupertype(bProductType.getQualifiedName());
        IProductCmpt cProduct = newProductCmpt(cProductType, "cProduct");

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        ipsProject.setProperties(props);
        TestDependencyIpsArtefactBuilder builderProjectA = new TestDependencyIpsArtefactBuilder();
        TestIpsArtefactBuilderSet builderSetProjectA = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { builderProjectA });
        builderSetProjectA.setIpsProject(ipsProject);
        builderSetProjectA.setAggregateRootBuilder(false);

        // the project needs to have its own builder set otherwise the project is considered
        // invalid since there is no builder set found for the builder set id defined in the
        // project properties
        props = projectB.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        projectB.setProperties(props);
        TestDependencyIpsArtefactBuilder builderProjectB = new TestDependencyIpsArtefactBuilder();
        TestIpsArtefactBuilderSet builderSetProjectB = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { builderProjectB });
        builderSetProjectB.setIpsProject(projectB);
        builderSetProjectB.setAggregateRootBuilder(false);

        props = projectC.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        projectC.setProperties(props);
        TestDependencyIpsArtefactBuilder builderProjectC = new TestDependencyIpsArtefactBuilder();
        TestIpsArtefactBuilderSet builderSetProjectC = new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { builderProjectC });
        builderSetProjectC.setIpsProject(projectC);
        builderSetProjectC.setAggregateRootBuilder(false);

        IIpsArtefactBuilderSetInfo[] builderSetInfos = new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(
                TestIpsArtefactBuilderSet.ID, new IIpsArtefactBuilderSet[] { builderSetProjectA, builderSetProjectB,
                        builderSetProjectC }) };
        ((IpsModel)ipsProject.getIpsModel()).setIpsArtefactBuilderSetInfos(builderSetInfos);

        // first initial build
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        projectB.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        projectC.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        // expect the following object in the builders of the projects
        List<IIpsObject> buildObjects = builderProjectA.getBuiltIpsObjects();
        assertTrue(buildObjects.contains(a));
        assertTrue(buildObjects.contains(aProduct));

        List<IIpsObject> buildObjectsB = builderProjectB.getBuiltIpsObjects();
        assertTrue(buildObjectsB.contains(b));
        assertTrue(buildObjectsB.contains(bProduct));

        List<IIpsObject> buildObjectsC = builderProjectC.getBuiltIpsObjects();
        assertTrue(buildObjectsC.contains(c));
        assertTrue(buildObjectsC.contains(cProduct));

        // clean the builders after initial build
        builderProjectA.clear();
        builderProjectB.clear();
        builderProjectC.clear();

        // change a product component type in the root project
        IProductCmptTypeAttribute aProductTypeAttr = aProductType.newProductCmptTypeAttribute();
        aProductTypeAttr.setName("aProductTypeAttr");
        aProductTypeAttr.setDatatype("Integer");
        aProductTypeAttr.setModifier(Modifier.PUBLIC);
        aProductTypeAttr.getIpsSrcFile().save(true, null);

        // build
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        // expect a build of the product components in all the projects
        buildObjects = builderProjectA.getBuiltIpsObjects();
        assertTrue(buildObjects.contains(aProduct));
        assertEquals(ipsProject, builderProjectA.ipsProjectOfBeforeBuildProcess);
        assertEquals(ipsProject, builderProjectA.ipsProjectOfAfterBuildProcess);

        buildObjectsB = builderProjectB.getBuiltIpsObjects();
        assertTrue(buildObjectsB.contains(bProduct));
        assertEquals(projectB, builderProjectB.ipsProjectOfBeforeBuildProcess);
        assertEquals(projectB, builderProjectB.ipsProjectOfAfterBuildProcess);

        buildObjectsC = builderProjectC.getBuiltIpsObjects();
        assertTrue(buildObjectsC.contains(cProduct));
        assertEquals(projectC, builderProjectC.ipsProjectOfBeforeBuildProcess);
        assertEquals(projectC, builderProjectC.ipsProjectOfAfterBuildProcess);
    }

    private IIpsProject createSubProject(IIpsProject superProject, String projectName) throws CoreException {
        IIpsProject subProject = newIpsProject(projectName);
        // set the reference from the ips project to the referenced project
        IIpsObjectPath path = subProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(superProject);
        IProjectDescription description = subProject.getProject().getDescription();
        description.setReferencedProjects(new IProject[] { superProject.getProject() });
        subProject.getProject().setDescription(description, new NullProgressMonitor());
        subProject.setIpsObjectPath(path);
        return subProject;
    }

    private TestDependencyIpsArtefactBuilder createTestBuilderForProject(IIpsProject project,
            boolean isAggregateRootBuilderSet) throws CoreException {

        IIpsProjectProperties props = project.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        project.setProperties(props);
        TestDependencyIpsArtefactBuilder builder = new TestDependencyIpsArtefactBuilder();
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet(new IIpsArtefactBuilder[] { builder });
        builderSet.setIpsProject(project);
        builderSet.setAggregateRootBuilder(isAggregateRootBuilderSet);
        IIpsArtefactBuilderSetInfo[] builderSetInfos = new IIpsArtefactBuilderSetInfo[] { new TestArtefactBuilderSetInfo(
                builderSet) };
        ((IpsModel)project.getIpsModel()).setIpsArtefactBuilderSetInfos(builderSetInfos);
        return builder;
    }

    @Test
    public void testCleanBuild() throws CoreException {
        newPolicyCmptType(ipsProject, "mycompany.motor.MotorPolicy");
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        archiveFile.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        File file = archiveFile.getLocation().toFile();
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(ipsProject.getIpsPackageFragmentRoots(),
                file);
        operation.setInclJavaBinaries(true);
        operation.setInclJavaSources(true);
        operation.run(null);

        IIpsProject project2 = newIpsProject("TestProject2");
        IFile archiveFile2 = project2.getProject().getFile(archiveFile.getLocation());
        IPath archivePath2 = archiveFile2.getLocation();

        IpsArchiveEntry archiveEntry = new IpsArchiveEntry((IpsObjectPath)project2.getIpsObjectPath());
        archiveEntry.setArchivePath(project2, archivePath2);
        IIpsObjectPathEntry[] entries = project2.getIpsObjectPath().getEntries();
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length - 1] = archiveEntry;
        IIpsObjectPath newPath = project2.getIpsObjectPath();
        newPath.setEntries(newEntries);
        project2.setIpsObjectPath(newPath);

        project2.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
    }

    @Test
    public void testArtefactBuilderSetIfIpsProjectIsSet() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsArtefactBuilderSet builderSet = ((IpsModel)ipsProject.getIpsModel()).getIpsArtefactBuilderSet(ipsProject,
                false);
        assertEquals(ipsProject, builderSet.getIpsProject());
    }
}
