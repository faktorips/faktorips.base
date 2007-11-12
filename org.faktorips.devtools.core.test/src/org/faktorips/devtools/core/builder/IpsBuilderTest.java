/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
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
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.MessageList;

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

    public IpsBuilderTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        root = ipsProject.getIpsPackageFragmentRoots()[0];
    }
    
    public void testCleanBuildNonDerivedFiles() throws CoreException{
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        IProductCmpt productCmpt = newProductCmpt(type, "Product");
        
        IFolder folder = productCmpt.getIpsPackageFragment().getRoot().getArtefactDestination(true);
        //the artefact destination is expected to be there right from the beginning
        assertTrue(folder.exists());
        
        //after an incremental build the base package and the generated xml file for the product cmpt
        //is expected to be there
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        IFolder baseDir = folder.getFolder(new Path("/org/faktorips/sample/model"));
        assertTrue(baseDir.exists());
        //TODO little dirty here. Better to ask the builder for its package
        IFile productFile = folder.getFile(new Path("/org/faktorips/sample/model/internal/Product.xml"));
        assertTrue(productFile.exists());
        
        //a clean build is expected to remove the base directory and the product xml file.
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        assertFalse(productFile.exists());
        assertFalse(baseDir.exists());

        //a full build creates the base directory and the product component xml file again.
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(baseDir.exists());
        assertTrue(productFile.exists());
        
        //Putting an arbitrary file into a sub folder of the derived destination folder.
        IFile file = baseDir.getFile("keep.txt");
        file.create(new ByteArrayInputStream("".getBytes()), true, null);
        file.setDerived(false);
        assertTrue(file.exists());
        
        //after the clean build the non derived file in the destinations sub folder is expected to stay
        productCmpt.getIpsProject().getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        assertTrue(file.exists());
        assertTrue(baseDir.exists());
    }
    
    private void setTestArtefactBuilder(IIpsProject project, IIpsArtefactBuilder builder) throws CoreException {
        IIpsProjectProperties props = project.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        project.setProperties(props);
        ((IpsModel)project.getIpsModel()).setIpsArtefactBuilderSet(project, new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { builder }));
    }
    
    class AssertThatFullBuildIsTriggeredBuilder extends AbstractArtefactBuilder {

        boolean called = false;
        int buildKind = -1;
        
        /**
         * @param builderSet
         */
        public AssertThatFullBuildIsTriggeredBuilder() {
            super(new TestIpsArtefactBuilderSet());
        }
        
        public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
            called = true;
            this.buildKind = buildKind;
        }


        /**
         * {@inheritDoc}
         */
        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        }

        /**
         * {@inheritDoc}
         */
        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        }

        /**
         * {@inheritDoc}
         */
        public String getName() {
            return "AssertThatFullBuildIsTriggeredBuilder";
        }

        /**
         * {@inheritDoc}
         */
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
            return false;
        }
        
    }

    private static class TestRemoveIpsArtefactBuilder extends AbstractArtefactBuilder {

        /**
         * @param builderSet
         */
        public TestRemoveIpsArtefactBuilder() {
            super(new TestIpsArtefactBuilderSet());
        }

        private boolean buildCalled = false;
        private boolean deleteCalled = false;

        /**
         * {@inheritDoc}
         */
        public String getName() {
            return "TestRemoveIpsArtefactBuilder";
        }

        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
            buildCalled = true;
        }

        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
            return true;
        }

        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
            deleteCalled = true;
        }

    }

    private static class TestDependencyIpsArtefactBuilder extends AbstractArtefactBuilder {

        private List builtIpsObjects = new ArrayList();
        private IIpsProject ipsProjectOfBeforeBuildProcess;
        private IIpsProject ipsProjectOfAfterBuildProcess;

        /**
         * @param builderSet
         */
        public TestDependencyIpsArtefactBuilder() {
            super(new TestIpsArtefactBuilderSet());
        }

        public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
            ipsProjectOfBeforeBuildProcess = project;
        }

        /**
         * {@inheritDoc}
         */
        public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
            ipsProjectOfAfterBuildProcess = project;
        }

        public List getBuiltIpsObjects() {
            return builtIpsObjects;
        }

        public void clear() {
            builtIpsObjects.clear();
            ipsProjectOfBeforeBuildProcess = null;
            ipsProjectOfAfterBuildProcess = null;
        }

        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
            builtIpsObjects.add(ipsSrcFile.getIpsObject());
        }

        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
            return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE_V2)
                || ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)
                || ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT);
        }

        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        }

        /**
         * {@inheritDoc}
         */
        public String getName() {
            return "TestDependencyIpsArtefactBuilder";
        }

    }

    public void testMarkerHandling() throws Exception {
        IPolicyCmptType pcType = newPolicyCmptType(root, "TestPolicy");
        pcType.setSupertype("unknownSupertype");
        pcType.getIpsSrcFile().save(true, null);
        MessageList msgList = pcType.validate(pcType.getIpsProject());
        int numOfMsg = msgList.getNoOfMessages();
        assertTrue(numOfMsg > 0);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        IResource resource = pcType.getEnclosingResource();
        IMarker[] markers = resource.findMarkers(IpsPlugin.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertTrue(markers.length > 0);
        assertEquals(msgList.getMessage(0).getText(), (String)markers[0].getAttribute(IMarker.MESSAGE));
        assertEquals(IMarker.SEVERITY_ERROR, markers[0].getAttribute(IMarker.SEVERITY, -1));

        // test if marker got's deleted if the problem is fixed.
        pcType.setSupertype("");
        pcType.getIpsSrcFile().save(true, null);
        msgList = pcType.validate(ipsProject);
        assertTrue(msgList.getNoOfMessages() < numOfMsg);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        resource = pcType.getEnclosingResource();
        markers = resource.findMarkers(IpsPlugin.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals(msgList.getNoOfMessages(), markers.length);
    }

    public void testDependencyGraphInstanceOfDependency() throws Exception{
        IProductCmptType a = newProductCmptType(root, "A");
        IProductCmptType b = newProductCmptType(root, "B");
        IAssociation aToB = a.newAssociation();
        aToB.setTarget(b.getQualifiedName());
        
        IProductCmpt aProduct = newProductCmpt(a, "AProduct");
        
        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, false);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        List builtIpsObjects = builder.getBuiltIpsObjects();
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
    
    public void testDependencyGraphWithAggregateRootBuilderNoComposits() throws Exception{

        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.A");
        IPolicyCmptType b= newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.C");
        IPolicyCmptTypeAssociation rel = a.newPolicyCmptTypeAssociation();
        rel.setTarget(b.getQualifiedName());
        rel.setAssociationType(AssociationType.ASSOCIATION);
        rel = b.newPolicyCmptTypeAssociation();
        rel.setTarget(c.getQualifiedName());
        rel.setAssociationType(AssociationType.ASSOCIATION);
        
        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, true);
        //initial build: all ipsobjects will be touched
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        
        IPolicyCmptTypeAttribute cAttr = c.newPolicyCmptTypeAttribute();
        cAttr.setName("cAttr");
        c.getIpsSrcFile().save(true, null);
        
        builder.getBuiltIpsObjects().clear();
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        //list is expected to be empty since only master to detail compositions will be build when the
        //the builder set is an aggregate root builder set
        List builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(c));
    }
    
    public void testDependencyGraphWithAggregateRootBuilderWithMasterToChildComposits() throws Exception{

        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.A");
        IPolicyCmptType b= newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.C");
        IPolicyCmptTypeAssociation rel = a.newPolicyCmptTypeAssociation();
        rel.setTarget(b.getQualifiedName());
        rel.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        rel = b.newPolicyCmptTypeAssociation();
        rel.setTarget(c.getQualifiedName());
        rel.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        
        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, true);
        //initial build: all ipsobjects will be touched
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        
        IPolicyCmptTypeAttribute cAttr = c.newPolicyCmptTypeAttribute();
        cAttr.setName("cAttr");
        c.getIpsSrcFile().save(true, null);
        
        builder.getBuiltIpsObjects().clear();
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        //all dependent objects are expected to be in the list since all relations are composite master to
        //detail relations
        List builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(b));
        assertTrue(builtIpsObjects.contains(c));
    }
    
    public void testDependencyGraphWithAggregateRootBuilderWithChildToMasterComposits() throws Exception{

        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.A");
        IPolicyCmptType b= newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.B");
        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "a.b.C");
        IPolicyCmptTypeAssociation rel = a.newPolicyCmptTypeAssociation();
        rel.setTarget(b.getQualifiedName());
        rel.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        rel = b.newPolicyCmptTypeAssociation();
        rel.setTarget(c.getQualifiedName());
        rel.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        
        TestDependencyIpsArtefactBuilder builder = createTestBuilderForProject(ipsProject, true);
        //initial build: all ipsobjects will be touched
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        
        IPolicyCmptTypeAttribute cAttr = c.newPolicyCmptTypeAttribute();
        cAttr.setName("cAttr");
        c.getIpsSrcFile().save(true, null);
        
        builder.getBuiltIpsObjects().clear();
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        //all dependent objects are expected to be in the list since all relations are composite master to
        //detail relations
        List builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(c));
    }
    
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
        List builtIpsObjects = builder.getBuiltIpsObjects();
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

    public void testIsFullBuildTriggeredAfterChangesToIpsArchiveOnObjectPath() throws CoreException {
        IFile archive = ipsProject.getProject().getFile("archive.ipsar");
        IIpsProject project2 = newIpsProject("Project2");
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project2, archive.getLocation().toFile());
        op.run(null);
        archive.refreshLocal(1, null);
        assertTrue(archive.exists());
        
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.newArchiveEntry(archive);
        ipsProject.setIpsObjectPath(path);
        
        AssertThatFullBuildIsTriggeredBuilder builder = new AssertThatFullBuildIsTriggeredBuilder();
        setTestArtefactBuilder(ipsProject, builder);
        builder.called = false;
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(builder.called);
        
        builder.buildKind = -1;
        builder.called = false;
        archive.touch(null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertTrue(builder.called);
        assertEquals(IncrementalProjectBuilder.FULL_BUILD, builder.buildKind);
    }

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
    
    public void testMarkerForNotParsableIpsSrcFiles() throws CoreException{
        IFile file = ((IContainer)root.getCorrespondingResource()).getFile(new Path("test." + IpsObjectType.POLICY_CMPT_TYPE.getFileExtension()));
        file.create(new ByteArrayInputStream("".getBytes()), true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        IMarker[] markers = file.findMarkers(IMarker.PROBLEM, true, 0);
        boolean isMessageThere = false;
        for (int i = 0; i < markers.length; i++) {
            String msg = (String)markers[i].getAttribute(IMarker.MESSAGE);
            if(msg.equals(Messages.IpsBuilder_ipsSrcFileNotParsable)){
                isMessageThere = true;
            }
        }
        assertTrue("The expected message could not be found", isMessageThere);
        
    }
    
    public void testRemoveResource() throws CoreException {
        TestRemoveIpsArtefactBuilder builder = new TestRemoveIpsArtefactBuilder();

        IIpsProjectProperties props = ipsProject.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        ipsProject.setProperties(props);
        ((IpsModel)ipsProject.getIpsModel()).setIpsArtefactBuilderSet(ipsProject, new TestIpsArtefactBuilderSet(
                new IIpsArtefactBuilder[] { builder }));

        IIpsObject ipsObject = this.newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "IpsObjectToRemove");
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        assertTrue(builder.buildCalled);
        ipsObject.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        assertTrue(builder.deleteCalled);
    }

    // TODO pk
    public void testDependencyGraphWithMultipleDelete() {

    }

    public void testDependencyGraphWithReferencingProjects() throws Exception {

        IIpsProject projectB = createSubProject(ipsProject, "projectB");
        IIpsProject projectC = createSubProject(projectB, "projectC");

        IPolicyCmptType a = newPolicyCmptType(ipsProject, "A");
        
        IPolicyCmptType b = newPolicyCmptType(projectB, "B");
        b.setSupertype(a.getQualifiedName());
        
        IPolicyCmptType c = newPolicyCmptType(projectC, "C");
        c.setSupertype(b.getQualifiedName());
        
        TestDependencyIpsArtefactBuilder builderProjectA = createTestBuilderForProject(ipsProject, false);
        // the project needs to have its own builder set otherwise the project is considered
        // invalid since there is no builder set found for the builder set id defined in the
        // project properties
        TestDependencyIpsArtefactBuilder builderProjectB = createTestBuilderForProject(projectB, false);
        TestDependencyIpsArtefactBuilder builderProjectC = createTestBuilderForProject(projectC, false);

        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        projectB.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        projectC.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        List buildObjects = builderProjectA.getBuiltIpsObjects();
        assertTrue(buildObjects.contains(a));

        List buildObjectsB = builderProjectB.getBuiltIpsObjects();
        assertTrue(buildObjectsB.contains(b));

        List buildObjectsC = builderProjectC.getBuiltIpsObjects();
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
        
        TestDependencyIpsArtefactBuilder builderProjectA = createTestBuilderForProject(ipsProject, false);
        // the project needs to have its own builder set otherwise the project is considered
        // invalid since there is no builder set found for the builder set id defined in the
        // project properties
        TestDependencyIpsArtefactBuilder builderProjectB = createTestBuilderForProject(projectB, false);
        TestDependencyIpsArtefactBuilder builderProjectC = createTestBuilderForProject(projectC, false);

        //first initial build
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        projectB.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        projectC.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        //expect the following object in the builders of the projects
        List buildObjects = builderProjectA.getBuiltIpsObjects();
        assertTrue(buildObjects.contains(a));
        assertTrue(buildObjects.contains(aProduct));

        List buildObjectsB = builderProjectB.getBuiltIpsObjects();
        assertTrue(buildObjectsB.contains(b));
        assertTrue(buildObjectsB.contains(bProduct));

        List buildObjectsC = builderProjectC.getBuiltIpsObjects();
        assertTrue(buildObjectsC.contains(c));
        assertTrue(buildObjectsC.contains(cProduct));

        //clean the builders after initial build
        builderProjectA.clear();
        builderProjectB.clear();
        builderProjectC.clear();
 
        //change a product component type in the root project
        IProductCmptTypeAttribute aProductTypeAttr = aProductType.newProductCmptTypeAttribute();
        aProductTypeAttr.setName("aProductTypeAttr");
        aProductTypeAttr.setDatatype("Integer");
        aProductTypeAttr.setModifier(Modifier.PUBLIC);
        aProductTypeAttr.getIpsSrcFile().save(true, null);
        
        //build
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());

        //expect a build of the product components in all the projects
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

    private TestDependencyIpsArtefactBuilder createTestBuilderForProject(IIpsProject project, boolean isAggregateRootBuilderSet) throws CoreException {
        IIpsProjectProperties props = project.getProperties();
        props.setBuilderSetId(TestIpsArtefactBuilderSet.ID);
        project.setProperties(props);
        TestDependencyIpsArtefactBuilder builder = new TestDependencyIpsArtefactBuilder();
        TestIpsArtefactBuilderSet builderSet = new TestIpsArtefactBuilderSet(new IIpsArtefactBuilder[] { builder });
        builderSet.setIpsProject(project);
        builderSet.setAggregateRootBuilder(isAggregateRootBuilderSet);
        ((IpsModel)project.getIpsModel()).setIpsArtefactBuilderSet(project, builderSet);
        return builder;
    }

    public void testCleanBuild() throws CoreException{
        newPolicyCmptType(ipsProject, "mycompany.motor.MotorPolicy");
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        archiveFile.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        File file = archiveFile.getLocation().toFile();
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(ipsProject.getIpsPackageFragmentRoots(), file);
        operation.setInclJavaBinaries(true);
        operation.setInclJavaSources(true);
        operation.run(null);

        IIpsProject project2 = newIpsProject("TestProject2");
        IFile archiveFile2 = project2.getProject().getFile(archiveFile.getLocation());

        IpsArchiveEntry archiveEntry = new IpsArchiveEntry((IpsObjectPath)project2.getIpsObjectPath());
        archiveEntry.setArchiveFile(archiveFile2);
        IIpsObjectPathEntry[] entries = project2.getIpsObjectPath().getEntries();
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length - 1] = archiveEntry;
        IIpsObjectPath newPath = project2.getIpsObjectPath();
        newPath.setEntries(newEntries);
        project2.setIpsObjectPath(newPath);
        
        project2.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
    }

    public void testArtefactBuilderSetIfIpsProjectIsSet() throws CoreException{
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsArtefactBuilderSet builderSet = ((IpsModel)ipsProject.getIpsModel()).getIpsArtefactBuilderSet(ipsProject, false);
        assertEquals(ipsProject, builderSet.getIpsProject());
        
    }
}
