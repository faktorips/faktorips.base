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

package org.faktorips.devtools.core.refactor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public abstract class IpsRefactoringDescriptor extends RefactoringDescriptor {

    private Map<String, String> arguments;

    protected IpsRefactoringDescriptor(String id, Map<String, String> arguments) {
        this(id, null, "NOT_AVAILABLE", null, arguments, RefactoringDescriptor.BREAKING_CHANGE
                | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
    }

    protected IpsRefactoringDescriptor(String id, String project, String description, String comment,
            Map<String, String> arguments, int flags) {

        super(id, project, description, comment, flags);
        this.arguments = arguments;
    }

    public void init() throws CoreException {
        initArguments();
    }

    protected abstract void initArguments() throws CoreException;

    public Map<String, String> getArguments() {
        return new HashMap<String, String>(arguments);
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
        IpsRefactoringContribution contribution = (IpsRefactoringContribution)RefactoringCore
                .getRefactoringContribution(getID());
        Refactoring refactoring = contribution.createRefactoring(this, status);
        return refactoring;
    }

}
