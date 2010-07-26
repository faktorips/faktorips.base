/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.internal.refactor.IpsRenameProcessor;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.util.message.MessageList;

/**
 * This is the "Rename Enumeration Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public class RenameEnumAttributeProcessor extends IpsRenameProcessor {

    /** Set containing all potentially referencing enumeration types (subclasses). */
    private Set<IIpsSrcFile> enumTypeSrcFiles;

    /**
     * @param enumAttribute The <tt>IEnumAttribute</tt> to be refactored.
     */
    public RenameEnumAttributeProcessor(IEnumAttribute enumAttribute) {
        super(enumAttribute, enumAttribute.getName());

        /*
         * Refactoring the current default value provider attribute makes it necessary to ignore
         * this validation.
         */
        getIgnoredValidationMessageCodes()
                .add(
                        IEnumLiteralNameAttribute.MSGCODE_ENUM_LITERAL_NAME_ATTRIBUTE_DEFAULT_VALUE_PROVIDER_ATTRIBUTE_DOES_NOT_EXIST);
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        getEnumAttribute().setName(getNewName());

        getEnumAttribute().getIpsModel().clearValidationCache();
        MessageList validationMessageList = getEnumAttribute().validate(getIpsProject());
        validationMessageList.add(getEnumType().validate(getIpsProject()));
        addValidationMessagesToStatus(validationMessageList, status);

        getEnumAttribute().setName(getOriginalName());
    }

    private IIpsSrcFile getIpsSrcFile() {
        return getEnumAttribute().getIpsSrcFile();
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        addIpsSrcFile(getIpsSrcFile());
        enumTypeSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.ENUM_TYPE);
        for (IIpsSrcFile ipsSrcFile : enumTypeSrcFiles) {
            addIpsSrcFile(ipsSrcFile);
        }
        if (!(getEnumType().isContainingValues())) {
            IEnumContent enumContent = getEnumType().findEnumContent(getIpsProject());
            if (enumContent != null) {
                addIpsSrcFile(enumContent.getIpsSrcFile());
            }
        }
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        updateSubclassReferences();
        if (!(getEnumType().isAbstract())) {
            if (getEnumType().isContainingValues()) {
                updateLiteralNameReference();
            } else {
                updateEnumContentReference();
            }
        }
        updateEnumAttributeName();
    }

    /** Updates the default value provider reference in the literal name attribute. */
    private void updateLiteralNameReference() {
        IEnumLiteralNameAttribute literalNameAttribute = getEnumType().getEnumLiteralNameAttribute();
        if (literalNameAttribute.getDefaultValueProviderAttribute().equals(getOriginalName())) {
            literalNameAttribute.setDefaultValueProviderAttribute(getNewName());
        }
    }

    /** Updates all inherited attributes based on the enumeration attribute to be refactored. */
    private void updateSubclassReferences() throws CoreException {
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

    /** Updates the reference to the enumeration attribute in the enumeration content. */
    private void updateEnumContentReference() throws CoreException {
        IEnumContent enumContent = getEnumType().findEnumContent(getIpsProject());
        if (enumContent != null) {
            IEnumAttributeReference attributeReference = enumContent.getEnumAttributeReference(getOriginalName());
            if (attributeReference != null) {
                attributeReference.setName(getNewName());
            }
        }
    }

    /** Updates the name of the enumeration attribute itself. */
    private void updateEnumAttributeName() {
        getEnumAttribute().setName(getNewName());
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
