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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.ui.actions.WrapperAction;
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

    protected void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new ProductMenuBuilder());

        Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(contextMenu);
//         do not register contextmenue to prevent insertion of MB-Additions
    }
    
    private class ProductMenuBuilder extends MenuBuilder{
        /*
         * TODO find clean solution for adding specific actions from other plugins without inserting mb-additions
         */
        protected void createAdditionalActions(IMenuManager manager, Object selected) {
            if(!(selected instanceof IProject)){
                MenuManager teamMenu = new MenuManager(Messages.ProductExplorer_submenuTeam);
                teamMenu.add(new WrapperAction(treeViewer, Messages.ProductExplorer_actionCommit, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.commit")); //$NON-NLS-2$ //$NON-NLS-1$
                teamMenu.add(new WrapperAction(treeViewer, Messages.ProductExplorer_actionUpdate, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.update")); //$NON-NLS-2$ //$NON-NLS-1$
                teamMenu.add(new WrapperAction(treeViewer, Messages.ProductExplorer_actionReplace, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.replace")); //$NON-NLS-2$ //$NON-NLS-1$
                teamMenu.add(new WrapperAction(treeViewer, Messages.ProductExplorer_actionAdd, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.add")); //$NON-NLS-2$ //$NON-NLS-1$
                teamMenu.add(new WrapperAction(treeViewer, Messages.ProductExplorer_actionShowHistory, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.showHistory"));  //$NON-NLS-1$//$NON-NLS-2$
                manager.add(teamMenu);
            }
        }
    }
}
