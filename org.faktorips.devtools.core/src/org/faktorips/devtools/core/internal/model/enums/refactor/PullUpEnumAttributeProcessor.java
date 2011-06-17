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

package org.faktorips.devtools.core.internal.model.enums.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.enums.EnumTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;

/**
 * Refactoring processor for the "Pull Up Enum Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public class PullUpEnumAttributeProcessor extends IpsPullUpProcessor {

    public PullUpEnumAttributeProcessor(IEnumAttribute enumAttribute) {
        super(enumAttribute);
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        addIpsSrcFile(getIpsSrcFile());
        addIpsSrcFile(getTarget().getIpsSrcFile());
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        pullUpEnumAttribute();
        deleteOriginalEnumAttribute();
    }

    private void pullUpEnumAttribute() throws CoreException {
        IEnumAttribute newEnumAttribute = getTargetEnumType().newEnumAttribute();
        newEnumAttribute.copyFrom(getEnumAttribute());
    }

    private void deleteOriginalEnumAttribute() {
        getEnumAttribute().delete();
    }

    /**
     * Checks that the enumeration attribute to be refactored is not the literal name attribute.
     * <p>
     * Furthermore, checks that the enumeration type of the enumeration attribute to be refactored
     * has a super enumeration type and if it has that the super enumeration type can be found.
     */
    @Override
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (getEnumAttribute() instanceof IEnumLiteralNameAttribute) {
            status.addFatalError(Messages.PullUpEnumAttributeProcessor_msgLiteralNameAttributeCannotBePulledUp);
            return;
        }

        if (!getEnumType().hasSuperEnumType()) {
            status.addFatalError(NLS.bind(Messages.PullUpEnumAttributeProcessor_msgEnumTypeHasNoSuperEnumType,
                    getEnumType().getName()));
            return;
        }

        if (getEnumType().findSuperEnumType(getIpsProject()) == null) {
            status.addFatalError(NLS.bind(Messages.PullUpEnumAttributeProcessor_msgSuperEnumTypeCannotBeFound,
                    getEnumType().getSuperEnumType()));
            return;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks that the target enumeration type is a super enumeration type of
     * the enumeration attribute's enumeration type and that no enumeration attribute with the same
     * name as the enumeration attribute to be refactored already exists in the target enumeration
     * type.
     * <p>
     * Furthermore, if the enumeration attribute is marked as inherited the base enumeration
     * attribute must be found in the super type hierarchy of the target enumeration type.
     */
    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (!getEnumType().isSubEnumTypeOf(getTargetEnumType(), getIpsProject())) {
            status.addFatalError(Messages.PullUpEnumAttributeProcessor_msgTargetEnumTypeMustBeASupertype);
            return;
        }

        if (getTargetEnumType().containsEnumAttribute(getEnumAttribute().getName())) {
            status.addFatalError(NLS.bind(Messages.PullUpEnumAttributeProcessor_msgEnumAttributeAlreadyExistsInTarget,
                    getEnumAttribute().getName()));
            return;
        }

        if (getEnumAttribute().isInherited()) {
            BaseOfOverriddenAttributeVisitor visitor = new BaseOfOverriddenAttributeVisitor(getIpsProject());
            visitor.start(getTargetEnumType());
            if (!visitor.baseOfOverriddenAttributeFound) {
                status.addFatalError(Messages.PullUpEnumAttributeProcessor_msgEnumAttributeBaseOfInheritedAttributeNotFound);
                return;
            }
        }
    }

    @Override
    protected boolean isTargetTypeAllowed(IIpsObjectPartContainer target) {
        return target instanceof IEnumType;
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.enums.refactor.PullUpEnumAttributeProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.PullUpEnumAttributeProcessor_processorName;
    }

    private IIpsSrcFile getIpsSrcFile() {
        return getEnumAttribute().getIpsSrcFile();
    }

    private IEnumAttribute getEnumAttribute() {
        return (IEnumAttribute)getIpsElement();
    }

    private IEnumType getEnumType() {
        return getEnumAttribute().getEnumType();
    }

    private IEnumType getTargetEnumType() {
        return (IEnumType)getTarget();
    }

    private class BaseOfOverriddenAttributeVisitor extends EnumTypeHierarchyVisitor {

        private boolean baseOfOverriddenAttributeFound;

        private BaseOfOverriddenAttributeVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IEnumType currentType) throws CoreException {
            if (currentType.containsEnumAttribute(getEnumAttribute().getName())) {
                baseOfOverriddenAttributeFound = true;
                return false;
            }
            return true;
        }

    }

}
