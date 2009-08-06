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
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumAttribute</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttribute extends AtomicIpsObjectPart implements IEnumAttribute {

    /** The icon representing an enum attribute. */
    private final static String ICON = "EnumAttribute.gif"; //$NON-NLS-1$

    /** The icon representing an overridden enum attribute. */
    private final static String OVERRIDDEN_ICON = "EnumAttributeOverridden.gif"; //$NON-NLS-1$

    /** The icon representing an enum attribute that is marked as unique literalName. */
    private final static String UNIQUE_IDENTIFIER_ICON = "EnumAttributeUniqueIdentifier.gif"; //$NON-NLS-1$

    /** The icon representing an overridden unique literalName enum attribute. */
    private final static String OVERRIDDEN_UNIQUE_IDENTIFIER_ICON = "EnumAttributeOverriddenUniqueIdentifier.gif"; //$NON-NLS-1$

    /** The data type of this enum attribute. */
    private String datatype;

    /** Flag indicating whether this enum attribute is used as literal name. */
    private boolean literalName;

    /** Flag indicating whether this enum attribute is inherited from the super type. */
    private boolean inherited;

    /** Flag indicating whether this enum attribute is unique. */
    private boolean unique;

    /**
     * Flag indicating whether this enum attribute is the identifying attribute of this enumeration
     * type.
     */
    private boolean identifier;

    /**
     * Flag indicating whether this enumeration attribute is used as display name.
     */
    private boolean usedAsNameInFaktorIpsUi;

    /**
     * Creates a new <code>EnumAttribute</code>.
     * 
     * @param parent The enum type this enum attribute belongs to.
     * @param id A unique id for this enum attribute.
     */
    public EnumAttribute(IEnumType parent, int id) {
        super(parent, id);

        this.datatype = ""; //$NON-NLS-1$
        this.literalName = false;
        this.inherited = false;
        this.unique = false;
        this.identifier = false;
        this.usedAsNameInFaktorIpsUi = false;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        ArgumentCheck.notNull(name);

        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String datatype) {
        ArgumentCheck.notNull(datatype);

        String oldDatatype = this.datatype;
        this.datatype = datatype;
        valueChanged(oldDatatype, datatype);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLiteralName() {
        return literalName;
    }

    /**
     * {@inheritDoc}
     */
    public void setLiteralName(boolean literalName) {
        boolean oldIsIdentifier = this.literalName;
        this.literalName = literalName;
        valueChanged(oldIsIdentifier, literalName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initFromXml(Element element, Integer id) {
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        literalName = Boolean.parseBoolean(element.getAttribute(PROPERTY_LITERAL_NAME));
        unique = Boolean.parseBoolean(element.getAttribute(PROPERTY_UNIQUE));
        identifier = Boolean.parseBoolean(element.getAttribute(PROPERTY_IDENTIFIER));
        usedAsNameInFaktorIpsUi = Boolean.parseBoolean(element.getAttribute(PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI));
        inherited = Boolean.parseBoolean(element.getAttribute(PROPERTY_INHERITED));

        super.initFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_LITERAL_NAME, String.valueOf(literalName));
        element.setAttribute(PROPERTY_UNIQUE, String.valueOf(unique));
        element.setAttribute(PROPERTY_IDENTIFIER, String.valueOf(identifier));
        element.setAttribute(PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI, String.valueOf(usedAsNameInFaktorIpsUi));
        element.setAttribute(PROPERTY_INHERITED, String.valueOf(inherited));
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        try {
            IIpsProject ipsProject = getIpsProject();
            if (inherited && findSuperEnumAttribute(ipsProject) == null) {
                return IpsPlugin.getDefault().getImage(OVERRIDDEN_ICON);
            }

            boolean isUniqueIdentifier = findIsUnique(ipsProject);
            if (isUniqueIdentifier && inherited) {
                return IpsPlugin.getDefault().getImage(OVERRIDDEN_UNIQUE_IDENTIFIER_ICON);
            } else if (isUniqueIdentifier) {
                return IpsPlugin.getDefault().getImage(UNIQUE_IDENTIFIER_ICON);
            } else if (inherited) {
                return IpsPlugin.getDefault().getImage(OVERRIDDEN_ICON);
            } else {
                return IpsPlugin.getDefault().getImage(ICON);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        validateName(list, ipsProject);

        if (!inherited) {
            validateDatatype(list, ipsProject);
            validateDuplicateIndicator(list, ipsProject, new LiteralNameIndictionProvider());
            validateDuplicateIndicator(list, ipsProject, new IdentifierIndictionProvider());
            validateDuplicateIndicator(list, ipsProject, new DisplayNameIndictionProvider());
        }

        validateInherited(list, ipsProject);
    }

    /** Validates the <code>name</code> property. */
    private void validateName(MessageList list, IIpsProject ipsProject) {
        String text;
        Message validationMessage;
        List<IEnumAttribute> enumAttributesThisType = getEnumType().getEnumAttributes();

        // Check for name missing
        if (name.equals("")) { //$NON-NLS-1$
            text = Messages.EnumAttribute_NameMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING, text, Message.ERROR, this,
                    PROPERTY_NAME);
            list.add(validationMessage);
        }

        // Check for other attributes with the same name
        int numberEnumAttributesThisName = 0;
        for (IEnumAttribute currentEnumAttribute : enumAttributesThisType) {
            if (currentEnumAttribute.getName().equals(name)) {
                numberEnumAttributesThisName++;
            }
            if (numberEnumAttributesThisName > 1) {
                text = NLS.bind(Messages.EnumAttribute_DuplicateName, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME, text, Message.ERROR, this,
                        PROPERTY_NAME);
                list.add(validationMessage);
                break;
            }
        }
    }

    /** Validates the <code>datatype</code> property. */
    private void validateDatatype(MessageList list, IIpsProject ipsProject) throws CoreException {
        String text;
        Message validationMessage;
        IEnumType enumType = getEnumType();
        Datatype ipsDatatype = getIpsProject().findDatatype(datatype);

        // The datatype must be specified.
        if (datatype.equals("")) { //$NON-NLS-1$
            text = Messages.EnumAttribute_DatatypeMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // The datatype must exist.
        if (ipsDatatype == null) {
            text = NLS.bind(Messages.EnumAttribute_DatatypeDoesNotExist, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // The datatype may not be primitive.
        if (ipsDatatype.isPrimitive()) {
            text = NLS.bind(Messages.EnumAttribute_DatatypeIsPrimitive, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_PRIMITIVE, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // The datatype may not be void.
        if (ipsDatatype.isVoid()) {
            text = Messages.EnumAttribute_DatatypeIsVoid;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_VOID, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // The datatype may not be abstract.
        if (ipsDatatype.isAbstract()) {
            text = NLS.bind(Messages.EnumAttribute_DatatypeIsAbstract, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_ABSTRACT, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // The datatype must not be the enum type that contains this enum attribute (or subclasses
        // of it).
        if (ipsDatatype instanceof EnumTypeDatatypeAdapter) {
            EnumTypeDatatypeAdapter adaptedEnumType = (EnumTypeDatatypeAdapter)ipsDatatype;
            List<IEnumType> subEnumTypes = enumType.findAllSubEnumTypes(ipsProject);
            if (adaptedEnumType.getEnumType().equals(enumType) || subEnumTypes.contains(adaptedEnumType.getEnumType())) {
                text = Messages.EnumAttribute_DatatypeIsContainingEnumTypeOrSubclass;;
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_CONTAINING_ENUM_TYPE_OR_SUBCLASS,
                        text, Message.ERROR, this, PROPERTY_DATATYPE);
                list.add(validationMessage);
                return;
            }

        }

        // If this enum attribute is marked to be used as literal name the datatype must be String.
        if (literalName) {
            if (!(ipsDatatype.getName().equals(Datatype.STRING.getName()))) {
                text = Messages.EnumAttribute_LiteralNameNotOfDatatypeString;
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_NOT_OF_DATATYPE_STRING, text,
                        Message.ERROR, this, PROPERTY_DATATYPE);
                list.add(validationMessage);
                return;
            }
        }

        /*
         * The datatype may not be an enum that does not contain values if the enum type this enum
         * attribute belongs to does contain values.
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
                return;
            }
        }
    }

    /** Validates the <code>inherited</code> property. */
    private void validateInherited(MessageList list, IIpsProject ipsProject) throws CoreException {
        String text;
        Message validationMessage;

        // Check existence in supertype hierarchy if this enum attribute is inherited
        if (inherited) {
            if (findSuperEnumAttribute(ipsProject) == null) {
                text = NLS.bind(Messages.EnumAttribute_NoSuchAttributeInSupertypeHierarchy, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY, text,
                        Message.ERROR, this, PROPERTY_INHERITED);
                list.add(validationMessage);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInherited() {
        return inherited;
    }

    /**
     * {@inheritDoc}
     */
    public void setInherited(boolean isInherited) {
        boolean oldIsInherited = this.inherited;
        this.inherited = isInherited;
        valueChanged(oldIsInherited, isInherited);

        if (isInherited) {
            setDatatype(""); //$NON-NLS-1$
            setLiteralName(false);
            setUnique(false);
            setIdentifier(false);
            setUsedAsNameInFaktorIpsUi(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IEnumType getEnumType() {
        return (IEnumType)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Returns the original enum attribute this enum attribute is a copy of (if this enum attribute
     * is inherited).
     * <p>
     * Returns <code>null</code> if this enum attribute is not inherited or the super enum attribute
     * cannot be found.
     */
    private IEnumAttribute findSuperEnumAttribute(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (!inherited) {
            return null;
        }

        IEnumType enumType = getEnumType();
        return enumType.findEnumAttributeIncludeSupertypeOriginals(ipsProject, name);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * {@inheritDoc}
     */
    public void setUnique(boolean uniqueIdentifier) {
        boolean oldIsUniqueIdentifier = this.unique;
        this.unique = uniqueIdentifier;

        // Update unique identifier validation cache.
        if (oldIsUniqueIdentifier != uniqueIdentifier) {
            EnumValueContainer enumValueContainerImpl = (EnumValueContainer)getEnumType();
            if (enumValueContainerImpl.isUniqueIdentifierValidationCacheInitialized()) {
                int index = getEnumType().getIndexOfEnumAttribute(this);
                if (uniqueIdentifier) {
                    enumValueContainerImpl.addUniqueIdentifierToValidationCache(index);
                    /*
                     * Add all the unique identifier values of the column to the unique identifier
                     * validation cache.
                     */
                    List<IEnumAttribute> newUniqueAttributeList = new ArrayList<IEnumAttribute>(1);
                    newUniqueAttributeList.add(this);
                    try {
                        enumValueContainerImpl.initValidationCacheUniqueIdentifierEntries(newUniqueAttributeList);
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

    /**
     * {@inheritDoc}
     */
    public Boolean findIsLiteralName(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.isLiteralName();
        } else {
            return isLiteralName();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Boolean findIsUnique(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.isUnique();
        } else {
            return isUnique();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUsedAsNameInFaktorIpsUi() {
        return usedAsNameInFaktorIpsUi;
    }

    /**
     * {@inheritDoc}
     */
    public void setIdentifier(boolean usedAsIdInFaktorIpsUi) {
        boolean oldUsedAsIdInFaktorIpsUi = this.identifier;
        this.identifier = usedAsIdInFaktorIpsUi;
        valueChanged(oldUsedAsIdInFaktorIpsUi, usedAsIdInFaktorIpsUi);
    }

    /**
     * {@inheritDoc}
     */
    public void setUsedAsNameInFaktorIpsUi(boolean usedAsNameInFaktorIpsUi) {
        boolean oldUsedAsNameInFaktorIpsUi = this.usedAsNameInFaktorIpsUi;
        this.usedAsNameInFaktorIpsUi = usedAsNameInFaktorIpsUi;
        valueChanged(oldUsedAsNameInFaktorIpsUi, usedAsNameInFaktorIpsUi);
    }

    /**
     * {@inheritDoc}
     */
    public Boolean findIsIdentifier(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.isIdentifier();
        } else {
            return isIdentifier();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Boolean findIsUsedAsNameInFaktorIpsUi(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute(ipsProject);
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.isUsedAsNameInFaktorIpsUi();
        } else {
            return isUsedAsNameInFaktorIpsUi();
        }
    }

    private void validateDuplicateIndicator(MessageList list,
            IIpsProject ipsProject,
            IndicationProvider indicationProvider) throws CoreException {
        String text;
        Message validationMessage;
        List<IEnumAttribute> enumAttributes = getEnumType().getEnumAttributesIncludeSupertypeCopies();

        if (indicationProvider.uiValue()) {
            // Check for other enum attributes being indicated
            int numberOfIndicatedAttributes = 0;
            for (IEnumAttribute currentEnumAttribute : enumAttributes) {
                Boolean isLiteralName = currentEnumAttribute.findIsLiteralName(ipsProject);
                if (isLiteralName == null) {
                    continue;
                }
                if (indicationProvider.modelValue(currentEnumAttribute)) {
                    numberOfIndicatedAttributes++;
                }
                if (numberOfIndicatedAttributes > 1) {
                    list.add(indicationProvider.message());
                    break;
                }
            }

            // A literal name must also be a unique literalName
            if (!unique) {
                text = NLS.bind(Messages.EnumAttribute_LiteralNameButNotUniqueIdentifier, indicationProvider
                        .getPropertyDisplayName());
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_BUT_NOT_UNIQUE_IDENTIFIER, text,
                        Message.ERROR, this, indicationProvider.getPropertyName());
                list.add(validationMessage);
            }
        }
    }

    private interface IndicationProvider {

        public boolean uiValue();

        public boolean modelValue(IEnumAttribute enumAttribute);

        public Message message();

        public String getPropertyName();

        public String getPropertyDisplayName();
    }

    private class LiteralNameIndictionProvider implements IndicationProvider {

        public Message message() {
            String text = Messages.EnumAttribute_DuplicateLiteralName;
            return new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_LITERAL_NAME, text, Message.ERROR, this,
                    PROPERTY_LITERAL_NAME);
        }

        public boolean modelValue(IEnumAttribute enumAttribute) {
            return enumAttribute.isLiteralName();
        }

        public boolean uiValue() {
            return literalName;
        }

        public String getPropertyName() {
            return PROPERTY_LITERAL_NAME;
        }

        public String getPropertyDisplayName() {
            return Messages.EnumAttribute_PropertyDisplayName_LiteralName;
        }

    }

    private class IdentifierIndictionProvider implements IndicationProvider {

        public Message message() {
            String text = Messages.EnumAttribute_DuplicateUsedAsIdInFaktorIpsUi;
            return new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_ID_IN_FAKTOR_IPS_UI, text, Message.ERROR, this,
                    PROPERTY_IDENTIFIER);
        }

        public boolean modelValue(IEnumAttribute enumAttribute) {
            return enumAttribute.isIdentifier();
        }

        public boolean uiValue() {
            return identifier;
        }

        public String getPropertyName() {
            return PROPERTY_IDENTIFIER;
        }

        public String getPropertyDisplayName() {
            return Messages.EnumAttribute_PropertyDisplayName_Identifier;
        }

    }

    private class DisplayNameIndictionProvider implements IndicationProvider {

        public Message message() {
            String text = Messages.EnumAttribute_DuplicateUsedAsNameInFaktorIpsUi;
            return new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_NAME_IN_FAKTOR_IPS_UI, text, Message.ERROR,
                    this, PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI);
        }

        public boolean modelValue(IEnumAttribute enumAttribute) {
            return enumAttribute.isUsedAsNameInFaktorIpsUi();
        }

        public boolean uiValue() {
            return usedAsNameInFaktorIpsUi;
        }

        public String getPropertyName() {
            return PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI;
        }

        public String getPropertyDisplayName() {
            return Messages.EnumAttribute_PropertyDisplayNameDisplayName;
        }

    }
}
