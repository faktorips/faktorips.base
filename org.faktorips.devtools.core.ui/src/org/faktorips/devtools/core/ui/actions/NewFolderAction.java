/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Opens a dialog to let the user specify a foldername and creates the folder in the filesystem. It
 * is possible to create subfolders (subpackages) by specifying a path separated with dots (".").
 * This action will then create the folder defined by the path and all parent folders/packages if
 * they have not been existing yet.
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class NewFolderAction extends IpsAction {
    private Shell shell;

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final String BLANK = " "; //$NON-NLS-1$
    private static final String DOT = "."; //$NON-NLS-1$

    public NewFolderAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.shell = shell;
        setText(Messages.NewFolderAction_name);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewFolder.gif")); //$NON-NLS-1$
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        IContainer container = getContainer(selected);
        if (container == null) {
            MessageDialog.openError(shell, Messages.NewFolderAction_titleNewFolder,
                    Messages.NewFolderAction_msgNoParentFound);
            return;
        }

        String message = NLS.bind(Messages.NewFolderAction_descriptionNewFolder, container.getName());
        Validator validator = new Validator(container);
        InputDialog d = new InputDialog(shell, Messages.NewFolderAction_titleNewFolder, message,
                Messages.NewFolderAction_valueNewFolder, validator);
        d.open();
        if (d.getReturnCode() == Window.OK) {
            createFolder(container, d.getValue());
        }
    }

    private IContainer getContainer(Object selected) {
        if (selected == null) {
            return null;
        }
        IResource res = null;
        if (selected instanceof IIpsProject) {
            res = ((IIpsProject)selected).getProject();
        } else if (selected instanceof IIpsElement) {
            res = ((IIpsElement)selected).getEnclosingResource();
        } else if (selected instanceof IResource) {
            res = (IResource)selected;
        }
        // search for next folder
        while (res != null && !(res instanceof IContainer)) {
            res = res.getParent();
        }
        return (IContainer)res;
    }

    public void createFolder(IContainer container, String name) {
        conditionalCreateFolder(container, name, true);
    }

    public IFolder getFolder(IContainer container, String name) {
        return conditionalCreateFolder(container, name, false);
    }

    /**
     * If <code>createResource</code> is false this method creates an <code>IFolder</code> which
     * might or might not exist. If <code>createResource</code> is true folders are created in the
     * filsystem. In this case the method creates the folder represented by the given path string,
     * and all of its parentfolders if they have not been existing yet.
     */
    private IFolder conditionalCreateFolder(IContainer parent, String name, boolean createResource) {
        if (name.indexOf(DOT) != -1) {
            /*
             * Create IFolder from first segment of the "."-separated string (name). Call
             * recursiveley so the IFolder corresponding to the last segment is created by its
             * parentfolder and returned as result of this method call.
             */
            String firstSegment = name.substring(0, name.indexOf(DOT));
            String restSegment = name.substring(name.indexOf(DOT) + 1, name.length());
            IFolder current = parent.getFolder(new Path(firstSegment));
            if (createResource) {
                createResource(current);
            }
            return conditionalCreateFolder(current, restSegment, createResource);
        } else {
            IFolder current = parent.getFolder(new Path(name));
            if (createResource) {
                createResource(current);
            }
            return current;
        }
    }

    /**
     * Created the folder in the filsystem the given IFolder points to (if not existent).
     */
    private void createResource(IFolder folder) {
        if (!folder.exists()) {
            try {
                folder.create(true, true, null);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    /**
     * Checks that the entered name does not result in an existing folder.
     * 
     * @author Thorsten Guenther
     * @author Stefan Widmaier
     */
    private class Validator implements IInputValidator {

        private IContainer parent;

        public Validator(IContainer parent) {
            this.parent = parent;
        }

        @Override
        public String isValid(String newText) {
            if (newText.indexOf(BLANK) != -1) {
                return Messages.NewFolderAction_FoldernameMustNotContainBlanks;
            }
            if (newText.trim().equals(EMPTY_STRING)) {
                return Messages.NewFolderAction_InvalidFoldername;
            }
            if (JavaConventions.validatePackageName(newText).getSeverity() == IStatus.ERROR) {
                return Messages.NewFolderAction_InvalidFoldername;
            }
            IFolder folder = getFolder(parent, newText);
            if (folder != null) {
                if (folder.exists()) {
                    return NLS
                            .bind(Messages.NewFolderAction_msgFolderAllreadyExists, folder.getFullPath().toOSString());
                }
            }
            return null;
        }
    }

}
