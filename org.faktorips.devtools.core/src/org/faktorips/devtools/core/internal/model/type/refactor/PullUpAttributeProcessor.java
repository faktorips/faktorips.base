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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IpsPullUpProcessor;

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
    protected void addIpsSrcFiles() {
        addIpsSrcFile(getIpsSrcFile());
        addIpsSrcFile(getTargetType().getIpsSrcFile());
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) {
        pullUpAttribute();
        deleteOriginalAttribute();
    }

    /**
     * Pulls the attribute up to the target type and returns the new attribute.
     */
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
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (!getType().hasSupertype()) {
            status.addFatalError(NLS.bind(Messages.PullUpAttributeProcessor_msgTypeHasNoSupertype, getType().getName()));
            return;
        }
        if (getType().findSupertype(getIpsProject()) == null) {
            status.addFatalError(NLS.bind(Messages.PullUpAttributeProcessor_msgSupertypeCouldNotBeFound, getType()
                    .getSupertype()));
            return;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks that the target type is a supertype of the attribute's type and
     * that no attribute with the same name as the attribute to be refactored already exists in the
     * target type.
     */
    @Override
    public void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (!getType().isSubtypeOf(getTargetType(), getIpsProject())) {
            status.addFatalError(Messages.PullUpAttributeProcessor_msgTargetTypeMustBeSupertype);
            return;
        }

        if (getTargetType().getAttribute(getAttribute().getName()) != null) {
            status.addFatalError(NLS.bind(Messages.PullUpAttributeProcessor_msgAttributeAlreadyExistingInTargetType,
                    getAttribute().getName()));
            return;
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

}
