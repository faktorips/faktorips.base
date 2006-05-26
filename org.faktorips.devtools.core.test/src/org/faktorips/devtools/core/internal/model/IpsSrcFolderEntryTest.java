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

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFolderEntryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsObjectPath path;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = new IpsObjectPath();
    }
    
    public void testGetOutputFolderForGeneratedJavaFiles() {
        IFolder src = ipsProject.getProject().getFolder("src");
        IFolder out1 = ipsProject.getProject().getFolder("out1");
        IFolder out2 = ipsProject.getProject().getFolder("out2");
        path.setOutputFolderForGeneratedJavaFiles(out1);
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setSpecificOutputFolderForGeneratedJavaFiles(out2);

        path.setOutputDefinedPerSrcFolder(false);
        assertEquals(out1, entry.getOutputFolderForGeneratedJavaFiles());

        path.setOutputDefinedPerSrcFolder(true);
        assertEquals(out2, entry.getOutputFolderForGeneratedJavaFiles());
    }

    public void testGetBasePackageNameForGeneratedJavaClasses() {
        IFolder src = ipsProject.getProject().getFolder("src");
        path.setBasePackageNameForGeneratedJavaClasses("pack1");
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setSpecificBasePackageNameForGeneratedJavaClasses("pack2");

        path.setOutputDefinedPerSrcFolder(false);
        assertEquals("pack1", entry.getBasePackageNameForGeneratedJavaClasses());

        path.setOutputDefinedPerSrcFolder(true);
        assertEquals("pack2", entry.getBasePackageNameForGeneratedJavaClasses());
    }
    
    public void testGetOutputFolderForExtensionJavaFiles() {
        IFolder src = ipsProject.getProject().getFolder("src");
        IFolder out1 = ipsProject.getProject().getFolder("out1");
        IFolder out2 = ipsProject.getProject().getFolder("out2");
        path.setOutputFolderForExtensionJavaFiles(out1);
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setSpecificOutputFolderForExtensionJavaFiles(out2);

        path.setOutputDefinedPerSrcFolder(false);
        assertEquals(out1, entry.getOutputFolderForExtensionJavaFiles());

        path.setOutputDefinedPerSrcFolder(true);
        assertEquals(out2, entry.getOutputFolderForExtensionJavaFiles());
    }

    public void testGetBasePackageNameForExtensionJavaClasses() {
        IFolder src = ipsProject.getProject().getFolder("src");
        path.setBasePackageNameForExtensionJavaClasses("pack1");
        IIpsSrcFolderEntry entry = path.newSourceFolderEntry(src);
        entry.setSpecificBasePackageNameForExtensionJavaClasses("pack2");

        path.setOutputDefinedPerSrcFolder(false);
        assertEquals("pack1", entry.getBasePackageNameForExtensionJavaClasses());

        path.setOutputDefinedPerSrcFolder(true);
        assertEquals("pack2", entry.getBasePackageNameForExtensionJavaClasses());
    }
    
    public void testInitFromXml() {
        IProject project = ipsProject.getProject();
        IpsSrcFolderEntry entry = new IpsSrcFolderEntry(path);
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName("Entry");
        
        entry.initFromXml((Element)nl.item(0), ipsProject.getProject());
        assertEquals(project.getFolder("ipssrc"), entry.getSourceFolder());
        assertEquals(project.getFolder("generated"), entry.getSpecificOutputFolderForGeneratedJavaFiles());
        assertEquals("org.sample.generated", entry.getSpecificBasePackageNameForGeneratedJavaClasses());
        assertEquals(project.getFolder("extensions"), entry.getSpecificOutputFolderForExtensionJavaFiles());
        assertEquals("org.sample.extensions", entry.getSpecificBasePackageNameForExtensionJavaClasses());

        entry.initFromXml((Element)nl.item(1), ipsProject.getProject());
        assertNull(entry.getSpecificOutputFolderForGeneratedJavaFiles());
        assertEquals("", entry.getSpecificBasePackageNameForGeneratedJavaClasses());
        assertEquals("", entry.getSpecificBasePackageNameForExtensionJavaClasses());
    }

    public void testToXml() {
        IProject project = ipsProject.getProject();
        IpsSrcFolderEntry entry = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        entry.setSpecificOutputFolderForGeneratedJavaFiles(project.getFolder("javasrc").getFolder("modelclasses"));
        entry.setSpecificBasePackageNameForGeneratedJavaClasses("org.faktorips.sample.model");
        Element element = entry.toXml(newDocument());
        entry = new IpsSrcFolderEntry(path);
        entry.initFromXml(element, project);
        assertEquals(project.getFolder("ipssrc").getFolder("modelclasses"), entry.getSourceFolder());
        assertEquals(project.getFolder("javasrc").getFolder("modelclasses"), entry.getSpecificOutputFolderForGeneratedJavaFiles());
        assertEquals("org.faktorips.sample.model", entry.getSpecificBasePackageNameForGeneratedJavaClasses());
        
        // null, default values for new entries
        entry = new IpsSrcFolderEntry(path, project.getFolder("ipssrc").getFolder("modelclasses"));
        element = entry.toXml(newDocument());
        entry = new IpsSrcFolderEntry(path);
        entry.initFromXml(element, project);
        assertNull(entry.getSpecificOutputFolderForGeneratedJavaFiles());
        assertNull(entry.getSpecificOutputFolderForExtensionJavaFiles());
        assertEquals("", entry.getSpecificBasePackageNameForGeneratedJavaClasses());
        assertEquals("", entry.getSpecificBasePackageNameForExtensionJavaClasses());
        
        
    }

}
