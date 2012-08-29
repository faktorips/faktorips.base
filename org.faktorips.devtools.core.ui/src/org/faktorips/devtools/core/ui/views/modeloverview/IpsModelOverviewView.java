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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class IpsModelOverviewView extends ViewPart {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modeloverview.ModelOverview"; //$NON-NLS-1$

    private TreeViewer treeviewer;

    public IpsModelOverviewView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        initToolBar();
        this.treeviewer = new TreeViewer(parent);
        this.treeviewer.setContentProvider(new ModelOverviewContentProvider());
        this.treeviewer.setLabelProvider(new IpsModelOverviewLabelProvider());
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

    public void showOverview(IType input) {
        this.treeviewer.setInput(input);
    }

    public void showOverview(IIpsProject input) {
        this.treeviewer.setInput(input);
    }

    private void initToolBar() {
        IActionBars actionBars = getViewSite().getActionBars();

        Action showPolicyComponentStructureAction = new Action() {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("PolicyCmptType.gif"); //$NON-NLS-1$
                //                return IpsUIPlugin.getImageHandling().createImageDescriptor("ProductCmptType.gif"); //$NON-NLS-1$
            }

            @Override
            public String getToolTipText() {
                // TODO Tooltip internationalisieren
                return "Show Policy Component Structure";
            }

            @Override
            public void run() {
                // TODO Action implementieren
                // Filter setzen?
                // view refreshen
            }

        };
        Action showProductComponentStructureAction = new Action() {
            @Override
            public ImageDescriptor getImageDescriptor() {
                return IpsUIPlugin.getImageHandling().createImageDescriptor("ProductCmptType.gif"); //$NON-NLS-1$
            }

            @Override
            public String getToolTipText() {
                // TODO Tooltip internationalisieren
                return "Show Product Component Structure";
            }

            @Override
            public void run() {
                // TODO Action implementieren
                // Filter setzen?
                // view refreshen
            }

        };

        actionBars.getToolBarManager().add(showPolicyComponentStructureAction);
        actionBars.getToolBarManager().add(showProductComponentStructureAction);
    }

}
