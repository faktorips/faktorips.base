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

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * This dialog is used as container for Faktor-IPS refactoring wizards. The dialog provided by LTK
 * is not sufficient as it is currently not possible to disable the cancel button during the
 * refactoring process when using LTK's refactoring wizard dialogs.
 * <p>
 * Unfortunately the refactoring wizard dialogs provided by LTK are located in internal packages.
 * The alternative would be to copy the entire {@link RefactoringWizardDialog}.
 * 
 * @author Alexander Weickmann
 */
public final class IpsRefactoringDialog extends RefactoringWizardDialog {

    /**
     * @param shell The parent shell
     * @param wizard The {@link IpsRefactoringWizard} to start using this dialog
     */
    public IpsRefactoringDialog(Shell shell, IpsRefactoringWizard wizard) {
        super(shell, wizard);
    }

    /**
     * This implementation never allows the runnable to be canceled.
     * <p>
     * At the moment we cannot allow the cancel for refactoring wizards as we are directly executing
     * the refactoring during the 'create change' phase. If the user then presses cancel many
     * changes of the refactoring have already been applied.
     */
    @Override
    public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
            InterruptedException {

        super.run(fork, false, runnable);
    }

}
