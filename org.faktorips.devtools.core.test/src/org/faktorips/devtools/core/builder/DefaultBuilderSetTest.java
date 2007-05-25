/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.TableContentsEnumDatatypeAdapter;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultBuilderSetTest extends AbstractIpsPluginTest {

    private IIpsProject project;

	protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
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
		public IIpsArtefactBuilder[] getArtefactBuilders() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isSupportFlIdentifierResolver() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public IParameterIdentifierResolver getFlParameterIdentifierResolver() {
			return null;
		}

        /**
         * {@inheritDoc}
         */
        public String getClassNameForTableBasedEnum(ITableStructure structure) {
            return null;
        }

        public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException {
        }

        public DatatypeHelper getDatatypeHelperForTableBasedEnum(TableContentsEnumDatatypeAdapter datatype) {
            return null;
        }

        public String getVersion() {
            return null;
        }
        
	}
}
