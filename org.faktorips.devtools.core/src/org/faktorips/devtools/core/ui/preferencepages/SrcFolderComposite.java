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

import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.FolderSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;

/**
 * Composite for modifying IPS source folders
 * @author Roman Grutza
 */
public class SrcFolderComposite extends Composite {

    private UIToolkit toolkit;
    private TreeViewer treeViewer;
    private Tree tree;
    private IIpsObjectPath ipsObjectPath;
    private Button addSrcFolderButton;
    private Button removeSrcFolderButton;
    private Button editSelectionButton;

    private FolderSelectionControl derivedSrcFolderControl;
    private FolderSelectionControl mergableSrcFolderControl;
    
    private IpsPckFragmentRootRefControl basePackageDerivedControl;
    private IpsPckFragmentRootRefControl basePackageMergableControl;
    
    // separate output folders for model folders
    private CheckboxField multipleOutputCheckbutton;
    private boolean dataChanged = false;


    /**
     * @param parent Composite
     */
    public SrcFolderComposite(Composite parent) {
        super(parent, SWT.NONE);
        
        this.toolkit = new UIToolkit(null);

        this.setLayout(new GridLayout(1, true));

        Composite tableWithButtons = toolkit.createGridComposite(this, 2, false, true);
        tableWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        IpsSrcFolderAdapter srcFolderAdapter = new IpsSrcFolderAdapter();
        
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
        
        multipleOutputCheckbutton = new CheckboxField(toolkit.createCheckbox(tableWithButtons, Messages.SrcFolderComposite_multipleFolders_checkbox_label));
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        multipleOutputCheckbutton.getCheckbox().setLayoutData(gd);
        multipleOutputCheckbutton.addChangeListener(srcFolderAdapter);
        
        toolkit.createLabel(tableWithButtons, Messages.SrcFolderComposite_derived_sources_label);        
        derivedSrcFolderControl = new FolderSelectionControl(tableWithButtons, toolkit, Messages.SrcFolderComposite_browse_button_text);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        derivedSrcFolderControl.setLayoutData(gd);
        
        toolkit.createLabel(tableWithButtons, Messages.SrcFolderComposite_mergable_sources_label);
        mergableSrcFolderControl = new FolderSelectionControl(tableWithButtons, toolkit, Messages.SrcFolderComposite_browse_button_text);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mergableSrcFolderControl.setLayoutData(gd);
        
        toolkit.createLabel(tableWithButtons, "Base package derived:");
        basePackageDerivedControl = new IpsPckFragmentRootRefControl(tableWithButtons, true, toolkit);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        basePackageDerivedControl.setLayoutData(gd);
        
        toolkit.createLabel(tableWithButtons, "Base package mergable:");
        basePackageMergableControl = new IpsPckFragmentRootRefControl(tableWithButtons, true, toolkit);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        basePackageMergableControl.setLayoutData(gd);
    }

    
    private TreeViewer createViewer(Composite parent, IpsSrcFolderAdapter srcFolderAdapter) {
        tree = new Tree(parent, SWT.BORDER | SWT.SINGLE);
        treeViewer = new TreeViewer(tree);
        treeViewer.addSelectionChangedListener(srcFolderAdapter);
        treeViewer.setLabelProvider(new DecoratingLabelProvider(
                new IpsObjectPathLabelProvider(),  
                IpsPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()
        ));
        
        return treeViewer;
    }


    private void createButtons(Composite buttons, IpsSrcFolderAdapter srcFolderAdapter) {
        addSrcFolderButton = toolkit.createButton(buttons, Messages.SrcFolderComposite_add_folder_text);
        addSrcFolderButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        addSrcFolderButton.addSelectionListener(srcFolderAdapter);
        
        removeSrcFolderButton = toolkit.createButton(buttons, Messages.SrcFolderComposite_remove_folder_text);
        removeSrcFolderButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        removeSrcFolderButton.addSelectionListener(srcFolderAdapter);
        
        editSelectionButton = toolkit.createButton(buttons, Messages.SrcFolderComposite_edit_item_tet);
        editSelectionButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        editSelectionButton.addSelectionListener(srcFolderAdapter);
    }

    /**
     * Initializes the composite with the given Ips object path
     * @param ipsObjectPath, must not be null
     */
    public void init(IIpsObjectPath ipsObjectPath) {
        this.ipsObjectPath = ipsObjectPath;

        treeViewer.setContentProvider(new IpsObjectPathContentProvider());
        treeViewer.addFilter(new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IIpsSrcFolderEntry || parentElement instanceof IIpsSrcFolderEntry)
                    return true;
                return false;
            }});

        treeViewer.setInput(ipsObjectPath);

        derivedSrcFolderControl.setRoot(ipsObjectPath.getIpsProject().getProject());
        derivedSrcFolderControl.setFolder(ipsObjectPath.getOutputFolderForDerivedSources());

        mergableSrcFolderControl.setRoot(ipsObjectPath.getIpsProject().getProject());
        mergableSrcFolderControl.setFolder(ipsObjectPath.getOutputFolderForMergableSources());
        
        basePackageDerivedControl.setPdPckFragmentRoot(ipsObjectPath.getIpsProject().getIpsPackageFragmentRoot(
                ipsObjectPath.getBasePackageNameForDerivedJavaClasses()));

        basePackageMergableControl.setPdPckFragmentRoot(ipsObjectPath.getIpsProject().getIpsPackageFragmentRoot(
                ipsObjectPath.getBasePackageNameForMergableJavaClasses()));
        
        updateWidgetEnabledStates();
        treeViewer.refresh(false);
    }

    /**
     * IPS source path entries have been modified
     * @return true if source path entries have been modified, false otherwise
     */
    public final boolean isDataChanged() {
        return dataChanged;
    }

    private void updateWidgetEnabledStates() {
        
        boolean multipleOutputFolders = (ipsObjectPath != null) && ipsObjectPath.isOutputDefinedPerSrcFolder();
        
        multipleOutputCheckbutton.getCheckbox().setChecked(multipleOutputFolders);
        
        mergableSrcFolderControl.setEnabled(! multipleOutputFolders);
        derivedSrcFolderControl.setEnabled(! multipleOutputFolders);
        basePackageDerivedControl.setEnabled(! multipleOutputFolders);
        basePackageMergableControl.setEnabled(! multipleOutputFolders);
        
        
        if (treeViewer.getSelection().isEmpty()) {
            removeSrcFolderButton.setEnabled(false);
            editSelectionButton.setEnabled(false);

            return;
        }
        
        Object selectedElement = treeViewer.getTree().getSelection()[0].getData();
        if (selectedElement instanceof IIpsSrcFolderEntry) {
            removeSrcFolderButton.setEnabled(true);
            editSelectionButton.setEnabled(false);
        }
        
        if (selectedElement instanceof IIpsObjectPathEntryAttribute) {
            removeSrcFolderButton.setEnabled(false);
            editSelectionButton.setEnabled(true);
        }
    }

    private void removeSrcFolders() {
        ISelection selection = treeViewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        if (selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            for (Iterator it = treeSelection.iterator(); it.hasNext();) {
                Object next = it.next();
                if (! (next instanceof IIpsSrcFolderEntry)) {
                    continue;
                }
                IIpsSrcFolderEntry srcFolderEntry = (IIpsSrcFolderEntry) next;
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
            for (int i = 0; i < selectedFolders.length; i++) {
                
                IFolder folder = (IFolder) selectedFolders[i];
                ipsObjectPath.newSourceFolderEntry(folder);
                treeViewer.refresh(false);
                
                dataChanged = true;
            }
        }
    }

    // enable/disable output folder TextButton controls if multiple output folders are set/cleared
    private void updateSpecificOutputFolders(boolean multipleEnabled) {
        mergableSrcFolderControl.setEnabled(multipleEnabled);
        derivedSrcFolderControl.setEnabled(multipleEnabled);
    }
    
    // enable/disable package name TextButton controls if multiple output folders are set/cleared
    private void updateSpecificPackageNames(boolean multipleEnabled) {
        basePackageDerivedControl.setEnabled(multipleEnabled);
        basePackageMergableControl.setEnabled(multipleEnabled);
    }
    
    
    private void editSelection() {
        ISelection selection = treeViewer.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        
        if (selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            Object selectedElement = treeSelection.getFirstElement();
            
            TreeItem[] treeItems = treeViewer.getTree().getSelection();
            IIpsSrcFolderEntry srcFolderEntry = (IIpsSrcFolderEntry) treeItems[0].getParentItem().getData();

            if (selectedElement instanceof IpsObjectPathEntryAttribute) {
                IIpsObjectPathEntryAttribute attribute = (IIpsObjectPathEntryAttribute) selectedElement;
                                
                if (attribute.isFolderForDerivedSources() || attribute.isFolderForMergableSources()) {
                    
                    OutputFolderEditDialog editDialog = new OutputFolderEditDialog(getShell(), srcFolderEntry, attribute);
                    if (editDialog.open() == Window.OK) {
                        IContainer newOutputFolder = editDialog.getSelectedFolder();
                        if (attribute.isFolderForDerivedSources()) {
                            
                            IFolder defaultOutputFolderForDerivedSources = ipsObjectPath.getOutputFolderForDerivedSources();
                            if (defaultOutputFolderForDerivedSources.equals(newOutputFolder)) {
                                srcFolderEntry.setSpecificOutputFolderForDerivedJavaFiles(null);
                            } else {
                                srcFolderEntry.setSpecificOutputFolderForDerivedJavaFiles( (IFolder) newOutputFolder.getAdapter(IFolder.class));
                            }
                        } else {
                            IFolder defaultOutpuFolderForMergableSources = ipsObjectPath.getOutputFolderForMergableSources();
                            if (defaultOutpuFolderForMergableSources.equals(newOutputFolder)) {
                                srcFolderEntry.setSpecificOutputFolderForMergableJavaFiles(null);
                            } else
                                srcFolderEntry.setSpecificOutputFolderForMergableJavaFiles( (IFolder) newOutputFolder.getAdapter(IFolder.class));
                        }
                        dataChanged = true;
                    }
                } else if (attribute.isPackageNameForDerivedSources()) {
                    
                    PackageNameEditDialog editDialog = new PackageNameEditDialog(getShell(), srcFolderEntry, attribute);
                    if (editDialog.open() == Window.OK) {
                        String newPackageName = editDialog.getPackageName();
                        String defaultName = ipsObjectPath.getBasePackageNameForDerivedJavaClasses();
                        if (newPackageName.equals(defaultName)) {
                            srcFolderEntry.setSpecificBasePackageNameForDerivedJavaClasses("");
                        } else {
                            srcFolderEntry.setSpecificBasePackageNameForDerivedJavaClasses(newPackageName);
                        }
                        dataChanged = true;                        
                    }
                }  else if (attribute.isPackageNameForMergableSources()) {
                    PackageNameEditDialog editDialog = new PackageNameEditDialog(getShell(), srcFolderEntry, attribute);
                    if (editDialog.open() == Window.OK) {
                        String newPackageName = editDialog.getPackageName();
                        String defaultName = ipsObjectPath.getBasePackageNameForMergableJavaClasses();
                        if (newPackageName.equals(defaultName)) {
                            srcFolderEntry.setSpecificBasePackageNameForMergableJavaClasses("");
                        } else {
                            srcFolderEntry.setSpecificBasePackageNameForMergableJavaClasses(newPackageName);
                        }
                        dataChanged = true;                        
                    }                    
                }
                else if (attribute.isTocPath()) {
                    
                    String tocPath = srcFolderEntry.getBasePackageRelativeTocPath();
                    IInputValidator inputValidator = new IInputValidator() {

                        public String isValid(String newText) {
                            if (newText == null || newText.length() < 1) {
                                return "File name invalid";
                            }
                            return null;
                        }};
                    InputDialog newTocPathDialog = new InputDialog(getShell(), "Toc Path Configuration", "Enter name for table of contents file:", tocPath, inputValidator);
                    
                    if (newTocPathDialog.open() == Window.OK) {
                        srcFolderEntry.setBasePackageRelativeTocPath(newTocPathDialog.getValue());
                        dataChanged = true;                        
                    }
                }
            }
        }
    }
        
    private ElementTreeSelectionDialog createSelectFolderDialog() {
        final IProject project = ipsObjectPath.getIpsProject().getProject();

        // According to the validation rules for IpsSrcFolderEntry objects, a source (model) folder must be a
        // direct child of the project root directory. Thus the generic WorkbenchContentProvider is modified 
        // to restrict the depth of the shown directory tree to one.

        WorkbenchContentProvider workbenchContentProvider = new WorkbenchContentProvider() {
            public boolean hasChildren(Object element) {
                // depth of one -> only root has children
                return element.equals(project);
            }
        };

        ViewerFilter foldersOnlyFilter = new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IFolder) {
                    return (! ipsObjectPath.containsSrcFolderEntry((IFolder) element));
                }
                return false;
            }
        };

        final ElementTreeSelectionDialog selectFolderDialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), workbenchContentProvider) {

            protected Control createDialogArea(Composite parent) {
                Composite composite = (Composite) super.createDialogArea(parent);

                Button newSrcFolderButton = new Button(composite, SWT.NONE);
                newSrcFolderButton.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent event) {
                        
                        IInputValidator validator = new IInputValidator() {

                            public String isValid(String folderName) {
                                if (folderName == null || folderName.length() == 0)
                                    return Messages.SrcFolderComposite_enterFolderName_validator;
                                if (ipsObjectPath.getIpsProject().getProject().getFolder(folderName).exists())
                                    return Messages.SrcFolderComposite_folder_already_exists_validator;
                                return null;
                            }
                        };
                        
                        InputDialog dialog = new InputDialog(getShell(), 
                                Messages.SrcFolderComposite_create_new_folder_title, 
                                Messages.SrcFolderComposite_create_new_folder_message, 
                                Messages.SrcFolderComposite_create_new_folder_defaultText, 
                                validator);
                        
                        if (dialog.open() == Window.OK) {
                            IFolder newFolder = ipsObjectPath.getIpsProject().getProject().getFolder(dialog.getValue());
                            try {

                                newFolder.create(true, false, null);

                            } catch (CoreException e) {
                                IpsPlugin.logAndShowErrorDialog(e);
                            }
                        }
                        
                    }
                });
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
    
        
    private class IpsSrcFolderAdapter implements ISelectionChangedListener, SelectionListener, ValueChangeListener {

        /**
         * {@inheritDoc}
         */
        public void selectionChanged(SelectionChangedEvent event) {
            updateWidgetEnabledStates();
        }

        /**
         * {@inheritDoc}
         */
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

        /**
         * {@inheritDoc}
         */
        public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do */ }

        /**
         * {@inheritDoc}
         */
        public void valueChanged(FieldValueChangedEvent e) {
            boolean multipleEnabled = multipleOutputCheckbutton.getCheckbox().isChecked();
            updateSpecificOutputFolders(! multipleEnabled);
            updateSpecificPackageNames(! multipleEnabled);
            
            ipsObjectPath.setOutputDefinedPerSrcFolder(multipleEnabled);
            treeViewer.refresh();
            dataChanged = true;            
        }

    }
}
