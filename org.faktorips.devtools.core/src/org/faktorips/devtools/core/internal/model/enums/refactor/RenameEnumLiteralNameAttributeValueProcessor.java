/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.util.message.MessageList;

/**
 * Refactoring processor for the "Rename Literal Name" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public class RenameEnumLiteralNameAttributeValueProcessor extends IpsRenameProcessor {

    public RenameEnumLiteralNameAttributeValueProcessor(IEnumLiteralNameAttributeValue literalNameAttributeValue) {
        super(literalNameAttributeValue, literalNameAttributeValue.getStringValue());
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<IIpsSrcFile>();
        result.add(getIpsSrcFile());
        return result;
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) throws CoreException {
        validationMessageList.add(getEnumLiteralNameAttributeValue().validate(getIpsProject()));
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getIpsElement());
        addAffectedSrcFiles(modificationSet);
        getEnumLiteralNameAttributeValue().setValue(ValueFactory.createStringValue(getNewName()));
        return modificationSet;
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
