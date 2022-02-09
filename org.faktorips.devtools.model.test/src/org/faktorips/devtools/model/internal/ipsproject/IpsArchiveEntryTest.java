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

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveEntryTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IpsArchiveEntry entry;
    private AFile archiveFile;
    private Path archivePath;

    private QualifiedNameType qntMotorPolicy;
    private QualifiedNameType qntMotorCollision;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        PolicyCmptType motorPolicyCmptType = newPolicyAndProductCmptType(archiveProject, "pack1.MotorPolicy",
                "pack1.MotorProduct");
        qntMotorPolicy = motorPolicyCmptType.getQualifiedNameType();
        qntMotorCollision = newPolicyCmptTypeWithoutProductCmptType(archiveProject, "pack2.MotorCollision")
                .getQualifiedNameType();
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "pack3.HomePolicy").getQualifiedNameType();
        AFile iconFile = ((AFolder)archiveProject.getIpsPackageFragmentRoots()[0].getCorrespondingResource())
                .getFile("myTest.gif");
        iconFile.create(new ByteArrayInputStream("imageContent".getBytes()), new NullProgressMonitor());
        IProductCmptType prodType = motorPolicyCmptType.findProductCmptType(archiveProject);
        prodType.setInstancesIcon("myTest.gif");

        project = newIpsProject("TestProject");
        archiveFile = project.getProject().getFile("test.ipsar");
        archivePath = archiveFile.getLocation();

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        entry = (IpsArchiveEntry)path.newArchiveEntry(archivePath);
        project.setIpsObjectPath(path);
    }

    @Test
    public void testFindIpsSrcFiles() throws Exception {
        List<IIpsSrcFile> result = entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsObject motorPolicy = project.findIpsObject(qntMotorPolicy);
        IIpsObject motorCollision = project.findIpsObject(qntMotorCollision);

        assertTrue(result.contains(motorPolicy.getIpsSrcFile()));
        assertTrue(result.contains(motorCollision.getIpsSrcFile()));
    }

    @Test
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        entry.initFromXml(XmlUtil.getElement(docEl, 0), project.getProject());
        AFile archiveFile = project.getProject().getFolder("lib").getFile("test.ipsar");
        Path archivePath = archiveFile.getWorkspaceRelativePath();
        if (!Abstractions.isEclipseRunning()) {
            archivePath = Path.of("/").resolve(archivePath);
        }
        assertEquals(archivePath, entry.getIpsArchive().getArchivePath());

        entry.initFromXml(XmlUtil.getElement(docEl, 1), project.getProject());
        assertNull(entry.getArchiveLocation());
    }

    @Test
    public void testGetIpsArchive() {
        IIpsArchive archive = entry.getIpsArchive();
        assertNotNull(archive);
        assertEquals(archivePath, archive.getLocation());
    }

    @Test
    public void testGetIpsPackageFragementRootName() {
        assertEquals(entry.getIpsPackageFragmentRoot().getName(), entry.getIpsPackageFragmentRootName());
    }

    @Test
    public void testGetIpsPackageFragementRoot() {
        IIpsPackageFragmentRoot root = entry.getIpsPackageFragmentRoot();
        assertNotNull(root);
        assertEquals(archiveFile.getName(), root.getName());
    }

    @Test
    public void testToXml() {
        Element el = entry.toXml(newDocument());
        // to create a new entry we need a handle to an existing file path
        Path dummyArchivePath = project.getIpsProjectPropertiesFile().getLocation();
        IpsArchiveEntry newEntry = (IpsArchiveEntry)project.getIpsObjectPath().newArchiveEntry(dummyArchivePath);
        newEntry.initFromXml(el, project.getProject());
        assertEquals(archivePath, newEntry.getArchiveLocation());
    }

    @Test
    public void testValidate() {
        MessageList ml = entry.validate();
        assertEquals(0, ml.size());

        IIpsProjectProperties props = project.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();
        IIpsArchiveEntry[] archiveEntries = path.getArchiveEntries();
        assertEquals(1, archiveEntries.length);
        assertEquals(entry.getArchiveLocation(), archiveEntries[0].getArchiveLocation());

        entry.initStorage(project.getProject().getFile("NoneExistingFile").getLocation());
        ml = entry.validate();
        assertThat(ml, hasMessageCode(IIpsObjectPathEntry.MSGCODE_MISSING_ARCHVE));

        entry.initStorage(null);
        ml = entry.validate();
        assertThat(ml, hasMessageCode(IIpsObjectPathEntry.MSGCODE_MISSING_ARCHVE));

    }

    @Test
    public void testContainsResource_false() throws Exception {
        boolean containsResource = entry.containsResource("asdasd");

        assertFalse(containsResource);
    }

    @Test
    public void testContainsResource_ipsObject() throws Exception {
        boolean containsResource = entry.containsResource("pack1/MotorPolicy.ipspolicycmpttype");

        assertTrue(containsResource);
    }

    @Test
    public void testContainsResource_resource() throws Exception {
        boolean containsResource = entry.containsResource("myTest.gif");

        assertTrue(containsResource);
    }

}
