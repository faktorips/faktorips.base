/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.CreateIpsArchiveOperation;
import org.faktorips.devtools.model.internal.ipsproject.IpsArchive;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.util.IoUtil;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveOperationTest extends AbstractIpsPluginTest {

    @Test
    public void testRun() {
        IIpsProject project = newIpsProject();
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy", "mycompany.motor.MotorProduct");
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorCoverage", "mycompany.motor.MotorCoverageType");
        newPolicyAndProductCmptType(project, "mycompany.home.HomePolicy", "mycompany.home.HomeProduct");
        AFile archiveFile = project.getProject().getFile("test.ipsar");
        archiveFile.getWorkspace().build(ABuildKind.FULL, null);

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
        assertTrue(
                qnt.contains(new QualifiedNameType("mycompany.motor.MotorCoverage", IpsObjectType.POLICY_CMPT_TYPE)));
        assertTrue(qnt.contains(new QualifiedNameType("mycompany.home.HomePolicy", IpsObjectType.POLICY_CMPT_TYPE)));
    }

    @Test
    public void testRunWithIconFile() {
        IIpsProject project = newIpsProject();
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy", "mycompany.motor.MotorProduct");

        // configure Icon
        IIpsSrcFile productSrcFile = project.findIpsSrcFile(new QualifiedNameType("mycompany.motor.MotorProduct",
                IpsObjectType.PRODUCT_CMPT_TYPE));
        IProductCmptType prodType = (IProductCmptType)productSrcFile.getIpsObject();
        prodType.setInstancesIcon("test.gif");
        productSrcFile.save(new NullProgressMonitor());

        // create fake icon file
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        AFolder folder = (AFolder)root.getEnclosingResource();
        AFile iconFile = folder.getFile("test.gif");
        // fake content, this is not a valid gif-file
        iconFile.create(new ByteArrayInputStream("test".getBytes()), new NullProgressMonitor());

        AFile archiveFile = project.getProject().getFile("test123.ipsar");
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
        assertTrue(
                qnt.contains(new QualifiedNameType("mycompany.motor.MotorProduct", IpsObjectType.PRODUCT_CMPT_TYPE)));

        assertFalse(coreExceptionThrownOnGetResourceAsStream(archive));
    }

    /**
     * FIPS-1756
     * <p>
     * The same icon is used in two product component types
     */
    @Test
    public void testRun_withDoubledIconFile() {
        IIpsProject project = newIpsProject();
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy", "mycompany.motor.MotorProduct");
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy2", "mycompany.motor.MotorProduct2");

        // configure Icon
        IIpsSrcFile productSrcFile = project.findIpsSrcFile(new QualifiedNameType("mycompany.motor.MotorProduct",
                IpsObjectType.PRODUCT_CMPT_TYPE));
        IProductCmptType prodType = (IProductCmptType)productSrcFile.getIpsObject();
        prodType.setInstancesIcon("test.gif");
        productSrcFile.save(new NullProgressMonitor());

        IIpsSrcFile productSrcFile2 = project.findIpsSrcFile(new QualifiedNameType("mycompany.motor.MotorProduct2",
                IpsObjectType.PRODUCT_CMPT_TYPE));
        IProductCmptType prodType2 = (IProductCmptType)productSrcFile2.getIpsObject();
        prodType2.setInstancesIcon("test.gif");
        productSrcFile2.save(new NullProgressMonitor());

        // create fake icon file
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        AFolder folder = (AFolder)root.getEnclosingResource();
        AFile iconFile = folder.getFile("test.gif");
        // fake content, this is not a valid gif-file
        iconFile.create(new ByteArrayInputStream("test".getBytes()), new NullProgressMonitor());

        AFile archiveFile = project.getProject().getFile("test123.ipsar");
        File file = archiveFile.getLocation().toFile();
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(project.getIpsPackageFragmentRoots(), file);
        operation.run(new NullProgressMonitor());
        createLinkIfNecessary(archiveFile, file);

        assertTrue(archiveFile.exists());
    }

    @Test
    public void testRunWithErroneousIconFile() {
        IIpsProject project = newIpsProject();
        newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy", "mycompany.motor.MotorProduct");

        // configure Icon
        IIpsSrcFile productSrcFile = project.findIpsSrcFile(new QualifiedNameType("mycompany.motor.MotorProduct",
                IpsObjectType.PRODUCT_CMPT_TYPE));
        IProductCmptType prodType = (IProductCmptType)productSrcFile.getIpsObject();
        prodType.setInstancesIcon("test_doesNotExist.gif");
        productSrcFile.save(new NullProgressMonitor());

        // create fake icon file
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        AFolder folder = (AFolder)root.getEnclosingResource();
        AFile iconFile = folder.getFile("test.gif");
        // fake content, this is not a valid gif-file
        iconFile.create(new ByteArrayInputStream("test".getBytes()), new NullProgressMonitor());

        AFile archiveFile = project.getProject().getFile("test.ipsar");
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
        assertTrue(
                qnt.contains(new QualifiedNameType("mycompany.motor.MotorProduct", IpsObjectType.PRODUCT_CMPT_TYPE)));

        assertTrue(coreExceptionThrownOnGetResourceAsStream(archive));

    }

    private boolean coreExceptionThrownOnGetResourceAsStream(IIpsArchive archive) {
        InputStream inputStream = null;
        try {
            inputStream = archive.getResourceAsStream("test.gif");
        } catch (IpsException e) {
            return true;
        } finally {
            IoUtil.close(inputStream);
        }
        return false;
    }
}
