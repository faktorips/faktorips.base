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

import java.util.ArrayList;
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
 * Implementation of <tt>IEnumContent</tt>, see the corresponding interface for more details.
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
     * A <tt>String</tt> representing a list containing all referenced <tt>IEnumAttribute</tt>s. The
     * ordering is important.
     */
    private String referencedEnumAttributes;

    /**
     * A list containing the names of all referenced <tt>IEnumAttribute</tt>s as stored in this
     * <tt>IEnumContent</tt>.
     */
    private List<String> referencedEnumAttributeNames;

    /**
     * Creates a new <tt>EnumContent</tt>.
     * 
     * @param file The IPS source file in which this <tt>IEnumContent</tt> will be stored in.
     */
    public EnumContent(IIpsSrcFile file) {
        super(file);
        enumType = "";
        referencedEnumAttributes = "";
        referencedEnumAttributeNames = new ArrayList<String>();
    }

    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.ENUM_CONTENT;
    }

    public IEnumType findEnumType(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        IIpsSrcFile ipsSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, enumType);
        if (ipsSrcFile != null && ipsSrcFile.exists()) {
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
            List<IEnumAttribute> enumAttributes = newEnumType.getEnumAttributesIncludeSupertypeCopies(false);
            String referencedEnumAttributes = "";
            for (IEnumAttribute currentReferencedEnumAttribute : enumAttributes) {
                referencedEnumAttributes += currentReferencedEnumAttribute.getName() + ", ";
            }
            if (referencedEnumAttributes.length() > 0) {
                setReferencedEnumAttributes(referencedEnumAttributes
                        .substring(0, referencedEnumAttributes.length() - 2));
            }
        }
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
        referencedEnumAttributes = element.getAttribute(PROPERTY_REFERENCED_ENUM_ATTRIBUTES);
        refreshAttributeNames();

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_ENUM_TYPE, enumType);
        element.setAttribute(PROPERTY_REFERENCED_ENUM_ATTRIBUTES, referencedEnumAttributes);
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
            validateReferencedEnumAttributes(list, referencedEnumType, ipsProject);
        }
    }

    /** Validates the referenced <tt>IEnumAttribute</tt>s as stored in this <tt>IEnumContent</tt>. */
    private void validateReferencedEnumAttributes(MessageList validationMessageList,
            IEnumType enumType,
            IIpsProject ipsProject) throws CoreException {

        /*
         * First, the number of referenced EnumAttributes as stored in this EnumContent must match
         * the number defined in the EnumType.
         */
        if (enumType.getEnumAttributesCountIncludeSupertypeCopies(false) != getReferencedEnumAttributesCount()) {
            String text = NLS.bind(Messages.EnumContent_ReferencedEnumAttributesCountInvalid, enumType
                    .getQualifiedName());
            Message validationMessage = new Message(
                    IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(this, IEnumContent.PROPERTY_REFERENCED_ENUM_ATTRIBUTES) });
            validationMessageList.add(validationMessage);
            return;
        }

        /*
         * Second, the names of the referenced EnumAttributes as stored in this EnumContent must
         * match the names as defined in the EnumType.
         */
        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (!(referencedEnumAttributeNames.contains(currentEnumAttribute.getName()))) {
                String text = NLS.bind(Messages.EnumContent_ReferencedEnumAttributeNamesInvalid, enumType
                        .getQualifiedName());
                Message validationMessage = new Message(
                        IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID, text, Message.ERROR,
                        new ObjectProperty[] { new ObjectProperty(this,
                                IEnumContent.PROPERTY_REFERENCED_ENUM_ATTRIBUTES) });
                validationMessageList.add(validationMessage);
                return;
            }
        }

        /*
         * Third, the ordering of the referenced EnumAttributes as stored in this EnumContent must
         * match the ordering as defined in the EnumType.
         */
        for (int i = 0; i < enumAttributes.size(); i++) {
            String currentEnumAttributeName = enumAttributes.get(i).getName();
            String currentName = referencedEnumAttributeNames.get(i);
            if (!(currentEnumAttributeName.equals(currentName))) {
                String text = NLS.bind(Messages.EnumContent_ReferencedEnumAttributesOrderingInvalid, enumType
                        .getQualifiedName());
                Message validationMessage = new Message(
                        IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID, text,
                        Message.ERROR, new ObjectProperty[] { new ObjectProperty(this,
                                IEnumContent.PROPERTY_REFERENCED_ENUM_ATTRIBUTES) });
                validationMessageList.add(validationMessage);
                break;
            }
        }
    }

    @Override
    public IDependency[] dependsOn() throws CoreException {
        IDependency enumTypeDependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                new QualifiedNameType(getEnumType(), IpsObjectType.ENUM_TYPE));
        return new IDependency[] { enumTypeDependency };
    }

    public String getReferencedEnumAttributes() {
        return referencedEnumAttributes;
    }

    public List<String> getReferencedEnumAttributeNames() {
        return referencedEnumAttributeNames;
    }

    /**
     * Sets the referenced <tt>IEnumAttribute</tt>s.
     * 
     * @param referencedEnumAttributes A <tt>String</tt> representing a list containing the names of
     *            all referenced <tt>IEnumAttribute</tt>s in the right order.
     * 
     * @throws NullPointerException If <tt>referencedEnumAttributes</tt> is <tt>null</tt>.
     */
    private void setReferencedEnumAttributes(String referencedEnumAttributes) {
        ArgumentCheck.notNull(referencedEnumAttributes);

        String oldValue = this.referencedEnumAttributes;
        this.referencedEnumAttributes = referencedEnumAttributes;
        valueChanged(oldValue, referencedEnumAttributes);

        refreshAttributeNames();
    }

    /**
     * Refreshes the list containing all names of the referenced <tt>IEnumAttribute</tt>s as stored
     * in this <tt>IEnumContent</tt>.
     */
    private void refreshAttributeNames() {
        referencedEnumAttributeNames.clear();
        if (!(referencedEnumAttributes.contains(", "))) {
            if (referencedEnumAttributes.length() > 0) {
                referencedEnumAttributeNames.add(referencedEnumAttributes);
            }
            return;
        }
        String searchString = referencedEnumAttributes;
        while (searchString.contains(", ")) {
            int indexSeparator = searchString.indexOf(", ");
            String currentName = searchString.substring(0, indexSeparator);
            referencedEnumAttributeNames.add(currentName);
            searchString = searchString.substring(indexSeparator + 2);
        }
        referencedEnumAttributeNames.add(searchString);
    }

    public int getReferencedEnumAttributesCount() {
        return referencedEnumAttributeNames.size();
    }

    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, getEnumType());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <tt>false</tt>.
     */
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException {
        // TODO AW: What shall we do here?
        return false;
    }

    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreException {
        // TODO AW: What shall we do here?
    }

    public String getMetaClass() {
        return getEnumType();
    }

    public boolean isFixToModelRequired() throws CoreException {
        MessageList validationList = new MessageList();
        validateThis(validationList, getIpsProject());
        return validationList.getMessageByCode(MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST) != null
                || validationList.getMessageByCode(MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT) != null
                || validationList.getMessageByCode(MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING) != null
                || validationList.getMessageByCode(MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE) != null
                || validationList.getMessageByCode(MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID) != null
                || validationList.getMessageByCode(MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID) != null
                || validationList.getMessageByCode(MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID) != null;
    }

}
