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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
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
import org.faktorips.devtools.abstraction.mapping.PathMapping;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * Composite for modifying IPS archive references
 * 
 * @author Roman Grutza
 */
public class ArchiveComposite extends DataChangeableComposite {

    private UIToolkit toolkit;
    private TableViewer tableViewer;
    private Button addIpsArchivesButton;
    private Button removeIpsArchivesButton;
    private Button addExternalIpsArchivesButton;
    private Table table;
    private IIpsObjectPath ipsObjectPath;
    private boolean dataChanged = false;

    /**
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

    /**
     * Referenced IPS archives for the current IPS projects have been modified
     * 
     * @return true if current project's archives have been modified, false otherwise
     */
    public final boolean isDataChanged() {
        return dataChanged;
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
     */
    public void init(final IIpsObjectPath ipsObjectPath) {
        this.ipsObjectPath = ipsObjectPath;
        dataChanged = false;

        tableViewer.setInput(ipsObjectPath);

        if (Display.getCurrent() != null) {
            tableViewer.refresh();
        } else {
            Display.getDefault().asyncExec(tableViewer::refresh);
        }
    }

    private void addIpsArchives() {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(null, new WorkbenchLabelProvider(),
                new WorkbenchContentProvider());
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        dialog.setMessage(Messages.ArchiveComposite_dialog_message_add);
        dialog.setTitle(Messages.ArchiveComposite_dialog_title_add);

        List<String> alreadyRefArchives = new ArrayList<>();
        IIpsArchiveEntry[] entries = ipsObjectPath.getArchiveEntries();
        for (IIpsArchiveEntry entrie : entries) {
            alreadyRefArchives.add(entrie.getArchiveLocation().toString());
        }

        dialog.addFilter(new IpsarViewerFilter(alreadyRefArchives, true));

        ISelectionStatusValidator validator = selection -> {
            for (Object element : selection) {
                if (element == null || !(element instanceof IFile)) {
                    return new IpsStatus(IStatus.ERROR, Messages.ArchiveComposite_dialog_warning_select_archive);
                }
            }
            return new IpsStatus(IStatus.OK, " "); //$NON-NLS-1$
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
                    IPath archivePath = archiveFile.getFullPath();
                    ipsObjectPath.newArchiveEntry(PathMapping.toJavaPath(archivePath));
                    tableViewer.refresh(false);
                }
                dataChanged = true;
            }
        } catch (CoreRuntimeException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
    }

    private void addExternalIpsArchives() {
        FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
        fileDialog.setFilterExtensions(new String[] { "*.ipsar", "*.jar", "*.zip", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        String fileName = fileDialog.open();
        if (fileName != null) {
            Path path = Path.of(fileName);
            try {
                ipsObjectPath.newArchiveEntry(path);
                tableViewer.refresh(false);
                dataChanged = true;

            } catch (CoreRuntimeException e) {
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

    /**
     * Manually update the UI
     */
    public void doUpdateUI() {
        if (Display.getCurrent() != null) {
            tableViewer.refresh();
        } else {
            Display.getDefault().asyncExec(tableViewer::refresh);
        }
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        table.setEnabled(changeable);
    }

    private class IpsArchiveAdapter implements SelectionListener, ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (event.getSelection().isEmpty()) {
                removeIpsArchivesButton.setEnabled(false);
            } else {
                removeIpsArchivesButton.setEnabled(true);
            }
        }

        @Override
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

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // nothing to do
        }
    }

}
