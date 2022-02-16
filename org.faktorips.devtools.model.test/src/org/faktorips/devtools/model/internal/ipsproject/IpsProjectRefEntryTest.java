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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsProjectRefEntryTest extends AbstractIpsPluginTest {

    private static final String MY_RESOURCE_PATH = "myResourcePath";

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
    public void testFindIpsSrcFiles() throws Exception {
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.B");

        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "b.C");

        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);
        ipsProject.setIpsObjectPath(path);

        List<IIpsSrcFile> result = entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        assertTrue(result.isEmpty());

        result = refProject.findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        assertTrue(result.contains(a.getIpsSrcFile()));
        assertTrue(result.contains(b.getIpsSrcFile()));
        assertFalse(result.contains(c.getIpsSrcFile()));
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path);
        entry.initFromXml(doc.getDocumentElement(), ipsProject.getProject());
        assertEquals(IIpsModel.get().getIpsProject("RefProject"), entry.getReferencedIpsProject());
        assertFalse(entry.isUseNWDITrackPrefix());
        assertFalse(entry.isReexported());
    }

    @Test
    public void testToXml() {
        IIpsProject refProject = IIpsModel.get().getIpsProject("RefProject");
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path, refProject);
        assertTrue(entry.isReexported());
        entry.setReexported(false);
        Element element = entry.toXml(newDocument());

        entry = new IpsProjectRefEntry(path);
        entry.initFromXml(element, ipsProject.getProject());
        assertEquals(refProject, entry.getReferencedIpsProject());
        assertFalse(entry.isUseNWDITrackPrefix());
        assertFalse(entry.isReexported());
    }

    @Test
    public void testValidate() {
        IIpsProjectProperties props = ipsProject.getProperties();
        path = (IpsObjectPath)props.getIpsObjectPath();
        IIpsProject refProject = this.newIpsProject("TestProject2");
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setProperties(props);
        IpsProjectTest.updateSrcFolderEntryQalifiers(refProject, "2");

        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.size());

        // validate missing project reference
        refProject = IIpsModel.get().getIpsProject("none");
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_MISSING_PROJECT));

        // validate empty project name
        path.removeProjectRefEntry(refProject);
        path.newIpsProjectRefEntry(null);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.size());
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_PROJECT_NOT_SPECIFIED));
    }

    @Test
    public void testContainsResource_false() throws Exception {
        IpsProjectRefEntry projectRefEntry = new IpsProjectRefEntry(path, ipsProject);

        assertFalse(projectRefEntry.containsResource(MY_RESOURCE_PATH));
    }

    @Test
    public void testFindIpsScrFile() {
        IpsProjectRefEntry projectRefEntry = new IpsProjectRefEntry(path, ipsProject);
        assertNull(projectRefEntry.findIpsSrcFile(new QualifiedNameType("x.X", IpsObjectType.POLICY_CMPT_TYPE)));
    }
}
