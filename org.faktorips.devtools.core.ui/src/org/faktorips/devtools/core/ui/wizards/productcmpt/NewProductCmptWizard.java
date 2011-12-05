/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Wizard to create a new product component.
 * <p>
 * This wizard is used to create new product components. Normally you start with the fist page by
 * selecting an abstract product component type from a list. The list is created in context to the
 * selected project.
 * <p>
 * This wizard was completely rewritten in version 3.6
 * 
 * @author dirmeier
 * 
 */
public class NewProductCmptWizard extends Wizard implements IWorkbenchWizard {

    private final NewProductCmptPMO newProductCmptPMO;
    private final TypeSelectionPage typeSelectionPage;
    private final ProductCmptPage productCmptPage;
    private FolderAndPackagePage folderAndPackagePage;

    /**
     * Creating a the new wizard.
     */
    public NewProductCmptWizard() {
        super();
        setWindowTitle("Create new product component");
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductCmptWizard.png")); //$NON-NLS-1$
        newProductCmptPMO = new NewProductCmptPMO(IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
        typeSelectionPage = new TypeSelectionPage(newProductCmptPMO);
        productCmptPage = new ProductCmptPage(newProductCmptPMO);
        folderAndPackagePage = new FolderAndPackagePage(newProductCmptPMO);
    }

    @Override
    public void addPages() {
        addPage(typeSelectionPage);
        addPage(productCmptPage);
        addPage(folderAndPackagePage);
    }

    @Override
    public boolean performFinish() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        Object element = selection.getFirstElement();
        if (element instanceof IAdaptable) {
            IAdaptable adaptableObject = (IAdaptable)element;
            IResource resource = (IResource)adaptableObject.getAdapter(IResource.class);
            if (resource != null) {
                newProductCmptPMO.initDefaults(resource);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
