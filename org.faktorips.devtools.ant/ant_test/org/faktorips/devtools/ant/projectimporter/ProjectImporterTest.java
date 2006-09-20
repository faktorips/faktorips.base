package org.faktorips.devtools.ant.projectimporter;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;

public class ProjectImporterTest extends TestCase {

    public void testNullAttribute() {

        ProjectImporter projectimporter = new ProjectImporter();

        projectimporter.setDir(null);

        try {
            projectimporter.execute();
        }
        catch (BuildException e) {
            // is expected
        }
        catch (NullPointerException e) {
            fail();
        }

    }

    public void testNotExistingDirectory() {

        ProjectImporter projectimporter = new ProjectImporter();
        projectimporter.setDir(this.getTempDirPath());

        try {
            projectimporter.execute();
            fail();
        }
        catch (BuildException e) {
            // expected
        }
    }

    public void testIsNoDirectory() {

        ProjectImporter projectimporter = new ProjectImporter();
        projectimporter.setDir(this.getTempDirPath() + "/file.txt");

        File d = new File(this.getTempDirPath());
        File f = new File(this.getTempDirPath() + "/file.txt");
        
        d.deleteOnExit();
        f.deleteOnExit();

        try {
            d.mkdir();
            f.createNewFile();
            projectimporter.execute();
            fail();
        }
        catch (BuildException e) {
            // expected
        }
        catch (IOException e) {
            // expected
        }
    }

    public void testImport() {
        String path = this.getTempDirPath();
        ProjectImporter projectimporter = new ProjectImporter();
        projectimporter.setDir(path);

        // first test import without .project file
        File dir = new File(path);
        dir.mkdir();
        dir.deleteOnExit();

        try {
            projectimporter.execute();
            fail();
        }
        catch (Exception e) {
            // expected
        }

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
