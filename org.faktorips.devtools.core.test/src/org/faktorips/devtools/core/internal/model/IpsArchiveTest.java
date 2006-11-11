/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsArchive;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IIpsArchive archive;
    private IFile archiveFile;
    
    private IPolicyCmptType motorPolicyType;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("ArchiveProject");
        motorPolicyType = newPolicyCmptType(project, "motor.MotorPolicy");
        newPolicyCmptType(project, "motor.collision.SimpleCollisionCoverage");
        newPolicyCmptType(project, "motor.collision.ExtendedCollisionCoverage");
        newPolicyCmptType(project, "home.base.HomePolicy");
        archiveFile = project.getProject().getFile("test.ipsar");
        createArchive(project, archiveFile);
        archive = new IpsArchive(archiveFile);
    }
    
    /**
     * Tests if the access methods work correct if we change the underlying archive file on disk.
     */
    public void testModificationToUnderlyingFile() throws Exception {
        QualifiedNameType qnt = new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertTrue(archive.contains(qnt));

        motorPolicyType.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        createArchive(project, archiveFile);
        assertFalse(archive.contains(qnt));
    }
    
    public void testBasePackageNameForGeneratedJavaClass() throws CoreException {
        String expPackage = motorPolicyType.getIpsSrcFile().getBasePackageNameForGeneratedJavaClass();
        assertEquals(expPackage, archive.getBasePackageNameForGeneratedJavaClass(motorPolicyType.getQualifiedNameType())); 
    }
    
    public void testBasePackageNameForExtensionJavaClass() throws CoreException {
        String expPackage = motorPolicyType.getIpsSrcFile().getBasePackageNameForExtensionJavaClass();
        assertEquals(expPackage, archive.getBasePackageNameForExtensionJavaClass(motorPolicyType.getQualifiedNameType())); 
    }
    
    public void testContains() throws CoreException {
        assertTrue(archive.contains(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertFalse(archive.contains(new QualifiedNameType("Unknown", IpsObjectType.POLICY_CMPT_TYPE)));
    }
    
    public void testContainsPackage() throws CoreException {
        assertTrue(archive.containsPackage(""));
        assertFalse(archive.containsPackage(null));
        
        assertTrue(archive.containsPackage("motor"));
        assertTrue(archive.containsPackage("motor.collision"));
        assertTrue(archive.containsPackage("home"));
        assertFalse(archive.containsPackage("unknwon"));
        
    }

    public void testGetArchiveFile() {
        assertEquals(archiveFile, archive.getArchiveFile());
    }

    public void testExists() {
        assertTrue(archive.exists());
        
        IIpsArchive archive = new IpsArchive(null);
        assertFalse(archive.exists());
        
        archive = new IpsArchive(project.getProject().getFile("UnknownFile"));
        assertFalse(archive.exists());
    }

    public void testGetNoneEmptyPackages() throws CoreException {
        String[] packs = archive.getNoneEmptyPackages();
        assertEquals(3, packs.length);
        assertEquals("motor", packs[0]);
        assertEquals("home.base", packs[1]);
        assertEquals("motor.collision", packs[2]);
    }

    public void testGetNoneEmptySubpackages() throws CoreException {
        Set subpacks = archive.getNoneEmptySubpackages("");
        assertEquals(2, subpacks.size());
        assertTrue(subpacks.contains("motor"));
        assertTrue(subpacks.contains("home"));
        
        subpacks = archive.getNoneEmptySubpackages("motor");
        assertEquals(1, subpacks.size());
        assertTrue(subpacks.contains("motor.collision"));
        
        assertEquals(0, archive.getNoneEmptySubpackages(null).size());
        
        subpacks = archive.getNoneEmptySubpackages("noneExistingPack");
        assertEquals(0, subpacks.size());
    }

    public void testGetQNameTypes() throws CoreException {
        Set qnt = archive.getQNameTypes();
        assertEquals(4, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("motor.collision.SimpleCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("motor.collision.ExtendedCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("home.base.HomePolicy", IpsObjectType.POLICY_CMPT_TYPE)));
    }
    
    public void testGetQNameType_Pack() throws CoreException {
        Set qnt = archive.getQNameTypes(null);
        assertEquals(0, qnt.size());
        
        qnt = archive.getQNameTypes("motor");
        assertEquals(1, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));
        
        qnt = archive.getQNameTypes("motor.collision");
        assertEquals(2, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("motor.collision.SimpleCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("motor.collision.ExtendedCollisionCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        
        qnt = archive.getQNameTypes("home");
        assertEquals(0, qnt.size());
        
        qnt = archive.getQNameTypes("unknown");
        assertEquals(0, qnt.size());
    }
    
    public void testGetContent() throws CoreException {
        assertNull(archive.getContent(null, "UTF-8"));
        
        QualifiedNameType qnt = new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertNotNull(archive.getContent(qnt, project.getXmlFileCharset()));
        
        qnt = new QualifiedNameType("Unknown", IpsObjectType.POLICY_CMPT_TYPE);
        assertNull(archive.getContent(qnt, project.getXmlFileCharset()));
        
        qnt = new QualifiedNameType("MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertNull(archive.getContent(qnt, "UTF-8"));
    }
}
