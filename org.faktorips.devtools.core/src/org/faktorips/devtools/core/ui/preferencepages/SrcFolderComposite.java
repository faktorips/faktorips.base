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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
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
    
    
    /**
     * @param parent
     * @param style
     */
    public SrcFolderComposite(Composite parent) {
        super(parent, SWT.NONE);
        
        this.toolkit = new UIToolkit(null);

        this.setLayout(new GridLayout(1, true));

        Composite tableWithButtons = toolkit.createGridComposite(this, 2, false, true);
        tableWithButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        IpsSrcFolderAdapter srcFolderAdapter = new IpsSrcFolderAdapter();
        
        Label treeViewerLabel = new Label(tableWithButtons, SWT.NONE);
        treeViewerLabel.setText("Ips source folders on the build path:");
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
        
        multipleOutputCheckbutton = new CheckboxField(toolkit.createCheckbox(tableWithButtons, "Allow java source output folders for model folders"));
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        multipleOutputCheckbutton.getCheckbox().setLayoutData(gd);
        multipleOutputCheckbutton.addChangeListener(srcFolderAdapter);
        
        toolkit.createLabel(tableWithButtons, "Default output folder for derived sources:");        
        derivedSrcFolderControl = new FolderSelectionControl(tableWithButtons, toolkit, "Browse");
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        derivedSrcFolderControl.setLayoutData(gd);
        
        toolkit.createLabel(tableWithButtons, "Default output folder for mergable sources:");
        mergableSrcFolderControl = new FolderSelectionControl(tableWithButtons, toolkit, "Browse");
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mergableSrcFolderControl.setLayoutData(gd);
    }

    
    private TreeViewer createViewer(Composite parent, IpsSrcFolderAdapter srcFolderAdapter) {
        tree = new Tree(parent, SWT.BORDER | SWT.MULTI);
        treeViewer = new TreeViewer(tree);
        treeViewer.addSelectionChangedListener(srcFolderAdapter);
        treeViewer.setLabelProvider(new IpsObjectPathLabelProvider());

        return treeViewer;
    }


    private void createButtons(Composite buttons, IpsSrcFolderAdapter srcFolderAdapter) {
        addSrcFolderButton = toolkit.createButton(buttons, "Add Folder");
        addSrcFolderButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        addSrcFolderButton.addSelectionListener(srcFolderAdapter);
        
        removeSrcFolderButton = toolkit.createButton(buttons, "Remove Folder");
        removeSrcFolderButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        removeSrcFolderButton.addSelectionListener(srcFolderAdapter);
        
        editSelectionButton = toolkit.createButton(buttons, "Edit");
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
    }

    private void updateWidgetEnabledStates() {
        boolean multipleOutputFolders = ipsObjectPath.isOutputDefinedPerSrcFolder();
        removeSrcFolderButton.setEnabled(! treeViewer.getSelection().isEmpty());
        
        multipleOutputCheckbutton.getCheckbox().setChecked(multipleOutputFolders);
        updateSpecificOutputFolders(! multipleOutputFolders);
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
                    return;
                }
                IIpsSrcFolderEntry srcFolderEntry = (IIpsSrcFolderEntry) next;
                ipsObjectPath.removeSrcFolderEntry(srcFolderEntry.getSourceFolder());
                treeViewer.remove(srcFolderEntry);
            }
        }
    }

    private void addSrcFolders() {
        MultipleFolderSelectionDialog srcFolderDialog = new MultipleFolderSelectionDialog(getShell());
        try {
            srcFolderDialog.setProject(ipsObjectPath.getIpsProject().getProject());
            srcFolderDialog.setInitialElementSelections(getInitialSelectedSourceFolders());
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
        
        if (srcFolderDialog.open() == Window.OK) {

            // add new selected source folders to IPS object path
            List selectedFolders = srcFolderDialog.getSelectedFolders();
            for (Iterator it = selectedFolders.iterator(); it.hasNext();) {
                
                IFolder folder = (IFolder) it.next();
                if (ipsObjectPath.containsSrcFolderEntry(folder)) {
                    continue;
                }
                IIpsSrcFolderEntry entry = ipsObjectPath.newSourceFolderEntry(folder);
                treeViewer.add(ipsObjectPath, entry);
            }
            
            // remove deselected source folders from IPS object path
            IIpsSrcFolderEntry[] sourceFolderEntries = ipsObjectPath.getSourceFolderEntries();
            for (int i = 0; i < sourceFolderEntries.length; i++) {
                IFolder folder = sourceFolderEntries[i].getSourceFolder();
                if (! selectedFolders.contains(folder)) {
                    ipsObjectPath.removeSrcFolderEntry(folder);
                    treeViewer.remove(sourceFolderEntries[i]);
                }
            }

            try {
                if (ipsObjectPath.validate().containsErrorMsg())
                    throw new CoreException(new IpsStatus(ipsObjectPath.validate().getText()));
                                    
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return;
            }
        }
    }

    // returns a list containing IFolder-objects which are currently used as IPS source folders 
    private List getInitialSelectedSourceFolders() {
        List list = new ArrayList();
        IIpsSrcFolderEntry[] sourceFolderEntries = ipsObjectPath.getSourceFolderEntries();
        for (int i = 0; i < sourceFolderEntries.length; i++) {
            list.add(sourceFolderEntries[i].getSourceFolder());
        }
        return list;
    }


    private void editSelection() {
    }
    
    // enable/disable TextButton controls if multiple output folders are set/cleared
    private void updateSpecificOutputFolders(boolean multipleEnabled) {
        mergableSrcFolderControl.setEnabled(multipleEnabled);
        derivedSrcFolderControl.setEnabled(multipleEnabled);
        
        ipsObjectPath.setOutputDefinedPerSrcFolder(! multipleEnabled);
        treeViewer.refresh();
    }
    
    
        
    private class IpsSrcFolderAdapter implements ISelectionChangedListener, SelectionListener, ValueChangeListener {

        /**
         * {@inheritDoc}
         */
        public void selectionChanged(SelectionChangedEvent event) {
            if (event.getSelection().isEmpty()) {
                removeSrcFolderButton.setEnabled(false);
                editSelectionButton.setEnabled(false);
            } 
            else {
                removeSrcFolderButton.setEnabled(true);
            }
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
        }
    }
}
