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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.wizards.refactor.RefactoringDialog;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for global actions that want to provide refactoring support.
 * <p>
 * This class provides basic functionality common to refactoring actions such as opening a
 * refactoring wizard.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringAction extends IpsAction implements IShellProvider {

    /** A <tt>Shell</tt> that can be used by this action. */
    private final Shell shell;

    /**
     * Creates the <tt>IpsRefactoringAction</tt>.
     * 
     * @param shell The shell to be used by this action.
     * @param selectionProvider Provides the elements to be refactored.
     * 
     * @throws NullPointerException If <tt>shell</tt> is <tt>null</tt>.
     */
    protected IpsRefactoringAction(Shell shell, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        ArgumentCheck.notNull(shell);
        this.shell = shell;
    }

    /**
     * Checks the initial conditions for the given <tt>Refactoring</tt>.
     * <p>
     * Returns <tt>true</tt> if the check succeeded, <tt>false</tt> otherwise.
     * 
     * @param refactoring The <tt>Refactoring</tt> to work with.
     * 
     * @throws CoreException If an error occurs during initial condition checking.
     * @throws NullPointerException If <tt>refactoring</tt> is <tt>null</tt>.
     */
    protected final boolean checkInitialConditions(Refactoring refactoring) throws CoreException {
        ArgumentCheck.notNull(refactoring);
        RefactoringStatus status = refactoring.checkInitialConditions(new NullProgressMonitor());
        if (!(status.isOK())) {
            MessageDialog.openInformation(getShell(), refactoring.getName(),
                    Messages.IpsRefactoringAction_refactoringCurrentlyNotApplicable + "\n\n      - "
                            + status.getEntryWithHighestSeverity().getMessage());
            return false;
        }
        return true;
    }

    /**
     * Opens the provided <tt>RefactoringWizard</tt> inside a <tt>RefactoringDialog</tt>.
     * <p>
     * While the wizard is opened the workspace root will be blocked so that work done to the
     * workspace by the refactoring cannot be interrupted.
     * 
     * @param refactoringWizard The <tt>RefactoringWizard</tt> that is to be opened.
     * 
     * @throws NullPointerException If <tt>refactoringWizard</tt> is <tt>null</tt>.
     */
    protected final void openWizard(RefactoringWizard refactoringWizard) {
        ArgumentCheck.notNull(refactoringWizard);
        IJobManager jobManager = Job.getJobManager();
        jobManager.beginRule(ResourcesPlugin.getWorkspace().getRoot(), null);
        try {
            Dialog dialog = new RefactoringDialog(getShell(), refactoringWizard);
            dialog.create();
            dialog.open();
        } finally {
            jobManager.endRule(ResourcesPlugin.getWorkspace().getRoot());
        }
    }

    /** Returns the <tt>Shell</tt> used by this action. */
    public final Shell getShell() {
        return shell;
    }

}
