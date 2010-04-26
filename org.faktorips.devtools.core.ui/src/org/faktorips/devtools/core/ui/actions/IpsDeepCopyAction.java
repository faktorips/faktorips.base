/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.actions;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;

/**
 * Performs a deep copy (copy of all objects and all related objects of this one and all related of
 * the related ones and so on).
 * 
 * @author Thorsten Guenther
 */
public class IpsDeepCopyAction extends IpsAction {

    private Shell shell;

    /**
     * One of DeepCopyWizard.TYPE_COPY_PRODUCT or DeepCopyWizard.TYPE_NEW_VERSION.
     */
    private int type;

    /**
     * Creates a new action to start the deep copy wizard.
     * 
     * @param shell The shell to use as parent for the wizard
     * @param selectionProvider The provider of the selected item to use as root for the copy.
     * @param type One of DeepCopyWizard.TYPE_COPY_PRODUCT or DeepCopyWizard.TYPE_NEW_VERSION
     */
    public IpsDeepCopyAction(Shell shell, ISelectionProvider selectionProvider, int type) {
        super(selectionProvider);
        if (type != DeepCopyWizard.TYPE_COPY_PRODUCT && type != DeepCopyWizard.TYPE_NEW_VERSION) {
            throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
        }
        this.type = type;

        this.shell = shell;

        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            setText(Messages.IpsDeepCopyAction_name);
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("DeepCopyWizard.gif")); //$NON-NLS-1$
        } else {
            setText(NLS.bind(Messages.IpsDeepCopyAction_nameNewVersion, IpsPlugin.getDefault().getIpsPreferences()
                    .getChangesOverTimeNamingConvention().getVersionConceptNameSingular()));
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("NewVersionWizard.gif")); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IStructuredSelection selection) {
        IIpsObject selected = getIpsObjectForSelection(selection);
        if (selected instanceof IProductCmpt) {
            IProductCmpt root = (IProductCmpt)selected;
            GregorianCalendar validFrom = root.getGeneration(root.getGenerations().size() - 1).getValidFrom();
            runCopyWizard(root, validFrom);
        } else if (selected instanceof IProductCmptGeneration) {
            IProductCmptGeneration selGeneration = (IProductCmptGeneration)selected;
            IProductCmpt root = selGeneration.getProductCmpt();
            GregorianCalendar validFrom = selGeneration.getValidFrom();
            runCopyWizard(root, validFrom);
        }
    }

    protected void runCopyWizard(IProductCmpt root, GregorianCalendar validFrom) {
        IProductCmptNamingStrategy ns = null;
        try {
            ns = root.getIpsProject().getProductCmptNamingStrategy();
        } catch (CoreException e1) {
            IpsPlugin.log(e1);
        }
        if (type == DeepCopyWizard.TYPE_NEW_VERSION && ns == null) {
            String title = NLS.bind(Messages.IpsDeepCopyAction_titleNoVersion, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
            MessageDialog.openInformation(shell, title, Messages.IpsDeepCopyAction_msgNoVersion);
            return;
        }

        DeepCopyWizard dcw = new DeepCopyWizard(root, validFrom, type);
        WizardDialog wd = new WizardDialog(shell, dcw);
        wd.setBlockOnOpen(true);
        wd.open();
        if (wd.getReturnCode() == Window.OK) {

            try {
                IViewReference[] views = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().getViewReferences();
                IViewPart pe = null;
                for (IViewReference view : views) {
                    if (view.getId().equals(ProductStructureExplorer.EXTENSION_ID)) {
                        pe = view.getView(true);
                        break;
                    }
                }

                if (pe == null) {
                    pe = IpsPlugin.getDefault().getWorkbench().getViewRegistry().find(
                            ProductStructureExplorer.EXTENSION_ID).createView();
                }

                if (pe == null) {
                    return;
                }

                ((ProductStructureExplorer)pe).showStructure(dcw.getCopiedRoot());

            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }
}
