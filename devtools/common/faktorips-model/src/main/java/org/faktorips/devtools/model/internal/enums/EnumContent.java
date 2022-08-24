/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumContent</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumContent extends EnumValueContainer implements IEnumContent {

    private static final List<String> FIX_REQUIRING_ERROR_CODES = Arrays.asList(
            MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST, MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT,
            MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING, MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE,
            MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID,
            MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID,
            MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID,
            IEnumAttributeValue.MSGCODE_INVALID_VALUE_TYPE);

    /** The <code>IEnumType</code> this <code>IEnumContent</code> is build upon. */
    private String enumType;

    /**
     * Collection containing <code>IPartReference</code>s that belong to this
     * <code>IEnumContent</code>.
     */
    private IpsObjectPartCollection<IPartReference> enumAttributeReferences;

    /**
     * Creates a new <code>IEnumContent</code>.
     * 
     * @param file The IPS source file in which this <code>IEnumContent</code> will be stored in.
     */
    public EnumContent(IIpsSrcFile file) {
        super(file);

        enumType = IpsStringUtils.EMPTY;
        enumAttributeReferences = new IpsObjectPartCollection<>(this, EnumAttributeReference.class,
                IPartReference.class, EnumAttributeReference.XML_TAG);
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.ENUM_CONTENT;
    }

    @Override
    public IEnumType findEnumType(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        IIpsSrcFile ipsSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, enumType);
        if (ipsSrcFile != null && ipsSrcFile.exists()) {
            return (IEnumType)ipsSrcFile.getIpsObject();
        }
        return null;
    }

    @Override
    public void setEnumType(String enumType) {
        ArgumentCheck.notNull(enumType);

        String oldEnumType = this.enumType;
        this.enumType = enumType;
        valueChanged(oldEnumType, enumType);

        refreshEnumAttributeReferences();
    }

    @Override
    public List<IEnumValue> findAggregatedEnumValues() {
        List<IEnumValue> aggrregatedEnumValues = findEnumType(getIpsProject()).getEnumValues();
        aggrregatedEnumValues.addAll(getEnumValues());
        return aggrregatedEnumValues;
    }

    /**
     * Refreshes the <code>IPartReference</code>s that belong to this <code>IEnumContent</code> by
     * looking up recent information in the base <code>IEnumType</code>.
     */
    private void refreshEnumAttributeReferences() {
        IEnumType newEnumType = findEnumType(getIpsProject());
        if (newEnumType != null) {
            enumAttributeReferences.clear();
            List<IEnumAttribute> enumAttributes = newEnumType.getEnumAttributesIncludeSupertypeCopies(false);
            for (IEnumAttribute currentEnumAttribute : enumAttributes) {
                IPartReference reference = enumAttributeReferences.newPart();
                reference.setName(currentEnumAttribute.getName());
            }
        }

    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        enumType = element.getAttribute(PROPERTY_ENUM_TYPE);
        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ENUM_TYPE, enumType);
    }

    @Override
    public String getEnumType() {
        return enumType;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        EnumContentValidations.validateEnumType(list, this, enumType, ipsProject);
        if (list.containsErrorMsg()) {
            return;
        }

        IEnumType referencedEnumType = findEnumType(ipsProject);
        EnumContentValidations.validateEnumContentName(list, this, referencedEnumType, getQualifiedName());
        validateEnumAttributeReferences(list, referencedEnumType);
    }

    /** Validates the <code>IPartReference</code>s. */
    private void validateEnumAttributeReferences(MessageList validationMessageList, IEnumType enumType) {
        validateEnumAttributeReferencesCount(validationMessageList, enumType);
        if (validationMessageList.containsErrorMsg()) {
            return;
        }

        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);

        validateEnumAttributeReferenceNames(validationMessageList, enumType, enumAttributes);
        if (validationMessageList.containsErrorMsg()) {
            return;
        }

        validateEnumAttributeReferenceOrdering(validationMessageList, enumType, enumAttributes);
    }

    /**
     * The ordering of the <code>IPartReference</code>s must match the ordering of the
     * <code>IEnumAttribute</code>s in the base <code>IEnumType</code>.
     */
    private void validateEnumAttributeReferenceOrdering(MessageList validationMessageList,
            IEnumType enumType,
            List<IEnumAttribute> enumAttributes) {

        for (int i = 0; i < enumAttributes.size(); i++) {
            String currentEnumAttributeName = enumAttributes.get(i).getName();
            String currentReference = enumAttributeReferences.getPart(i).getName();
            if (!(currentEnumAttributeName.equals(currentReference))) {
                String text = MessageFormat.format(Messages.EnumContent_ReferencedEnumAttributesOrderingInvalid,
                        enumType.getQualifiedName());
                Message validationMessage = new Message(
                        IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID, text,
                        Message.ERROR, this);
                validationMessageList.add(validationMessage);
                break;
            }
        }
    }

    /**
     * The names of the <code>IPartReference</code>s must match the names of the
     * <code>IEnumAttribute</code>s in the base <code>IEnumType</code>.
     */
    private void validateEnumAttributeReferenceNames(MessageList validationMessageList,
            IEnumType enumType,
            List<IEnumAttribute> enumAttributes) {

        for (IEnumAttribute currentEnumAttribute : enumAttributes) {
            if (!(containsEnumAttributeReference(currentEnumAttribute.getName()))) {
                String text = MessageFormat.format(Messages.EnumContent_ReferencedEnumAttributeNamesInvalid,
                        enumType.getQualifiedName());
                Message validationMessage = new Message(
                        IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID, text, Message.ERROR,
                        this);
                validationMessageList.add(validationMessage);
            }
        }
    }

    /**
     * The number of <code>IPartReference</code>s must match the number of
     * <code>IEnumAttribute</code>s defined in the base <code>IEnumType</code>.
     */
    private void validateEnumAttributeReferencesCount(MessageList validationMessageList, IEnumType enumType) {
        if (enumType.getEnumAttributesCountIncludeSupertypeCopies(false) != getEnumAttributeReferencesCount()) {
            String text = MessageFormat.format(Messages.EnumContent_ReferencedEnumAttributesCountInvalid,
                    enumType.getQualifiedName());
            Message validationMessage = new Message(
                    IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID, text, Message.ERROR,
                    this);
            validationMessageList.add(validationMessage);
        }
    }

    /**
     * Returns <code>true</code> if an <code>IPartReference</code> with the given name exists in
     * this <code>IEnumContent</code>, <code>false</code> otherwise.
     */
    private boolean containsEnumAttributeReference(String name) {
        for (IPartReference currentReference : enumAttributeReferences) {
            if (currentReference.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        IDependency enumTypeDependency = IpsObjectDependency.createInstanceOfDependency(getQualifiedNameType(),
                new QualifiedNameType(getEnumType(), IpsObjectType.ENUM_TYPE));
        addDetails(details, enumTypeDependency, this, PROPERTY_ENUM_TYPE);
        return new IDependency[] { enumTypeDependency };

    }

    @Override
    public List<IPartReference> getEnumAttributeReferences() {
        List<IPartReference> referencesList = new ArrayList<>();
        IIpsObjectPart[] parts = enumAttributeReferences.getParts();
        for (IIpsObjectPart part : parts) {
            referencesList.add((IPartReference)part);
        }
        return referencesList;
    }

    @Override
    public IPartReference getEnumAttributeReference(String name) {
        ArgumentCheck.notNull(name);
        for (IPartReference reference : getEnumAttributeReferences()) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        return null;
    }

    @Override
    public int getEnumAttributeReferencesCount() {
        return enumAttributeReferences.size();
    }

    @Override
    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) {
        return ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, getEnumType());
    }

    /**
     * Returns <code>false</code>.
     */
    @Override
    public boolean containsDifferenceToModel(IIpsProject ipsProject) {
        // TODO AW: What shall we do here?
        return false;
    }

    @Override
    public void fixAllDifferencesToModel(IIpsProject ipsProject) {
        // TODO AW: What shall we do here?
    }

    @Override
    public IFixDifferencesComposite computeDeltaToModel(IIpsProject ipsProject) {
        // TODO AW: What shall we do here?
        return null;
    }

    @Override
    public String getMetaClass() {
        return getEnumType();
    }

    /**
     * Returns <code>true</code> if the referenced base <code>IEnumType</code> can be found and if
     * this <code>IEnumContent</code> is consistent with this base <code>IEnumType</code>.
     */
    @Override
    public boolean isCapableOfContainingValues() {
        return (findEnumType(getIpsProject()) == null) ? false : !(isFixToModelRequired());
    }

    @Override
    public boolean isFixToModelRequired() {

        MessageList messages = validate(getIpsProject());

        for (Message message : messages) {
            if (FIX_REQUIRING_ERROR_CODES.contains(message.getCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isIdentifierNamespaceBelowBoundary() {
        return false;
    }

}
