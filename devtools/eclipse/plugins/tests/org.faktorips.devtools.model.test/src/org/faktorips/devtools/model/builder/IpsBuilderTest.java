/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.ABuilder;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceTreeTraversalDepth;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.eclipse.internal.EclipseImplementation;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsArchiveEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * A common base class for builder tests.
 *
 * @author Jan Ortmann
 */
@SuppressWarnings("restriction")
// IMPORTANT: in the test methods the test builder set has to be set to the model after the
// properties have been set otherwise the builder set will be removed since the setProperties method
// causes a clean to the builder set map of the ips model. Hence when the model is requested
// for the builder set it will look registered builder sets at the extension point and
// won't find the test builder set
@Category(EclipseImplementation.class)
public class IpsBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsBuilder builder;

    public IpsBuilderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        builder = new IpsBuilder(new ABuilder() {

            @Override
            public AProject getProject() {
                return ipsProject.getProject();
            }

            @Override
            public AResourceDelta getDelta() {
                return null;
            }
        });
    }

    class AssertThatFullBuildIsTriggeredBuilder extends AbstractArtefactBuilder {

        boolean called = false;
        ABuildKind buildKind = null;

        public AssertThatFullBuildIsTriggeredBuilder() {
            super(new TestIpsArtefactBuilderSet());
        }

        @Override
        public void beforeBuildProcess(IIpsProject project, ABuildKind buildKind) {
            called = true;
            this.buildKind = buildKind;
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) {
            // nothing to do
        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) {
            // nothing to do
        }

        @Override
        public String getName() {
            return "AssertThatFullBuildIsTriggeredBuilder";
        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
            return false;
        }

        @Override
        public boolean isBuildingInternalArtifacts() {
            return false;
        }

    }

    @Test
    public void testCleanBuild() {
        newPolicyCmptType(ipsProject, "mycompany.motor.MotorPolicy");
        AFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        archiveFile.getWorkspace().build(ABuildKind.FULL, null);

        File file = archiveFile.getLocation().toFile();
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(ipsProject.getIpsPackageFragmentRoots(),
                file);
        operation.setInclJavaBinaries(true);
        operation.setInclJavaSources(true);
        operation.run(null);

        IIpsProject project2 = newIpsProject("TestProject2");
        AFile archiveFile2 = project2.getProject().getFile(archiveFile.getLocation());
        Path archivePath2 = archiveFile2.getLocation();

        IpsArchiveEntry archiveEntry = new IpsArchiveEntry((IpsObjectPath)project2.getIpsObjectPath());
        archiveEntry.initStorage(archivePath2);
        IIpsObjectPathEntry[] entries = project2.getIpsObjectPath().getEntries();
        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        newEntries[newEntries.length - 1] = archiveEntry;
        IIpsObjectPath newPath = project2.getIpsObjectPath();
        newPath.setEntries(newEntries);
        project2.setIpsObjectPath(newPath);

        project2.getProject().build(ABuildKind.CLEAN, null);
    }

    @Test
    public void testArtefactBuilderSetIfIpsProjectIsSet() {
        ipsProject.getProject().build(ABuildKind.FULL, null);
        IIpsArtefactBuilderSet builderSet = ((IpsModel)ipsProject.getIpsModel()).getIpsArtefactBuilderSet(ipsProject,
                false);
        assertEquals(ipsProject, builderSet.getIpsProject());
    }

    @Test
    public void testIpsStatusToMarker_PreProcessWithException() {
        IpsStatus s = new IpsStatus(
                "ProductCmptClassBuilderBuilder: Error during: BeforeBuildProcessCmd[kind=AUTO]: null",
                new NullPointerException());
        builder.createMarkersFromBuildStatus(s, IpsBuilder.PROBLEM_MARKER);

        Set<AMarker> markers = ipsProject.getProject().findMarkers(IpsBuilder.PROBLEM_MARKER, true,
                AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS);

        assertThat(markers, is(not(empty())));
        AMarker marker = markers.iterator().next();
        assertThat((String)marker.getAttribute(IMarker.MESSAGE), containsString("java.lang.NullPointerException"));
        assertThat((String)marker.getAttribute(IMarker.MESSAGE), containsString("project"));
        assertThat(marker.getAttribute(IMarker.SEVERITY), is(IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testIpsStatusToMarker_PostProcessWithoutException() {
        IpsStatus s = new IpsStatus(
                "ProductCmptClassBuilderBuilder: Error during: AfterBuildProcessCmd[kind=FULL]: some error",
                null);
        builder.createMarkersFromBuildStatus(s, IpsBuilder.PROBLEM_MARKER);

        Set<AMarker> markers = ipsProject.getProject().findMarkers(IpsBuilder.PROBLEM_MARKER, true,
                AResourceTreeTraversalDepth.RESOURCE_AND_DIRECT_MEMBERS);

        assertThat(markers, is(not(empty())));
        AMarker marker = markers.iterator().next();
        assertThat((String)marker.getAttribute(IMarker.MESSAGE),
                is("ProductCmptClassBuilderBuilder: Error during: AfterBuildProcessCmd[kind=FULL]: some error"));
        assertThat(marker.getAttribute(IMarker.SEVERITY), is(IMarker.SEVERITY_ERROR));
    }

    @Test
    public void testIpsStatusToMarker_FileBuildWithException() {
        PolicyCmptType p = newPolicyCmptType(ipsProject, "mycompany.motor.MotorPolicy");

        IpsStatus s = new IpsStatus(
                "PolicyCmptClassBuilder: Error during: Build file "
                        + p.getIpsSrcFile().getCorrespondingFile().getWorkspaceRelativePath().toString().substring(1)
                        + ".",
                new IllegalArgumentException("wrong arg"));
        builder.createMarkersFromBuildStatus(s, IpsBuilder.PROBLEM_MARKER);

        Set<AMarker> markers = ipsProject.getProject().findMarkers(IpsBuilder.PROBLEM_MARKER, true,
                AResourceTreeTraversalDepth.INFINITE);

        assertThat(markers, is(not(empty())));
        AMarker marker = markers.iterator().next();
        assertThat((String)marker.getAttribute(IMarker.MESSAGE), containsString("PolicyCmptClassBuilder"));
        assertThat((String)marker.getAttribute(IMarker.MESSAGE), containsString("wrong arg"));
        assertThat((String)marker.getAttribute(IMarker.MESSAGE), containsString("this file"));
        assertThat(marker.getAttribute(IMarker.SEVERITY), is(IMarker.SEVERITY_ERROR));
        IResource markerResource = ((Marker)marker.unwrap()).getResource();
        IResource projectResource = p.getIpsSrcFile().getEnclosingResource().unwrap();
        assertThat(markerResource, is(projectResource));
    }

    @Test
    public void testIpsStatusToMarker_FileDeleteWithOutException() {
        PolicyCmptType p = newPolicyCmptType(ipsProject, "mycompany.motor.MotorPolicy");

        String msg = "PolicyCmptClassBuilder: Error during: Delete file "
                + p.getIpsSrcFile().getCorrespondingFile().getWorkspaceRelativePath().toString().substring(1)
                + ".";
        IpsStatus s = new IpsStatus(msg, null);
        builder.createMarkersFromBuildStatus(s, IpsBuilder.PROBLEM_MARKER);

        Set<AMarker> markers = ipsProject.getProject().findMarkers(IpsBuilder.PROBLEM_MARKER, true,
                AResourceTreeTraversalDepth.INFINITE);

        assertThat(markers, is(not(empty())));
        AMarker marker = markers.iterator().next();
        assertThat((String)marker.getAttribute(IMarker.MESSAGE), is(msg));
        assertThat(marker.getAttribute(IMarker.SEVERITY), is(IMarker.SEVERITY_ERROR));
        IResource markerResource = ((Marker)marker.unwrap()).getResource();
        IResource projectResource = p.getIpsSrcFile().getEnclosingResource().unwrap();
        assertThat(markerResource, is(projectResource));
    }

    @Test
    public void testIpsStatusToMarker_FileBuildWithExceptionNotExistingFile() {
        newPolicyCmptType(ipsProject, "mycompany.motor.MotorPolicy");

        IpsStatus s = new IpsStatus(
                "PolicyCmptClassBuilder: Error during: Build file /I/am/not/here.",
                new IllegalArgumentException("wrong arg"));
        builder.createMarkersFromBuildStatus(s, IpsBuilder.PROBLEM_MARKER);

        Set<AMarker> markers = ipsProject.getProject().findMarkers(IpsBuilder.PROBLEM_MARKER, true,
                AResourceTreeTraversalDepth.INFINITE);

        assertThat(markers, is(empty()));
    }
}
