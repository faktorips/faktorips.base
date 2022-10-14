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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.eclipse.internal.EclipseImplementation;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsArchiveEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
@Category(EclipseImplementation.class)
public class IpsBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    public IpsBuilderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
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
}
