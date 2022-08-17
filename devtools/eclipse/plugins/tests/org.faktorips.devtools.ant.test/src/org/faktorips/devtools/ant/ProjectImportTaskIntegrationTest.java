/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.mapping.PathMapping;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class ProjectImportTaskIntegrationTest extends AbstractIpsPluginTest {

    @Test
    public void testImport_NoCopy_NotAlreadyInWorkspace() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        String projectName = ipsProject.getName();
        Path location = ipsProject.getProject().getLocation();
        Path newProjectPath = getTempDirPath();
        new RecursiveCopy().copyDir(location, newProjectPath);
        ipsProject.getProject().delete(null);

        ProjectImportTask projectimporter = new ProjectImportTask();
        projectimporter.setDir(newProjectPath.toString());
        projectimporter.setCopy(false);

        projectimporter.execute();
        IIpsProject project = IIpsModel.get().getIpsProject(projectName);
        assertTrue(project.exists());
        assertThat(getCanonicalPath(project.getProject().getLocation()),
                is(getCanonicalPath(newProjectPath)));
    }

    @Test
    public void testImport_Copy_NotAlreadyInWorkspace() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        String projectName = ipsProject.getName();
        Path location = ipsProject.getProject().getLocation();
        Path newProjectPath = getTempDirPath();
        new RecursiveCopy().copyDir(location, newProjectPath);
        ipsProject.getProject().delete(null);

        ProjectImportTask projectimporter = new ProjectImportTask();
        projectimporter.setDir(newProjectPath.toString());
        projectimporter.setCopy(true);

        projectimporter.execute();
        IIpsProject project = IIpsModel.get().getIpsProject(projectName);
        assertTrue(project.exists());
        IPath projectParentPath = PathMapping.toEclipsePath(ipsProject.getProject().getLocation())
                .removeLastSegments(1);
        assertThat(projectParentPath, is(ResourcesPlugin.getWorkspace().getRoot().getLocation()));
    }

    private Path getTempDirPath() {

        File dir = null;
        Path path = null;

        while (dir == null || dir.exists()) {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss_SS";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getDefault());

            path = Path.of(System.getProperty("java.io.tmpdir"), sdf.format(cal.getTime()));
            dir = path.toFile();
        }

        return path;
    }

    private String getCanonicalPath(Path path) throws IOException {
        return path.toFile().getCanonicalPath();
    }
}
