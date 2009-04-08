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

package org.faktorips.devtools.core.model.enums;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.enums.EnumTypeHierachyVisitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <code>IEnumType</code>, where some are also
 * used in the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeValidations {

    /**
     * Validates whether the given super enum type exists in the ips object path of the given ips
     * project and that super enum type is abstract.
     * <p>
     * Adds validation messages to the given message list.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumType The enum type that might be invalid or <code>null</code> if that information
     *            cannot be supported.
     * @param superEnumTypeQualifiedName The qualified name of the super enum type.
     * @param ipsProject The ips object path of this ips project will be searched.
     * 
     * @throws CoreException If an error occurs while searching for the super enum type.
     * @throws IllegalArgumentException If <code>superEnumTypeQualifiedName</code> is an empty
     *             string.
     * @throws NullPointerException If <code>validationMessageList</code>,
     *             <code>superEnumTypeQualifiedName</code> or <code>ipsProject</code> is
     *             <code>null</code>.
     */
    public static void validateSuperEnumType(MessageList validationMessageList,
            IEnumType enumType,
            String superEnumTypeQualifiedName,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { validationMessageList, superEnumTypeQualifiedName, ipsProject });
        ArgumentCheck.isTrue(!(superEnumTypeQualifiedName.equals(""))); //$NON-NLS-1$

        // Super enum type exists?
        IEnumType superEnumType = ipsProject.findEnumType(superEnumTypeQualifiedName);
        if (superEnumType == null) {
            String text = NLS.bind(Messages.EnumType_SupertypeDoesNotExist, superEnumTypeQualifiedName);
            Message message = new Message(
                    IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST,
                    text,
                    Message.ERROR,
                    enumType != null ? new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) }
                            : new ObjectProperty[0]);
            validationMessageList.add(message);
            return;
        }

        // Super enum type abstract?
        if (!(superEnumType.isAbstract())) {
            String text = NLS.bind(Messages.EnumType_SupertypeIsNotAbstract, superEnumType.getQualifiedName());
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) });
            validationMessageList.add(message);
        }
    }

    /**
     * Checks is the super type hierarchy of this enumeration type is valid. Therefore this method
     * checks if there exists a cycle in the type hierarchy, if there is an enumeration type in the
     * hierarchy for which the super type cannot be found and if there exists a super type that is
     * not abstract. Last is a constraint specific to faktor ips enumerations.
     * 
     * @param msgList the message list where messages are added to in cases of failing validations
     * @param enumType the enumeration type that is validated
     * @param ipsProject the IpsProject used as starting point for searches. Note: Not the
     *            IpsProject of the provided enumeration type is used within this method
     * @throws CoreException is thrown if an exception occurs during processing
     */
    public static void validateSuperTypeHierarchy(MessageList msgList, IEnumType enumType, IIpsProject ipsProject)
            throws CoreException {
        IEnumType superEnumType = enumType.findSuperEnumType();
        if (superEnumType == null) {
            return;
        }
        SupertypeCollector collector = new SupertypeCollector(ipsProject);
        collector.start(superEnumType);
        if (collector.cycleDetected()) {
            String msg = Messages.EnumType_cycleDetected;
            msgList.add(new Message(IEnumType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg, Message.ERROR, enumType,
                    IEnumType.PROPERTY_SUPERTYPE));
        } else {
            for (Iterator<IEnumType> it = collector.superTypes.iterator(); it.hasNext();) {
                MessageList superResult = it.next().validate(ipsProject);
                if (!superResult.isEmpty()) {
                    if (superResult.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST) != null
                            || superResult.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT) != null) {
                        String text = Messages.EnumType_inconsistentHierarchy;
                        msgList.add(new Message(IEnumType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY, text, Message.ERROR,
                                enumType, IEnumType.PROPERTY_SUPERTYPE));
                    }
                }
            }
        }
    }

    /**
     * Validates whether the given enum type inherits all enum attributes defined in its supertype
     * hierarchy.
     * <p>
     * Adds validation messages to the given message list.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumType The enum type to validate.
     * 
     * @throws CoreException If an error occurs while searching for attributes in the supertype
     *             hierarchy.
     * @throws NullPointerException If <code>validationMessageList</code> or <code>enumType</code>
     *             is <code>null</code>.
     */
    // TODO pk benötigt man diese methode wirklich als static an dieser Klasse. Wieso ist sie nicht
    // am IEnumType
    public static void validateInheritedAttributes(MessageList validationMessageList, IEnumType enumType)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { validationMessageList, enumType });

        // All attributes from supertype hierarchy inherited?
        List<IEnumAttribute> notInheritedAttributes = getNotInheritedAttributes(enumType);
        int notInheritedAttributesCount = notInheritedAttributes.size();
        if (notInheritedAttributesCount > 0) {
            IEnumAttribute firstNotInheritedAttribute = notInheritedAttributes.get(0);
            String showFirst = firstNotInheritedAttribute.getName() + " (" + firstNotInheritedAttribute.getDatatype() //$NON-NLS-1$
                    + ')';
            String text = (notInheritedAttributesCount > 1) ? NLS.bind(
                    Messages.EnumType_NotInheritedAttributesInSupertypeHierarchyPlural, notInheritedAttributesCount,
                    showFirst) : NLS.bind(Messages.EnumType_NotInheritedAttributesInSupertypeHierarchySingular,
                    showFirst);
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY,
                    text, Message.ERROR, new ObjectProperty[] { new ObjectProperty(enumType,
                            IEnumType.PROPERTY_SUPERTYPE) });
            validationMessageList.add(message);
        }
    }

    /**
     * Validates whether the given enum type has at least one attribute being marked as literal
     * name.
     * <p>
     * If the given enum type is abstract the validation will succeed even if there is no literal
     * name attribute.
     * <p>
     * Adds validation messages to the given message list.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumType The enum type to validate.
     * 
     * @throws NullPointerException If <code>validationMessageList</code> or <code>enumType</code>
     *             is <code>null</code>.
     */
    // TODO pk benötigt man diese methode wirklich als static an dieser Klasse. Wieso ist sie nicht
    // am IEnumType
    public static void validateLiteralNameAttribute(MessageList validationMessageList, IEnumType enumType) {
        ArgumentCheck.notNull(new Object[] { validationMessageList, enumType });

        // Pass validation if given enum type is abstract
        if (enumType.isAbstract()) {
            return;
        }

        boolean literalNameAttributeFound = false;
        for (IEnumAttribute currentEnumAttribute : enumType.findAllEnumAttributes()) {
            if (currentEnumAttribute.isLiteralName()) {
                literalNameAttributeFound = true;
                break;
            }
        }

        if (!(literalNameAttributeFound)) {
            String text = Messages.EnumType_NoLiteralNameAttribute;
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(enumType, null) });
            validationMessageList.add(message);
        }
    }

    /**
     * Returns all attributes from the supertype hierarchy of the given enum type that are not
     * inherited in the given enum type.
     */
    private static List<IEnumAttribute> getNotInheritedAttributes(IEnumType enumType) throws CoreException {
        List<IEnumAttribute> inheritedAttributes = new ArrayList<IEnumAttribute>();
        for (IEnumAttribute currentEnumAttribute : enumType.findAllEnumAttributes()) {
            if (currentEnumAttribute.isInherited()) {
                inheritedAttributes.add(currentEnumAttribute);
            }
        }
        List<IEnumAttribute> supertypeHierarchyAttributes = findAllAttributesInSupertypeHierarchy(enumType);
        List<IEnumAttribute> notInheritedAttributes = new ArrayList<IEnumAttribute>();

        for (IEnumAttribute currentSupertypeHierarchyAttribute : supertypeHierarchyAttributes) {
            if (!(EnumsUtil.containsEqualEnumAttribute(inheritedAttributes, currentSupertypeHierarchyAttribute))) {
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
                boolean currentIsIdentifier = currentEnumAttribute.isLiteralName();

                boolean attributeInList = false;
                for (IEnumAttribute currentAttributeInReturnList : returnAttributesList) {
                    if (currentAttributeInReturnList.getName().equals(currentName)
                            && currentAttributeInReturnList.getDatatype().equals(currentDatatype)
                            && currentAttributeInReturnList.isLiteralName() == currentIsIdentifier) {
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

    private static class SupertypeCollector extends EnumTypeHierachyVisitor {

        private List<IEnumType> superTypes = new ArrayList<IEnumType>();

        public SupertypeCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IEnumType currentType) throws CoreException {
            superTypes.add(currentType);
            return true;
        }
    }

}
