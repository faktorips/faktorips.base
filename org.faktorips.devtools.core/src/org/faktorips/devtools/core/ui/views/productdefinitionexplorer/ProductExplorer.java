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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.TreeViewer;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorer;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration;

/**
 * A <code>ModelExplorer</code> that displays productdefinition projects along with all
 * contained <code>ProductCmpt</code>s, <code>TableContents</code>, <code>TestCases</code>
 * and <code>TestCaseTypes</code>. 
 * 
 * @author Stefan Widmaier
 */
public class ProductExplorer extends ModelExplorer {
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productDefinitionExplorer"; //$NON-NLS-1$
    
	public ProductExplorer() {
		super();
	}

	protected ModelExplorerConfiguration createConfig() {
		return new ModelExplorerConfiguration(new Class[] { IProductCmpt.class,
				ITableContents.class, ITestCase.class, ITestCaseType.class}
				, new Class[]{IFile.class, IFolder.class, IProject.class});
	}
	protected void createFilters(TreeViewer tree) {
		super.createFilters(tree);
		tree.addFilter(new ProductExplorerFilter());
	}

}
