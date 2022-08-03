/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import static java.util.Objects.requireNonNull;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;
import org.faktorips.runtime.ObjectProperty;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A class that contains validations for the model class {@link IType} which are also used in the
 * creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Peter Erzberger
 */

public class TypeValidations {

    private static final Set<Set<IpsObjectType>> SAME_QNAME_ALLOWED = Set.of(
            Set.of(IpsObjectType.ENUM_TYPE, IpsObjectType.ENUM_CONTENT),
            Set.of(IpsObjectType.TABLE_STRUCTURE, IpsObjectType.TABLE_CONTENTS),
            Set.of(IpsObjectType.TEST_CASE_TYPE, IpsObjectType.TEST_CASE));

    private static final Map<IpsObjectType, List<IpsObjectType>> CONFLICTING_OBJECT_TYPES = new HashMap<>();

    private TypeValidations() {
        // Utility class not to be instantiated.
    }

    /**
     * Validates if there exists already a policy component type or product component type with the
     * same name in the IPS object path. The method checks when a product component type is
     * validated if a policy component type with the same name exists within the IPS object path and
     * vice versa.
     * <p>
     * Returns a message if the validation fails, otherwise <code>null</code>.
     * 
     * @param otherIpsObjectType The {@link IpsObjectType} of the other type. If for example a
     *            product component type is validated, this has to be
     *            {@link IpsObjectType#POLICY_CMPT_TYPE}.
     * @param qualifiedName The qualified name of the type that is to validate.
     * @param ipsProject The IPS project.
     * @param ipsObject The model object of the type that is to validate.
     * 
     * @throws IpsException Any raised exceptions are delegated by this method.
     * @deprecated since 21.6; use {@link #validateUniqueQualifiedName(IIpsObject)} or
     *                 {@link #validateUniqueQualifiedName(IpsObjectType, String, IIpsProject)}
     *                 instead.
     */
    @Deprecated
    public static Message validateOtherTypeWithSameNameTypeInIpsObjectPath(IpsObjectType otherIpsObjectType,
            String qualifiedName,
            IIpsProject ipsProject,
            IIpsObject ipsObject) {
        IIpsSrcFile file = ipsProject.findIpsSrcFile(otherIpsObjectType, qualifiedName);
        if (file != null) {
            if (ipsProject.equals(file.getIpsProject())) {
                return new Message(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS, MessageFormat.format(
                        Messages.Type_msgOtherTypeWithSameQNameInSameProject, otherIpsObjectType.getDisplayName()),
                        Message.ERROR, ipsObject != null ? new ObjectProperty[] { new ObjectProperty(ipsObject, null) }
                                : new ObjectProperty[0]);
            }
            return new Message(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS,
                    MessageFormat.format(
                            Messages.Type_msgOtherTypeWithSameQNameInDependentProject,
                            otherIpsObjectType.getId(), file.getIpsProject()),
                    Message.WARNING,
                    ipsObject != null ? new ObjectProperty[] { new ObjectProperty(ipsObject, IIpsObject.PROPERTY_NAME) }
                            : new ObjectProperty[0]);

        }
        return null;
    }

    /**
     * Validates if there exists already another with the same qualified name in the IPS object path
     * that is not of an allowed type for duplicate names, for example an enum content matching an
     * enum type or a single table content matching a table structure.
     * <p>
     * Returns a message list that contains a message for every other object found, if any.
     * 
     * @param ipsObject the model object that is to be validated
     * 
     * @throws IpsException Any raised exceptions are delegated by this method.
     */
    public static MessageList validateUniqueQualifiedName(IIpsObject ipsObject) {
        requireNonNull(ipsObject, "ipsObject must not be null"); //$NON-NLS-1$
        return validateUniqueQualifiedName(ipsObject, ipsObject.getIpsObjectType(), ipsObject.getQualifiedName(),
                ipsObject.getIpsProject());
    }

    /**
     * Validates if there exists already another with the same qualified name in the IPS object path
     * that is not of an allowed type for duplicate names, for example an enum content matching an
     * enum type or a single table content matching a table structure.
     * <p>
     * Returns a message list that contains a message for every other object found, if any.
     *
     * @param ipsObjectType the model object type that is to be validated
     * @param qualifiedName the qualified name of the type that is to be validated
     * @param ipsProject the IPS project
     * 
     * @throws IpsException Any raised exceptions are delegated by this method.
     */
    public static MessageList validateUniqueQualifiedName(IpsObjectType ipsObjectType,
            String qualifiedName,
            IIpsProject ipsProject) {
        return validateUniqueQualifiedName(null, ipsObjectType, qualifiedName, ipsProject);
    }

    /**
     * Validates if there exists already another with the same qualified name in the IPS object path
     * that is not of an allowed type for duplicate names, for example an enum content matching an
     * enum type or a single table content matching a table structure.
     * <p>
     * Returns a message list that contains a message for every other object found, if any.
     * 
     * @param ipsObject the model object that is to be validated (optional, will be used as invalid
     *            object in messages)
     * @param ipsObjectType the model object type that is to be validated
     * @param qualifiedName the qualified name of the type that is to be validated
     * @param ipsProject the IPS project
     * 
     * @throws IpsException Any raised exceptions are delegated by this method.
     */
    private static MessageList validateUniqueQualifiedName(@CheckForNull IIpsObject ipsObject,
            IpsObjectType ipsObjectType,
            String qualifiedName,
            IIpsProject ipsProject) {
        requireNonNull(ipsObjectType, "ipsObjectType must not be null"); //$NON-NLS-1$
        requireNonNull(qualifiedName, "qualifiedName must not be null"); //$NON-NLS-1$
        requireNonNull(ipsProject, "ipsProject must not be null"); //$NON-NLS-1$
        return CONFLICTING_OBJECT_TYPES
                .computeIfAbsent(ipsObjectType, ot -> Arrays.stream(IIpsModel.get().getIpsObjectTypes())
                        .filter(t -> !ot.equals(t))
                        .filter(t -> SAME_QNAME_ALLOWED.stream().noneMatch(s -> s.contains(ot) && s.contains(t)))
                        .collect(Collectors.toList()))
                .stream()
                .map(t -> ipsProject.findIpsSrcFile(t, qualifiedName))
                .filter(f -> f != null)
                .map(file -> dupicateQualifiedName(ipsObject, ipsProject, file))
                .collect(MessageLists.collectMessages());
    }

    private static Message dupicateQualifiedName(IIpsObject ipsObject, IIpsProject ipsProject, IIpsSrcFile file) {
        var otherIpsObjectType = file.getIpsObjectType();
        var invalidObjectProperties = ipsObject != null
                ? new ObjectProperty[] { new ObjectProperty(ipsObject, IIpsObject.PROPERTY_NAME) }
                : new ObjectProperty[0];
        if (ipsProject.equals(file.getIpsProject())) {
            return new Message(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS, MessageFormat.format(
                    Messages.Type_msgOtherTypeWithSameQNameInSameProject, otherIpsObjectType.getDisplayName()),
                    Message.ERROR,
                    invalidObjectProperties);
        }
        return new Message(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS, MessageFormat.format(
                Messages.Type_msgOtherTypeWithSameQNameInDependentProject, otherIpsObjectType.getId(),
                file.getIpsProject()), Message.WARNING,
                invalidObjectProperties);
    }

    /**
     * Validates the type hierarchy of the given type. Add
     * 
     * @param type The type of which you want to validate the hierarchy
     * @param ipsProject the project that is used as reference to find other objects
     */
    public static Message validateTypeHierachy(IType type, IIpsProject ipsProject) {
        if (StringUtils.isEmpty(type.getSupertype())) {
            return null;
        }
        IType superType = type.findSupertype(ipsProject);
        if (superType == null) {
            String text = MessageFormat.format(Messages.Type_msg_supertypeNotFound, type.getSupertype());
            return new Message(IType.MSGCODE_SUPERTYPE_NOT_FOUND, text, Message.ERROR, type, IType.PROPERTY_SUPERTYPE);
        }

        SupertypesValidator validator = new SupertypesValidator(ipsProject);

        validator.start(superType);

        if (!validator.result) {
            String text = Messages.Type_msg_TypeHierarchyInconsistent;
            return new Message(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY, text, Message.ERROR, type,
                    IType.PROPERTY_SUPERTYPE);
        }

        if (validator.cycleDetected()) {
            String msg = Messages.Type_msg_cycleInTypeHierarchy;
            return new Message(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg, Message.ERROR, type,
                    IType.PROPERTY_SUPERTYPE);
        }
        return null;
    }

    private static class SupertypesValidator extends TypeHierarchyVisitor<IType> {

        private boolean result;

        public SupertypesValidator(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) {
            if (StringUtils.isEmpty(currentType.getSupertype())) {
                // there should be no more super type
                result = true;
                return false;
            }
            result = isNull(currentType);
            return result;
        }

        private boolean isNull(IType currentType) {
            return currentType.findSupertype(getIpsProject()) != null;
        }

    }

}
