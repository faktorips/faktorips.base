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
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class ProjectImportTaskIntegrationTest extends AbstractIpsPluginTest {

    @Test
    public void testImport_NoCopy_NotAlreadyInWorkspace() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        String projectName = ipsProject.getName();
        IPath location = ipsProject.getProject().getLocation();
        String newProjectPath = getTempDirPath();
        new RecursiveCopy().copyDir(location.toPortableString(), newProjectPath);
        ipsProject.getProject().delete(null);

        ProjectImportTask projectimporter = new ProjectImportTask();
        projectimporter.setDir(newProjectPath);
        projectimporter.setCopy(false);

        projectimporter.execute();
        IIpsProject project = IIpsModel.get().getIpsProject(projectName);
        assertTrue(project.exists());
        assertThat(getCanonicalPath(project.getProject().getLocation()), is(getCanonicalPath(newProjectPath)));
    }

    @Test
    public void testImport_Copy_NotAlreadyInWorkspace() throws Exception {
        IIpsProject ipsProject = newIpsProject();
        String projectName = ipsProject.getName();
        IPath location = ipsProject.getProject().getLocation();
        String newProjectPath = getTempDirPath();
        new RecursiveCopy().copyDir(location.toPortableString(), newProjectPath);
        ipsProject.getProject().delete(null);

        ProjectImportTask projectimporter = new ProjectImportTask();
        projectimporter.setDir(newProjectPath);
        projectimporter.setCopy(true);

        projectimporter.execute();
        IIpsProject project = IIpsModel.get().getIpsProject(projectName);
        assertTrue(project.exists());
        IPath projectParentPath = project.getProject().getLocation().removeLastSegments(1);
        assertThat(projectParentPath, is(ResourcesPlugin.getWorkspace().getRoot().getLocation()));
    }

    private String getTempDirPath() {

        File dir = null;
        String fakeDir = "";

        while (dir == null || dir.exists()) {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss_SS";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getDefault());

            fakeDir = System.getProperty("java.io.tmpdir") + "/" + sdf.format(cal.getTime()) + "/";
            dir = new File(fakeDir);
        }

        return new Path(fakeDir).toPortableString();
    }

    private String getCanonicalPath(IPath path) throws IOException {
        return new File(path.toPortableString()).getCanonicalPath();
    }

    private String getCanonicalPath(String path) throws IOException {
        return new File(path).getCanonicalPath();
    }
}
