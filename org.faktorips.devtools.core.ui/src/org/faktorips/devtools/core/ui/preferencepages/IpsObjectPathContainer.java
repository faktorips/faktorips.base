/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Ips object path preference page container
 * 
 * @author Roman Grutza
 */
public class IpsObjectPathContainer {

    private static final ImageDescriptor packageFragmentRootImage = IpsUIPlugin.getImageHandling()
            .createImageDescriptor("IpsPackageFragmentRoot.gif"); //$NON-NLS-1$

    private static final ImageDescriptor projectImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "IpsProject.gif"); //$NON-NLS-1$

    private static final ImageDescriptor archiveImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "IpsAr.gif"); //$NON-NLS-1$

    private static final ImageDescriptor objectPathImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "obj16/cp_order_obj.gif"); //$NON-NLS-1$

    private IIpsProject currentIpsProject;

    private IIpsObjectPath ipsObjectPath;

    private int pageIndex;

    private ReferencedProjectsComposite refProjectsComposite;
    private ArchiveComposite archiveComposite;
    private SrcFolderComposite srcFolderComposite;
    private ObjectPathOrderComposite orderComposite;

    private ResourceManager resourceManager;

    public IpsObjectPathContainer(int pageToShow, IWorkbenchPreferenceContainer preferenceContainer) {
        pageIndex = pageToShow;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    /**
     * @param ipsProject The IPS project to configure.
     */
    public void init(IIpsProject ipsProject) throws CoreException {
        currentIpsProject = ipsProject;
        ipsObjectPath = ipsProject.getIpsObjectPath();

        reinitComposites();
    }

    /**
     * Main UI creation method for configuring IPS objectpath entries
     * 
     * @param parent control
     * @return IPS objectpath control
     * @throws CoreException if the control could not be created
     */
    public Control createControl(Composite parent) throws CoreException {
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

        srcFolderComposite = new SrcFolderComposite(folder);
        refProjectsComposite = new ReferencedProjectsComposite(folder);
        archiveComposite = new ArchiveComposite(folder);
        orderComposite = new ObjectPathOrderComposite(folder);

        addTabItem(folder, Messages.IpsObjectPathContainer_tab_source, (Image)resourceManager
                .get(packageFragmentRootImage), srcFolderComposite);
        addTabItem(folder, Messages.IpsObjectPathContainer_tab_projects, (Image)resourceManager.get(projectImage),
                refProjectsComposite);
        addTabItem(folder, Messages.IpsObjectPathContainer_tab_archives, (Image)resourceManager.get(archiveImage),
                archiveComposite);
        addTabItem(folder, Messages.IpsObjectPathContainer_tab_path_order, (Image)resourceManager.get(objectPathImage),
                orderComposite);

        srcFolderComposite.init(ipsObjectPath);
        refProjectsComposite.init(ipsObjectPath);
        archiveComposite.init(ipsObjectPath);
        orderComposite.init(ipsObjectPath);

        folder.setSelection(pageIndex);

        folder.addSelectionListener(new SelectionAdapter() {
            @Override
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

    private void tabChanged(Widget widget) {
        if (widget instanceof TabItem) {
            TabItem tabItem = (TabItem)widget;
            pageIndex = tabItem.getParent().getSelectionIndex();
        }
        doUpdateUI();
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

    /**
     * Check whether values have been modified
     * 
     * @return true if data has changed, false otherwise
     */
    public boolean hasChangesInDialog() {
        return (archiveComposite.isDataChanged() || orderComposite.isDataChanged()
                || refProjectsComposite.isDataChanged() || srcFolderComposite.isDataChanged());
    }

    /**
     * Manually update the UI
     */
    public void doUpdateUI() {
        switch (pageIndex) {
            case 0:
                srcFolderComposite.doUpdateUI();
                break;
            case 1:
                refProjectsComposite.doUpdateUI();
                break;
            case 2:
                archiveComposite.doUpdateUI();
                break;
            case 3:
                orderComposite.doUpdateUI();
                break;
        }
    }

    private void reinitComposites() {
        if (archiveComposite != null) {
            archiveComposite.init(ipsObjectPath);
        }
        if (orderComposite != null) {
            orderComposite.init(ipsObjectPath);
        }
        if (refProjectsComposite != null) {
            refProjectsComposite.init(ipsObjectPath);
        }
        if (srcFolderComposite != null) {
            srcFolderComposite.init(ipsObjectPath);
        }
    }

    public void dispose() {
        resourceManager.dispose();
    }

}
