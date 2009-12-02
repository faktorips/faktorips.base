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

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog2;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;

/**
 * This dialog is used as container for Faktor-IPS refactoring wizards. The dialog provided by LTK
 * is not sufficient as it is currently not possible to disable the cancel button during the
 * refactoring process when using LTK's refactoring wizard dialogs.
 * <p>
 * Unfortunately the refactoring wizard dialogs provided by LTK are located in internal packages.
 * This should not be a problem however because we only override the
 * <tt>run(boolean, boolean, IRunnableWithProgress)</tt> method declared in the
 * <tt>IRunnableContext</tt> interface. The alternative would be to copy the entire
 * <tt>RefactoringWizardDialog2</tt>.
 * 
 * @author Alexander Weickmann
 */
public class RefactoringDialog extends RefactoringWizardDialog2 {

    /**
     * Creates a <tt>RefactoringDialog</tt>.
     * 
     * @param shell The parent shell.
     * @param wizard The <tt>RefactoringWizard</tt> to start with this dialog.
     */
    public RefactoringDialog(Shell shell, RefactoringWizard wizard) {
        super(shell, wizard);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to always call the super implementation with the <tt>cancelable</tt> flag set to
     * <tt>false</tt> because Faktor-IPS refactorings may not be canceled.
     */
    @Override
    public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
            InterruptedException {

        super.run(fork, false, runnable);
    }

}