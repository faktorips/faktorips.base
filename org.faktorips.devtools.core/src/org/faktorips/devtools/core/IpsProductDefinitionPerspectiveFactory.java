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

package org.faktorips.devtools.core;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.faktorips.devtools.core.ui.views.attrtable.AttributesTable;
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorer;
import org.faktorips.devtools.core.ui.views.productdefinitionexplorer.ProductExplorer;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer;

/**
 * Perspective for ProductDefinition.
 * 
 * @author Thorsten Guenther
 */
public class IpsProductDefinitionPerspectiveFactory implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        IFolderLayout folder= layout.createFolder("top_left", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
        folder.addView(ProductExplorer.EXTENSION_ID);
        folder.addPlaceholder(ModelExplorer.EXTENSION_ID);
        
        folder = layout.createFolder("bottom_left", IPageLayout.BOTTOM, (float)0.5, "top_left"); //$NON-NLS-1$ //$NON-NLS-2$
        folder.addView(ProductStructureExplorer.EXTENSION_ID);
        
        folder = layout.createFolder("bottom_right", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
        folder.addView(IPageLayout.ID_PROBLEM_VIEW);
        
        folder.addPlaceholder(AttributesTable.EXTENSION_ID);
        
        layout.addActionSet("org.faktorips.devtools.department.productActionSet"); //$NON-NLS-1$
	}

}
