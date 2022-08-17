/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.ui.IMemento;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog.IpsObjectSelectionHistory;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenIpsObjectSelectionDialogTest extends AbstractIpsPluginTest {

    private static final String TESTARCHIVE_JAR = "testarchive.jar";

    private static final String MY_NAME_TYPE = "myNameType.ipsproduct";

    @Mock
    private IMemento memento;

    @Mock
    private IIpsObjectPathEntry archiveEntry;

    private AFile resource;

    private IpsObjectSelectionHistory selectionHistory;

    private IpsProject ipsProject;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)newIpsProject();
        when(archiveEntry.getIpsPackageFragmentRootName()).thenReturn(TESTARCHIVE_JAR);
        resource = ipsProject.getProject().getFile(TESTARCHIVE_JAR);
    }

    @Before
    public void createOpenIpsObjectSelectionDialog() throws Exception {
        selectionHistory = new IpsObjectSelectionHistory();
    }

    @Test
    public void testGetIpsSrcFileFromArchive() throws Exception {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(memento.getString(IpsObjectSelectionHistory.TAG_NAMETYPE)).thenReturn(MY_NAME_TYPE);
        when(archiveEntry.findIpsSrcFile(QualifiedNameType.newQualifedNameType(MY_NAME_TYPE))).thenReturn(
                ipsSrcFile);
        ipsProject.getIpsObjectPathInternal().setEntries(new IIpsObjectPathEntry[] { archiveEntry });

        IIpsSrcFile ipsSrcFileFromArchive = selectionHistory.getIpsSrcFileFromArchive(memento, resource);

        assertEquals(ipsSrcFile, ipsSrcFileFromArchive);
    }

}
