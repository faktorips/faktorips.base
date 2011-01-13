/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveEntryTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IpsArchiveEntry entry;
    private IFile archiveFile;
    private IPath archivePath;

    private QualifiedNameType qntMotorPolicy;
    private QualifiedNameType qntMotorCollision;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        qntMotorPolicy = newPolicyCmptTypeWithoutProductCmptType(archiveProject, "pack1.MotorPolicy")
                .getQualifiedNameType();
        qntMotorCollision = newPolicyCmptTypeWithoutProductCmptType(archiveProject, "pack2.MotorCollision")
                .getQualifiedNameType();
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "pack3.HomePolicy").getQualifiedNameType();

        project = newIpsProject();
        archiveFile = project.getProject().getFile("test.ipsar");
        archivePath = archiveFile.getLocation();

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        entry = (IpsArchiveEntry)path.newArchiveEntry(archivePath);
        project.setIpsObjectPath(path);
    }

    public void testFindIpsSrcFilesStartingWith() throws CoreException {
        IIpsObject motorPol = project.findIpsObject(qntMotorPolicy);
        IIpsObject motorCol = project.findIpsObject(qntMotorCollision);

        IIpsSrcFile motorPolFile = motorPol.getIpsSrcFile();
        IIpsSrcFile motorColFile = motorCol.getIpsSrcFile();

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();

        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        entry.findIpsSrcFilesStartingWithInternal(IpsObjectType.POLICY_CMPT_TYPE, "motor", true, result, visitedEntries);
        assertEquals(2, result.size());
        assertTrue(result.contains(motorPolFile));
        assertTrue(result.contains(motorColFile));

        result = new ArrayList<IIpsSrcFile>();
        visitedEntries.clear();
        entry.findIpsSrcFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "motor", false, result, visitedEntries);
        assertEquals(0, result.size());

        visitedEntries.clear();
        entry.findIpsSrcFilesStartingWith(IpsObjectType.POLICY_CMPT_TYPE, "Motor", false, result, visitedEntries);
        assertEquals(2, result.size());
        assertTrue(result.contains(motorPolFile));
        assertTrue(result.contains(motorColFile));

    }

    public void testFindIpsSrcFiles() throws Exception {
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        Set<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();
        entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE, result, visitedEntries);

        IIpsObject motorPolicy = project.findIpsObject(qntMotorPolicy);
        IIpsObject motorCollision = project.findIpsObject(qntMotorCollision);

        assertTrue(result.contains(motorPolicy.getIpsSrcFile()));
        assertTrue(result.contains(motorCollision.getIpsSrcFile()));
    }

    public void testFindIpsSrcFilesWithPackageFragment() throws Exception {
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE, "pack1", result, new HashSet<IIpsObjectPathEntry>());

        assertEquals(1, result.size());
        IIpsObject motorPolicy = project.findIpsObject(qntMotorPolicy);
        assertTrue(result.contains(motorPolicy.getIpsSrcFile()));

        result = new ArrayList<IIpsSrcFile>();
        entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE, "pack2", result, new HashSet<IIpsObjectPathEntry>());

        assertEquals(1, result.size());
        IIpsObject motorCollision = project.findIpsObject(qntMotorCollision);
        assertTrue(result.contains(motorCollision.getIpsSrcFile()));
    }

    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        entry.initFromXml(XmlUtil.getElement(docEl, 0), project.getProject());
        IFile archiveFile = project.getProject().getFolder("lib").getFile("test.ipsar");
        IPath archivePath = archiveFile.getFullPath();
        assertEquals(archivePath, entry.getIpsArchive().getArchivePath());

        entry.initFromXml(XmlUtil.getElement(docEl, 1), project.getProject());
        assertNull(entry.getArchiveLocation());
    }

    public void testGetIpsArchive() {
        IIpsArchive archive = entry.getIpsArchive();
        assertNotNull(archive);
        assertEquals(archivePath, archive.getArchivePath());
    }

    public void testGetIpsPackageFragementRootName() {
        assertEquals(entry.getIpsPackageFragmentRoot().getName(), entry.getIpsPackageFragmentRootName());
    }

    public void testGetIpsPackageFragementRoot() {
        IIpsPackageFragmentRoot root = entry.getIpsPackageFragmentRoot();
        assertNotNull(root);
        assertEquals(archiveFile.getName(), root.getName());
    }

    public void testToXml() throws CoreException {
        Element el = entry.toXml(newDocument());
        // to create a new entry we need a handle to an existing file path
        IPath dummyArchivePath = project.getIpsProjectPropertiesFile().getLocation();
        IpsArchiveEntry newEntry = (IpsArchiveEntry)project.getIpsObjectPath().newArchiveEntry(dummyArchivePath);
        newEntry.initFromXml(el, project.getProject());
        assertEquals(archivePath, newEntry.getArchiveLocation());
    }

    public void testValidate() throws CoreException {
        MessageList ml = entry.validate();
        assertEquals(0, ml.size());

        IIpsProjectProperties props = project.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        IIpsArchiveEntry[] archiveEntries = path.getArchiveEntries();
        assertEquals(1, archiveEntries.length);
        assertEquals(entry.getArchiveLocation(), archiveEntries[0].getArchiveLocation());

        entry.setArchivePath(project, project.getProject().getFile("NoneExistingFile").getLocation());
        ml = entry.validate();
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_MISSING_ARCHVE));

        entry.setArchivePath(null, null);
        ml = entry.validate();
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_MISSING_ARCHVE));

    }
}
