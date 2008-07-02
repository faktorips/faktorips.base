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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.FolderSelectionControl;

/**
 * Dialog for editing the main attributes of IPS source path entries (in this case outputFolderDerived 
 * and outputFolderMergable).
 * @author Roman Grutza
 */
public class AttributeEditDialog extends StatusDialog {

    
    private IIpsProject ipsProject;
    private FolderSelectionControl folderSelectionControl;
    private Button buttonDefaultFolderSelected;
    private Button buttonCustomFolderSelected;
    private String type;
    
    private boolean customFolderSelected = false;    

    /**
     * 
     * @param parent Composite
     * @param ipsProject for which the edit action has to be performed
     * @param type determines which attribute will be edited. It can be either 
     * IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES or IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES 
     */
    public AttributeEditDialog(Shell parent, IIpsProject ipsProject, String type) {
        super(parent);
        this.setTitle("Edit Attribute");
        this.setHelpAvailable(false);
        this.ipsProject = ipsProject;
        
        if (IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type) ||
                IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)) {
            this.type = type;
        }
    }


    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Group group = new Group(parent, SWT.NONE);
        
        buttonDefaultFolderSelected = new Button(group, SWT.RADIO);
        buttonDefaultFolderSelected.setSelection(true);
        buttonDefaultFolderSelected.setText("Use project default output folder (" +  getDefaultOutputFolder() + ").");
        buttonCustomFolderSelected = new Button(group, SWT.RADIO);
        buttonCustomFolderSelected.setText("Use package specific output folder (path relative to " + ipsProject.getName() + ").");

        folderSelectionControl = new FolderSelectionControl(group, new UIToolkit(null), "Browse");
        folderSelectionControl.setRoot(ipsProject.getProject());
        folderSelectionControl.setEnabled(false);
        
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 10;
        
        buttonDefaultFolderSelected.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do*/ }

            public void widgetSelected(SelectionEvent e) {
                folderSelectionControl.setEnabled(false);
                customFolderSelected = false;
            }
        });
        
        buttonCustomFolderSelected.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) { /* nothing to do*/ }

            public void widgetSelected(SelectionEvent e) {
                folderSelectionControl.setEnabled(true);
                customFolderSelected = true;
            }
        });
        
        group.setLayout(layout);
        
        return composite;
        
    }

    /**
     * Get the dialog's result
     * @return selected folder, or null if none was selected
     */
    public IContainer getSelectedFolder() {
        IContainer resultContainer = null;
        
        try {
            if (customFolderSelected) {
                resultContainer = folderSelectionControl.getFolder();
            }
            else if (IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)) {
                resultContainer = ipsProject.getIpsObjectPath().getOutputFolderForDerivedSources();
            }
            else if (IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)) {
                resultContainer = ipsProject.getIpsObjectPath().getOutputFolderForMergableSources();
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        return resultContainer; 
    }
    

    private String getDefaultOutputFolder() {
        
        String rootFolder = "";

        if (ipsProject != null) {
            IContainer folder = null;
            try {
                if (IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)) {
                    folder = ipsProject.getIpsObjectPath().getOutputFolderForDerivedSources();
                }
                else if (IIpsSrcFolderEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)) {
                    folder = ipsProject.getIpsObjectPath().getOutputFolderForMergableSources();
                }
                
                if (folder != null) {
                    rootFolder = ((IProject) ipsProject.getProject()).getName() + IPath.SEPARATOR;
                    rootFolder += folder.getProjectRelativePath().toOSString();
                }

            } catch (CoreException e) {
                IpsPlugin.log(e);
                return "";
            }
            
        }
        
        return rootFolder;
    }

    
}
