/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultBuilderSetTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private DefaultBuilderSet builderSet = new TestBuilderSet();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject();
        IIpsObjectPath path = project.getIpsObjectPath();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)path.getEntries()[0];
        entry.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample");
        entry.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample");
        entry.setBasePackageRelativeTocPath("motor/toc.xml");
        project.setIpsObjectPath(path);
    }

    @Test
    public void testGetRuntimeRepositoryTocFile() {
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        AFile file = builderSet.getRuntimeRepositoryTocFile(root);
        assertNotNull(file);
        assertEquals("extension/org/faktorips/sample/internal/motor/toc.xml", file.getProjectRelativePath().toString());
    }

    @Test
    public void testGetRuntimeRepositoryTocResourceName() {
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        String tocResource = builderSet.getRuntimeRepositoryTocResourceName(root);
        assertEquals("org/faktorips/sample/internal/motor/toc.xml", tocResource);
    }

    @Test
    public void testGetAdditionalAnnotations() throws CoreRuntimeException {
        builderSet.beforeBuildProcess(null);
        List<String> annotations = builderSet.getAdditionalAnnotations();

        assertEquals(4, annotations.size());
        assertTrue(annotations.contains("SuppressWarning(all)"));
        assertTrue(annotations.contains("Generated(test)"));
        assertTrue(annotations.contains("SomeAnnotation"));
        assertTrue(annotations.contains("foo.bar.Generated(\"Baz\")"));
    }

    @Test
    public void testGetAdditionalImports() throws CoreRuntimeException {
        builderSet.beforeBuildProcess(null);
        List<String> imports = builderSet.getAdditionalImports();

        assertEquals(2, imports.size());
        assertTrue(imports.contains("javax.test.Generated"));
        assertTrue(imports.contains("xyz.SomeAnnotation"));
        assertFalse(imports.contains("foo.bar.Generated"));
    }

    class TestBuilderSet extends DefaultBuilderSet {

        @Override
        protected String getConfiguredAdditionalAnnotations() {
            return "javax.test.Generated(test);SuppressWarning(all); xyz.SomeAnnotation; foo.bar.Generated(\"Baz\")";
        }

        @Override
        public IIpsArtefactBuilder[] getArtefactBuilders() {
            return new IIpsArtefactBuilder[0];
        }

        @Override
        public boolean isSupportFlIdentifierResolver() {
            return false;
        }

        @Override
        public String getVersion() {
            return "";
        }

        @Override
        protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() throws CoreRuntimeException {
            return new LinkedHashMap<>();
        }

        @Override
        public boolean isGeneratePublishedInterfaces() {
            return true;
        }

        @Override
        public DatatypeHelper getDatatypeHelper(Datatype datatype) {
            return null;
        }

    }

}
