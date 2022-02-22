/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectEditorSettingsTest extends AbstractIpsPluginTest {

    private IpsObjectEditorSettings settings;
    private IIpsProject project;
    private IIpsSrcFile srcFile;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        settings = new IpsObjectEditorSettings();
        project = newIpsProject();
        srcFile = newPolicyCmptType(project, "motor.Policy").getIpsSrcFile();
    }

    @Test
    public void testPut() {
        settings.put(srcFile, "KEY1", "value1");
        assertEquals("value1", settings.get(srcFile, "KEY1"));

        settings.put(srcFile, "KEY1", "value1a");
        assertEquals("value1a", settings.get(srcFile, "KEY1"));

        settings.put(srcFile, "KEY2", "value2");
        assertEquals("value1a", settings.get(srcFile, "KEY1"));
        assertEquals("value2", settings.get(srcFile, "KEY2"));

        IIpsSrcFile srcFile2 = newPolicyCmptType(project, "home.Policy").getIpsSrcFile();
        settings.put(srcFile2, "KEY1", "value3");
        assertEquals("value3", settings.get(srcFile2, "KEY1"));
        assertEquals("value1a", settings.get(srcFile, "KEY1"));
        assertEquals("value2", settings.get(srcFile, "KEY2"));

        settings.put(srcFile, "KEY1", null);
        assertNull(settings.get(srcFile, "KEY1"));

        try {
            settings.put(srcFile, null, "");
            fail();
        } catch (NullPointerException e) {
        }

        try {
            settings.put(srcFile, "KEY CONTAINGING A BLANK", "");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGet() {
        assertNull(settings.get(srcFile, "KEY1"));
        settings.put(srcFile, "KEY1", "value1");
        assertEquals("value1", settings.get(srcFile, "KEY1"));
    }

    @Test
    public void testGetBoolean() {
        assertFalse(settings.getBoolean(srcFile, "KEY1"));
        settings.put(srcFile, "KEY1", true);
        assertTrue(settings.getBoolean(srcFile, "KEY1"));
    }

    @Test
    public void testRemoveIIpsSrcFile() {
        settings.remove(srcFile);

        settings.put(srcFile, "KEY1", "value1");
        settings.put(srcFile, "KEY2", "value2");
        settings.remove(srcFile);

        assertNull(settings.get(srcFile, "KEY1"));
        assertNull(settings.get(srcFile, "KEY2"));
    }

    @Test
    public void testRemoveIIpsSrcFileString() {
        settings.remove(srcFile, "KEY1");

        settings.put(srcFile, "KEY1", "value1");
        settings.put(srcFile, "KEY2", "value2");
        settings.remove(srcFile, "KEY2");

        assertEquals("value1", settings.get(srcFile, "KEY1"));
        assertNull(settings.get(srcFile, "KEY2"));
    }

    @Test
    public void testLoadSave() throws IpsException, IOException {
        settings.put(srcFile, "KEY1", "value1");
        settings.put(srcFile, "KEY2", "value2");

        IIpsSrcFile srcFile2 = newPolicyCmptType(project, "home.Policy").getIpsSrcFile();
        settings.put(srcFile2, "KEY1", "value1");
        settings.remove(srcFile2);
        // not key/values for this file

        IIpsSrcFile srcFile3 = newPolicyCmptType(project, "Product 2005").getIpsSrcFile();
        settings.put(srcFile3, "KEY1", "value3");
        settings.put(srcFile3, "KEY2", "value4");

        IIpsSrcFile srcFile4 = newPolicyCmptType(project, "Product 2006").getIpsSrcFile();
        settings.put(srcFile4, "KEY1", "value5");
        settings.put(srcFile4, "KEY2", "value6");

        File file = File.createTempFile("IpsEditorSettingsTest", ".txt");
        file.deleteOnExit();
        settings.save(file);

        settings = new IpsObjectEditorSettings();
        settings.load(file);

        assertEquals("value1", settings.get(srcFile, "KEY1"));
        assertEquals("value2", settings.get(srcFile, "KEY2"));
        assertNull(settings.get(srcFile2, "KEY1"));
        assertEquals("value3", settings.get(srcFile3, "KEY1"));
        assertEquals("value4", settings.get(srcFile3, "KEY2"));
        assertEquals("value5", settings.get(srcFile4, "KEY1"));
        assertEquals("value6", settings.get(srcFile4, "KEY2"));
    }

}
