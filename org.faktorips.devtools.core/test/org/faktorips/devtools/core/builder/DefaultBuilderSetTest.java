package org.faktorips.devtools.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultBuilderSetTest extends IpsPluginTest {

	/*
	 * @see IpsPluginTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Test method for 'org.faktorips.devtools.core.builder.DefaultBuilderSet.getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot)'
	 */
	public void testGetRuntimeRepositoryTocFile() throws CoreException {
		IIpsProject project = newIpsProject("TestProject");
		DefaultBuilderSet builderSet = new TestBuilderSet();
		IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
		IFile file = builderSet.getRuntimeRepositoryTocFile(root);
		assertNotNull(file);
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
		public void initialize() throws CoreException {
			// TODO Auto-generated method stub
			
		}
		
	}
}
