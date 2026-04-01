/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.perspective;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.texteditor.templates.TemplatesView;

public class IpsModelPerspectiveFactory implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
        folder.addView(JavaUI.ID_PACKAGES);
        folder.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
        folder.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER);

        IFolderLayout outputfolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
        outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        outputfolder.addView(JavaUI.ID_JAVADOC_VIEW);
        outputfolder.addView(JavaUI.ID_SOURCE_VIEW);
        outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
        outputfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
        outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
        outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);

        IFolderLayout outlineFolder = layout.createFolder("right", IPageLayout.RIGHT, (float)0.75, editorArea); //$NON-NLS-1$
        outlineFolder.addView(IPageLayout.ID_OUTLINE);

        outlineFolder.addPlaceholder(TemplatesView.ID);

        layout.addActionSet(JavaUI.ID_ACTION_SET);
        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

        // views - java
        layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
        layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
        layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
        layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);

        // views - search
        layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

        // views - debugging
        layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

        // views - standard workbench
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
        layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
        layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
        layout.addShowViewShortcut(TemplatesView.ID);
        layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$

        // new actions - Java project creation wizard
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewJavaWorkingSetWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file"); //$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard"); //$NON-NLS-1$

        // 'Window' > 'Open Perspective' contributions
        layout.addPerspectiveShortcut(JavaUI.ID_BROWSING_PERSPECTIVE);
        layout.addPerspectiveShortcut(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);

    }

}
