/*******************************************************************************
 * // * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.projectproperties;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.preferencepages.ArchiveComposite;
import org.faktorips.devtools.core.ui.preferencepages.ObjectPathOrderComposite;
import org.faktorips.devtools.core.ui.preferencepages.ReferencedProjectsComposite;
import org.faktorips.devtools.core.ui.preferencepages.SrcFolderComposite;

public class BuildPathPropertiesPage extends ProjectPropertyPage {
    final static String PAGEID = "BuildPathProperties"; //$NON-NLS-1$
    private static final ImageDescriptor packageFragmentRootImage = IpsUIPlugin.getImageHandling()
            .createImageDescriptor("IpsPackageFragmentRoot.gif"); //$NON-NLS-1$

    private static final ImageDescriptor projectImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "IpsProject.gif"); //$NON-NLS-1$

    private static final ImageDescriptor archiveImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "IpsAr.gif"); //$NON-NLS-1$

    private static final ImageDescriptor objectPathImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "obj16/cp_order_obj.gif"); //$NON-NLS-1$
    private SrcFolderComposite srcFolderComposite;
    private ReferencedProjectsComposite refProjectsComposite;
    private ArchiveComposite archiveComposite;
    private ObjectPathOrderComposite orderComposite;
    private ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
    private IIpsObjectPath ipsObjectPath;
    private IIpsProject currentIpsProject;
    private int pageIndex;

    public BuildPathPropertiesPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        // TODO Auto-generated constructor stub
    }

    public BuildPathPropertiesPage(ProjectPropertyEditor projectPropertyEditor) throws CoreException {
        super(projectPropertyEditor, PAGEID, Messages.BuildPathPropertiesPage_description);
        currentIpsProject = projectPropertyEditor.getIpsProject();
        ipsObjectPath = currentIpsProject.getIpsObjectPath();
        reinitComposites();
    }

    // formBody.setLayout(createPageLayout(1, false));
    // TabFolder tf = new TabFolder(parent, SWT.NONE);
    // TabItem quellverzeichnis = new TabItem(tf, SWT.NONE);
    // quellverzeichnis.setText(Messages.BuildPathPropertiesPage_source);
    // quellverzeichnis.setControl(new Quellverzeichnis(tf, toolkit));
    // TabItem projects = new TabItem(tf, SWT.NONE);
    // projects.setText(Messages.BuildPathPropertiesPage_projects);
    // TabItem archive = new TabItem(tf, SWT.NONE);
    // archive.setText(Messages.BuildPathPropertiesPage_archive);
    // TabItem reihenfolge = new TabItem(tf, SWT.NONE);
    // reihenfolge.setText(Messages.BuildPathPropertiesPage_reihenfolge);
    private void tabChanged(Widget widget) {
        if (widget instanceof TabItem) {
            TabItem tabItem = (TabItem)widget;
            pageIndex = tabItem.getParent().getSelectionIndex();
        }
        doUpdateUI();
    }

    private TabItem addTabItem(TabFolder parent, String tabName, Image tabImage, Composite composite) {
        TabItem item = new TabItem(parent, SWT.NONE);
        item.setText(tabName);
        item.setImage(tabImage);
        item.setData(composite);
        item.setControl(composite);

        return item;
    }

    private Control Quellverzeichnis(TabFolder tf) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getPageName() {
        return Messages.BuildPathPropertiesPage_description;
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

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        Composite members = createGridComposite(toolkit, formBody, 1, true, GridData.FILL_BOTH);
        // Composite composite = new Composite(formBody, SWT.NONE);
        // composite.setFont(formBody.getFont());
        //
        // GridLayout layout = new GridLayout();
        // layout.marginWidth = 0;
        // layout.marginHeight = 0;
        // layout.numColumns = 1;
        // composite.setLayout(layout);

        TabFolder folder = new TabFolder(members, SWT.NONE);
        folder.setLayoutData(new GridData(GridData.FILL_BOTH));
        folder.setFont(members.getFont());

        srcFolderComposite = new SrcFolderComposite(folder);
        refProjectsComposite = new ReferencedProjectsComposite(folder);
        archiveComposite = new ArchiveComposite(folder);
        orderComposite = new ObjectPathOrderComposite(folder);
        //
        addTabItem(folder, Messages.BuildPathPropertiesPage_source, (Image)resourceManager
                .get(packageFragmentRootImage), srcFolderComposite);
        addTabItem(folder, Messages.BuildPathPropertiesPage_projects, (Image)resourceManager.get(projectImage),
                refProjectsComposite);
        addTabItem(folder, Messages.BuildPathPropertiesPage_archive, (Image)resourceManager.get(archiveImage),
                archiveComposite);
        addTabItem(folder, Messages.BuildPathPropertiesPage_path_order, (Image)resourceManager.get(objectPathImage),
                orderComposite);
        //
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

        Dialog.applyDialogFont(members);
        // return composite;

    }
}
