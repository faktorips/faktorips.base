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
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.runtime.MessageList;

/**
 * Refactoring processor for the "Rename Enumeration Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public class RenameEnumAttributeProcessor extends IpsRenameProcessor {

    /** Set containing all potentially referencing enumeration types (subclasses). */
    private Set<IIpsSrcFile> enumTypeSrcFiles;

    public RenameEnumAttributeProcessor(IEnumAttribute enumAttribute) {
        super(enumAttribute, enumAttribute.getName());
    }

    private IIpsSrcFile getIpsSrcFile() {
        return getEnumAttribute().getIpsSrcFile();
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<>();
        result.add(getIpsSrcFile());
        enumTypeSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.ENUM_TYPE);
        for (IIpsSrcFile ipsSrcFile : enumTypeSrcFiles) {
            result.add(ipsSrcFile);
        }
        if ((getEnumType().isExtensible())) {
            IEnumContent enumContent = getEnumType().findEnumContent(getIpsProject());
            if (enumContent != null) {
                result.add(enumContent.getIpsSrcFile());
            }
        }
        return result;
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) {
        validationMessageList.add(getEnumAttribute().validate(getIpsProject()));
        validationMessageList.add(getEnumType().validate(getIpsProject()));
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getIpsElement());
        try {
            addAffectedSrcFiles(modificationSet);
            updateSubclassReferences();
            if (!(getEnumType().isAbstract())) {
                if (getEnumType().isInextensibleEnum()) {
                    updateLiteralNameReference();
                } else {
                    updateEnumContentReference();
                }
            }
            updateEnumAttributeName();
        } catch (IpsException e) {
            modificationSet.undo();
            throw e;
        }
        return modificationSet;
    }

    private void updateLiteralNameReference() {
        IEnumLiteralNameAttribute literalNameAttribute = getEnumType().getEnumLiteralNameAttribute();
        if (literalNameAttribute.getDefaultValueProviderAttribute().equals(getOriginalName())) {
            literalNameAttribute.setDefaultValueProviderAttribute(getNewName());
        }
    }

    private void updateSubclassReferences() {
        for (IIpsSrcFile ipsSrcFile : enumTypeSrcFiles) {
            IEnumType enumType = (IEnumType)ipsSrcFile.getIpsObject();
            boolean isSubEnumType = enumType.isSubEnumTypeOf(getEnumType(), getIpsProject());
            if (isSubEnumType) {
                IEnumAttribute inheritedAttribute = enumType.getEnumAttributeIncludeSupertypeCopies(getOriginalName());
                if (inheritedAttribute != null) {
                    inheritedAttribute.setName(getNewName());
                }
            }
        }
    }

    private void updateEnumContentReference() {
        IEnumContent enumContent = getEnumType().findEnumContent(getIpsProject());
        if (enumContent != null) {
            IPartReference attributeReference = enumContent.getEnumAttributeReference(getOriginalName());
            if (attributeReference != null) {
                attributeReference.setName(getNewName());
            }
        }
    }

    private void updateEnumAttributeName() {
        getEnumAttribute().setName(getNewName());
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    private IEnumType getEnumType() {
        return getEnumAttribute().getEnumType();
    }

    private IEnumAttribute getEnumAttribute() {
        return (IEnumAttribute)getIpsElement();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumAttributeProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameEnumAttributeProcessor_processorName;
    }

}
