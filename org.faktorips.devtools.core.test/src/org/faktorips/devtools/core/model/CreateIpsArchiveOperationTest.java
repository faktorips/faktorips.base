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

package org.faktorips.devtools.core.model;

import java.io.File;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsArchive;

/**
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveOperationTest extends AbstractIpsPluginTest {


    /*
     * Test method for 'org.faktorips.devtools.core.model.CreateIpsArchiveOperation.run(IProgressMonitor)'
     */
    public void testRun() throws CoreException {
        IIpsProject project = newIpsProject();
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy", "mycompany.motor.MotorProduct");
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorCoverage", "mycompany.motor.MotorCoverageType");
        newPolicyAndProductCmptType(project, "mycompany.home.HomePolicy", "mycompany.home.HomeProduct");
        IFile archiveFile = project.getProject().getFile("test.ipsar");
        archiveFile.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        File file = archiveFile.getLocation().toFile();
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(project.getIpsPackageFragmentRoots(), file);
        operation.setInclJavaBinaries(true);
        operation.setInclJavaSources(true);
        operation.run(null);
        createLinkIfNecessary(archiveFile, file);

        assertTrue(archiveFile.exists());
        
        IIpsArchive archive = new IpsArchive(archiveFile);
        String[] packs = archive.getNoneEmptyPackages();
        assertEquals(2, packs.length);
        assertEquals("mycompany.motor", packs[0]);
        assertEquals("mycompany.home", packs[1]);
        
        Set qnt = archive.getQNameTypes();
        assertEquals(6, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.motor.MotorCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.home.HomePolicy", IpsObjectType.POLICY_CMPT_TYPE)));
    }
    

}
