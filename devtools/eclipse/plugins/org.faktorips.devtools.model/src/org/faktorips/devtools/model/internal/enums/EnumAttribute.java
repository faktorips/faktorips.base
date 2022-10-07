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
import java.util.List;
import java.util.Set;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.util.JavaConventions;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.DatatypeUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumAttribute</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttribute extends AtomicIpsObjectPart implements IEnumAttribute {
    /** The data type of this <code>IEnumAttribute</code>. */
    private String datatype;

    /**
     * Flag indicating whether this <code>IEnumAttribute</code> is inherited from the super type.
     */
    private boolean inherited;

    /** Flag indicating whether this <code>IEnumAttribute</code> is unique. */
    private boolean unique;

    /**
     * Flag indicating whether this <code>IEnumAttribute</code> is the identifying attribute of this
     * enumeration type.
     */
    private boolean identifier;

    /** Flag indicating whether this <code>IEnumAttribute</code> is used as display name. */
    private boolean usedAsNameInFaktorIpsUi;

    /** Flag indicating whether this <code>IEnumAttribute</code> is multilingual or not. */
    private boolean multilingual;

    /**
     * Creates a new <code>IEnumAttribute</code>.
     * 
     * @param parent The <code>IEnumType</code> this <code>IEnumAttribute</code> belongs to.
     * @param id A unique ID for this <code>IEnumAttribute</code>.
     */
    public EnumAttribute(IEnumType parent, String id) {
        super(parent, id);

        datatype = ""; //$NON-NLS-1$
        inherited = false;
        unique = false;
        identifier = false;
        usedAsNameInFaktorIpsUi = false;
    }

    @Override
    public void setName(String name) {
        ArgumentCheck.notNull(name);

        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name, PROPERTY_NAME);
    }

    @Override
    public void setDatatype(String datatype) {
        ArgumentCheck.notNull(datatype);

        String oldDatatype = this.datatype;
        this.datatype = datatype;
        valueChanged(oldDatatype, datatype);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        unique = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_UNIQUE);
        multilingual = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_MULTILINGUAL);
        identifier = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_IDENTIFIER);
        usedAsNameInFaktorIpsUi = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI);
        inherited = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_INHERITED);

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        if (unique) {
            element.setAttribute(PROPERTY_UNIQUE, String.valueOf(unique));
        }
        if (multilingual) {
            element.setAttribute(PROPERTY_MULTILINGUAL, String.valueOf(multilingual));
        }
        if (identifier) {
            element.setAttribute(PROPERTY_IDENTIFIER, String.valueOf(identifier));
        }
        if (usedAsNameInFaktorIpsUi) {
            element.setAttribute(PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI, String.valueOf(usedAsNameInFaktorIpsUi));
        }
        if (inherited) {
            element.setAttribute(PROPERTY_INHERITED, String.valueOf(inherited));
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        validateName(list, ipsProject);
        if (!(inherited)) {
            validateDatatype(list, ipsProject);
            validateDuplicateIndicator(list, new IdentifierIndicationProvider());
            validateDuplicateIndicator(list, new DisplayNameIndicationProvider());
            validateAllowedAsIdentifier(list);
        } else {
            validateInherited(list, ipsProject);
            validateSupertypeMismatchLingual(list, ipsProject);
        }
    }

    private void validateAllowedAsIdentifier(MessageList list) {
        if (isIdentifier() && isMultilingual()) {
            list.add(new Message(MSGCODE_MULTILINGUAL_ATTRIBUTES_CANNOT_BE_IDENTIFIERS, MessageFormat.format(
                    Messages.EnumAttribute_MultilingualCannotBeIdentifier, getName()), Message.ERROR, this,
                    PROPERTY_MULTILINGUAL, PROPERTY_IDENTIFIER));
        }
    }

    private void validateSupertypeMismatchLingual(MessageList list, IIpsProject ipsProject) {
        if (checkSupertypeMissmatchLingual(ipsProject)) {
            list.add(new Message(MSGCODE_ENUM_ATTRIBUTE_INHERITED_LINGUAL_MISMATCH,
                    Messages.EnumAttribute_InheritedMultiLingualMismatch, Message.ERROR, this, PROPERTY_MULTILINGUAL));
        }
    }

    private boolean checkSupertypeMissmatchLingual(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
        if (superEnumAttribute == null) {
            return false;
        }
        return multilingual != superEnumAttribute.isMultilingual();
    }

    /** Validates the <code>name</code> property. */
    private void validateName(MessageList list, IIpsProject ipsProject) {
        String text;
        Message validationMessage;

        // Check for name missing.
        if (name.length() == 0) {
            text = Messages.EnumAttribute_NameMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING, text, Message.ERROR, this,
                    PROPERTY_NAME);
            list.add(validationMessage);
            return;
        }

        // Check for valid java field name.
        Runtime.Version sourceVersion = ipsProject.getJavaProject().getSourceVersion();
        if (!JavaConventions.validateName(name, sourceVersion)) {
            text = MessageFormat.format(Messages.EnumAttribute_NameNotAValidFieldName, name);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NAME_NOT_A_VALID_FIELD_NAME, text, Message.ERROR,
                    this, PROPERTY_NAME);
            list.add(validationMessage);
            return;
        }

        // Check for other attributes with the same name in the containing enum type.
        int numberEnumAttributesThisName = 0;
        for (IEnumAttribute enumAttribute : getEnumType().getEnumAttributes(true)) {
            if (enumAttribute.getName().equals(name)) {
                numberEnumAttributesThisName++;
            }
            if (numberEnumAttributesThisName > 1) {
                text = MessageFormat.format(Messages.EnumAttribute_DuplicateName, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME, text, Message.ERROR, this,
                        PROPERTY_NAME);
                list.add(validationMessage);
                return;
            }
        }

        // Check for other attributes with the same name in the supertype hierarchy.
        List<IEnumAttribute> allEnumAttributes = getEnumType().findAllEnumAttributes(true, ipsProject);
        numberEnumAttributesThisName = 0;
        for (IEnumAttribute enumAttribute : allEnumAttributes) {
            if (enumAttribute.getName().equals(name)) {
                numberEnumAttributesThisName++;
            }
            if (numberEnumAttributesThisName > 1) {
                text = MessageFormat.format(Messages.EnumAttribute_DuplicateNameInSupertypeHierarchy, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME_IN_SUPERTYPE_HIERARCHY, text,
                        Message.ERROR, this, PROPERTY_NAME);
                list.add(validationMessage);
                break;
            }
        }
    }

    /** Validates the <code>datatype</code> property. */
    private void validateDatatype(MessageList list, IIpsProject ipsProject) {
        String text;
        Message validationMessage;
        IEnumType enumType = getEnumType();
        Datatype ipsDatatype = getIpsProject().findDatatype(datatype);

        // The data type must be specified.
        if (datatype.length() == 0) {
            text = Messages.EnumAttribute_DatatypeMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // The data type must exist.
        if (ipsDatatype == null) {
            text = MessageFormat.format(Messages.EnumAttribute_DatatypeDoesNotExist, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // The data type may not be void.
        if (ipsDatatype.isVoid()) {
            text = Messages.EnumAttribute_DatatypeIsVoid;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_VOID, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
        }

        // The data type may not be abstract.
        if (ipsDatatype.isAbstract()) {
            text = MessageFormat.format(Messages.EnumAttribute_DatatypeIsAbstract, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_ABSTRACT, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
        }

        /*
         * The data type must not be the EnumType that contains this EnumAttribute (or subclasses of
         * it).
         */
        if (ipsDatatype instanceof EnumTypeDatatypeAdapter) {
            EnumTypeDatatypeAdapter adapter = (EnumTypeDatatypeAdapter)ipsDatatype;
            IEnumType adaptedEnumType = adapter.getEnumType();
            if (adaptedEnumType.isSubEnumTypeOrSelf(enumType, ipsProject)) {
                text = Messages.EnumAttribute_DatatypeIsContainingEnumTypeOrSubclass;
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_CONTAINING_ENUM_TYPE_OR_SUBCLASS,
                        text, Message.ERROR, this, PROPERTY_DATATYPE);
                list.add(validationMessage);
            }
        }

        /*
         * The data type may not be an enumeration that does not contain values if the EnumType this
         * EnumAttribute belongs to does contain values.
         */
        if (ipsDatatype instanceof EnumTypeDatatypeAdapter) {
            EnumTypeDatatypeAdapter enumDatatypeAdapter = (EnumTypeDatatypeAdapter)ipsDatatype;
            IEnumType enumDatatype = enumDatatypeAdapter.getEnumType();
            if (enumType.isInextensibleEnum() && !(enumDatatype.isInextensibleEnum())) {
                text = MessageFormat.format(
                        Messages.EnumAttribute_EnumDatatypeDoesNotContainValuesButParentEnumTypeDoes,
                        enumDatatype.getQualifiedName());
                validationMessage = new Message(
                        MSGCODE_ENUM_ATTRIBUTE_ENUM_DATATYPE_DOES_NOT_CONTAIN_VALUES_BUT_PARENT_ENUM_TYPE_DOES, text,
                        Message.ERROR, this, PROPERTY_DATATYPE);
                list.add(validationMessage);
            }
        }
    }

    /** Checks the existence of the attribute in the supertype hierarchy. */
    private void validateInherited(MessageList list, IIpsProject ipsProject) {
        if (!(getEnumType().hasSuperEnumType())) {
            String text = Messages.EnumAttribute_InheritedButNoSupertype;
            Message validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_INHERITED_BUT_NO_SUPERTYPE, text,
                    Message.ERROR, this, PROPERTY_INHERITED);
            list.add(validationMessage);
            return;
        }

        if (getEnumType().hasExistingSuperEnumType(ipsProject)) {
            if (findSuperEnumAttribute(ipsProject) == null) {
                String text = MessageFormat.format(Messages.EnumAttribute_NoSuchAttributeInSupertypeHierarchy, name);
                Message validationMessage = new Message(
                        MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY, text, Message.ERROR, this,
                        PROPERTY_NAME, PROPERTY_INHERITED);
                list.add(validationMessage);
            }
        }
    }

    @Override
    public boolean isInherited() {
        return inherited;
    }

    @Override
    public void setInherited(boolean isInherited) {
        boolean oldIsInherited = inherited;
        inherited = isInherited;
        valueChanged(oldIsInherited, isInherited);

        if (isInherited) {
            setDatatype(""); //$NON-NLS-1$
            setUnique(false);
            setIdentifier(false);
            setUsedAsNameInFaktorIpsUi(false);
        }
    }

    @Override
    public IEnumType getEnumType() {
        return (IEnumType)getParent();
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setMultilingual(boolean multilingual) {
        boolean oldMultilingual = this.multilingual;
        this.multilingual = multilingual;
        valueChanged(oldMultilingual, multilingual, PROPERTY_MULTILINGUAL);
    }

    @Override
    public boolean isMultilingual() {
        return isMultilingualSupported() && multilingual;
    }

    @Override
    public boolean isMultilingualSupported() {
        return Datatype.STRING.equals(findDatatype(getIpsProject()));
    }

    @Override
    public ValueDatatype findDatatype(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.findDatatype(ipsProject);
        }
        return ipsProject.findValueDatatype(datatype);
    }

    @Override
    public ValueDatatype findDatatypeIgnoreEnumContents(IIpsProject ipsProject) {
        ValueDatatype foundDatatype = findDatatype(ipsProject);
        if (DatatypeUtil.isExtensibleEnumType(foundDatatype)) {
            EnumTypeDatatypeAdapter enumDatatype = (EnumTypeDatatypeAdapter)foundDatatype;
            return new EnumTypeDatatypeAdapter(enumDatatype.getEnumType(), null);
        }
        return foundDatatype;
    }

    @Override
    public IEnumAttribute findSuperEnumAttribute(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        if (!(inherited)) {
            return null;
        }

        IEnumType enumType = getEnumType();
        return enumType.findEnumAttributeIncludeSupertypeOriginals(ipsProject, name);
    }

    @Override
    public List<IEnumAttribute> searchInheritedCopies(IIpsProject ipsProject) {
        Set<IEnumType> subclassingEnumTypes = getEnumType().searchSubclassingEnumTypes();
        List<IEnumAttribute> inheritedAttributeCopies = new ArrayList<>(subclassingEnumTypes.size());
        for (IEnumType subclassingEnumType : subclassingEnumTypes) {
            IEnumAttribute inheritedAttribute = subclassingEnumType.getEnumAttributeIncludeSupertypeCopies(name);
            if (inheritedAttribute != null) {
                inheritedAttributeCopies.add(inheritedAttribute);
            }
        }
        return inheritedAttributeCopies;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }

    @Override
    public void setUnique(boolean uniqueIdentifier) {
        boolean oldIsUniqueIdentifier = unique;
        unique = uniqueIdentifier;
        valueChanged(oldIsUniqueIdentifier, uniqueIdentifier);
    }

    @Override
    public boolean findIsUnique(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return false;
            }
            return superEnumAttribute.isUnique();
        }
        return isUnique();
    }

    @Override
    public boolean isIdentifier() {
        return identifier;
    }

    @Override
    public boolean isUsedAsNameInFaktorIpsUi() {
        return usedAsNameInFaktorIpsUi;
    }

    @Override
    public void setIdentifier(boolean usedAsIdInFaktorIpsUi) {
        boolean oldUsedAsIdInFaktorIpsUi = identifier;
        identifier = usedAsIdInFaktorIpsUi;
        valueChanged(oldUsedAsIdInFaktorIpsUi, usedAsIdInFaktorIpsUi);
    }

    @Override
    public void setUsedAsNameInFaktorIpsUi(boolean usedAsNameInFaktorIpsUi) {
        boolean oldUsedAsNameInFaktorIpsUi = this.usedAsNameInFaktorIpsUi;
        this.usedAsNameInFaktorIpsUi = usedAsNameInFaktorIpsUi;
        valueChanged(oldUsedAsNameInFaktorIpsUi, usedAsNameInFaktorIpsUi);
    }

    @Override
    public boolean findIsIdentifier(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return false;
            }
            return superEnumAttribute.isIdentifier();
        }
        return isIdentifier();
    }

    @Override
    public boolean findIsUsedAsNameInFaktorIpsUi(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return false;
            }
            return superEnumAttribute.isUsedAsNameInFaktorIpsUi();
        }
        return isUsedAsNameInFaktorIpsUi();
    }

    @Override
    public boolean isLiteralNameDefaultValueProvider() {
        IEnumLiteralNameAttribute literalNameAttribute = getEnumType().getEnumLiteralNameAttribute();
        return (literalNameAttribute == null) ? false
                : literalNameAttribute.getDefaultValueProviderAttribute().equals(
                        name);
    }

    private void validateDuplicateIndicator(MessageList list, IndicationProvider indicationProvider) {
        List<IEnumAttribute> enumAttributes = getEnumType().getEnumAttributesIncludeSupertypeCopies(false);
        if (indicationProvider.uiValue()) {
            // Check for other EnumAttributes being indicated.
            int numberOfIndicatedAttributes = 0;
            for (IEnumAttribute currentEnumAttribute : enumAttributes) {
                if (indicationProvider.modelValue(currentEnumAttribute)) {
                    numberOfIndicatedAttributes++;
                }
                if (numberOfIndicatedAttributes > 1) {
                    list.add(indicationProvider.message());
                    break;
                }
            }
        }
    }

    @Override
    public boolean isEnumLiteralNameAttribute() {
        return this instanceof IEnumLiteralNameAttribute;
    }

    private interface IndicationProvider {

        boolean uiValue();

        boolean modelValue(IEnumAttribute enumAttribute);

        Message message();

        String getPropertyName();

        String getPropertyDisplayName();
    }

    private class IdentifierIndicationProvider implements IndicationProvider {

        @Override
        public Message message() {
            String text = Messages.EnumAttribute_DuplicateUsedAsIdInFaktorIpsUi;
            return new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_ID_IN_FAKTOR_IPS_UI, text, Message.ERROR,
                    EnumAttribute.this, PROPERTY_IDENTIFIER);
        }

        @Override
        public boolean modelValue(IEnumAttribute enumAttribute) {
            return enumAttribute.isIdentifier();
        }

        @Override
        public boolean uiValue() {
            return identifier;
        }

        @Override
        public String getPropertyName() {
            return PROPERTY_IDENTIFIER;
        }

        @Override
        public String getPropertyDisplayName() {
            return Messages.EnumAttribute_PropertyDisplayName_Identifier;
        }

    }

    private class DisplayNameIndicationProvider implements IndicationProvider {

        @Override
        public Message message() {
            String text = Messages.EnumAttribute_DuplicateUsedAsNameInFaktorIpsUi;
            return new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_NAME_IN_FAKTOR_IPS_UI, text, Message.ERROR,
                    EnumAttribute.this, PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI);
        }

        @Override
        public boolean modelValue(IEnumAttribute enumAttribute) {
            return enumAttribute.isUsedAsNameInFaktorIpsUi();
        }

        @Override
        public boolean uiValue() {
            return usedAsNameInFaktorIpsUi;
        }

        @Override
        public String getPropertyName() {
            return PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI;
        }

        @Override
        public String getPropertyDisplayName() {
            return Messages.EnumAttribute_PropertyDisplayNameDisplayName;
        }

    }

}
