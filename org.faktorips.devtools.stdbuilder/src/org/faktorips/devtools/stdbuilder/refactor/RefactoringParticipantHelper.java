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

package org.faktorips.devtools.stdbuilder.refactor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;

/**
 * Because all refactoring participants must be derived from an abstract LTK base class we can't use
 * inheritance ourselves to provide common functionality for the standard builder refactoring
 * participants.
 * <p>
 * Instead of using inheritance every participant may hold a <tt>RefactoringParticipantHelper</tt>
 * providing basic functionality common to all standard builder participants (delegation).
 * 
 * @author Alexander Weickmann
 */
public final class RefactoringParticipantHelper {

    /**
     * List containing the <tt>IJavaElement</tt>s generated for the <tt>IIpsElement</tt> to be
     * refactored.
     */
    private List<IJavaElement> generatedJavaElements;

    /**
     * List containing the <tt>IJavaElement</tt>s generated for the <tt>IIpsElement</tt> to be
     * refactored as they will be after the refactoring. This information is needed to be able to
     * provide the JDT refactorings with the correct new names for the <tt>IJavaElement</tt>s.
     */
    private List<IJavaElement> newJavaElements;

    /**
     * Checks for errors in <tt>ICompilationUnit</tt>s of the generated <tt>IJavaElement</tt>s.
     * 
     * @see RefactoringParticipant#checkConditions(IProgressMonitor, CheckConditionsContext)
     */
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {

        // List to remember broken compilation units.
        List<ICompilationUnit> invalidCompilationUnits = new LinkedList<ICompilationUnit>();

        RefactoringStatus status = new RefactoringStatus();
        for (IJavaElement javaElement : generatedJavaElements) {
            IType type = null;
            if (javaElement instanceof IType) {
                type = (IType)javaElement;
            } else if (javaElement instanceof IField) {
                IField field = (IField)javaElement;
                type = (IType)field.getParent();
            } else if (javaElement instanceof IMethod) {
                IMethod method = (IMethod)javaElement;
                type = (IType)method.getParent();
            }

            // Only report each broken compilation unit once.
            if (invalidCompilationUnits.contains(type.getCompilationUnit())) {
                continue;
            }

            try {
                if (type.getCompilationUnit().exists()) {
                    if (!(type.getCompilationUnit().isStructureKnown())) {
                        invalidCompilationUnits.add(type.getCompilationUnit());
                        status.addFatalError(NLS.bind(Messages.RefactoringParticipantHelper_errorSyntaxErrors, type
                                .getCompilationUnit().getElementName()));
                    }
                }
            } catch (JavaModelException e) {
                throw new RuntimeException(e);
            }
        }

        return status;
    }

    /**
     * Initializes the list of generated <tt>IJavaElement</tt>s for the provided
     * <tt>IIpsElement</tt> and returns <tt>true</tt>.
     * <p>
     * Returns <tt>false</tt> if the element passed to this operation is not an <tt>IIpsElement</tt>.
     * <p>
     * Refactoring participants still need to initialize the <tt>IJavaElement</tt>s that will be
     * generated for the <tt>IIpsElement</tt> after the refactoring has finished by themselves.
     * 
     * @see RefactoringParticipant#initialize(Object)
     */
    public boolean initialize(Object element) {
        if (!(element instanceof IIpsElement)) {
            return false;
        }

        IIpsElement ipsElement = (IIpsElement)element;
        StandardBuilderSet builderSet = (StandardBuilderSet)ipsElement.getIpsProject().getIpsArtefactBuilderSet();
        generatedJavaElements = builderSet.getGeneratedJavaElements(ipsElement);

        return true;
    }

    /**
     * Provides access to the list of <tt>IJavaElement</tt>s that will be generated for the
     * <tt>IIpsElement</tt> after the refactoring has finished.
     * <p>
     * <strong>Important:</strong> This operation will return <tt>null</tt> as long as
     * <tt>setNewJavaElements(List)</tt> has not been used to initialize the list.
     * 
     * @see #setNewJavaElements(List)
     */
    public List<IJavaElement> getNewJavaElements() {
        return newJavaElements;
    }

    /**
     * Allows to set the list of <tt>IJavaElement</tt>s that will be generated for the
     * <tt>IIpsElement</tt> after the refactoring has finished.
     * 
     * @throws NullPointerException If <tt>newJavaElement</tt> is <tt>null</tt>.
     */
    public void setNewJavaElements(List<IJavaElement> newJavaElements) {
        ArgumentCheck.notNull(newJavaElements);
        this.newJavaElements = newJavaElements;
    }

    /**
     * Provides access to the list <tt>IJavaElement</tt> generated for the provided
     * <tt>IIpsElement</tt>.
     */
    public List<IJavaElement> getGeneratedJavaElements() {
        return generatedJavaElements;
    }

}
