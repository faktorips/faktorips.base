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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Ips object path preference page container
 * 
 * @author Roman Grutza
 */
public class IpsObjectPathContainer {

    private IIpsProject currentIpsProject;
    
    private IIpsObjectPath ipsObjectPath;
    
    private Control mainControl; 

    private int pageIndex;

    private ReferencedProjectsComposite refProjectsComposite;
    private ArchiveComposite archiveComposite;
//    private SrcFolderComposite srcFolderComposite;
//    private ObjectPathOrderComposite orderComposite;
    
    
    public IpsObjectPathContainer(int pageToShow, IWorkbenchPreferenceContainer preferenceContainer) {
        pageIndex = pageToShow;
    }
    

    
    /**
     * @param ipsProject The IPS project to configure.
     * @param outputLocation The output location to be set in the page. If <code>null</code>
     * is passed, IPS default settings are used, or - if the project is an existing IPS project- the
     * output location of the existing project 
     * @throws CoreException 
     */ 
    public void init(IIpsProject ipsProject, IPath outputLocation) throws CoreException {

        currentIpsProject = ipsProject;
        this.ipsObjectPath = ipsProject.getIpsObjectPath();
    }
    
    /**
     * Main UI creation method for configuring IPS objectpath entries
     * 
     * @param parent control
     * @return IPS objectpath control
     * @throws CoreException if the control could not be created
     */
    public Control createControl(Composite parent) throws CoreException {
        mainControl = parent;

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 1;
        composite.setLayout(layout);

        TabFolder folder = new TabFolder(composite, SWT.NONE);
        folder.setLayoutData(new GridData(GridData.FILL_BOTH));
        folder.setFont(composite.getFont());

//        srcFolderComposite = new SrcFolderComposite(folder);
        refProjectsComposite = new ReferencedProjectsComposite(folder);
        archiveComposite = new ArchiveComposite(folder);
//        orderComposite = new ObjectPathOrderComposite(folder);
        
//        addTabItem(folder, "Source", 
//                IpsPlugin.getDefault().getImage("IpsPackageFragmentRoot.gif"), srcFolderComposite); //$NON-NLS-1$
        addTabItem(folder, Messages.IpsObjectPathContainer_tab_projects, 
                IpsPlugin.getDefault().getImage("IpsProject.gif"), refProjectsComposite); //$NON-NLS-1$
        addTabItem(folder, "Archives", 
                IpsPlugin.getDefault().getImage("IpsAr.gif"), archiveComposite); //$NON-NLS-1$
//        addTabItem(folder, "Path Order", 
//                IpsPlugin.getDefault().getImage("obj16" + IPath.SEPARATOR + "cp_order_obj.gif"), orderComposite); //$NON-NLS-1$

        
//        srcFolderComposite.init(ipsObjectPath);
        refProjectsComposite.init(ipsObjectPath);
        archiveComposite.init(ipsObjectPath);
//        orderComposite.init(ipsObjectPath);
        
        folder.setSelection(pageIndex);
        folder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                tabChanged(e.item);
            }   
        });

        Dialog.applyDialogFont(composite);
        return composite;
    }
    
    private TabItem addTabItem(TabFolder parent, String tabName, Image tabImage, Composite composite) {
        TabItem item = new TabItem(parent, SWT.NONE);
        item.setText(tabName); 
        item.setImage(tabImage);
        item.setData(composite);
        item.setControl(composite);

        return item; 
    }
    
    protected void updateUI() {
        if (mainControl == null || mainControl.isDisposed()) {
            return;
        }
        
        if (Display.getCurrent() != null) {
            doUpdateUI();
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    if (mainControl == null || mainControl.isDisposed()) {
                        return;
                    }
                    doUpdateUI();
                }
            });
        }
    }

    protected void doUpdateUI() {
        // TODO: to implement when more than one tab selectable 
    }
    
    private void tabChanged(Widget widget) {
        if (widget instanceof TabItem) {
            TabItem tabItem = (TabItem)widget;
            pageIndex = tabItem.getParent().getSelectionIndex();
        }
    }

    /**
     * Persists changes made in the property page dialog.
     * 
     * @return true if changes made in the dialog could be committed successfully, false otherwise
     */
    public boolean saveToIpsProjectFile() {
        
        try {
            currentIpsProject.setIpsObjectPath(ipsObjectPath);
        } catch (CoreException e) {
            
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }

}
