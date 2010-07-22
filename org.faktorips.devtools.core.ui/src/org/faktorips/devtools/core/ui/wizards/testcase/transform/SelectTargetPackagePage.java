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

package org.faktorips.devtools.core.ui.wizards.testcase.transform;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Wizard page to select the target ips package fragment where the new created test cases will be
 * stored.
 * 
 * @author Joerg Ortmann
 */
public class SelectTargetPackagePage extends WizardPage {
    private static final String PAGE_ID = "SelectRuntimeTestCasePage"; //$NON-NLS-1$

    private TransformRuntimeTestCaseWizard wizard;

    private TableViewer tableViewer;

    public SelectTargetPackagePage(TransformRuntimeTestCaseWizard wizard) {
        super(PAGE_ID, Messages.TransformWizard_SelectTarget_title, null);
        setDescription(Messages.TransformWizard_SelectTarget_description);
        this.wizard = wizard;
        setPageComplete(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        tableViewer = new TableViewer(container, SWT.SCROLL_PAGE | SWT.BORDER);
        tableViewer.setContentProvider(new IpsPackageFrmgmtContentProvider());
        tableViewer.setLabelProvider(new IpsPackageFrmgmtLabelProvider());
        tableViewer.setInput(wizard.getPackageFragments());

        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 200;
        data.heightHint = 300;
        tableViewer.getTable().setLayoutData(data);

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection structSelection = (IStructuredSelection)event.getSelection();
                    wizard.setTargetIpsPackageFragment((IIpsPackageFragment)structSelection.getFirstElement());
                } else {
                    wizard.setTargetIpsPackageFragment(null);
                }
            }
        });
        setControl(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canFlipToNextPage() {
        return wizard.getTargetIpsPackageFragment() != null;
    }

    /**
     * Content provider to show all package fragments the wizard provides.
     */
    class IpsPackageFrmgmtContentProvider implements IStructuredContentProvider {
        @Override
        public Object[] getElements(Object input) {
            return wizard.getPackageFragments();
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    /**
     * Label provider wich shows the project, the package fragment root name and the package
     * fragment.
     */
    class IpsPackageFrmgmtLabelProvider extends LabelProvider {
        @Override
        public String getText(Object obj) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)obj;
            return packageFragment.getIpsProject().getName()
                    + " - " + packageFragment.getRoot().getName() + "." + packageFragment.getName(); //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public Image getImage(Object obj) {
            return IpsUIPlugin.getImageHandling().getImage((IIpsPackageFragment)obj);
        }
    }
}
