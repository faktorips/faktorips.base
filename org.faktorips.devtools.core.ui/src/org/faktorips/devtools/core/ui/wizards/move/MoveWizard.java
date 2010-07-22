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

package org.faktorips.devtools.core.ui.wizards.move;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.refactor.MoveOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A wizard to move and/or rename package fragements or product components.
 * 
 * @author Thorsten Guenther
 */
public class MoveWizard extends Wizard {

    /**
     * The page to query the data (new name, new package) from.
     */
    private IWizardPage sourcePage;

    /**
     * All selected objects to move.
     */
    private IIpsElement[] selectedObjects;

    /**
     * Operate as move-wizard (does not show the input for new name). Value == 1.
     */
    public static final int OPERATION_MOVE = 1;

    /**
     * Operate as rename-wizard (does only allow a single selected object). Value == 10.
     */
    public static final int OPERATION_RENAME = 10;

    /**
     * The mode we are operating in
     * 
     * @see MoveWizard#OPERATION_MOVE;
     * @see MoveWizard#OPERATION_RENAME;
     */
    private int operation;

    /**
     * If the wizard encounters an selection error (e.g. more then one object selected in mode
     * rename) this string is set to a human readable error message.
     */
    private String selectionError;

    public MoveWizard(IStructuredSelection selection, int operation) {
        super();
        setNeedsProgressMonitor(true);

        this.operation = operation;
        if (operation == OPERATION_MOVE) {
            super.setWindowTitle(Messages.MoveWizard_titleMove);
            super.setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "wizards/MoveAndRenameWizard.png")); //$NON-NLS-1$
        } else if (operation == OPERATION_RENAME) {
            super.setWindowTitle(Messages.MoveWizard_titleRename);
            super.setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "wizards/RenameWizard.png")); //$NON-NLS-1$
        } else {
            String msg = NLS.bind(Messages.MoveWizard_warnInvalidOperation, "" + operation); //$NON-NLS-1$
            IpsStatus status = new IpsStatus(msg);
            IpsPlugin.log(status);
            this.operation = OPERATION_MOVE;
        }

        Object[] selected = selection.toArray();
        selectedObjects = new IIpsElement[selected.length];
        for (int i = 0; i < selection.size(); i++) {
            if (selected[i] instanceof IProductCmpt || selected[i] instanceof IIpsPackageFragment
                    || selected[i] instanceof ITableContents || selected[i] instanceof ITestCase) {
                selectedObjects[i] = (IIpsElement)selected[i];
            } else {
                selectionError = Messages.MoveWizard_errorUnsupported;

                // does not make sense to work on...
                break;
            }
        }

        if (selectionError == null && operation == OPERATION_RENAME && selectedObjects.length > 1) {
            selectionError = Messages.MoveWizard_errorToManySelected;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        if (selectionError != null) {
            sourcePage = new ErrorPage(selectionError);
        } else if (operation == MoveWizard.OPERATION_MOVE) {
            sourcePage = new MovePage(selectedObjects);
        } else if (operation == MoveWizard.OPERATION_RENAME) {
            sourcePage = new RenamePage(selectedObjects[0]);
        }
        super.addPage(sourcePage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        try {
            MoveOperation moveOp;
            if (operation == OPERATION_MOVE) {
                moveOp = new MoveOperation(selectedObjects, ((MovePage)sourcePage).getTarget());
            } else if (operation == OPERATION_RENAME) {
                RenamePage renamePage = (RenamePage)sourcePage;
                IProductCmpt productCmpt = getSelectedProductCmpt();
                if (productCmpt == null) {
                    moveOp = new MoveOperation(selectedObjects, new String[] { renamePage.getNewName() });
                } else {
                    moveOp = new MoveOperation(productCmpt, renamePage.getNewName(), renamePage.getNewRuntimeId());
                }
            } else {
                throw new CoreException(new IpsStatus("Wrong operation: " + operation));
            }
            getContainer().run(true, true, new ModifyOperation(moveOp));

        } catch (CoreException e) {
            if (e.getStatus() != null) {
                IStatus status = e.getStatus();
                if (status instanceof IpsStatus) {
                    MessageDialog.openError(getShell(), Messages.MoveWizard_error, e.getMessage());
                }
            }
            IpsPlugin.log(e);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        return true;
    }

    /**
     * Returns the selected product component if exactly one product component (or the ips source
     * file containing the component) is selected. Returns <code>null</code> otherwise.
     */
    private IProductCmpt getSelectedProductCmpt() throws CoreException {
        if (selectedObjects.length != 1) {
            return null;
        }
        if (selectedObjects[0] instanceof IProductCmpt) {
            return (IProductCmpt)selectedObjects[0];
        }
        if (selectedObjects[0] instanceof IIpsSrcFile) {
            return (IProductCmpt)((IIpsSrcFile)selectedObjects[0]).getIpsObject();
        }
        return null;
    }

    private class ModifyOperation extends WorkspaceModifyOperation {

        private MoveOperation move;

        public ModifyOperation(MoveOperation toExecute) {
            super();
            move = toExecute;
        }

        @Override
        protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                InterruptedException {

            move.run(monitor);
        }
    }

}
