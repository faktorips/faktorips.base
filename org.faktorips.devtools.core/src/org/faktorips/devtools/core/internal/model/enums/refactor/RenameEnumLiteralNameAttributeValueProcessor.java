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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.MessageList;

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
        HashSet<IIpsSrcFile> result = new HashSet<>();
        result.add(getIpsSrcFile());
        return result;
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) {
        validationMessageList.add(getEnumLiteralNameAttributeValue().validate(getIpsProject()));
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getIpsElement());
        try {
            addAffectedSrcFiles(modificationSet);
            getEnumLiteralNameAttributeValue().setValue(ValueFactory.createStringValue(getNewName()));
        } catch (IpsException e) {
            modificationSet.undo();
            throw e;
        }
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
