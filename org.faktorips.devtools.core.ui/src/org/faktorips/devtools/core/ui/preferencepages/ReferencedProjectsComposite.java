/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;

/**
 * Composite for modifying referenced IPS projects
 * 
 * @author Roman Grutza
 */
public class ReferencedProjectsComposite extends DataChangeableComposite {

    private UIToolkit toolkit;
    private Table table;

    private IIpsObjectPath ipsObjectPath;

    private Button addButton;
    private Button removeButton;
    private TableViewer tableViewer;

    /** flag to indicate changes in referenced IPS projects */
    private boolean dataChanged = false;

    public ReferencedProjectsComposite(Composite parent) {
        super(parent, SWT.NONE);
        toolkit = new UIToolkit(null);

        setLayout(new GridLayout(1, true));

        Composite tableWithButtons = toolkit.createGridComposite(this, 2, false, true);
        tableWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        IpsProjectsAdapter projectAdapter = new IpsProjectsAdapter();

        Label tableViewerLabel = new Label(tableWithButtons, SWT.NONE);
        tableViewerLabel.setText(Messages.ReferencedProjectsComposite_required_projects_label);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        tableViewerLabel.setLayoutData(gd);

        tableViewer = createViewer(tableWithButtons, projectAdapter);
        tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttons = toolkit.createComposite(tableWithButtons);
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

    private TableViewer createViewer(Composite parent, IpsProjectsAdapter projectAdapter) {
        table = new Table(parent, SWT.BORDER | SWT.MULTI);
        tableViewer = new TableViewer(table);
        tableViewer.addSelectionChangedListener(projectAdapter);

        IpsObjectPathContentProvider contentProvider = new IpsObjectPathContentProvider();
        contentProvider.setIncludedClasses(IIpsProjectRefEntry.class);
        tableViewer.setContentProvider(contentProvider);

        tableViewer.setLabelProvider(new DecoratingLabelProvider(new IpsObjectPathLabelProvider(), IpsPlugin
                .getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));

        return tableViewer;
    }

    /**
     * Initializes the composite with the given Ips object path
     */
    public void init(final IIpsObjectPath ipsObjectPath) {
        this.ipsObjectPath = ipsObjectPath;
        dataChanged = false;

        tableViewer.setInput(ipsObjectPath);

        if (Display.getCurrent() != null) {
            tableViewer.refresh();
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

    /**
     * Referenced IPS projects for the current IPS projects have been modified
     * 
     * @return true if current project's project-reference list has been modified, false otherwise
     */
    public final boolean isDataChanged() {
        return dataChanged;
    }

    /** add new project references to current IPS project, based on items selected in dialog */
    private void addIpsProjects() {
        IIpsProject[] ipsProjects = null;
        ipsProjects = getSelectableIpsProjects();

        ListSelectionDialog dialog = new ListSelectionDialog(null, ipsProjects, new ArrayContentProvider(),
                new TableLabelProvider(), Messages.ReferencedProjectsComposite_select_projects_label);

        dialog.setTitle(Messages.ReferencedProjectsComposite_select_projects_title);
        dialog.setInitialSelections((Object[])ipsObjectPath.getProjectRefEntries());
        if (dialog.open() == Window.OK) {
            Object[] selectedReferencedProjects = dialog.getResult();
            if (selectedReferencedProjects.length > 0) {
                for (Object selectedReferencedProject : selectedReferencedProjects) {

                    ipsObjectPath.newIpsProjectRefEntry((IIpsProject)selectedReferencedProject);
                    tableViewer.refresh(false);
                }
                dataChanged = true;
            }
        }
    }

    /** remove selected list item(s) from model */
    private void removeIpsProjects() {
        IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
        if (selection.size() > 0) {
            dataChanged = true;
            for (Iterator<?> it = selection.iterator(); it.hasNext();) {
                IIpsProjectRefEntry refEntry = (IIpsProjectRefEntry)it.next();
                ipsObjectPath.removeProjectRefEntry((refEntry).getReferencedIpsProject());
                tableViewer.refresh(false);
            }
        }
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        table.setEnabled(changeable);
    }

    /**
     * Get open IPS projects from the current workspace. Skip already referenced projects and the
     * current project.
     */
    private IIpsProject[] getSelectableIpsProjects() {
        ArrayList<IIpsProject> resultList = new ArrayList<IIpsProject>();
        IIpsProjectRefEntry[] projectRefEntries = ipsObjectPath.getProjectRefEntries();
        ArrayList<IIpsProject> references = new ArrayList<IIpsProject>();
        for (IIpsProjectRefEntry projectRefEntrie : projectRefEntries) {
            references.add(projectRefEntrie.getReferencedIpsProject());
        }
        IIpsProject[] ipsProjects = IIpsModel.get().getIpsProjects();
        IIpsProject currentIpsProject = ipsObjectPath.getIpsProject();

        for (IIpsProject ipsProject : ipsProjects) {
            if (ipsProject.equals(currentIpsProject) || references.contains(ipsProject)) {
                continue;
            }
            resultList.add(ipsProject);
        }

        IIpsProject[] refIpsProject = new IIpsProject[resultList.size()];

        return resultList.toArray(refIpsProject);
    }

    /**
     * Manually update the UI
     */
    public void doUpdateUI() {
        if (Display.getCurrent() != null) {
            tableViewer.refresh();
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

    private static final class TableLabelProvider extends LabelProvider {

        private static final String PROJECT_IMG = "IpsProject.gif"; //$NON-NLS-1$

        @Override
        public Image getImage(Object element) {
            return IpsUIPlugin.getImageHandling().getSharedImage(PROJECT_IMG, true);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof IIpsProjectRefEntry) {
                return ((IIpsProjectRefEntry)element).getReferencedIpsProject().getName();
            }
            if (element instanceof IIpsProject) {
                return ((IIpsProject)element).getName();
            }
            return Messages.ReferencedProjectsComposite_label_provider_invalid_element;
        }
    }

    // widget action handling
    private class IpsProjectsAdapter implements ISelectionChangedListener, SelectionListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (event.getSelection().isEmpty()) {
                removeButton.setEnabled(false);
            } else {
                removeButton.setEnabled(true);
            }
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == addButton) {
                addIpsProjects();
            }
            if (e.getSource() == removeButton) {
                removeIpsProjects();
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // nothing to do
        }
    }

}
