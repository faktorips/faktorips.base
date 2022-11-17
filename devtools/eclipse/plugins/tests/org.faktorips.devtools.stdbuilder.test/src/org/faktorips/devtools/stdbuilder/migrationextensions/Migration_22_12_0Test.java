/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.migrationextensions;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.core.internal.migrationextensions.Migration_22_12_0;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.eclipse.util.EclipseProjectUtil;
import org.faktorips.devtools.model.internal.pctype.CamelCaseToUpperUnderscoreColumnNamingStrategy;
import org.faktorips.devtools.model.internal.pctype.CamelCaseToUpperUnderscoreTableNamingStrategy;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class Migration_22_12_0Test extends AbstractIpsPluginTest {

    @Test
    public void testMigrate() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_22_12_0TestF");
        IProject project = ipsProject.getProject().unwrap();
        project.getFile(".ipsproject").delete(true, null);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project");
        copy(project, ".classpath");
        copy(project, ".ipsproject");
        ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));
        assertFalse(EclipseProjectUtil.hasIpsNature(project));
        Migration_22_12_0 migration_22_12_0 = new Migration_22_12_0(ipsProject, "irrelevant");

        try {
            MessageList messageList = migration_22_12_0.migrate(new NullProgressMonitor());

            assertTrue(messageList.isEmpty());
            assertTrue(EclipseProjectUtil.hasIpsNature(project));
            IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
            assertThat(ipsProjectProperties.getProductCmptNamingStrategy(),
                    is(instanceOf(DateBasedProductCmptNamingStrategy.class)));
            assertThat(ipsProjectProperties.getPersistenceOptions().getTableColumnNamingStrategy(),
                    is(instanceOf(CamelCaseToUpperUnderscoreColumnNamingStrategy.class)));
            assertThat(ipsProjectProperties.getPersistenceOptions().getTableNamingStrategy(),
                    is(instanceOf(CamelCaseToUpperUnderscoreTableNamingStrategy.class)));
        } catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            assertEquals("", Arrays.stream(npe.getStackTrace()).map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    }

    @Test
    public void testMigrate_RemoveDuplicates() throws Exception {
        IIpsProject ipsProject = newIpsProject("Migration_22_12_0TestF");
        IProject project = ipsProject.getProject().unwrap();
        project.getFile(".ipsproject").delete(true, null);
        project.getFolder("model").create(true, true, null);
        copy(project, ".project.dup", ".project");
        copy(project, ".classpath.dup", ".classpath");
        copy(project, ".ipsproject");
        ipsProject = IIpsModel.get().getIpsProject(wrap(project).as(AProject.class));
        assertTrue(EclipseProjectUtil.hasIpsNature(project));
        Migration_22_12_0 migration_22_12_0 = new Migration_22_12_0(ipsProject, "irrelevant");

        try {
            MessageList messageList = migration_22_12_0.migrate(new NullProgressMonitor());

            assertTrue(messageList.isEmpty());
            assertTrue(EclipseProjectUtil.hasIpsNature(project));
            String projectFile = Files.lines(project.getFile(".project").getRawLocation().toFile().toPath())
                    .collect(Collectors.joining("\n"));
            assertThat(projectFile, containsString("org.faktorips.devtools.model.eclipse.ipsbuilder"));
            assertThat(projectFile, not(containsString("org.faktorips.devtools.model.ipsbuilder")));
            assertThat(projectFile, not(containsString("org.faktorips.devtools.core.ipsbuilder")));
            String classpathFile = Files.lines(project.getFile(".classpath").getRawLocation().toFile().toPath())
                    .collect(Collectors.joining("\n"));
            assertThat(classpathFile, containsString("org.faktorips.devtools.model.eclipse.ipsClasspathContainer"));
            assertThat(classpathFile, not(containsString("org.faktorips.devtools.model.ipsClasspathContainer")));
            assertThat(classpathFile, not(containsString("org.faktorips.devtools.core.ipsClasspathContainer")));
        } catch (NullPointerException npe) {
            npe.printStackTrace(System.err);
            assertEquals("", Arrays.stream(npe.getStackTrace()).map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
        }
    }

    private void copy(IProject platformProject, String fileName) throws CoreException {
        copy(platformProject, fileName, fileName);
    }

    private void copy(IProject platformProject, String inputFileName, String outputFileName) throws CoreException {
        IFile file = platformProject.getFile(outputFileName);
        InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + inputFileName);
        if (file.exists()) {
            file.setContents(inputStream, true, true, null);
        } else {
            file.create(inputStream, true, null);
        }
    }

}
