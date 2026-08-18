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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NonIPSMoveOperationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IFolder folderSource;
    private IFolder folderTarget;
    private IFile sourceFile1;
    private IFile sourceFile2;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IProject project = ipsProject.getProject().unwrap();
        folderSource = project.getFolder("source");
        folderTarget = project.getFolder("target");
        folderSource.create(true, true, null);
        folderTarget.create(true, true, null);
        assertThat(folderSource, exists());
        assertThat(folderTarget, exists());
        sourceFile1 = folderSource.getFile("file1");
        sourceFile2 = folderSource.getFile("file2");
        sourceFile1.create(new ByteArrayInputStream("File1".getBytes()), true, null);
        sourceFile2.create(new ByteArrayInputStream("File2".getBytes()), true, null);
        assertThat(sourceFile1, exists());
        assertThat(sourceFile2, exists());
    }

    @Test
    public void testMoveFiles() throws Exception {
        NonIPSMoveOperation operation = new NonIPSMoveOperation(folderTarget.getProject(),
                new Object[] { sourceFile1, sourceFile2 }, folderTarget.getLocation().toOSString());
        operation.run(null);

        assertThat(sourceFile1, not(exists()));
        assertThat(sourceFile2, not(exists()));
        IFile targetFile1 = folderTarget.getFile("file1");
        IFile targetFile2 = folderTarget.getFile("file2");
        assertThat(targetFile1, exists());
        assertThat(targetFile2, exists());

        IIpsPackageFragment targetIpsPackageFragment = ipsProject.getIpsPackageFragmentRoots()[0]
                .getIpsPackageFragment("source");
        ((AFolder)targetIpsPackageFragment.getCorrespondingResource()).create(null);
        AResource aResource = ((AContainer)targetIpsPackageFragment.getEnclosingResource()).findMember("file1");
        assertThat(aResource, is(nullValue()));

        operation = new NonIPSMoveOperation(new Object[] { targetFile1 }, targetIpsPackageFragment);
        operation.run(null);
        aResource = ((AContainer)targetIpsPackageFragment.getEnclosingResource()).findMember("file1");
        assertThat(aResource, is(notNullValue()));
        IResource ipsPackageFile = aResource.unwrap();
        assertThat(ipsPackageFile, exists());

        // test move to project
        operation = new NonIPSMoveOperation(ipsProject.getProject().unwrap(), new Object[] { ipsPackageFile },
                ipsProject.getProject().getLocation().toString());
        operation.run(null);
        assertThat(ipsPackageFile, not(exists()));
        assertThat(ipsProject.getProject().findMember("file1").unwrap(), exists());
    }

    @Test
    public void testMoveLinks() throws Exception {
        NonIPSMoveOperation operation = new NonIPSMoveOperation(folderTarget.getProject(), new Object[] {
                sourceFile1.getLocation().toOSString(), sourceFile2.getLocation().toOSString() },
                folderTarget.getLocation()
                        .toOSString());
        operation.run(null);

        assertThat(sourceFile1, not(exists()));
        assertThat(sourceFile2, not(exists()));
        assertThat(folderTarget.getFile("file1"), exists());
        assertThat(folderTarget.getFile("file2"), exists());
    }

    private static Matcher<IResource> exists() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("exists");
            }

            @Override
            protected boolean matchesSafely(IResource file) {
                return file.exists();
            }
        };
    }
}
