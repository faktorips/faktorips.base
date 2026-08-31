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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.tools.ant.BuildException;
import org.junit.jupiter.api.Test;

public class ProjectImportTaskTest {

    @Test
    public void testNullAttribute() {

        ProjectImportTask projectimporter = new ProjectImportTask();

        projectimporter.setDir(null);

        // is expected
        assertThrows(BuildException.class, projectimporter::execute);
    }

    @Test
    public void testNotExistingDirectory() {

        ProjectImportTask projectimporter = new ProjectImportTask();
        projectimporter.setDir(getTempDirPath());

        // expected
        assertThrows(BuildException.class, projectimporter::execute);
    }

    @Test
    public void testIsNoDirectory() {

        ProjectImportTask projectimporter = new ProjectImportTask();
        projectimporter.setDir(getTempDirPath() + "/file.txt");

        File d = new File(getTempDirPath());
        File f = new File(getTempDirPath() + "/file.txt");

        d.deleteOnExit();
        f.deleteOnExit();

        // expected
        assertThrows(Exception.class, () -> {
            d.mkdir();
            f.createNewFile();
            projectimporter.execute();
        });
    }

    @Test
    public void testImport() {
        String path = getTempDirPath();
        ProjectImportTask projectimporter = new ProjectImportTask();
        projectimporter.setDir(path);

        // first test import without .project file
        File dir = new File(path);
        dir.mkdir();
        dir.deleteOnExit();

        // expected
        assertThrows(Exception.class, projectimporter::execute);
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

        return fakeDir;
    }
}
