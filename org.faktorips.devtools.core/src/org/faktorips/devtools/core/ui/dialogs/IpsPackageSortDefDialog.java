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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.util.QNameUtil;

/**
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

    /**
     * @param parentShell
     * @param project
     */
    public IpsPackageSortDefDialog(Shell parentShell, String title, IIpsProject project) {
        super(parentShell);

        this.title = title;
        this.project = project;
        sortOrderPM = new IpsProjectSortOrdersPM(project);

        toolkit = new UIToolkit(null);

        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.RESIZE | SWT.MAX);
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
     * @param contents
     */
    private void createHeadline(Composite parent) {

        Composite headline = toolkit.createComposite(parent);

        headline.setLayoutData(new GridData());

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        headline.setLayout(layout);

        toolkit.createLabel(headline, "FaktorIps-Projekt sortieren:");
        toolkit.createLabel(headline, project.getName());
    }

    /**
     * @param parent
     */
    private void createRestoreButton(Composite parent) {

        restore = toolkit.createButton(parent, "Restore Defaults");
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
     * @param parent
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
     * @param sortComposite
     */
    private void createTreeViewer(Composite sortComposite) {
        treeViewer = new TreeViewer(sortComposite);
        treeViewer.setLabelProvider(new IpsPackageSortDefLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, true));

        IpsPackageSortDefContentProvider contentProvider = new IpsPackageSortDefContentProvider(sortOrderPM);
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setInput(sortOrderPM);
    }

    /**
     * @param upDown
     */
    private void createUpDownButtons(Composite parent) {
        Composite upDownComposite = toolkit.createComposite(parent);

        upDownComposite.setLayoutData(new GridData());

        GridLayout layout = new GridLayout();
        upDownComposite.setLayout(layout);

        up = toolkit.createButton(upDownComposite, "Up");
        up.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) {
                upPressed();
                treeViewer.refresh();
            }
        });

        down = toolkit.createButton(upDownComposite, "Down");
        down.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) {
                downPressed();
                treeViewer.refresh();
            }
        });
    }

    /**
     *
     */
    protected void restorePressed() {
        sortOrderPM.restore();
    }

    /**
     *
     */
    protected void downPressed() {
        Object element = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();

        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            sortOrderPM.moveOneDown(fragment);
        }
    }

    /**
     *
     */
    protected void upPressed() {
        Object element = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();

        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            sortOrderPM.moveOneUp(fragment);
         }
    }

    /**
     * {@inheritDoc}
     */
    protected void okPressed() {

        try {
            IpsPackageSortDefDelta delta = sortOrderPM.createSortDefDelta();
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
     *
     * @author Markus Blum
     */
    private class IpsPackageSortDefLabelProvider extends LabelProvider {

        public Image getImage(Object element) {

            if (element instanceof IIpsElement) {
                return ((IIpsElement)element).getImage();
            }

            return null;
        }

        public String getText(Object element) {
            if (element instanceof IIpsPackageFragment) {
                IIpsPackageFragment fragment = (IIpsPackageFragment)element;
                String name;

                if (fragment.isDefaultPackage()) {
                    name = fragment.getRoot().getName();
                } else {
                    name = fragment.getName();
                }

                return QNameUtil.getUnqualifiedName(name);
            }

            return "null";
        }
    }
}
