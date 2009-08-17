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

    /** The <tt>IEnumType</tt> this <tt>IEnumContent</tt> is build upon. */
    private String enumType;

    /**
     * The number of <tt>IEnumAttribute</tt>s to be referenced by <tt>IEnumAttributeValue</tt>s.
     * <p>
     * This number will become invalid when a new <tt>IEnumAttribute</tt> is added to the referenced
     * <tt>IEnumType</tt> or an <tt>IEnumAttribute</tt> is deleted from the referenced
     * <tt>IEnumType</tt>. The whole <tt>IEnumContent</tt> is invalid then and needs to be fixed by
     * the user.
     */
    private int enumAttributesCount;

    /**
     * Creates a new <code>EnumContent</code>.
     * 
     * @param file The IPS source file in which this <tt>IEnumContent</tt> will be stored in.
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

        IIpsSrcFile ipsSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, enumType);
        if(ipsSrcFile != null && ipsSrcFile.exists()){
            return (IEnumType)ipsSrcFile.getIpsObject();
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
        initValidationCacheUniqueIdentifierEntries(uniqueEnumAttributes, referencedEnumType);
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
            IEnumType referencedEnumType = findEnumType(ipsProject);
            EnumContentValidations.validateEnumContentName(list, this, referencedEnumType, getQualifiedName());
            validateReferencedEnumAttributesCount(list, referencedEnumType, ipsProject);
        }
    }

    /**
     * Validates the number of referenced <tt>IEnumAttribute</tt>s. This number is invalid if it
     * does not correspond to the number of <tt>IEnumAttribute</tt>s stored in the referenced
     * <tt>IEnumType</tt> (without counting literal name attributes).
     */
    private void validateReferencedEnumAttributesCount(MessageList validationMessageList, IEnumType enumType, IIpsProject ipsProject)
            throws CoreException {

        if (enumType.getEnumAttributesCountIncludeSupertypeCopies(false) != getReferencedEnumAttributesCount()) {
            String text = NLS.bind(Messages.EnumContent_ReferencedEnumAttributesCountInvalid, enumType
                    .getQualifiedName());
            Message validationMessage = new Message(
                    IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(this,
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

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>true</tt> if the number of referenced enumeration attributes does not correspond
     * to the number of enumeration attributes stored in the base enumeration type (without counting
     * literal name attributes).
     * <p>
     * Returns <tt>false</tt> if the numbers match or the base enumeration type could not be found.
     */
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException {
        IEnumType enumType = findEnumType(ipsProject);
        if (enumType == null) {
            return false;
        }
        return enumType.getEnumAttributesCountIncludeSupertypeCopies(false) != getReferencedEnumAttributesCount();
    }

    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreException {
        // TODO AW: What shall we do here?
    }

    public String getMetaClass() {
        return getEnumType();
    }

}
