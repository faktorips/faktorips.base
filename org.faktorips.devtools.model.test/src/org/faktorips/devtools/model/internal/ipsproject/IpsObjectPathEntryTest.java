/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathEntryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath(ipsProject);
    }

    @Test
    public void testGetIndex() throws CoreRuntimeException {
        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        assertEquals(1, path.getEntries().length);

        IIpsObjectPathEntry entry0 = path.getEntries()[0];
        assertEquals(0, entry0.getIndex());

        IIpsObjectPathEntry entry1 = path
                .newArchiveEntry(ipsProject.getProject().getFile("someArchive.jar")
                        .getWorkspaceRelativePath());
        assertEquals(0, entry0.getIndex());
        assertEquals(1, entry1.getIndex());
    }

    @Test
    public void testCreateFromXml() {
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);
        IIpsObjectPathEntry entry = IpsObjectPathEntry
                .createFromXml(path, (Element)nl.item(0), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_SRC_FOLDER, entry.getType());
        assertTrue(entry.isReexported());

        entry = IpsObjectPathEntry.createFromXml(path, (Element)nl.item(1), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE, entry.getType());
        assertFalse(entry.isReexported());
    }

    @Test
    public void testCreateFromXml_DefaultReexport() {
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName(IpsObjectPathEntry.XML_ELEMENT);

        IIpsObjectPathEntry entry = IpsObjectPathEntry
                .createFromXml(path, (Element)nl.item(2), ipsProject.getProject());
        assertEquals(IIpsObjectPathEntry.TYPE_PROJECT_REFERENCE, entry.getType());
        assertTrue(entry.isReexported());
    }

    @Test
    public void testIsReexport() throws Exception {
        IIpsObjectPathEntry ipsObjectPathEntry = path.newArchiveEntry(ipsProject.getProject()
                .getFile("someArchive.jar").getWorkspaceRelativePath());
        assertTrue(ipsObjectPathEntry.isReexported());
        ipsObjectPathEntry.setReexported(false);
        assertFalse(ipsObjectPathEntry.isReexported());
    }
}
