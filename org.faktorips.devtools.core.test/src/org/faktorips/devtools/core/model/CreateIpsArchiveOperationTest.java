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

package org.faktorips.devtools.core.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArchive;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveOperationTest extends AbstractIpsPluginTest {

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
        operation.run(new NullProgressMonitor());
        createLinkIfNecessary(archiveFile, file);

        assertTrue(archiveFile.exists());

        IIpsArchive archive = new IpsArchive(project, archiveFile.getLocation());
        String[] packs = archive.getNonEmptyPackages();
        assertEquals(2, packs.length);
        assertEquals("mycompany.home", packs[0]);
        assertEquals("mycompany.motor", packs[1]);

        Set<QualifiedNameType> qnt = archive.getQNameTypes();
        assertEquals(6, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.motor.MotorCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.home.HomePolicy", IpsObjectType.POLICY_CMPT_TYPE)));
    }

    public void testRunWithIconFile() throws CoreException {
        IIpsProject project = newIpsProject();
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy", "mycompany.motor.MotorProduct");

        // configure Icon
        IIpsSrcFile productSrcFile = project.findIpsSrcFile(new QualifiedNameType("mycompany.motor.MotorProduct",
                IpsObjectType.PRODUCT_CMPT_TYPE));
        IProductCmptType prodType = (IProductCmptType)productSrcFile.getIpsObject();
        prodType.setInstancesIcon("test.gif");
        productSrcFile.save(true, new NullProgressMonitor());

        // create fake icon file
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IFolder folder = (IFolder)root.getEnclosingResource();
        IFile iconFile = folder.getFile("test.gif");
        // fake content, this is not a valid gif-file
        iconFile.create(new ByteArrayInputStream("test".getBytes()), true, new NullProgressMonitor());

        IFile archiveFile = project.getProject().getFile("test123.ipsar");
        File file = archiveFile.getLocation().toFile();
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(project.getIpsPackageFragmentRoots(), file);
        operation.run(new NullProgressMonitor());
        createLinkIfNecessary(archiveFile, file);

        assertTrue(archiveFile.exists());

        IIpsArchive archive = new IpsArchive(project, archiveFile.getLocation());
        String[] packs = archive.getNonEmptyPackages();
        assertEquals(1, packs.length);
        assertEquals("mycompany.motor", packs[0]);

        Set<QualifiedNameType> qnt = archive.getQNameTypes();
        assertEquals(2, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.motor.MotorProduct", IpsObjectType.PRODUCT_CMPT_TYPE)));

        assertFalse(coreExceptionThrownOnGetResourceAsStream(archive));
    }

    public void testRunWithErroneousIconFile() throws CoreException {
        IIpsProject project = newIpsProject();
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy", "mycompany.motor.MotorProduct");

        // configure Icon
        IIpsSrcFile productSrcFile = project.findIpsSrcFile(new QualifiedNameType("mycompany.motor.MotorProduct",
                IpsObjectType.PRODUCT_CMPT_TYPE));
        IProductCmptType prodType = (IProductCmptType)productSrcFile.getIpsObject();
        prodType.setInstancesIcon("test_doesNotExist.gif");
        productSrcFile.save(true, new NullProgressMonitor());

        // create fake icon file
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IFolder folder = (IFolder)root.getEnclosingResource();
        IFile iconFile = folder.getFile("test.gif");
        // fake content, this is not a valid gif-file
        iconFile.create(new ByteArrayInputStream("test".getBytes()), true, new NullProgressMonitor());

        IFile archiveFile = project.getProject().getFile("test.ipsar");
        File file = archiveFile.getLocation().toFile();
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(project.getIpsPackageFragmentRoots(), file);
        operation.run(new NullProgressMonitor());
        createLinkIfNecessary(archiveFile, file);

        assertTrue(archiveFile.exists());

        IIpsArchive archive = new IpsArchive(project, archiveFile.getLocation());
        String[] packs = archive.getNonEmptyPackages();
        assertEquals(1, packs.length);
        assertEquals("mycompany.motor", packs[0]);

        Set<QualifiedNameType> qnt = archive.getQNameTypes();
        assertEquals(2, qnt.size());
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.motor.MotorPolicy", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.motor.MotorProduct", IpsObjectType.PRODUCT_CMPT_TYPE)));

        assertTrue(coreExceptionThrownOnGetResourceAsStream(archive));
    }

    private boolean coreExceptionThrownOnGetResourceAsStream(IIpsArchive archive) {
        try {
            archive.getResourceAsStream("test.gif");
        } catch (CoreException e) {
            return true;
        }
        return false;
    }
}
