/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.core.refactor.IpsSrcFileModificationSet;
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
    protected IpsSrcFileModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException {
        IpsSrcFileModificationSet modifications = createDefaultModifications();
        getEnumLiteralNameAttributeValue().setValue(getNewName());
        return modifications;
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
