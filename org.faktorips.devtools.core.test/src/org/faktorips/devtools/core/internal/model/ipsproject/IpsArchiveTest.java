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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.File;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
        motorPolicyType = newPolicyCmptTypeWithoutProductCmptType(project, "motor.MotorPolicy");
        newPolicyCmptTypeWithoutProductCmptType(project, "motor.collision.SimpleCollisionCoverage");
        newPolicyCmptTypeWithoutProductCmptType(project, "motor.collision.ExtendedCollisionCoverage");
        newPolicyCmptTypeWithoutProductCmptType(project, "home.base.HomePolicy");
        
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

        motorPolicyType.getIpsSrcFile().getCorrespondingFile().delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
        
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
        
        archive = new IpsArchive(project.getProject().getFile("UnknownFile"));
        assertFalse(archive.exists());
    }

    public void testGetNoneEmptyPackages() throws CoreException {
        String[] packs = archive.getNonEmptyPackages();
        assertEquals(3, packs.length);
        assertEquals("home.base", packs[0]);
        assertEquals("motor", packs[1]);
        assertEquals("motor.collision", packs[2]);
    }

    public void testGetNoneEmptySubpackages() throws CoreException {
        String [] subpacks = archive.getNonEmptySubpackages("");
        assertEquals(2, subpacks.length);
        assertEquals("home", subpacks[0]);
        assertEquals("motor", subpacks[1]);
        
        subpacks = archive.getNonEmptySubpackages("motor");
        assertEquals(1, subpacks.length);
        assertEquals("motor.collision", subpacks[0]);
        
        assertEquals(0, archive.getNonEmptySubpackages(null).length);
        
        subpacks = archive.getNonEmptySubpackages("noneExistingPack");
        assertEquals(0, subpacks.length);
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
        assertNull(archive.getContent(null));
        
        QualifiedNameType qnt = new QualifiedNameType("motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertNotNull(archive.getContent(qnt));
        
        qnt = new QualifiedNameType("Unknown", IpsObjectType.POLICY_CMPT_TYPE);
        assertNull(archive.getContent(qnt));
        
        qnt = new QualifiedNameType("MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE);
        assertNull(archive.getContent(qnt));
    }
    
    public void testDuplicateEntryAndTestRefreshInWorkspace() throws Exception{
        // store archive time to check if the refresh is successful after re-creating the archive
        long timeBefore = archiveFile.getLocalTimeStamp();
        
        // check if duplicate entries will be overridden in the archive, e.g. test case runtime xml's are generated 
        // in the source and afterwards copied to the bin folder
        newIpsObject(project, IpsObjectType.TEST_CASE, "test.testcase");

        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        
        File file = createFileIfNecessary(archiveFile);
        CreateIpsArchiveOperation op = new CreateIpsArchiveOperation(project, file);
        op.setInclJavaBinaries(true);
        op.setInclJavaSources(true);
        ResourcesPlugin.getWorkspace().run(op, null);
        createLinkIfNecessary(archiveFile, file);
        // no exception test was successful
        
        // check if the archive file was refreshed
        assertFalse(timeBefore == archiveFile.getLocalTimeStamp());
    }
}
