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
public class PullUpAttributeProcessor extends IpsPullUpProcessor<IType> {

    public PullUpAttributeProcessor(IAttribute attribute) {
        super(attribute);
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        addIpsSrcFile(getIpsSrcFile());
        IType supertype = getType().findSupertype(getIpsProject());
        addIpsSrcFile(supertype.getIpsSrcFile());
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        pullUpAttribute();
        deleteOriginalAttribute();
    }

    /**
     * Pulls the attribute up to the target type and returns the new attribute.
     */
    private IAttribute pullUpAttribute() throws CoreException {
        IType superType = getType().findSupertype(getIpsProject());
        IAttribute newAttribute = superType.newAttribute();
        getAttribute().copy(newAttribute);
        return newAttribute;
    }

    private void deleteOriginalAttribute() {
        getAttribute().delete();
    }

    @Override
    public void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (!getType().isSubtypeOf(getTarget(), getIpsProject())) {
            status.addFatalError(Messages.PullUpAttributeProcessor_msgTargetTypeMustBeSupertype);
            return;
        }

        if (getTarget().getAttribute(getAttribute().getName()) != null) {
            status.addFatalError(NLS.bind(Messages.PullUpAttributeProcessor_msgAttributeAlreadyExistingInTargetType,
                    getAttribute().getName()));
            return;
        }
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

}
