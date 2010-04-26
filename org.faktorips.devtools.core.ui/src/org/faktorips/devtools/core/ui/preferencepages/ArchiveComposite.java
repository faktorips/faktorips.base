/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.preferencepages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Composite for modifying IPS archive references
 * 
 * @author Roman Grutza
 */
public class ArchiveComposite extends Composite {

    private UIToolkit toolkit;
    private TableViewer tableViewer;
    private Button addIpsArchivesButton;
    private Button removeIpsArchivesButton;
    private Button addExternalIpsArchivesButton;
    private Table table;
    private IIpsObjectPath ipsObjectPath;
    private boolean dataChanged = false;

    /**
     * Referenced IPS archives for the current IPS projects have been modified
     * 
     * @return true if current project's archives have been modified, false otherwise
     */
    public final boolean isDataChanged() {
        return dataChanged;
    }

    /**
     * 
     * @param parent Composite
     */
    public ArchiveComposite(Composite parent) {
        super(parent, SWT.NONE);

        toolkit = new UIToolkit(null);

        setLayout(new GridLayout(1, true));

        Composite tableWithButtons = toolkit.createGridComposite(this, 2, false, true);
        tableWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        IpsArchiveAdapter archiveAdapter = new IpsArchiveAdapter();

        Label tableViewerLabel = new Label(tableWithButtons, SWT.NONE);
        tableViewerLabel.setText(Messages.ArchiveComposite_viewer_label);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        tableViewerLabel.setLayoutData(gd);

        tableViewer = createViewer(tableWithButtons, archiveAdapter);
        tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttons = toolkit.createComposite(tableWithButtons);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);
        createButtons(buttons, archiveAdapter);
    }

    private void createButtons(Composite buttons, IpsArchiveAdapter archiveAdapter) {
        addIpsArchivesButton = toolkit.createButton(buttons, Messages.ArchiveComposite_button_add_archive);
        addIpsArchivesButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING));
        addIpsArchivesButton.addSelectionListener(archiveAdapter);

        addExternalIpsArchivesButton = toolkit.createButton(buttons,
                Messages.ArchiveComposite_button_add_external_archive);
        addExternalIpsArchivesButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING));
        addExternalIpsArchivesButton.addSelectionListener(archiveAdapter);

        removeIpsArchivesButton = toolkit.createButton(buttons, Messages.ArchiveComposite_button_remove_archive);
        removeIpsArchivesButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING));
        removeIpsArchivesButton.addSelectionListener(archiveAdapter);
        removeIpsArchivesButton.setEnabled(false);

    }

    private TableViewer createViewer(Composite parent, IpsArchiveAdapter archiveAdapter) {
        table = new Table(parent, SWT.BORDER | SWT.MULTI);
        tableViewer = new TableViewer(table);
        tableViewer.addSelectionChangedListener(archiveAdapter);

        IpsObjectPathContentProvider contentProvider = new IpsObjectPathContentProvider();
        contentProvider.setIncludedClasses(IIpsArchiveEntry.class);
        tableViewer.setContentProvider(contentProvider);

        tableViewer.setLabelProvider(new DecoratingLabelProvider(new IpsObjectPathLabelProvider(), IpsPlugin
                .getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));

        tableViewer.setInput(ipsObjectPath);

        return tableViewer;
    }

    /**
     * Initializes the composite using the given IPS object path
     * 
     * @throws CoreException
     */
    public void init(final IIpsObjectPath ipsObjectPath) {

        this.ipsObjectPath = ipsObjectPath;
        dataChanged = false;

        tableViewer.setInput(ipsObjectPath);

        if (Display.getCurrent() != null) {
            tableViewer.refresh();
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

    private void addIpsArchives() {

        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(null, new WorkbenchLabelProvider(),
                new WorkbenchContentProvider());
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        dialog.setMessage(Messages.ArchiveComposite_dialog_message_add);
        dialog.setTitle(Messages.ArchiveComposite_dialog_title_add);

        List<IPath> alreadyRefArchives = new ArrayList<IPath>();
        IIpsArchiveEntry[] entries = ipsObjectPath.getArchiveEntries();
        for (IIpsArchiveEntry entrie : entries) {
            alreadyRefArchives.add(entrie.getArchivePath());
        }

        dialog.addFilter(new IpsarViewerFilter(alreadyRefArchives, true));

        ISelectionStatusValidator validator = new ISelectionStatusValidator() {
            public IStatus validate(Object[] selection) {
                for (int i = 0; i < selection.length; i++) {
                    if (selection[i] == null || !(selection[i] instanceof IFile)) {
                        return new IpsStatus(IStatus.ERROR, Messages.ArchiveComposite_dialog_warning_select_archive);
                    }
                }
                return new IpsStatus(IStatus.OK, " "); //$NON-NLS-1$
            }
        };
        // prevent user to click OK when a folder is selected, only IPS archives are commitable
        dialog.setValidator(validator);

        try {
            if (dialog.open() == Window.OK) {
                Object[] selectedArchives = dialog.getResult();
                if (selectedArchives.length < 1) {
                    return;
                }

                for (Object selectedArchive : selectedArchives) {
                    IFile archiveFile = (IFile)selectedArchive;
                    IPath archivePath = null;
                    if (ipsObjectPath.getIpsProject().getProject().equals(archiveFile.getProject())) {
                        archivePath = archiveFile.getProjectRelativePath();
                    } else {
                        archivePath = archiveFile.getFullPath();
                    }
                    IIpsArchiveEntry newEntry = ipsObjectPath.newArchiveEntry(archivePath);
                    alreadyRefArchives.add(newEntry.getArchivePath());

                    tableViewer.refresh(false);
                }
                dataChanged = true;
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
    }

    private void addExternalIpsArchives() {
        FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
        fileDialog.setFilterExtensions(new String[] { "*.ipsar", "*.jar", "*.zip", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        String fileName = fileDialog.open();
        if (fileName != null) {
            IPath path = new Path(fileName);
            try {
                ipsObjectPath.newArchiveEntry(path);
                tableViewer.refresh(false);
                dataChanged = true;

            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return;
            }
        }
    }

    private void removeIpsArchives() {
        IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
        if (selection.size() > 0) {
            dataChanged = true;
            for (Iterator<?> it = selection.iterator(); it.hasNext();) {
                IIpsArchiveEntry archiveEntry = (IIpsArchiveEntry)it.next();

                ipsObjectPath.removeArchiveEntry(archiveEntry.getIpsArchive());
            }
            tableViewer.refresh(false);
        }
    }

    private class IpsArchiveAdapter implements SelectionListener, ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (event.getSelection().isEmpty()) {
                removeIpsArchivesButton.setEnabled(false);
            } else {
                removeIpsArchivesButton.setEnabled(true);
            }
        }

        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == addIpsArchivesButton) {
                addIpsArchives();
            }
            if (e.getSource() == addExternalIpsArchivesButton) {
                addExternalIpsArchives();
            }
            if (e.getSource() == removeIpsArchivesButton) {
                removeIpsArchives();
            }
        }

        public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do */
        }
    }

    /**
     * Manually update the UI
     */
    public void doUpdateUI() {
        if (Display.getCurrent() != null) {
            tableViewer.refresh();
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    tableViewer.refresh();
                }
            });
        }
    }

}
