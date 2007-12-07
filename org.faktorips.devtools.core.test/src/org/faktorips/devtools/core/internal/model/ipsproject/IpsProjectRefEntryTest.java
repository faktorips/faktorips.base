/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsProjectRefEntryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath();
    }

    public void testFindIpsObjectsInternal() throws Exception{
        
        IpsProject refProject = (IpsProject)newIpsProject("RefProject");
        IPolicyCmptType a = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.A");
        IPolicyCmptType b = newPolicyCmptTypeWithoutProductCmptType(refProject, "a.B");

        IPolicyCmptType c = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "b.C");

        
        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
        IpsProjectRefEntry entry = (IpsProjectRefEntry)path.newIpsProjectRefEntry(refProject);
        
        ArrayList result = new ArrayList();
        Set visitedEntries = new HashSet();
        entry.findIpsObjects(refProject, result, visitedEntries);
        
        assertTrue(result.contains(a));
        assertTrue(result.contains(b));
        assertFalse(result.contains(c));
    }
    
    public void testInitFromXml() {
        Document doc = getTestDocument();
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path);
        entry.initFromXml(doc.getDocumentElement(), ipsProject.getProject());
        assertEquals(IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject"), entry.getReferencedIpsProject());
    }

    public void testToXml() {
        IIpsProject refProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("RefProject");
        IpsProjectRefEntry entry = new IpsProjectRefEntry(path, refProject);
        Element element = entry.toXml(newDocument());
        
        entry = new IpsProjectRefEntry(path);
        entry.initFromXml(element, ipsProject.getProject());
        assertEquals(refProject, entry.getReferencedIpsProject());
    }

    public void testValidate() throws CoreException{
        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        IIpsProject refProject = this.newIpsProject("TestProject2");
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setProperties(props);
        
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.getNoOfMessages());
        
        // validate missing project reference
        refProject = IpsPlugin.getDefault().getIpsModel().getIpsProject("none");
        path.newIpsProjectRefEntry(refProject);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsProjectRefEntry.MSGCODE_MISSING_PROJECT));
        
        // validate empty project name
        path.removeProjectRefEntry(refProject);
        path.newIpsProjectRefEntry(null);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(IIpsProjectRefEntry.MSGCODE_PROJECT_NOT_SPECIFIED));
    }
}
