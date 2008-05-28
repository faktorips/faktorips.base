/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A folder selection dialog with the possibility to select more than one folder (using checkboxes).
 * @author Roman Grutza
 */
public class MultipleFolderSelectionDialog extends CheckedTreeSelectionDialog {

    private IProject project;
    private IContainer selectedFolder;

    private List excludedFolders;

    
    /**
     * @param parent
     * @param labelProvider
     * @param contentProvider
     */
    public MultipleFolderSelectionDialog(Shell parent) {
        super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
        setTitle("Select source folder");
        addFilter(new ViewerFilter() {

            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (project == null)
                    return (element instanceof IContainer);
                
                boolean isIncluded = false;
                if (element instanceof IContainer) {
                    IContainer c = (IContainer) element;
                    isIncluded = (c.getProject().equals(project));
                    isIncluded = isIncluded && ! isInExcludedFolders(c);
                }
                return isIncluded;
            }
            
        });
    }

    public MultipleFolderSelectionDialog(Shell parent, IProject project) {
        this(parent);
        this.project = project;
    }

    private boolean isInExcludedFolders(IContainer c) {
        return excludedFolders != null && excludedFolders.contains(c.getFullPath());
    }

    /**
     * Sets the dialogs' selection root to the given project
     * @param project project 
     * @throws CoreException
     */
    public void setProject(IProject project) throws CoreException {
        this.project = project;
        setInput(ResourcesPlugin.getWorkspace().getRoot());
    }

    /**
     * @return a list of currently selected folders or an empty list if none were selected
     */
    public List getSelectedFolders() {
        List list = new ArrayList();
        Object[] selection = getResult();
        if (selection != null) {
            for (int i = 0; i < selection.length; i++) {
                if (selection[i] instanceof IFolder) {
                    list.add(selection[i]);
                }
            }
        }
        return list;
    }
    
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Button newSrcFolderButton = new Button(composite, SWT.NONE);
        newSrcFolderButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                buttonClicked();
            }
        });
        newSrcFolderButton.setText("Create New Folder");
        
        return composite;
    }

    protected void buttonClicked() {
        // TODO: implement, e.g new folder wizard
        MessageDialog.openInformation(getShell(), "IMPL", "to be implemented");
    }

}
