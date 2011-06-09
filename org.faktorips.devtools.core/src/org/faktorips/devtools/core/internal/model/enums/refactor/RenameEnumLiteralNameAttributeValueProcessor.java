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

package org.faktorips.devtools.core.internal.model.enums.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.util.message.MessageList;

/**
 * Refactoring processor for the "Rename Literal Name" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public class RenameEnumLiteralNameAttributeValueProcessor extends IpsRenameProcessor {

    public RenameEnumLiteralNameAttributeValueProcessor(IEnumLiteralNameAttributeValue literalNameAttributeValue) {
        super(literalNameAttributeValue, literalNameAttributeValue.getValue());
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        IEnumAttributeValue enumAttributeValue = getEnumLiteralNameAttributeValue();
        enumAttributeValue.setValue(getNewName());

        enumAttributeValue.getIpsModel().clearValidationCache();
        MessageList validationMessageList = enumAttributeValue.validate(getIpsProject());
        addValidationMessagesToStatus(validationMessageList, status);

        enumAttributeValue.setValue(getOriginalName());
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        addIpsSrcFile(getIpsSrcFile());
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        getEnumLiteralNameAttributeValue().setValue(getNewName());
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumLiteralNameAttributeValueProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameEnumLiteralNameAttributeValueProcessor_processorName;
    }

    private IEnumAttributeValue getEnumLiteralNameAttributeValue() {
        return (IEnumAttributeValue)getIpsElement();
    }

    private IIpsSrcFile getIpsSrcFile() {
        return getEnumLiteralNameAttributeValue().getIpsSrcFile();
    }

}
