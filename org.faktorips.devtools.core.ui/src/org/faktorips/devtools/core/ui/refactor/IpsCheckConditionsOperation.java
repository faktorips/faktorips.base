/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.refactor;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.util.ArgumentCheck;

/**
 * Operation that allows to check the initial, final, or all conditions of a Faktor-IPS refactoring.
 * <p>
 * The user can be requested to save all opened editors (if this is necessary). The user's decision
 * then can be retrieved by the method {@link #getEditorsSaved()}. Condition checking will be
 * aborted if the user did not confirm saving the opened editors.
 * 
 * @author Alexander Weickmann
 */
public class IpsCheckConditionsOperation {

    public static final int NONE = CheckConditionsOperation.NONE;

    public static final int INITIAL_CONDITIONS = CheckConditionsOperation.INITIAL_CONDITONS;

    public static final int FINAL_CONDITIONS = CheckConditionsOperation.FINAL_CONDITIONS;

    public static final int ALL_CONDITIONS = CheckConditionsOperation.ALL_CONDITIONS;

    private final CheckConditionsOperation ltkCheckOperation;

    private final boolean ensureEditorsSaved;

    private boolean editorsSaved;

    /**
     * @param refactoring The refactoring for which to check the conditions
     * @param conditionType One of {@code INITIAL_CONDITIONS}, {@code FINAL_CONDITIONS} and
     *            {@code ALL_CONDITIONS}
     * @param ensureEditorsSaved Flag indicating whether the user is requested to save all opened
     *            editors before the refactoring may happen
     * 
     * @throws NullPointerException If the refactoring is null
     */
    public IpsCheckConditionsOperation(IIpsRefactoring refactoring, int conditionType, boolean ensureEditorsSaved) {
        ArgumentCheck.notNull(refactoring);
        this.ensureEditorsSaved = ensureEditorsSaved;
        ltkCheckOperation = conditionType == NONE ? null : new CheckConditionsOperation((Refactoring)refactoring,
                conditionType);
    }

    public void run(IProgressMonitor progressMonitor) throws CoreException {
        if (ensureEditorsSaved) {
            editorsSaved = IpsPlugin.getDefault().getWorkbench().saveAllEditors(true);
        }
        if ((ltkCheckOperation != null) && (editorsSaved || !(ensureEditorsSaved))) {
            ResourcesPlugin.getWorkspace().run(ltkCheckOperation, progressMonitor);
        }
    }

    /**
     * Returns the outcome of the operation or null if an error occurred during condition checking,
     * the operation has not been executed yet or the user did not confirm to save all opened
     * editors.
     */
    public RefactoringStatus getStatus() {
        return ltkCheckOperation == null ? new RefactoringStatus() : ltkCheckOperation.getStatus();
    }

    /**
     * Returns whether the user has confirmed that all opened editors have been saved.
     */
    public boolean getEditorsSaved() {
        return editorsSaved;
    }

}
