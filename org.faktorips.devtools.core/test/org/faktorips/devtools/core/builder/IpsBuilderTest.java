package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.util.message.MessageList;

/**
 * A common base class for builder tests.
 * 
 * @author Jan Ortmann
 */
public class IpsBuilderTest extends IpsPluginTest {

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
        ipsProject = this.newIpsProject("TestProject");
        root = ipsProject.getIpsPackageFragmentRoots()[0];
    }

    public void testMarkerHandling() throws Exception {
        IPolicyCmptType pcType = newPolicyCmptType(root, "TestPolicy");
        pcType.setSupertype("unknownSupertype");
        pcType.getIpsSrcFile().save(true, null);
        MessageList msgList = pcType.validate();
        assertEquals(1, msgList.getNoOfMessages());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
        IResource resource = pcType.getEnclosingResource();
        IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        assertEquals(1, markers.length);
        assertEquals(msgList.getMessage(0).getText(), (String)markers[0]
                .getAttribute(IMarker.MESSAGE));
        assertEquals(IMarker.SEVERITY_ERROR, markers[0].getAttribute(IMarker.SEVERITY, -1));

        // test if marker got's deleted if the problem is fixed.
        pcType.setSupertype("");
        pcType.getIpsSrcFile().save(true, null);
        msgList = pcType.validate();
        assertEquals(0, msgList.getNoOfMessages());
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
        resource = pcType.getEnclosingResource();
        markers = resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        assertEquals(0, markers.length);
    }

    public void testRemoveResource() throws CoreException {
        TestRemoveIpsArtefactBuilder builder = new TestRemoveIpsArtefactBuilder();
        ipsProject.getIpsModel().setAvailableArtefactBuilderSets(
            new IIpsArtefactBuilderSet[] { new TestIpsArtefactBuilderSet(
                    new IIpsArtefactBuilder[] { builder }) });
        ipsProject.setCurrentArtefactBuilderSet(TestIpsArtefactBuilderSet.ID);
        IIpsObject ipsObject = this.newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE,
            "IpsObjectToRemove");
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
        assertTrue(builder.buildCalled);
        ipsObject.getIpsSrcFile().getCorrespondingFile().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
        assertTrue(builder.deleteCalled);
    }

    public void testDependencyGraphWithMultipleDelete() {

    }

    public void testDependencyGraph() throws CoreException {

        IIpsPackageFragment frag = root.createPackageFragment("dependency", true, null);

        IPolicyCmptType a = (IPolicyCmptType)newIpsObject(frag, IpsObjectType.POLICY_CMPT_TYPE, "A");
        IPolicyCmptType b = (IPolicyCmptType)newIpsObject(frag, IpsObjectType.POLICY_CMPT_TYPE, "B");
        b.setSupertype(a.getQualifiedName());
        IPolicyCmptType c = (IPolicyCmptType)newIpsObject(frag, IpsObjectType.POLICY_CMPT_TYPE, "C");
        c.newRelation().setTarget(a.getQualifiedName());
        IProductCmpt aProduct = (IProductCmpt)newIpsObject(frag, IpsObjectType.PRODUCT_CMPT,
            "AProduct");
        aProduct.setPolicyCmptType(a.getQualifiedName());
        IPolicyCmptType d = (IPolicyCmptType)newIpsObject(frag, IpsObjectType.POLICY_CMPT_TYPE, "D");
        a.newRelation().setTarget(d.getQualifiedName());

        TestDependencyIpsArtefactBuilder builder = new TestDependencyIpsArtefactBuilder();
        ipsProject.getIpsModel().setAvailableArtefactBuilderSets(
            new IIpsArtefactBuilderSet[] { new TestIpsArtefactBuilderSet(
                    new IIpsArtefactBuilder[] { builder }) });
        ipsProject.setCurrentArtefactBuilderSet(TestIpsArtefactBuilderSet.ID);

        // after this incremental build the TestDependencyIpsArtefactBuilder is expected to contain
        // all new IpsObjects in
        // its build list.
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
        List builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(b));
        assertTrue(builtIpsObjects.contains(c));
        assertTrue(builtIpsObjects.contains(d));
        assertTrue(builtIpsObjects.contains(aProduct));

        builder.clear();
        // after this second build no IpsObjects are expected in the build list since nothing has
        // changed since the last build
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
        assertTrue(builder.getBuiltIpsObjects().isEmpty());

        // since the PolicyCmpt b has been deleted after this build the
        // TestDependencyIpsArtefactBuilder is expected to contain
        // all dependent IpsObjects
        d.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
        builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(b));
        assertTrue(builtIpsObjects.contains(c));
        assertTrue(builtIpsObjects.contains(aProduct));

        // recreate d. All dependant are expected to be rebuilt
        d = (IPolicyCmptType)newIpsObject(frag, IpsObjectType.POLICY_CMPT_TYPE, "D");
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
        builtIpsObjects = builder.getBuiltIpsObjects();
        assertTrue(builtIpsObjects.contains(a));
        assertTrue(builtIpsObjects.contains(b));
        assertTrue(builtIpsObjects.contains(c));
        assertTrue(builtIpsObjects.contains(aProduct));

        // delete d and dependants. The IpsBuilder has to make sure to only build the existing
        // IpsObjects though the graph still contains the dependency chain of the deleted IpsOjects
        // during the build cycle
        d.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        a.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        b.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        c.getIpsSrcFile().getCorrespondingResource().delete(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
            new NullProgressMonitor());
    }

    private static class TestRemoveIpsArtefactBuilder implements IIpsArtefactBuilder {

        private boolean buildCalled = false;
        private boolean deleteCalled = false;

        public void beforeBuildProcess(int buildKind) throws CoreException {
        }

        public void afterBuildProcess(int buildKind) throws CoreException {
        }

        public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        }

        public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
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

    private static class TestDependencyIpsArtefactBuilder implements IIpsArtefactBuilder {

        private List builtIpsObjects = new ArrayList();

        public List getBuiltIpsObjects() {
            return builtIpsObjects;
        }

        public void clear() {
            builtIpsObjects.clear();
        }

        public void beforeBuildProcess(int buildKind) throws CoreException {
        }

        public void afterBuildProcess(int buildKind) throws CoreException {
        }

        public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        }

        public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
        }

        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
            builtIpsObjects.add(ipsSrcFile.getIpsObject());
        }

        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
            return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)
                    || ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT);
        }

        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        }
    }

}
