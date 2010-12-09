/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.internal.refactor.IpsRenameProcessor;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.MessageList;

/**
 * Faktor-IPS "Rename Association" refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameAssociationProcessor extends IpsRenameProcessor {

    public RenameAssociationProcessor(IAssociation association) {
        super(association, association.getName(), association.getTargetRolePlural());
        setPluralNameRefactoringRequired(true);
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        getAssociation().setTargetRoleSingular(getNewName());
        getAssociation().setTargetRolePlural(getNewPluralName());

        MessageList validationMessageList = getAssociation().validate(getIpsProject());
        validationMessageList.add(getType().validate(getIpsProject()));
        addValidationMessagesToStatus(validationMessageList, status);

        getAssociation().setTargetRoleSingular(getOriginalName());
        getAssociation().setTargetRolePlural(getOriginalPluralName());
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        updateTargetRoleSingular();
        updateTargetRolePlural();
    }

    private void updateTargetRoleSingular() {
        getAssociation().setTargetRoleSingular(getNewName());
    }

    private void updateTargetRolePlural() {
        getAssociation().setTargetRolePlural(getNewPluralName());
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        addIpsSrcFile(getAssociation().getIpsSrcFile());
    }

    private IAssociation getAssociation() {
        return (IAssociation)getIpsElement();
    }

    private IType getType() {
        return getAssociation().getType();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameAssociationProcessor"; //$NON-NLS-1$;
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameAssociationProcessor_processorName;
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return false;
    }

}
