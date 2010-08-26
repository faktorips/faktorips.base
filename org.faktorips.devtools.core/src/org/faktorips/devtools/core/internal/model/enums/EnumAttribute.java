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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.enums.refactor.RenameEnumAttributeProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumAttribute</tt>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttribute extends AtomicIpsObjectPart implements IEnumAttribute {

    /** The data type of this <tt>IEnumAttribute</tt>. */
    protected String datatype;

    /** Flag indicating whether this <tt>IEnumAttribute</tt> is inherited from the super type. */
    protected boolean inherited;

    /** Flag indicating whether this <tt>IEnumAttribute</tt> is unique. */
    protected boolean unique;

    /**
     * Flag indicating whether this <tt>IEnumAttribute</tt> is the identifying attribute of this
     * enumeration type.
     */
    protected boolean identifier;

    /** Flag indicating whether this <tt>IEnumAttribute</tt> is used as display name. */
    protected boolean usedAsNameInFaktorIpsUi;

    /**
     * Creates a new <tt>IEnumAttribute</tt>.
     * 
     * @param parent The <tt>IEnumType</tt> this <tt>IEnumAttribute</tt> belongs to.
     * @param id A unique ID for this <tt>IEnumAttribute</tt>.
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
        valueChanged(oldName, name);
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
    protected void initFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        unique = Boolean.parseBoolean(element.getAttribute(PROPERTY_UNIQUE));
        identifier = Boolean.parseBoolean(element.getAttribute(PROPERTY_IDENTIFIER));
        usedAsNameInFaktorIpsUi = Boolean.parseBoolean(element.getAttribute(PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI));
        inherited = Boolean.parseBoolean(element.getAttribute(PROPERTY_INHERITED));

        super.initFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_UNIQUE, String.valueOf(unique));
        element.setAttribute(PROPERTY_IDENTIFIER, String.valueOf(identifier));
        element.setAttribute(PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI, String.valueOf(usedAsNameInFaktorIpsUi));
        element.setAttribute(PROPERTY_INHERITED, String.valueOf(inherited));
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        validateName(list, ipsProject);
        if (!(inherited)) {
            validateDatatype(list, ipsProject);
            validateDuplicateIndicator(list, new IdentifierIndicationProvider());
            validateDuplicateIndicator(list, new DisplayNameIndicationProvider());
        } else {
            validateInherited(list, ipsProject);
        }
    }

    /** Validates the <tt>name</tt> property. */
    private void validateName(MessageList list, IIpsProject ipsProject) throws CoreException {
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
        String complianceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
        String sourceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
        IStatus status = JavaConventions.validateFieldName(name, sourceLevel, complianceLevel);
        if (!(status.isOK())) {
            text = NLS.bind(Messages.EnumAttribute_NameNotAValidFieldName, name);
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
                text = NLS.bind(Messages.EnumAttribute_DuplicateName, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME, text, Message.ERROR, this,
                        PROPERTY_NAME);
                list.add(validationMessage);
                return;
            }
        }

        // Check for other attributes with the same name in the supertype hierarchy.
        List<IEnumAttribute> allEnumAttributes = getEnumType().findAllEnumAttributesIncludeSupertypeOriginals(true,
                ipsProject);
        numberEnumAttributesThisName = 0;
        for (IEnumAttribute enumAttribute : allEnumAttributes) {
            if (enumAttribute.getName().equals(name)) {
                numberEnumAttributesThisName++;
            }
            if (numberEnumAttributesThisName > 1) {
                text = NLS.bind(Messages.EnumAttribute_DuplicateNameInSupertypeHierarchy, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME_IN_SUPERTYPE_HIERARCHY, text,
                        Message.ERROR, this, PROPERTY_NAME);
                list.add(validationMessage);
                break;
            }
        }
    }

    /** Validates the <tt>datatype</tt> property. */
    private void validateDatatype(MessageList list, IIpsProject ipsProject) throws CoreException {
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
            text = NLS.bind(Messages.EnumAttribute_DatatypeDoesNotExist, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // The data type may not be primitive.
        if (ipsDatatype.isPrimitive()) {
            text = NLS.bind(Messages.EnumAttribute_DatatypeIsPrimitive, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_PRIMITIVE, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
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
            text = NLS.bind(Messages.EnumAttribute_DatatypeIsAbstract, datatype);
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
            if (adaptedEnumType.equals(enumType)
                    || adaptedEnumType.findAllSuperEnumTypes(ipsProject).contains(enumType)) {
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
            if (enumType.isContainingValues() && !(enumDatatype.isContainingValues())) {
                text = NLS.bind(Messages.EnumAttribute_EnumDatatypeDoesNotContainValuesButParentEnumTypeDoes,
                        enumDatatype.getQualifiedName());
                validationMessage = new Message(
                        MSGCODE_ENUM_ATTRIBUTE_ENUM_DATATYPE_DOES_NOT_CONTAIN_VALUES_BUT_PARENT_ENUM_TYPE_DOES, text,
                        Message.ERROR, this, PROPERTY_DATATYPE);
                list.add(validationMessage);
            }
        }
    }

    /** Checks the existence of the attribute in the supertype hierarchy. */
    private void validateInherited(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (!(getEnumType().hasSuperEnumType())) {
            String text = Messages.EnumAttribute_InheritedButNoSupertype;
            Message validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_INHERITED_BUT_NO_SUPERTYPE, text,
                    Message.ERROR, this, PROPERTY_INHERITED);
            list.add(validationMessage);
            return;
        }

        if (getEnumType().hasExistingSuperEnumType(ipsProject)) {
            if (findSuperEnumAttribute(ipsProject) == null) {
                String text = NLS.bind(Messages.EnumAttribute_NoSuchAttributeInSupertypeHierarchy, name);
                Message validationMessage = new Message(
                        MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY, text, Message.ERROR, this,
                        new String[] { PROPERTY_NAME, PROPERTY_INHERITED });
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
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException {
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
    public IEnumAttribute findSuperEnumAttribute(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);
        if (!(inherited)) {
            return null;
        }

        IEnumType enumType = getEnumType();
        return enumType.findEnumAttributeIncludeSupertypeOriginals(ipsProject, name);
    }

    @Override
    public List<IEnumAttribute> searchInheritedCopies(IIpsProject ipsProject) throws CoreException {
        Set<IEnumType> subclassingEnumTypes = getEnumType().searchSubclassingEnumTypes();
        List<IEnumAttribute> inheritedAttributeCopies = new ArrayList<IEnumAttribute>(subclassingEnumTypes.size());
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

        // Update unique identifier validation cache.
        if (oldIsUniqueIdentifier != uniqueIdentifier) {
            EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumType();
            if (enumValueContainerImpl.isUniqueIdentifierValidationCacheInitialized()) {
                IEnumType enumType = getEnumType();
                int index = enumType.getIndexOfEnumAttribute(this);
                if (uniqueIdentifier) {
                    enumValueContainerImpl.addUniqueIdentifierToValidationCache(index);
                    /*
                     * Add all the unique identifier values of the column to the unique identifier
                     * validation cache.
                     */
                    List<IEnumAttribute> newUniqueAttributeList = new ArrayList<IEnumAttribute>(1);
                    newUniqueAttributeList.add(this);
                    try {
                        enumValueContainerImpl.initValidationCacheUniqueIdentifierEntries(newUniqueAttributeList,
                                enumType);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    enumValueContainerImpl.removeUniqueIdentifierFromValidationCache(index);
                }
            }
        }

        valueChanged(oldIsUniqueIdentifier, uniqueIdentifier);
    }

    @Override
    public Boolean findIsUnique(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return null;
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
    public Boolean findIsIdentifier(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.isIdentifier();
        }
        return isIdentifier();
    }

    @Override
    public Boolean findIsUsedAsNameInFaktorIpsUi(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.isUsedAsNameInFaktorIpsUi();
        }
        return isUsedAsNameInFaktorIpsUi();
    }

    @Override
    public boolean isLiteralNameDefaultValueProvider() {
        IEnumLiteralNameAttribute literalNameAttribute = getEnumType().getEnumLiteralNameAttribute();
        return (literalNameAttribute == null) ? false : literalNameAttribute.getDefaultValueProviderAttribute().equals(
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

    @Override
    public ProcessorBasedRefactoring getRenameRefactoring() {
        return new ProcessorBasedRefactoring(new RenameEnumAttributeProcessor(this));
    }

    @Override
    public boolean hasDescriptionSupport() {
        return true;
    }

    @Override
    public boolean hasLabelSupport() {
        return true;
    }

    private interface IndicationProvider {

        public boolean uiValue();

        public boolean modelValue(IEnumAttribute enumAttribute);

        public Message message();

        public String getPropertyName();

        public String getPropertyDisplayName();
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
