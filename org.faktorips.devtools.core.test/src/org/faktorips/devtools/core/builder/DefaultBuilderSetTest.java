/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.IdentifierResolver;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultBuilderSetTest extends AbstractIpsPluginTest {

    private IIpsProject project;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject();
        IIpsObjectPath path = project.getIpsObjectPath();
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)path.getEntries()[0];
        entry.setSpecificBasePackageNameForMergableJavaClasses("org.faktorips.sample");
        entry.setSpecificBasePackageNameForDerivedJavaClasses("org.faktorips.sample");
        entry.setBasePackageRelativeTocPath("motor/toc.xml");
        project.setIpsObjectPath(path);
    }

    public void testGetRuntimeRepositoryTocFile() throws CoreException {
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        DefaultBuilderSet builderSet = new TestBuilderSet();
        IFile file = builderSet.getRuntimeRepositoryTocFile(root);
        assertNotNull(file);
        assertEquals("extension/org/faktorips/sample/internal/motor/toc.xml", file.getProjectRelativePath().toString());
    }

    public void testGetRuntimeRepositoryTocResourceName() throws CoreException {
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        DefaultBuilderSet builderSet = new TestBuilderSet();
        String tocResource = builderSet.getRuntimeRepositoryTocResourceName(root);
        assertEquals("org/faktorips/sample/internal/motor/toc.xml", tocResource);
    }

    class TestBuilderSet extends DefaultBuilderSet {

        /**
         * {@inheritDoc}
         */
        @Override
        public IIpsArtefactBuilder[] getArtefactBuilders() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSupportFlIdentifierResolver() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public IdentifierResolver createFlIdentifierResolver(IFormula formula) throws CoreException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getClassNameForTableBasedEnum(ITableStructure structure) {
            return null;
        }

        public void initialize(IIpsArtefactBuilderSetConfigModel config) throws CoreException {
        }

        @Override
        public String getVersion() {
            return "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IIpsArtefactBuilder[] createBuilders() throws CoreException {
            return new IIpsArtefactBuilder[0];
        }
    }
}
