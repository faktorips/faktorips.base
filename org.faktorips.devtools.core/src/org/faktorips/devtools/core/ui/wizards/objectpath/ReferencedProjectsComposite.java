/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.wizards.objectpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Composite for modifying referenced IPS projects
 * 
 * @author Roman Grutza
 */
public class ReferencedProjectsComposite extends Composite {

    private UIToolkit toolkit;
    private List list;

    private IIpsProject currentIpsProject;

    // model for the list viewer
    private ArrayList referencedIpsProjects;
    private Button addButton;
    private Button removeButton;
    private ListViewer listViewer;
    
    // flag to indicate changes in referenced IPS projects
    private boolean dataChanged = false;

    
    public ReferencedProjectsComposite(Composite parent) {

        super(parent, SWT.NONE);
        this.toolkit = new UIToolkit(null);

        this.setLayout(new GridLayout(1, true));

        Composite listWithButtons = toolkit.createGridComposite(this, 2, false, true);
        IpsProjectsAdapter projectAdapter = new IpsProjectsAdapter();

        Label listViewerLabel = new Label(listWithButtons, SWT.NONE);
        listViewerLabel.setText(Messages.ReferencedProjectsComposite_required_projects_label);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        listViewerLabel.setLayoutData(gd);

        listWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        listViewer = createViewer(listWithButtons, projectAdapter, toolkit);
        listViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttons = toolkit.createComposite(listWithButtons);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);
        createButtons(buttons, projectAdapter, toolkit);
    }

    private void createButtons(Composite buttons, IpsProjectsAdapter projectAdapter, UIToolkit toolkit) {
        addButton = toolkit.createButton(buttons, Messages.ReferencedProjectsComposite_projects_add_button);
        addButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        addButton.addSelectionListener(projectAdapter);

        removeButton = toolkit.createButton(buttons, Messages.ReferencedProjectsComposite_projects_remove_button);
        removeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        removeButton.addSelectionListener(projectAdapter);
        removeButton.setEnabled(false);
    }

    private ListViewer createViewer(Composite parent, IpsProjectsAdapter projectAdapter, UIToolkit toolkit) {
        list = new List(parent, SWT.BORDER | SWT.MULTI);
        listViewer = new ListViewer(list);
        listViewer.addSelectionChangedListener(projectAdapter);
        listViewer.setContentProvider(new ListContentProvider()); // new ArrayContentProvider());
        listViewer.setLabelProvider(new ListLabelProvider());
        
        listViewer.setInput(referencedIpsProjects);

        return listViewer;
    }

    /**
     * Initializes the composite for an existing IPS Project
     * @param ipsProject IPS project to initialize
     * @throws CoreException 
     */
    public void init(final IIpsProject ipsProject) throws CoreException {

        currentIpsProject = ipsProject;

        IIpsProject[] refProjects = ipsProject.getReferencedIpsProjects();
        referencedIpsProjects = new ArrayList();
        listViewer.setInput(referencedIpsProjects);

        for (int i = 0; i < refProjects.length; i++) {
            referencedIpsProjects.add(refProjects[i]);
        }


        if (Display.getCurrent() != null) {
            listViewer.refresh();
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    listViewer.refresh();
                }
            });
        }
    }

    /**
     * Referenced IPS projects for the current IPS projects have been modified
     * @return true if current project's project-reference list has been modified, false otherwise
     */
    public final boolean isDataChanged() {
        return dataChanged;
    }

    /**
     * @return Returns an array of referenced IPS projects. If no projects 
     *  are referenced an array with length 0 is returned
     */
    public final IIpsProject[] getReferencedIpsProjects() {
        IIpsProject[] copy = new IIpsProject[referencedIpsProjects.size()];
        return (IIpsProject[]) referencedIpsProjects.toArray(copy);
    }
    
    // add new project references to current IPS project, based on items selected in dialog
    private void addIpsProjects() {

        IIpsProject[] ipsProjects = null;
        try {
            ipsProjects = getSelectableIpsProjects();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }

        ListSelectionDialog dialog = new ListSelectionDialog(null, ipsProjects, new ArrayContentProvider(),
                new ListLabelProvider(), Messages.ReferencedProjectsComposite_select_projects_label);

        dialog.setTitle(Messages.ReferencedProjectsComposite_select_projects_title);
        dialog.setInitialSelections(referencedIpsProjects.toArray());
        if (dialog.open() == Window.OK) {
            
            Object[] selectedReferencedProjects = dialog.getResult();
            if (selectedReferencedProjects.length > 0) {
                referencedIpsProjects.addAll(Arrays.asList(selectedReferencedProjects));
                dataChanged = true;
            }
        }
        listViewer.refresh();
    }

    // remove selected list item(s) from model
    private void removeIpsProjects() {
        IStructuredSelection selection = (IStructuredSelection)listViewer.getSelection();
        if (selection.size() > 0) {
            dataChanged = true;
            for (Iterator it = selection.iterator(); it.hasNext(); ) {
                referencedIpsProjects.remove(it.next());     
            }
        }        
        listViewer.refresh();
    }

    // Get open IPS projects from the current workspace. Skip already referenced projects and the current project.
    private IIpsProject[] getSelectableIpsProjects() throws CoreException {

        IIpsProject[] ipsProjects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
        ArrayList resultList = new ArrayList();
        
        resultList.addAll(Arrays.asList(ipsProjects));
        resultList.remove(currentIpsProject);
        resultList.removeAll(referencedIpsProjects);
        
        IIpsProject[] refIpsProject = new IIpsProject[resultList.size()];
        
        return (IIpsProject[]) resultList.toArray(refIpsProject);
    }
    
    private static final class ListLabelProvider extends LabelProvider {
        public Image getImage(Object element) {
            return IpsPlugin.getDefault().getImage("IpsProject.gif"); //$NON-NLS-1$
        }
    }

    private static final class ListContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof ArrayList) {
                return ((ArrayList)inputElement).toArray();
            }
            return null;
        }

        public void dispose() { /* nothing to do */ }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { /* nothing to do */ }
    }

    // widget action handling
    private class IpsProjectsAdapter implements ISelectionChangedListener, SelectionListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (event.getSelection().isEmpty()) {
                removeButton.setEnabled(false);
            } else {
                removeButton.setEnabled(true);
            }
        }

        public void widgetSelected(SelectionEvent e) {

            if (e.getSource() == addButton) {
                addIpsProjects();
            }
            if (e.getSource() == removeButton) {
                removeIpsProjects();
            }
        }

        public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do */ }

    }

}
