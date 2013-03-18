/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IMemento;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog.IpsObjectSelectionHistory;
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

    private IResource resource;

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
