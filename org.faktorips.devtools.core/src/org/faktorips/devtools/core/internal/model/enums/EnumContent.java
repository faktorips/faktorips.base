/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.enums.EnumContentValidations;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumContent</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContent extends EnumValueContainer implements IEnumContent {

    /** The enum type this enum values is build upon. */
    private String enumType;

    /**
     * The number of enum attributes to be referenced by enum attribute values.
     * <p>
     * This number will become invalid when a new enum attribute is added to the referenced enum
     * type or an enum attribute is deleted from the referenced enum type. The whole enum content is
     * invalid then and needs to be fixed by the user.
     */
    private int enumAttributesCount;

    /**
     * Creates a new <code>EnumContent</code>.
     * 
     * @param file The ips source file in which this enum content will be stored in.
     */
    public EnumContent(IIpsSrcFile file) {
        super(file);

        enumType = null;
        enumAttributesCount = 0;
    }

    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.ENUM_CONTENT;
    }

    public IEnumType findEnumType(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        IIpsSrcFile[] enumTypeSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.ENUM_TYPE);

        for (IIpsSrcFile currentIpsSrcFile : enumTypeSrcFiles) {
            if (currentIpsSrcFile.getIpsObject().getQualifiedName().equals(enumType)) {
                return (IEnumType)currentIpsSrcFile.getIpsObject();
            }
        }

        return null;
    }

    public void setEnumType(String enumType) throws CoreException {
        ArgumentCheck.notNull(enumType);

        String oldEnumType = this.enumType;
        this.enumType = enumType;
        valueChanged(oldEnumType, enumType);

        IEnumType newEnumType = findEnumType(getIpsProject());
        if (newEnumType != null) {
            setEnumAttributesCount(newEnumType.getEnumAttributesCountIncludeSupertypeCopies(false));
        }
    }

    /**
     * Sets the number of <tt>EnumAttribute</tt>s that must be referred by
     * <tt>EnumAttributeValue</tt>s by this <tt>EnumContent</tt>.
     */
    private void setEnumAttributesCount(int enumAttributesCount) {
        int oldEnumAttributesCount = this.enumAttributesCount;
        this.enumAttributesCount = enumAttributesCount;
        valueChanged(oldEnumAttributesCount, enumAttributesCount);
    }

    @Override
    public boolean initUniqueIdentifierValidationCacheImpl() throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        IEnumType referencedEnumType = findEnumType(ipsProject);

        /*
         * If we can't find the base EnumType we can't initialize the validation cache. This is no
         * problem however, because unique identifiers are not validated as long as the base
         * EnumType cannot be found.
         */
        if (referencedEnumType == null) {
            return false;
        }

        List<IEnumAttribute> uniqueEnumAttributes = referencedEnumType.findUniqueEnumAttributes(false, ipsProject);
        for (IEnumAttribute currentUniqueAttribute : uniqueEnumAttributes) {
            addUniqueIdentifierToValidationCache(referencedEnumType.getIndexOfEnumAttribute(currentUniqueAttribute));
        }
        initValidationCacheUniqueIdentifierEntries(uniqueEnumAttributes);
        return true;
    }

    @Override
    protected void initFromXml(Element element, Integer id) {
        enumType = element.getAttribute(PROPERTY_ENUM_TYPE);
        enumAttributesCount = Integer.parseInt(element.getAttribute(PROPERTY_REFERENCED_ENUM_ATTRIBUTES_COUNT));

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_ENUM_TYPE, enumType);
        element.setAttribute(PROPERTY_REFERENCED_ENUM_ATTRIBUTES_COUNT, String.valueOf(enumAttributesCount));
    }

    public String getEnumType() {
        return enumType;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        EnumContentValidations.validateEnumType(list, this, enumType, ipsProject);
        if (list.getNoOfMessages() == 0) {
            EnumContentValidations.validateEnumContentName(list, this, findEnumType(ipsProject), getQualifiedName());
        }

        validateReferencedEnumAttributesCount(list, this, ipsProject);
    }

    /**
     * Validates the number of referenced enum attributes. This number is invalid if it does not
     * correspond to the number of enum attributes in the referenced enum type.
     */
    private void validateReferencedEnumAttributesCount(MessageList validationMessageList,
            IEnumContent enumContent,
            IIpsProject ipsProject) throws CoreException {

        IEnumType enumType = enumContent.findEnumType(ipsProject);
        if (enumType == null) {
            return;
        }

        if (enumType.getEnumAttributesCountIncludeSupertypeCopies(false) != enumContent
                .getReferencedEnumAttributesCount()) {
            String text = NLS.bind(Messages.EnumContent_ReferencedEnumAttributesCountInvalid, enumType
                    .getQualifiedName());
            Message validationMessage = new Message(
                    IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(enumContent,
                            IEnumContent.PROPERTY_REFERENCED_ENUM_ATTRIBUTES_COUNT) });
            validationMessageList.add(validationMessage);
        }
    }

    @Override
    public IDependency[] dependsOn() throws CoreException {
        IDependency enumTypeDependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                new QualifiedNameType(getEnumType(), IpsObjectType.ENUM_TYPE));
        return new IDependency[] { enumTypeDependency };
    }

    public int getReferencedEnumAttributesCount() {
        return enumAttributesCount;
    }

    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, getEnumType());
    }

    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException {
        // TODO AW: Auto-generated method stub
        return false;
    }

    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreException {
        // TODO AW: Auto-generated method stub
    }

    public String getMetaClass() {
        return getEnumType();
    }

}
