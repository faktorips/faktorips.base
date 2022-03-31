/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.junit.Before;
import org.junit.Test;

public class IpsSrcFileImmutableTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsSrcFileImmutable srcFileImmutable;
    private IProductCmpt product;
    private IProductCmpt productImmutable;
    private AFile file;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // create srcfile with contents
        ipsProject = newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        product = newProductCmpt(root, "TestProductCmpt");
        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration();
        generation.newAttributeValue();
        generation.newLink("");

        // save contents
        product.getIpsSrcFile().save(null);

        file = product.getIpsSrcFile().getCorrespondingFile();
        srcFileImmutable = new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct", file.getContents());
        productImmutable = (IProductCmpt)srcFileImmutable.getIpsObject();
    }

    @Test
    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.getTypeForExtension("ipsproduct"), srcFileImmutable.getIpsObjectType());
    }

    @Test
    public void testGetCorrespondingResource() {
        assertNull(srcFileImmutable.getCorrespondingResource());
    }

    @Test
    public void testGetCorrespondingFile() {
        assertNull(srcFileImmutable.getCorrespondingFile());
    }

    @Test
    public void testIsDirty() {
        assertFalse(srcFileImmutable.isDirty());
        productImmutable.newGeneration();
        assertFalse(srcFileImmutable.isDirty());
    }

    @Test
    public void testGetIpsObject() {
        IpsSrcFileImmutable srcFileImm2 = new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct",
                file.getContents());
        IProductCmpt prodImm2 = (IProductCmpt)srcFileImm2.getIpsObject();
        assertEquals(productImmutable.getQualifiedName(), prodImm2.getQualifiedName());

        // compare generations
        List<?> gens1 = Arrays.asList(productImmutable.getGenerationsOrderedByValidDate());
        List<?> gens2 = Arrays.asList(prodImm2.getGenerationsOrderedByValidDate());
        assertEquals(gens1.size(), gens2.size());
        Iterator<?> iterator1 = gens1.iterator();
        Iterator<?> iterator2 = gens2.iterator();
        while (iterator1.hasNext()) {
            IProductCmptGeneration gen1 = (IProductCmptGeneration)iterator1.next();
            IProductCmptGeneration gen2 = (IProductCmptGeneration)iterator2.next();
            assertEquals(gen1.getValidFrom(), gen2.getValidFrom());
        }

        IProductCmptGeneration generation1 = productImmutable.getFirstGeneration();
        IProductCmptGeneration generation2 = prodImm2.getFirstGeneration();

        // compare configelements in first generation
        List<?> configElements1 = Arrays.asList(generation1.getConfiguredDefaults());
        List<?> configElements2 = Arrays.asList(generation2.getConfiguredDefaults());
        assertEquals(configElements1.size(), configElements1.size());
        iterator1 = configElements1.iterator();
        iterator2 = configElements2.iterator();
        while (iterator1.hasNext()) {
            IConfiguredDefault configElement1 = (IConfiguredDefault)iterator1.next();
            IConfiguredDefault configElement2 = (IConfiguredDefault)iterator2.next();
            assertEquals(configElement1.getId(), configElement2.getId());
            assertEquals(configElement1.getValue(), configElement2.getValue());
        }

        // compare relations in first generation
        List<?> relations1 = Arrays.asList(generation1.getLinks());
        List<?> relations2 = Arrays.asList(generation2.getLinks());
        assertEquals(relations1.size(), relations2.size());
        iterator1 = relations1.iterator();
        iterator2 = relations2.iterator();
        while (iterator1.hasNext()) {
            IProductCmptLink relation1 = (IProductCmptLink)iterator1.next();
            IProductCmptLink relation2 = (IProductCmptLink)iterator2.next();
            assertEquals(relation1.getTarget(), relation2.getTarget());
        }
    }

    @Test
    public void testSave() {
        productImmutable.newGeneration();
        srcFileImmutable.save(null);

        // nothing was saved
        IProductCmpt prodImm2 = (IProductCmpt)new IpsSrcFileImmutable("ProdImm2.ipsproduct", file.getContents())
                .getIpsObject();
        assertEquals(1, prodImm2.getNumOfGenerations());
    }

    @Test
    public void testIsMutable() {
        assertFalse(srcFileImmutable.isMutable());
    }

    @Test
    public void testIsHistoric() {
        assertTrue(srcFileImmutable.isHistoric());
    }

    @Test
    public void testIsContentParsable() {
        IpsSrcFileImmutable srcFile = new IpsSrcFileImmutable("TestSrcFileImmutable.ipsproduct", file.getContents());
        assertTrue(srcFile.isContentParsable());

        // redirekt System.error as the XML-Parser's error handling is messed up. It writes
        // something like
        // "[Fatal Error] :1:1: Content is not allowed in prolog" when the XML isn't valid so that
        // you assume that something has gone wrong, when it hasn't.
        PrintStream errorStream = System.err;
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        suppressLoggingDuringExecutionOfThisTestCase(); // invalid xml contents leads to "expected"
        // exception
        try {
            srcFile = new IpsSrcFileImmutable("Test.ipsproduct", new ByteArrayInputStream(new byte[100]));
            assertFalse(srcFile.isContentParsable());
        } finally {
            System.setErr(errorStream);
        }
    }

    @Test
    public void testGetQualifiedNameType() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PolicyCmptType/>";
        String fileName = IpsObjectType.POLICY_CMPT_TYPE.getFileName("Test");
        IpsSrcFileImmutable srcFile = new IpsSrcFileImmutable(fileName, new ByteArrayInputStream(xml.getBytes()));
        QualifiedNameType qnt = srcFile.getQualifiedNameType();
        assertEquals(IpsObjectType.POLICY_CMPT_TYPE, qnt.getIpsObjectType());
        assertEquals("Test", qnt.getUnqualifiedName());
        assertEquals("", qnt.getPackageName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelete() {
        srcFileImmutable.delete();
    }

}
