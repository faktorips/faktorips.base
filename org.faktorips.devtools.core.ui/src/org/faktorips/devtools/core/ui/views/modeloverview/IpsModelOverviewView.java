/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modeloverview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;

public class IpsModelOverviewView extends ViewPart {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modeloverview.ModelOverview"; //$NON-NLS-1$

    private TreeViewer treeViewer;
    private UIToolkit uiToolkit = new UIToolkit(null);
    private Label label;

    public IpsModelOverviewView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        initToolBar();
        // uiToolkit = new UIToolkit(new FormToolkit(parent.getDisplay()));
        Composite panel = uiToolkit.createGridComposite(parent, 1, false, true, new GridData(SWT.FILL, SWT.FILL, true,
                true));

        label = uiToolkit.createLabel(panel, "", SWT.LEFT, new GridData(SWT.FILL, SWT.FILL, //$NON-NLS-1$
                true, false));

        this.treeViewer = new TreeViewer(panel);
        this.treeViewer.setContentProvider(new ModelOverviewContentProvider());
        this.treeViewer.setLabelProvider(new IpsModelOverviewLabelProvider());
        this.treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    @Override
    public void setFocus() {
        this.treeViewer.getTree().setFocus();
    }

    @Override
    public void dispose() {
        uiToolkit.dispose();
    }

    public void showOverview(IType input) {
        this.treeViewer.setInput(input);
        this.updateView();
    }

    public void showOverview(IIpsProject input) {
        this.treeViewer.setInput(input);
        this.updateView();
    }

    private void updateView() {
        Object element = treeViewer.getInput();
        if (element instanceof IType) {
            this.label.setText(((IType)element).getQualifiedName());
        } else {
            this.label.setText(((IIpsProject)element).getName());
        }
    }

    private void initToolBar() {
        final IActionBars actionBars = getViewSite().getActionBars();

        final Action showToggleTypeAction = new Action() {
            private boolean showPolicyComponents = true;

            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("PolicyCmptType.gif"); //$NON-NLS-1$
            }

            @Override
            public String getToolTipText() {
                return Messages.IpsModelOverview_tooltipShowOnlyPolicies;
            }

            @Override
            public void run() {
                // TODO get this method to work
                // switch state
                showPolicyComponents = !showPolicyComponents;
                if (showPolicyComponents) {
                    this.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("PolicyCmptType.gif")); //$NON-NLS-1$
                    this.setToolTipText(Messages.IpsModelOverview_tooltipShowOnlyPolicies);
                } else {
                    this.setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("ProductCmptType.gif")); //$NON-NLS-1$
                    this.setToolTipText("Produkt Tooltip");
                }
            }

        };

        actionBars.getToolBarManager().add(showToggleTypeAction);
    }
}
