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
        
        boolean multipleOutputFolders = ipsObjectPath.isOutputDefinedPerSrcFolder();
        
        if (treeViewer.getSelection().isEmpty()) {
            addSrcFolderButton.setEnabled(true);
            removeSrcFolderButton.setEnabled(false);
            editSelectionButton.setEnabled(false);
            multipleOutputCheckbutton.getCheckbox().setChecked(multipleOutputFolders);
            mergableSrcFolderControl.setEnabled(multipleOutputFolders);
            derivedSrcFolderControl.setEnabled(multipleOutputFolders);

            return;
        }
        
        Object selectedElement = treeViewer.getTree().getSelection()[0].getData();
        if (selectedElement instanceof IIpsSrcFolderEntry) {
            removeSrcFolderButton.setEnabled(true);
        }
        else {
            removeSrcFolderButton.setEnabled(false);
        }
            
        
        if (selectedElement instanceof IIpsSrcFolderEntryAttribute && 
                ((IIpsSrcFolderEntryAttribute) selectedElement).getValue() instanceof IFolder) {
            editSelectionButton.setEnabled(true);
        }
        else {
            editSelectionButton.setEnabled(false);
        }
        
        multipleOutputCheckbutton.getCheckbox().setChecked(multipleOutputFolders);
        mergableSrcFolderControl.setEnabled(multipleOutputFolders);
        derivedSrcFolderControl.setEnabled(multipleOutputFolders);
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
            }
        }
    }

    // enable/disable TextButton controls if multiple output folders are set/cleared
    private void updateSpecificOutputFolders(boolean multipleEnabled) {
        mergableSrcFolderControl.setEnabled(multipleEnabled);
        derivedSrcFolderControl.setEnabled(multipleEnabled);
        
        ipsObjectPath.setOutputDefinedPerSrcFolder(! multipleEnabled);
    }
    
    private void editSelection() {
        ISelection selection = treeViewer.getSelection();
        if (selection.isEmpty())
            return;
        
        if (selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            Object selectedElement = treeSelection.getFirstElement();
            
            IIpsSrcFolderEntry srcFolderEntry = (IIpsSrcFolderEntry) treeViewer.getTree().getSelection()[0].getParentItem().getData();
                        
            if (selectedElement instanceof IpsSrcFolderEntryAttribute) {
                IIpsSrcFolderEntryAttribute attribute = (IIpsSrcFolderEntryAttribute) selectedElement;
                
                AttributeEditDialog editDialog = null;
                if (attribute.isFolderForDerivedSources()) {
                    editDialog = new AttributeEditDialog(getShell(), 
                            ipsObjectPath.getIpsProject(), 
                            IIpsSrcFolderEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES,
                            srcFolderEntry.getSpecificOutputFolderForDerivedJavaFiles() );    
                }
                else if (attribute.isFolderForMergableSources()) {
                    editDialog = new AttributeEditDialog(getShell(), 
                            ipsObjectPath.getIpsProject(), 
                            IIpsSrcFolderEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES,
                            srcFolderEntry.getSpecificOutputFolderForMergableJavaFiles());
                }
                
                if (editDialog != null && editDialog.open() == Window.OK) {
                    IContainer folder = editDialog.getSelectedFolder();
                    if (folder == null) {
                        return;
                    }
                    
                    if (attribute.isFolderForDerivedSources()) {
                        srcFolderEntry.setSpecificOutputFolderForDerivedJavaFiles( (IFolder) folder.getAdapter(IFolder.class));
                    }
                    else if (attribute.isFolderForMergableSources()) {
                        srcFolderEntry.setSpecificOutputFolderForMergableJavaFiles( (IFolder) folder.getAdapter(IFolder.class));
                    }
                    treeViewer.refresh(srcFolderEntry, false);
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
                                if (folderName == null || folderName.isEmpty())
                                    return Messages.SrcFolderComposite_enterFolderName_validator;
                                if (ipsObjectPath.getIpsProject().getProject().getFolder(folderName).exists())
                                    return Messages.SrcFolderComposite_folder_already_exists_validator;
                                return null;
                            }
                        };
                        
                        InputDialog dialog = new InputDialog(getShell(), Messages.SrcFolderComposite_create_new_folder_title, Messages.SrcFolderComposite_create_new_folder_message, Messages.SrcFolderComposite_create_new_folder_defaultText, validator);
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
            updateSpecificOutputFolders(! multipleOutputCheckbutton.getCheckbox().isChecked());
            treeViewer.refresh();
        }
    }
}
