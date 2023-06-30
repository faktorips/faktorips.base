/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.model.internal.ipsproject.IpsContainerEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class Migration_23_12_0Test extends AbstractIpsPluginTest {

    private IIpsProject project;

    @Before
    public void setup() {
        project = newIpsProject("TooOldIpsProject");

        AFolder derived = project.getProject().getFolder(Path.of("derived"));
        derived.create(null);

        AFolder otherDerived = project.getProject().getFolder(Path.of("derived2"));
        otherDerived.create(null);

        createTestFiles(derived);
        createTestFiles(otherDerived);

        IpsObjectPath ipsObjectPath = new IpsObjectPath(project);
        ipsObjectPath.setOutputFolderForDerivedSources(derived);
        ipsObjectPath.setOutputDefinedPerSrcFolder(true);
        IpsSrcFolderEntry srcFolderEntry = new IpsSrcFolderEntry(ipsObjectPath, otherDerived);
        srcFolderEntry.setSpecificOutputFolderForDerivedJavaFiles(otherDerived);

        IpsContainerEntry containerEntry = new IpsContainerEntry(ipsObjectPath);

        ipsObjectPath.setEntries(List.of(srcFolderEntry, containerEntry).toArray(new IpsObjectPathEntry[0]));
        project.setIpsObjectPath(ipsObjectPath);
    }

    @Test
    public void testDeleteDerivedFolders() throws Exception {
        Migration_23_12_0 migration = new Migration_23_12_0(project, "");
        migration.migrate(new NullProgressMonitor());

        assertThat(project.getProject().findMember("derived/package/IpsFile.xml"), is(nullValue()));
        assertThat(project.getProject().findMember("derived2/package/IpsFile.xml"), is(nullValue()));
        assertThat(project.getProject().findMember("derived/package/OtherIpsFile.XML"), is(nullValue()));
        assertThat(project.getProject().findMember("derived2/package/OtherIpsFile.XML"), is(nullValue()));

        assertThat(project.getProject().findMember("derived/package/SomeFile.foo"), isA(AFile.class));
        assertThat(project.getProject().findMember("derived2/package/SomeFile.foo"), isA(AFile.class));
        assertThat(project.getProject().findMember("derived/package/SomeFile"), isA(AFile.class));
        assertThat(project.getProject().findMember("derived2/package/SomeFile"), isA(AFile.class));
        assertThat(project.getProject().findMember("derived/.gitignore"), isA(AFile.class));
        assertThat(project.getProject().findMember("derived2/.gitignore"), isA(AFile.class));
        assertThat(project.getProject().findMember("derived/Readme.txt"), isA(AFile.class));
        assertThat(project.getProject().findMember("derived2/Readme.txt"), isA(AFile.class));

        assertThat(project.getProject().findMember("derived/aFolder-xml"), isA(AFolder.class));
        assertThat(project.getProject().findMember("derived2/aFolder-xml"), isA(AFolder.class));
    }

    private AFile createFile(AFolder parent, String name) {
        AFile file = parent.getFile(Path.of(name));
        file.create(new ByteArrayInputStream("".getBytes()), null);
        return file;
    }

    private AFolder createFolder(AFolder parentFolder, String name) {
        AFolder packageFolder = parentFolder.getFolder(name);
        packageFolder.create(null);
        return packageFolder;
    }

    private void createTestFiles(AFolder parent) {
        AFolder packageFolder = createFolder(parent, "package");
        createFile(parent, ".gitignore");
        createFile(parent, "Readme.txt");
        createFile(packageFolder, "IpsFile.xml");
        createFile(packageFolder, "OtherIpsFile.XML");
        createFile(packageFolder, "SomeFile.foo");
        createFile(packageFolder, "SomeFile");
        createFolder(parent, "aFolder-xml");
    }

}
