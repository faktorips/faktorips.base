/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A composite that consists of a text control on the left containing a valid folder name and a
 * button attached to it on the right. If the button is clicked, a folder selection dialog appears,
 * using a given root folder as the folder selection root, or the workspace root if no root was
 * given.
 * 
 * @author Roman Grutza
 */
public class FolderSelectionControl extends TextButtonControl {

    private IContainer root;
    private IContainer folder;

    /**
     * @param parent Composite
     * @param toolkit UIToolkit
     * @param buttonText String which should be displayed on this Controls` button
     */
    public FolderSelectionControl(Composite parent, UIToolkit toolkit, String buttonText) {
        super(parent, toolkit, buttonText);
        this.root = ResourcesPlugin.getWorkspace().getRoot();
    }

    /**
     * @param parent Composite
     * @param toolkit UIToolkit
     * @param buttonText String which should be displayed on this Controls` button
     * @param root a Folder, Project or WorkspaceRoot instance. If null is given, no folders will be
     *            displayed for selection. You can reset the root with the method
     *            <code>setRoot()</code>.
     */
    public FolderSelectionControl(Composite parent, UIToolkit toolkit, String buttonText, IContainer root) {
        super(parent, toolkit, buttonText);
        this.root = root;
    }

    /**
     * @param parent Composite
     * @param toolkit UIToolkit
     * @param buttonText String which should be displayed on this Controls` button
     * @param root a Folder, Project or WorkspaceRoot instance which represents the root of
     *            selectable folders. If null is given, no folders will be displayed for selection.
     *            You can reset the root with the method <code>setRoot()</code>.
     * @param folder the currently selected folder
     */
    public FolderSelectionControl(Composite parent, UIToolkit toolkit, String buttonText, IContainer root,
            IContainer folder) {

        super(parent, toolkit, buttonText);
        this.root = root;
        updateText(folder);
    }

    /**
     * Set the root of selectable folders to the given root.
     * 
     * @param root a Folder, Project or WorkspaceRoot instance. If null is given, no folders will be
     *            displayed for selection.
     */
    public void setRoot(IContainer root) {
        this.root = root;
        setText(""); //$NON-NLS-1$
        this.folder = null;
    }

    /**
     * Sets the text field of this control to the given folder
     * 
     * @param folder a valid IFolder instance
     */
    public void setFolder(IContainer folder) {
        this.folder = folder;
        if (folder != null) {
            updateText(folder);
        }
    }

    /**
     * @return Returns the currently selected folder or null if none was selected.
     */
    public final IContainer getFolder() {
        return folder;
    }

    @Override
    protected void buttonClicked() {
        if (root == null) {
            return;
        }

        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
                new WorkbenchContentProvider());
        dialog.setTitle(Messages.FolderSelectionControl_select_folder_title);
        dialog.setMessage(Messages.FolderSelectionControl_select_folder_message);
        dialog.setHelpAvailable(false);
        dialog.setInput(root);
        dialog.setAllowMultiple(false);

        // make only folders visible/selectable
        dialog.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IContainer) {
                    return true;
                }
                return false;
            }
        });

        if (dialog.open() == Window.OK) {
            if (dialog.getResult().length == 1) {
                IFolder folder = (IFolder)dialog.getFirstResult();
                setFolder(folder);
            } else {
                setText(""); //$NON-NLS-1$
            }
        }

    }

    /** updates the text field with the given folder */
    private void updateText(IContainer folder) {
        String text = ""; //$NON-NLS-1$
        if (root != null) {
            if (root instanceof IProject) {
                text = ((IProject)root).getName() + IPath.SEPARATOR;
            }
            text += folder.getProjectRelativePath().toOSString();
        }
        setText(text);
    }

}
