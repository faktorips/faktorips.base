/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;

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

    @Override
    public void run(IStructuredSelection selection) {
        final TypedSelection<IAdaptable> typedSelection = new TypedSelection<>(IAdaptable.class, selection,
                1);

        BusyIndicator.showWhile(shell.getDisplay(), () -> {
            IProductCmptGeneration generation = null;
            if (typedSelection.getElement().getAdapter(IProductCmptGeneration.class) != null) {
                generation = typedSelection.getElement().getAdapter(
                        IProductCmptGeneration.class);
            } else if (typedSelection.getElement().getAdapter(IProductCmpt.class) != null) {
                IProductCmpt root = typedSelection.getElement().getAdapter(IProductCmpt.class);
                IIpsObjectGeneration[] generationsOrderedByValidDate = root.getGenerationsOrderedByValidDate();
                generation = (IProductCmptGeneration)generationsOrderedByValidDate[generationsOrderedByValidDate.length
                        - 1];
            }
            if (generation != null) {
                runCopyWizard(generation);
            }
        });
    }

    protected void runCopyWizard(IProductCmptGeneration generation) {
        IProductCmptNamingStrategy ns = generation.getIpsProject().getProductCmptNamingStrategy();
        if (type == DeepCopyWizard.TYPE_NEW_VERSION && ns == null) {
            String title = NLS.bind(Messages.IpsDeepCopyAction_titleNoVersion, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
            MessageDialog.openInformation(shell, title, Messages.IpsDeepCopyAction_msgNoVersion);
            return;
        }

        DeepCopyWizard dcw;
        try {
            dcw = new DeepCopyWizard(generation, type);
        } catch (CycleInProductStructureException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
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
                    pe = IpsPlugin.getDefault().getWorkbench().getViewRegistry()
                            .find(ProductStructureExplorer.EXTENSION_ID).createView();
                }

                /*
                 * Do nothing if reference is null or if part has not yet been initialized/created
                 * (site is null). Curiously the view's site may still be null even after both
                 * IViewReference#getView(true) and IViewDescriptor#createView() have been called.
                 * 
                 * Calling showStructure() with a null site caused InvocationTargetExceptions when
                 * creating new product versions. See FIPS-1040.
                 */
                if (pe == null || pe.getSite() == null) {
                    return;
                }

                ((ProductStructureExplorer)pe).showStructure(dcw.getCopyResultRoot());

            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

}
