/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;

/**
 * Refactoring processor for the "Pull Up Attribute" - refactoring.
 * 
 * @since 3.4
 * 
 * @author Alexander Weickmann
 */
public class PullUpAttributeProcessor extends IpsPullUpProcessor {

    public PullUpAttributeProcessor(IAttribute attribute) {
        super(attribute);
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<>();
        result.add(getIpsSrcFile());
        result.add(getTarget().getIpsSrcFile());
        return result;
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getIpsElement());
        try {
            addAffectedSrcFiles(modificationSet);
            IAttribute newAttribute = pullUpAttribute();
            modificationSet.setTargetElement(newAttribute);
            deleteOriginalAttribute();
        } catch (IpsException e) {
            modificationSet.undo();
            throw e;
        }
        return modificationSet;
    }

    private IAttribute pullUpAttribute() {
        IAttribute newAttribute = getTargetType().newAttribute();
        newAttribute.copyFrom(getAttribute());
        return newAttribute;
    }

    private void deleteOriginalAttribute() {
        getAttribute().delete();
    }

    /**
     * Checks that the type of the attribute to be refactored has a supertype and if it has that the
     * supertype can be found.
     */
    @Override
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) {
        if (!getType().hasSupertype()) {
            status.addFatalError(
                    NLS.bind(Messages.PullUpAttributeProcessor_msgTypeHasNoSupertype, getType().getName()));
            return;
        }

        if (getType().findSupertype(getIpsProject()) == null) {
            status.addFatalError(NLS.bind(Messages.PullUpAttributeProcessor_msgSupertypeCouldNotBeFound, getType()
                    .getSupertype()));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks that the target type is a supertype of the attribute's type and
     * that no attribute with the same name as the attribute to be refactored already exists in the
     * target type.
     * <p>
     * Furthermore, if the attribute is marked as overwrite the overwritten attribute must be found
     * in the super type hierarchy of the target type.
     */
    @Override
    public void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) {
        super.validateUserInputThis(status, pm);

        if (!getType().isSubtypeOf(getTargetType(), getIpsProject())) {
            status.addFatalError(Messages.PullUpAttributeProcessor_msgTargetTypeMustBeSupertype);
            return;
        }

        IAttribute targetAttribute = getTargetType().getAttribute(getAttribute().getName());
        if (targetAttribute != null) {
            status.addFatalError(NLS.bind(Messages.PullUpAttributeProcessor_msgAttributeAlreadyExistingInTargetType,
                    getAttribute().getName()));
            return;
        }

        if (getAttribute().isOverwrite()) {
            BaseOfOverriddenAttributeVisitor visitor = new BaseOfOverriddenAttributeVisitor(getIpsProject());
            visitor.start(getTargetType());
            if (!visitor.baseOfOverriddenAttributeFound) {
                status.addFatalError(Messages.PullUpAttributeProcessor_msgBaseOfOverwrittenAttributeNotFound);
            }
        }
    }

    @Override
    protected boolean isTargetTypeAllowed(IIpsObjectPartContainer target) {
        return target instanceof IType;
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.PullUpAttributeProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.PullUpAttributeProcessor_processorName;
    }

    private IIpsSrcFile getIpsSrcFile() {
        return getAttribute().getIpsSrcFile();
    }

    private IAttribute getAttribute() {
        return (IAttribute)getIpsElement();
    }

    private IType getType() {
        return getAttribute().getType();
    }

    private IType getTargetType() {
        return (IType)getTarget();
    }

    private class BaseOfOverriddenAttributeVisitor extends TypeHierarchyVisitor<IType> {

        private boolean baseOfOverriddenAttributeFound;

        private BaseOfOverriddenAttributeVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) {
            if (currentType.getAttribute(getAttribute().getName()) != null) {
                baseOfOverriddenAttributeFound = true;
                return false;
            }
            return true;
        }

    }

}
