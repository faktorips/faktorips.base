/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums.refactor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.model.enums.EnumTypeHierarchyVisitor;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Refactoring processor for the "Pull Up Enum Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public class PullUpEnumAttributeProcessor extends IpsPullUpProcessor {

    /** Set containing all sub enum types of the target enum type. */
    private final Set<IEnumType> subEnumTypes = new HashSet<>();

    public PullUpEnumAttributeProcessor(IEnumAttribute enumAttribute) {
        super(enumAttribute);
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<>();
        result.add(getIpsSrcFile());

        result.add(getTarget().getIpsSrcFile());

        // Sub enum types of target enum type
        for (IIpsSrcFile ipsSrcFile : findReferencingIpsSrcFiles(IpsObjectType.ENUM_TYPE)) {
            IEnumType enumType = (IEnumType)ipsSrcFile.getIpsObject();
            if (enumType.isSubEnumTypeOf(getTargetEnumType(), getIpsProject()) && !enumType.equals(getEnumType())) {
                result.add(ipsSrcFile);
                subEnumTypes.add(enumType);
            }
        }
        return result;
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreRuntimeException {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getIpsElement());
        try {
            addAffectedSrcFiles(modificationSet);

            IEnumAttribute newEnumAttr = pullUpEnumAttribute();
            modificationSet.setTargetElement(newEnumAttr);
            markeOriginalEnumAttributeInherited();
            inheritEnumAttributeInSubclassesOfTarget();
        } catch (CoreRuntimeException e) {
            modificationSet.undo();
            throw e;
        }
        return modificationSet;
    }

    private IEnumAttribute pullUpEnumAttribute() throws CoreRuntimeException {
        IEnumAttribute newEnumAttribute = getTargetEnumType().newEnumAttribute();
        newEnumAttribute.copyFrom(getEnumAttribute());
        return newEnumAttribute;
    }

    private void markeOriginalEnumAttributeInherited() {
        getEnumAttribute().setInherited(true);
    }

    private void inheritEnumAttributeInSubclassesOfTarget() throws CoreRuntimeException {
        IEnumAttribute pulledUpAttribute = getTargetEnumType().getEnumAttribute(getEnumAttribute().getName());
        for (IEnumType subEnumType : subEnumTypes) {
            subEnumType.inheritEnumAttributes(Arrays.asList(pulledUpAttribute));
        }
    }

    /**
     * Checks that the enumeration attribute to be refactored is not the literal name attribute.
     * <p>
     * Furthermore, checks that the enumeration type of the enumeration attribute to be refactored
     * has a super enumeration type and if it has that the super enumeration type can be found.
     */
    @Override
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) {
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
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreRuntimeException {
        super.validateUserInputThis(status, pm);

        if (!getEnumType().isSubEnumTypeOf(getTargetEnumType(), getIpsProject())) {
            status.addFatalError(Messages.PullUpEnumAttributeProcessor_msgTargetEnumTypeMustBeASupertype);
            return;
        }

        if (getTargetEnumType().containsEnumAttributeIncludeSupertypeCopies(getEnumAttribute().getName())) {
            status.addFatalError(NLS.bind(Messages.PullUpEnumAttributeProcessor_msgEnumAttributeAlreadyExistsInTarget,
                    getEnumAttribute().getName()));
            return;
        }

        if (getEnumAttribute().isInherited()) {
            BaseOfOverriddenAttributeVisitor visitor = new BaseOfOverriddenAttributeVisitor(getIpsProject());
            visitor.start(getTargetEnumType());
            if (!visitor.baseOfOverriddenAttributeFound) {
                status.addFatalError(
                        Messages.PullUpEnumAttributeProcessor_msgEnumAttributeBaseOfInheritedAttributeNotFound);
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
        protected boolean visit(IEnumType currentType) {
            if (currentType.containsEnumAttribute(getEnumAttribute().getName())) {
                baseOfOverriddenAttributeFound = true;
                return false;
            }
            return true;
        }

    }

}
