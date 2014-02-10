/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultBuilderSetTest extends AbstractIpsPluginTest {

    private IIpsProject project;

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
        DefaultBuilderSet builderSet = new TestBuilderSet();
        IFile file = builderSet.getRuntimeRepositoryTocFile(root);
        assertNotNull(file);
        assertEquals("extension/org/faktorips/sample/internal/motor/toc.xml", file.getProjectRelativePath().toString());
    }

    @Test
    public void testGetRuntimeRepositoryTocResourceName() {
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        DefaultBuilderSet builderSet = new TestBuilderSet();
        String tocResource = builderSet.getRuntimeRepositoryTocResourceName(root);
        assertEquals("org/faktorips/sample/internal/motor/toc.xml", tocResource);
    }

    class TestBuilderSet extends DefaultBuilderSet {

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
        protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() throws CoreException {
            return new LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder>();
        }

    }

}
