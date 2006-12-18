/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:  Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IFixDifferencesToModelSupport;

/**
 * 
 * @author Daniel Hohenberger
 */
public class FixDifferencesToModelWizard extends Wizard implements IWorkbenchWizard {
    private Set ipsElementsToFix;
    private ElementSelectionPage elementSelectionPage;

    public FixDifferencesToModelWizard(Set ipsElementsToFix) {
        this.ipsElementsToFix = ipsElementsToFix;
        setWindowTitle(Messages.FixDifferencesToModelWizard_Title);
        this
                .setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor(
                        "wizards/NewIpsPackageWizard.png")); //$NON-NLS-1$
    }

    public boolean performFinish() {
        final IFixDifferencesToModelSupport[] elementsToFix = elementSelectionPage.getElementsToFix();
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) {
                monitor.beginTask("fixing differences to model", elementsToFix.length);
                try {
                    for (int i = 0; i < elementsToFix.length; i++) {
                        elementsToFix[i].fixAllDifferencesToModel();
                        monitor.worked(1);
                    }
                }
                catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, true, new IRunnableWithProgress(){
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        IpsPlugin.getDefault().getIpsModel().runAndQueueChangeEvents(op, monitor);
                    }
                    catch (CoreException e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                    }
                }                
            });
        }
        catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // initialization is handled in the calling action, which transforms the selection to the
        // needed set.
        setNeedsProgressMonitor(true);
    }

    public void addPages() {
        super.addPages();
        elementSelectionPage = new ElementSelectionPage(ipsElementsToFix);
        super.addPage(elementSelectionPage);
    }

}
