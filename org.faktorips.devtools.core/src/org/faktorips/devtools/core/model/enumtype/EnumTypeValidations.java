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

package org.faktorips.devtools.core.model.enumtype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.enumtype.Messages;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <code>IEnumType</code>, where some are also
 * used in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class EnumTypeValidations {

    /**
     * Validates whether the given super enum type exists in the ips object path of the given ips
     * project and that super enum type is abstract.
     * <p>
     * If no abstract super enum type with the given qualified name exists in the object path of the
     * given ips project, an appropriate validation message will be returned.
     * 
     * @param enumType The enum type that might be invalid or <code>null</code> if that information
     *            cannot be supported.
     * @param superEnumTypeQualifiedName The qualified name of the super enum type.
     * @param ipsProject The ips object path of this ips project will be searched.
     * 
     * @return An appropriate validation message or <code>null</code> if the validation was
     *         successful.
     * 
     * @throws CoreException If an error occurs while searching for the super enum type.
     * @throws IllegalArgumentException If superEnumTypeQualifiedName is an empty string.
     * @throws NullPointerException If superEnumTypeQualifiedName or ipsProject is <code>null</code>
     *             .
     */
    public static Message validateSuperEnumType(IEnumType enumType,
            String superEnumTypeQualifiedName,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { superEnumTypeQualifiedName, ipsProject });
        ArgumentCheck.isTrue(!(superEnumTypeQualifiedName.equals("")));

        // Super enum type exists?
        IEnumType superEnumType = ipsProject.findEnumType(superEnumTypeQualifiedName);
        if (superEnumType == null) {
            String text = NLS.bind(Messages.EnumType_SupertypeDoesNotExist, superEnumTypeQualifiedName);
            return new Message(
                    IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST,
                    text,
                    Message.ERROR,
                    enumType != null ? new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) }
                            : new ObjectProperty[0]);

        }

        // Super enum type abstract?
        if (!(superEnumType.isAbstract())) {
            String text = NLS.bind(Messages.EnumType_SupertypeIsNotAbstract, superEnumType.getQualifiedName());
            return new Message(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) });
        }

        // All validations passed
        return null;
    }

    /**
     * Validates whether the given enum type inherits all attributes defined in its supertype
     * hierarchy.
     * 
     * @param enumType The enum type to validate.
     * 
     * @return An appropriate validation message or <code>null</code> if the validation was
     *         successful.
     * 
     * @throws CoreException If an error occurs while searching for attributes in the supertype
     *             hierarchy.
     * @throws NullPointerException If enumType is <code>null</code>.
     */
    public static Message validateInheritedAttributes(IEnumType enumType) throws CoreException {
        // All attributes from supertype hierarchy inherited?
        List<IEnumAttribute> notInheritedAttributes = getNotInheritedAttributes(enumType);
        int notInheritedAttributesCount = notInheritedAttributes.size();
        if (notInheritedAttributesCount > 0) {
            IEnumAttribute firstNotInheritedAttribute = notInheritedAttributes.get(0);
            String identifier = (firstNotInheritedAttribute.isIdentifier()) ? ", " + Messages.EnumAttribute_Identifier
                    : "";
            String showFirst = firstNotInheritedAttribute.getName() + " (" + firstNotInheritedAttribute.getDatatype()
                    + identifier + ')';
            String text = (notInheritedAttributesCount > 1) ? NLS.bind(
                    Messages.EnumType_NotInheritedAttributesInSupertypeHierarchyPlural, notInheritedAttributesCount,
                    showFirst) : NLS.bind(Messages.EnumType_NotInheritedAttributesInSupertypeHierarchySingular,
                    showFirst);
            return new Message(IEnumType.MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY, text,
                    Message.ERROR, new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) });
        }

        // All validations passed
        return null;
    }

    /**
     * Validates whether the given enum type has at least one attribute being marked as identifier.
     * <p>
     * If the given enum type is abstract the validation will succeed even if there is no identifier
     * attribute.
     * 
     * @param enumType The enum type to validate.
     * 
     * @return An appropriate validation message or <code>null</code> if the validation was
     *         successful.
     * 
     * @throws NullPointerException If enumType is <code>null</code>.
     */
    public static Message validateIdentifierAttribute(IEnumType enumType) {
        ArgumentCheck.notNull(enumType);

        // Pass if given enum type is abstract
        if (enumType.isAbstract()) {
            return null;
        }

        boolean identifierFound = false;
        for (IEnumAttribute currentEnumAttribute : enumType.getEnumAttributes()) {
            if (currentEnumAttribute.isIdentifier()) {
                identifierFound = true;
                break;
            }
        }

        if (!(identifierFound)) {
            String text = Messages.EnumType_NoIdentifierAttribute;
            return new Message(IEnumType.MSGCODE_ENUM_TYPE_NO_IDENTIFIER_ATTRIBUTE, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(enumType, null) });
        }

        // All validations passed
        return null;
    }

    /**
     * Returns all attributes from the supertype hierarchy of the given enum type that are not
     * inherited in the given enum type.
     */
    //TODO pk: wieso machst du das so? sollte es nicht eine Methode getAttributes und findAllAttributes am EnumType
    //geben getAttributes gibt nur die Attribute des EnumTypes zurück findAllAttributes gibt die Attribute des EnumType 
    //inklusive der geerbten Attribute zurück
    //getInheritedAttributes() braucht man dann nicht mehr oder?
    private static List<IEnumAttribute> getNotInheritedAttributes(IEnumType enumType) throws CoreException {
        List<IEnumAttribute> inheritedAttributes = enumType.getInheritedAttributes();
        List<IEnumAttribute> supertypeHierarchyAttributes = findAllAttributesInSupertypeHierarchy(enumType);
        List<IEnumAttribute> notInheritedAttributes = new ArrayList<IEnumAttribute>();

        for (IEnumAttribute currentSupertypeHierarchyAttribute : supertypeHierarchyAttributes) {
            if (!(EnumTypeUtil.containsEqualEnumAttribute(inheritedAttributes, currentSupertypeHierarchyAttribute))) {
                notInheritedAttributes.add(currentSupertypeHierarchyAttribute);
            }
        }

        return notInheritedAttributes;
    }

    /** Returns all attributes that are defined in the supertype hierarchy of the given enum type. */
    private static List<IEnumAttribute> findAllAttributesInSupertypeHierarchy(IEnumType enumType) throws CoreException {
        List<IEnumAttribute> returnAttributesList = new ArrayList<IEnumAttribute>();

        /* Go over all enum attributes of every enum type of the supertype hierarchy */
        for (IEnumType currentSuperEnumType : enumType.findAllSuperEnumTypes()) {
            for (IEnumAttribute currentEnumAttribute : currentSuperEnumType.getEnumAttributes()) {

                /*
                 * Add to the return list if the list does not yet contain an attribute with the
                 * name, datatype and identifier of the current inspected enum attribute from the
                 * supertype hierarchy.
                 */
                String currentName = currentEnumAttribute.getName();
                String currentDatatype = currentEnumAttribute.getDatatype();
                boolean currentIsIdentifier = currentEnumAttribute.isIdentifier();

                boolean attributeInList = false;
                for (IEnumAttribute currentAttributeInReturnList : returnAttributesList) {
                    if (currentAttributeInReturnList.getName().equals(currentName)
                            && currentAttributeInReturnList.getDatatype().equals(currentDatatype)
                            && currentAttributeInReturnList.isIdentifier() == currentIsIdentifier) {
                        attributeInList = true;
                        break;
                    }
                }

                if (!(attributeInList)) {
                    returnAttributesList.add(currentEnumAttribute);
                }
            }
        }

        return returnAttributesList;
    }

    /** Prohibits initialization. */
    private EnumTypeValidations() {

    }

}
