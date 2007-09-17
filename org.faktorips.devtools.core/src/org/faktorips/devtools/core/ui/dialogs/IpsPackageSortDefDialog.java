/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
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

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsPackageSortDefDelta;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * Dialog for changing the sort order of IIpsPackageFragments.
 *
 * @author Markus Blum
 */
public class IpsPackageSortDefDialog extends TrayDialog {

    private String title;
    private IIpsProject project;
    private IpsProjectSortOrdersPM sortOrderPM;

    private UIToolkit toolkit;
    private TreeViewer treeViewer;
    private Button up;
    private Button down;
    private Button restore;

    private boolean restoreDefault;

    /**
     * New instance.
     *
     * @param parentShell The active shell.
     * @param title Title of the dialog.
     * @param project The selected IIpsProject.
     */
    public IpsPackageSortDefDialog(Shell parentShell, String title, IIpsProject project) {
        super(parentShell);

        this.title = title;
        this.project = project;
        sortOrderPM = new IpsProjectSortOrdersPM(project);

        toolkit = new UIToolkit(null);

        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.RESIZE | SWT.MAX );

        restoreDefault = false;

        // TODO Save and set dialog size from settings.
    }

    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent) {
        getShell().setText(title);

        Composite contents = (Composite)super.createDialogArea(parent);
        contents.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, true));

        GridLayout layout = new GridLayout();
        contents.setLayout(layout);

        createHeadline(contents);
        createSortArea(contents);
        createRestoreButton(contents);

        Dialog.applyDialogFont(parent);

        //LayoutDebugUtil.colorize(contents);

        return contents;
    }

    /**
     * Create a headline with the chosen IpsProject name.
     */
    private void createHeadline(Composite parent) {

        Composite headline = toolkit.createComposite(parent);

        headline.setLayoutData(new GridData());

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        headline.setLayout(layout);

        toolkit.createLabel(headline, Messages.IpsPackageSortDefDialog_headlineText);
        toolkit.createLabel(headline, project.getName());
    }

    /**
     * @param parent
     */
    private void createRestoreButton(Composite parent) {

        restore = toolkit.createButton(parent, Messages.IpsPackageSortDefDialog_restore);
        restore.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) {
                restorePressed();
                treeViewer.refresh();
            }
        });
        restore.setLayoutData(new GridData(SWT.END));
    }

    /**
     * Create component for shifting IpsPackageFragments
     */
    private void createSortArea(Composite parent) {

        Composite sortComposite = toolkit.createComposite(parent);

        sortComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, true));

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        sortComposite.setLayout(layout);

        createTreeViewer(sortComposite);
        createUpDownButtons(sortComposite);
    }

    /**
     * Create the treeviewer.
     */
    private void createTreeViewer(Composite sortComposite) {
        treeViewer = new TreeViewer(sortComposite);
        treeViewer.setLabelProvider(new IpsPackageSortDefLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, true));

        IpsPackageSortDefContentProvider contentProvider = new IpsPackageSortDefContentProvider(sortOrderPM);
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setInput(sortOrderPM);
        treeViewer.expandToLevel(2);

        // TODO Set proper initial size.

    }

    /**
     * Create composite with up/buttons.
     */
    private void createUpDownButtons(Composite parent) {
        Composite upDownComposite = toolkit.createComposite(parent);

        upDownComposite.setLayoutData(new GridData());

        GridLayout layout = new GridLayout();
        upDownComposite.setLayout(layout);

        up = toolkit.createButton(upDownComposite, Messages.IpsPackageSortDefDialog_up);
        up.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) {
                upPressed();
                treeViewer.refresh();
            }
        });
        up.setLayoutData(new GridData(SWT.FILL,SWT.DEFAULT,true,false));

        down = toolkit.createButton(upDownComposite, Messages.IpsPackageSortDefDialog_down);
        down.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) {
                downPressed();
             }
        });
        down.setLayoutData(new GridData(SWT.FILL,SWT.DEFAULT,true,false));
    }

    /**
     *
     */
    protected void restorePressed() {

        restoreDefault = true;
        treeViewer.refresh(true);
    }

    /**
     *
     */
    protected void downPressed() {
        Object element = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();

        if (element instanceof IIpsPackageFragment) {
            restoreDefault = false;
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            sortOrderPM.moveOneDown(fragment);
            treeViewer.refresh(false);
        }
    }

    /**
     *
     */
    protected void upPressed() {
        Object element = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();

        if (element instanceof IIpsPackageFragment) {
            restoreDefault = false;
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            sortOrderPM.moveOneUp(fragment);
            treeViewer.refresh(false);
         }
    }

    /**
     * {@inheritDoc}
     */
    protected void okPressed() {

        try {
            IpsPackageSortDefDelta delta = sortOrderPM.createSortDefDelta(restoreDefault);
            delta.fix();

        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        super.okPressed();
    }

    /**
     * {@inheritDoc}
     */
    public boolean close() {

        if (toolkit != null) {
            toolkit.dispose();
        }

        return super.close();
    }

    /**
     * New LabelProvider for the treeViewer.
     * @author Markus Blum
     */
    private class IpsPackageSortDefLabelProvider extends LabelProvider {

        public Image getImage(Object element) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;

            Image image;
            if (fragment.isDefaultPackage()) {
                image = fragment.getRoot().getImage();
            } else {
                image = fragment.getImage();
            }

            return image;
        }

        public String getText(Object element) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            String name;

            if (fragment.isDefaultPackage()) {
                name = fragment.getRoot().getName();
            } else {
                name = fragment.getName();
            }

            return QNameUtil.getUnqualifiedName(name);
        }
    }
}
