/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.bf.ui.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.bf.properties.BusinessFunctionRefControl;
import org.faktorips.devtools.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

/** @deprecated for removal since 21.6 */
@Deprecated
public class BusinessFunctionRefControlTest extends AbstractIpsPluginTest {

    private Shell shell;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        shell = new Shell(Display.getCurrent());
    }

    @Override
    public void tearDownExtension() {
        shell.dispose();
    }

    @Test
    public void testGetIpsSrcFiles() throws Exception {
        UIToolkit toolkit = new UIToolkit(null);
        BusinessFunctionRefControl control = new BusinessFunctionRefControl(shell, toolkit);
        assertEquals(0, control.getIpsSrcFiles().length);

        IIpsProject ipsProject = newIpsProject("TestProject");
        assertEquals(0, control.getIpsSrcFiles().length);

        IBusinessFunction bf1 = (IBusinessFunction)newIpsObject(ipsProject,
                BusinessFunctionIpsObjectType.getInstance(), "bf1");
        IBusinessFunction bf2 = (IBusinessFunction)newIpsObject(ipsProject,
                BusinessFunctionIpsObjectType.getInstance(), "bf2");
        IBusinessFunction bf3 = (IBusinessFunction)newIpsObject(ipsProject,
                BusinessFunctionIpsObjectType.getInstance(), "bf3");
        control.setIpsProjects(ipsProject);
        assertEquals(3, control.getIpsSrcFiles().length);

        control.setCurrentBusinessFunction(bf1);
        assertEquals(2, control.getIpsSrcFiles().length);
        List<IIpsSrcFile> files = Arrays.asList(control.getIpsSrcFiles());
        assertTrue(files.contains(bf2.getIpsSrcFile()));
        assertTrue(files.contains(bf3.getIpsSrcFile()));
        assertFalse(files.contains(bf1.getIpsSrcFile()));
    }

}
