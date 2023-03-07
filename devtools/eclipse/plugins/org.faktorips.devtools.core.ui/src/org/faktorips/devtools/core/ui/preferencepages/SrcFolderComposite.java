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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.FolderSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;

/**
 * Composite for modifying IPS source folders
 * 
 * @author Roman Grutza
 */
public class SrcFolderComposite extends DataChangeableComposite {

    private UIToolkit toolkit;
    private TreeViewer treeViewer;
    private Tree tree;
    private IIpsObjectPath ipsObjectPath;
    private Button addSrcFolderButton;
    private Button removeSrcFolderButton;
    private Button editSelectionButton;

    private FolderSelectionControl mergableSrcFolderControl;
    private TextButtonField mergableSrcFolderField;

    private FolderSelectionControl derivedSrcFolderControl;
    private TextButtonField derivedSrcFolderField;

    private IpsPckFragmentRefControl basePackageMergableControl;
    private TextButtonField basePackageMergableField;

    private IpsPckFragmentRefControl basePackageDerivedControl;
    private TextButtonField basePackageDerivedField;

    // separate output folders for model folders
    private CheckboxField multipleOutputCheckBoxField;
    private boolean dataChanged = false;
    private IpsSrcFolderAdapter srcFolderAdapter;

    /**
     * @param parent Composite
     */
    public SrcFolderComposite(Composite parent) {
        super(parent, SWT.NONE);

        toolkit = new UIToolkit(null);

        setLayout(new GridLayout(1, true));

        Composite tableWithButtons = toolkit.createGridComposite(this, 2, false, true);
        tableWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        srcFolderAdapter = new IpsSrcFolderAdapter();
        Label treeViewerLabel = new Label(tableWithButtons, SWT.NONE);
        treeViewerLabel.setText(Messages.SrcFolderComposite_treeViewer_label);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        treeViewerLabel.setLayoutData(gd);

        treeViewer = createViewer(tableWithButtons, srcFolderAdapter);
        treeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttons = toolkit.createComposite(tableWithButtons);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        GridLayout buttonLayout = new GridLayout(1, true);
        buttonLayout.horizontalSpacing = 10;
        buttonLayout.marginWidth = 10;
        buttonLayout.marginHeight = 0;
        buttons.setLayout(buttonLayout);
        createButtons(buttons, srcFolderAdapter);

        multipleOutputCheckBoxField = new CheckboxField(toolkit.createCheckbox(tableWithButtons,
                Messages.SrcFolderComposite_multipleFolders_checkbox_label));
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        multipleOutputCheckBoxField.getCheckbox().setLayoutData(gd);

        toolkit.createLabel(tableWithButtons, Messages.SrcFolderComposite_mergable_sources_label);
        mergableSrcFolderControl = new FolderSelectionControl(tableWithButtons, toolkit,
                Messages.SrcFolderComposite_browse_button_text);
        mergableSrcFolderField = new TextButtonField(mergableSrcFolderControl);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mergableSrcFolderControl.setLayoutData(gd);

        toolkit.createLabel(tableWithButtons, Messages.SrcFolderComposite_derived_sources_label);
        derivedSrcFolderControl = new FolderSelectionControl(tableWithButtons, toolkit,
                Messages.SrcFolderComposite_browse_button_text);
        derivedSrcFolderField = new TextButtonField(derivedSrcFolderControl);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        derivedSrcFolderControl.setLayoutData(gd);

        toolkit.createLabel(tableWithButtons, Messages.SrcFolderComposite_mergable_package_label);
        basePackageMergableControl = new IpsPckFragmentRefControl(tableWithButtons, toolkit);
        basePackageMergableField = new TextButtonField(basePackageMergableControl);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        basePackageMergableControl.setLayoutData(gd);

        toolkit.createLabel(tableWithButtons, Messages.SrcFolderComposite_derived_package_label);
        basePackageDerivedControl = new IpsPckFragmentRefControl(tableWithButtons, toolkit);
        basePackageDerivedField = new TextButtonField(basePackageDerivedControl);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        basePackageDerivedControl.setLayoutData(gd);
    }

    private TreeViewer createViewer(Composite parent, IpsSrcFolderAdapter srcFolderAdapter) {
        tree = new Tree(parent, SWT.BORDER | SWT.SINGLE);
        treeViewer = new TreeViewer(tree);
        treeViewer.addSelectionChangedListener(srcFolderAdapter);
        treeViewer.setLabelProvider(new DecoratingLabelProvider(new IpsObjectPathLabelProvider(), IpsPlugin
                .getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));
        return treeViewer;
    }

    private void createButtons(Composite buttons, IpsSrcFolderAdapter srcFolderAdapter) {
        addSrcFolderButton = toolkit.createButton(buttons, Messages.SrcFolderComposite_add_folder_text);
        addSrcFolderButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING));
        addSrcFolderButton.addSelectionListener(srcFolderAdapter);

        removeSrcFolderButton = toolkit.createButton(buttons, Messages.SrcFolderComposite_remove_folder_text);
        removeSrcFolderButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING));
        removeSrcFolderButton.addSelectionListener(srcFolderAdapter);

        editSelectionButton = toolkit.createButton(buttons, Messages.SrcFolderComposite_edit_item_tet);
        editSelectionButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING));
        editSelectionButton.addSelectionListener(srcFolderAdapter);
    }

    /**
     * Initializes the composite with the given Ips object path
     */
    public void init(IIpsObjectPath ipsObjectPath) {
        this.ipsObjectPath = ipsObjectPath;

        IpsObjectPathContentProvider contentProvider = new IpsObjectPathContentProvider();
        contentProvider.setIncludedClasses(IIpsSrcFolderEntry.class);
        treeViewer.setContentProvider(contentProvider);

        treeViewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IIpsSrcFolderEntry || parentElement instanceof IIpsSrcFolderEntry) {
                    return true;
                }
                return false;
            }
        });

        treeViewer.setInput(ipsObjectPath);

        derivedSrcFolderControl.setRoot(ipsObjectPath.getIpsProject().getProject().unwrap());
        derivedSrcFolderControl.setFolder(ipsObjectPath.getOutputFolderForDerivedSources().unwrap());

        mergableSrcFolderControl.setRoot(ipsObjectPath.getIpsProject().getProject().unwrap());
        mergableSrcFolderControl.setFolder(ipsObjectPath.getOutputFolderForMergableSources().unwrap());

        IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsObjectPath.getIpsProject().getIpsPackageFragmentRoots();
        if (ipsPackageFragmentRoots.length > 0) {

            IIpsPackageFragmentRoot ipsPackageFragmentRoot = ipsPackageFragmentRoots[0];

            basePackageMergableControl.setIpsPckFragmentRoot(ipsPackageFragmentRoot);
            IIpsPackageFragment ipsPackageFragmentMergable = ipsPackageFragmentRoot.getIpsPackageFragment(ipsObjectPath
                    .getBasePackageNameForMergableJavaClasses());
            basePackageMergableControl.setIpsPackageFragment(ipsPackageFragmentMergable);

            basePackageDerivedControl.setIpsPckFragmentRoot(ipsPackageFragmentRoot);
            IIpsPackageFragment ipsPackageFragmentDerived = ipsPackageFragmentRoot.getIpsPackageFragment(ipsObjectPath
                    .getBasePackageNameForDerivedJavaClasses());
            basePackageDerivedControl.setIpsPackageFragment(ipsPackageFragmentDerived);
        }

        multipleOutputCheckBoxField.addChangeListener(srcFolderAdapter);
        mergableSrcFolderField.addChangeListener(srcFolderAdapter);
        derivedSrcFolderField.addChangeListener(srcFolderAdapter);
        basePackageMergableField.addChangeListener(srcFolderAdapter);
        basePackageDerivedField.addChangeListener(srcFolderAdapter);

        dataChanged = false;

        updateWidgetEnabledStates();
        treeViewer.refresh(false);
    }

    /**
     * IPS source path entries have been modified
     * 
     * @return true if source path entries have been modified, false otherwise
     */
    public final boolean isDataChanged() {
        return dataChanged;
    }

    private void updateWidgetEnabledStates() {
        boolean multipleOutputFolders = (ipsObjectPath != null) && ipsObjectPath.isOutputDefinedPerSrcFolder();
        multipleOutputCheckBoxField.getCheckbox().setChecked(multipleOutputFolders);

        if (treeViewer.getSelection().isEmpty()) {
            removeSrcFolderButton.setEnabled(false);
            editSelectionButton.setEnabled(false);

            return;
        }

        TreeItem[] selection = treeViewer.getTree().getSelection();
        if (selection.length > 0) {
            Object selectedElement = selection[0].getData();
            if (selectedElement instanceof IIpsSrcFolderEntry) {
                removeSrcFolderButton.setEnabled(true);
                editSelectionButton.setEnabled(false);
            }

            if (selectedElement instanceof IIpsObjectPathEntryAttribute) {
                removeSrcFolderButton.setEnabled(false);
                editSelectionButton.setEnabled(true);
            }
        }
    }

    private void removeSrcFolders() {
        ISelection selection = treeViewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        if (selection instanceof ITreeSelection treeSelection) {
            for (Object next : treeSelection) {
                if (!(next instanceof IIpsSrcFolderEntry srcFolderEntry)) {
                    continue;
                }
                ipsObjectPath.removeSrcFolderEntry(srcFolderEntry.getSourceFolder());
                dataChanged = true;

            }
        }
        treeViewer.refresh(false);
    }

    private void addSrcFolders() {
        final ElementTreeSelectionDialog selectFolderDialog = createSelectFolderDialog();
        if (selectFolderDialog.open() == Window.OK) {
            // add new selected source folders to IPS object path
            Object[] selectedFolders = selectFolderDialog.getResult();
            for (Object selectedFolder : selectedFolders) {
                IFolder folder = (IFolder)selectedFolder;
                ipsObjectPath.newSourceFolderEntry(Wrappers.wrap(folder).as(AFolder.class));
                treeViewer.refresh(false);

                dataChanged = true;
            }
        }
    }

    private void editSelection() {
        TypedSelection<IIpsObjectPathEntryAttribute> typedSelection = new TypedSelection<>(
                IIpsObjectPathEntryAttribute.class, treeViewer.getSelection());

        if (!typedSelection.isValid()) {
            return;
        }

        TreeItem[] treeItems = treeViewer.getTree().getSelection();
        IIpsSrcFolderEntry srcFolderEntry = (IIpsSrcFolderEntry)treeItems[0].getParentItem().getData();

        IIpsObjectPathEntryAttribute attribute = typedSelection.getElement();

        if (attribute.isFolderForDerivedSources() || attribute.isFolderForMergableSources()) {
            editSelectionFolder(srcFolderEntry, attribute);
        } else if (attribute.isPackageNameForDerivedSources()) {
            editSelectionPackageDerived(srcFolderEntry, attribute);
        } else if (attribute.isPackageNameForMergableSources()) {
            editSelectionPackageMergable(srcFolderEntry, attribute);
        } else if (attribute.isTocPath()) {
            editSelectionTocPath(srcFolderEntry);
        }
        treeViewer.refresh();
    }

    private void editSelectionFolder(IIpsSrcFolderEntry srcFolderEntry, IIpsObjectPathEntryAttribute attribute) {
        OutputFolderEditDialog editDialog = new OutputFolderEditDialog(getShell(), srcFolderEntry, attribute);
        if (editDialog.open() == Window.OK) {
            IContainer newOutputFolder = editDialog.getSelectedFolder();
            if (attribute.isFolderForDerivedSources()) {
                srcFolderEntry.setSpecificOutputFolderForDerivedJavaFiles(
                        Wrappers.wrap(newOutputFolder.getAdapter(IFolder.class)).as(AFolder.class));
            } else {
                srcFolderEntry.setSpecificOutputFolderForMergableJavaFiles(
                        Wrappers.wrap(newOutputFolder.getAdapter(IFolder.class)).as(AFolder.class));
            }
            dataChanged = true;
        }
    }

    private void editSelectionPackageDerived(IIpsSrcFolderEntry srcFolderEntry,
            IIpsObjectPathEntryAttribute attribute) {
        PackageNameEditDialog editDialog = new PackageNameEditDialog(getShell(), srcFolderEntry, attribute);
        if (editDialog.open() == Window.OK) {
            String newPackageName = editDialog.getPackageName();
            srcFolderEntry.setSpecificBasePackageNameForDerivedJavaClasses(newPackageName);
            dataChanged = true;
        }
    }

    private void editSelectionPackageMergable(IIpsSrcFolderEntry srcFolderEntry,
            IIpsObjectPathEntryAttribute attribute) {
        PackageNameEditDialog editDialog = new PackageNameEditDialog(getShell(), srcFolderEntry, attribute);
        if (editDialog.open() == Window.OK) {
            String newPackageName = editDialog.getPackageName();
            srcFolderEntry.setSpecificBasePackageNameForMergableJavaClasses(newPackageName);
            dataChanged = true;
        }
    }

    private void editSelectionTocPath(IIpsSrcFolderEntry srcFolderEntry) {
        String tocPath = srcFolderEntry.getBasePackageRelativeTocPath();
        IInputValidator inputValidator = newText -> {
            if (newText == null || newText.length() < 1) {
                return Messages.SrcFolderComposite_filename_invalid_validator;
            }
            return null;
        };
        InputDialog newTocPathDialog = new InputDialog(getShell(), Messages.SrcFolderComposite_tocpath_title,
                Messages.SrcFolderComposite_tocpath_message, tocPath, inputValidator);

        if (newTocPathDialog.open() == Window.OK) {
            srcFolderEntry.setBasePackageRelativeTocPath(newTocPathDialog.getValue());
            dataChanged = true;
        }
    }

    private ElementTreeSelectionDialog createSelectFolderDialog() {
        final IProject project = ipsObjectPath.getIpsProject().getProject().unwrap();

        /*
         * According to the validation rules for IpsSrcFolderEntry objects, a source (model) folder
         * must be a direct child of the project root directory. Thus the generic
         * WorkbenchContentProvider is modified to restrict the depth of the shown directory tree to
         * one.
         */
        WorkbenchContentProvider workbenchContentProvider = new WorkbenchContentProvider() {
            @Override
            public boolean hasChildren(Object element) {
                // depth of one -> only root has children
                return element.equals(project);
            }
        };

        ViewerFilter foldersOnlyFilter = new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IFolder) {
                    return (!ipsObjectPath.containsSrcFolderEntry(Wrappers.wrap(element).as(AFolder.class)));
                }
                return false;
            }
        };

        final ElementTreeSelectionDialog selectFolderDialog = new ElementTreeSelectionDialog(getShell(),
                new WorkbenchLabelProvider(), workbenchContentProvider) {

            @Override
            protected Control createDialogArea(Composite parent) {
                Composite composite = (Composite)super.createDialogArea(parent);

                Button newSrcFolderButton = new Button(composite, SWT.NONE);
                newSrcFolderButton.addSelectionListener(new NewButtonSelectionListener(ipsObjectPath, getShell()));
                newSrcFolderButton.setText(Messages.SrcFolderComposite_create_new_folder_title);

                return composite;
            }
        };

        selectFolderDialog.setAllowMultiple(true);
        selectFolderDialog.setTitle(Messages.SrcFolderComposite_folderSelection_dialog_title);
        selectFolderDialog.setMessage(Messages.SrcFolderComposite_folderSelection_dialog_message);
        selectFolderDialog.setHelpAvailable(false);
        selectFolderDialog.addFilter(foldersOnlyFilter);
        selectFolderDialog.setInput(project);
        return selectFolderDialog;
    }

    /**
     * Manually update the UI
     */
    public void doUpdateUI() {
        if (Display.getCurrent() != null) {
            treeViewer.setInput(ipsObjectPath);
        } else {
            Display.getDefault().asyncExec(() -> treeViewer.setInput(ipsObjectPath));
        }
    }

    /**
     * Sets the default output folder for mergable/derived sources
     * 
     * @param mergable if true the mergable folder is changed, derived folder otherwise
     */
    private void handleDefaultOutputFolderChanged(boolean mergable) {
        String folderName;
        if (mergable) {
            folderName = mergableSrcFolderField.getValue();
        } else {
            folderName = derivedSrcFolderField.getValue();
        }
        if (folderName == null || "".equals(folderName)) { //$NON-NLS-1$
            return;
        }

        /*
         * Folder field text can temporarily contain invalid values, because it is updated on each
         * keystroke when typed in with keyboard. That's why exceptions are ignored here.
         */
        IPath path = new Path(IPath.SEPARATOR + folderName);
        IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
        if (mergable) {
            ipsObjectPath.setOutputFolderForMergableSources(Wrappers.wrap(folder).as(AFolder.class));
        } else {
            ipsObjectPath.setOutputFolderForDerivedSources(Wrappers.wrap(folder).as(AFolder.class));
        }

        dataChanged = true;
    }

    private void handleOutputDefinedPerSrcFolderChanged(boolean multipleEnabled) {
        ipsObjectPath.setOutputDefinedPerSrcFolder(multipleEnabled);
        treeViewer.refresh();

        dataChanged = true;
    }

    /**
     * Sets the default package name for mergable/derived sources
     * 
     * @param mergable if true the mergable package name is changed, otherwise the derived package
     *            name
     */
    private void handleBasePackageNameChanged(boolean mergable) {
        if (mergable) {
            String packageName = basePackageMergableField.getText();
            ipsObjectPath.setBasePackageNameForMergableJavaClasses(packageName);
        } else {
            String packageName = basePackageDerivedField.getText();
            ipsObjectPath.setBasePackageNameForDerivedJavaClasses(packageName);
        }

        dataChanged = true;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        super.setDataChangeable(changeable);
        tree.setEnabled(changeable);
    }

    private static class NewButtonSelectionListener extends SelectionAdapter {

        private final IIpsObjectPath ipsObjectPath;
        private final Shell shell;

        public NewButtonSelectionListener(IIpsObjectPath ipsObjectPath, Shell shell) {
            this.ipsObjectPath = ipsObjectPath;
            this.shell = shell;
        }

        @Override
        public void widgetSelected(SelectionEvent event) {
            IInputValidator validator = folderName -> {
                if (folderName == null || folderName.length() == 0) {
                    return Messages.SrcFolderComposite_enterFolderName_validator;
                }
                if (ipsObjectPath.getIpsProject().getProject().getFolder(folderName).exists()) {
                    return Messages.SrcFolderComposite_folder_already_exists_validator;
                }
                return null;
            };

            InputDialog dialog = new InputDialog(shell, Messages.SrcFolderComposite_create_new_folder_title,
                    Messages.SrcFolderComposite_create_new_folder_message,
                    Messages.SrcFolderComposite_create_new_folder_defaultText, validator);

            if (dialog.open() == Window.OK) {
                IFolder newFolder = ipsObjectPath.getIpsProject().getProject().getFolder(dialog.getValue()).unwrap();
                try {
                    newFolder.create(true, false, null);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }

        }
    }

    private class IpsSrcFolderAdapter implements ISelectionChangedListener, SelectionListener, ValueChangeListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            updateWidgetEnabledStates();
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.getSource() == addSrcFolderButton) {
                addSrcFolders();
            }
            if (e.getSource() == removeSrcFolderButton) {
                removeSrcFolders();
            }
            if (e.getSource() == editSelectionButton) {
                editSelection();
                treeViewer.refresh(false);
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // nothing to do
        }

        @Override
        public void valueChanged(FieldValueChangedEvent e) {
            if (e.field == mergableSrcFolderField) {
                handleDefaultOutputFolderChanged(true);

            } else if (e.field == derivedSrcFolderField) {
                handleDefaultOutputFolderChanged(false);

            } else if (e.field == basePackageMergableField) {

                handleBasePackageNameChanged(true);
            } else if (e.field == basePackageDerivedField) {

                handleBasePackageNameChanged(false);
            } else if (e.field == multipleOutputCheckBoxField) {
                boolean multipleEnabled = (multipleOutputCheckBoxField.getValue()).booleanValue();
                handleOutputDefinedPerSrcFolderChanged(multipleEnabled);
            }
        }

    }

}
