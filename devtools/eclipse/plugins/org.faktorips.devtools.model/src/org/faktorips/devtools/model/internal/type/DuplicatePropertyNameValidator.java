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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

public abstract class DuplicatePropertyNameValidator extends TypeHierarchyVisitor<IType> {

    /*
     * Map with property names as keys. For a unique property name, the map contains the object
     * (with the name) as value. If there are multiple properties with a name, the value is a list
     * containing all the objects with the same name.
     */
    private Map<String, ObjectProperty[]> properties = new HashMap<>();
    private List<String> duplicateProperties = new ArrayList<>();

    public DuplicatePropertyNameValidator(IIpsProject ipsProject) {
        super(ipsProject);
    }

    public void addMessagesForDuplicates(IType currentType, MessageList messages) {
        for (String propertyName : duplicateProperties) {
            ObjectProperty[] duplicateObjProperties = properties.get(propertyName);
            if (!ignore(currentType, duplicateObjProperties)) {
                messages.add(createMessage(propertyName, duplicateObjProperties));
            }
        }
    }

    protected Message createMessage(String propertyName, ObjectProperty[] invalidObjProperties) {
        String text = createNLSBinding(propertyName, invalidObjProperties);
        return new Message(IType.MSGCODE_DUPLICATE_PROPERTY_NAME, text, Message.ERROR, invalidObjProperties);
    }

    private String createNLSBinding(String propertyName, ObjectProperty[] invalidObjProperties) {
        return MessageFormat.format(Messages.DuplicatePropertyNameValidator_msg, propertyName,
                createMoreSpecificErrorText(invalidObjProperties[0], invalidObjProperties[1]));
    }

    /**
     * The error message created in <code>createNLSBinding</code> can contain further informations.
     * If more than two IpsElements contain the same propertyName, only one error message will be
     * generated for the first two objectProperties. If the invalidObjProperties are from the same
     * {@link IType} but instances of different {@link IpsObjectPartContainer}, the message will
     * indicate which {@link IpsObjectPartContainer} have to be considered. If the elements are from
     * different {@link IType} the message indicates what {@link IpsObjectPartContainer} in which
     * {@link IType} are named ambiguously. If the invalidProperties are instances of the same
     * {@link IpsObjectPartContainer} and {@link IType}, an empty string will be returned.
     * 
     */
    protected String createMoreSpecificErrorText(ObjectProperty invalidObjProperty1,
            ObjectProperty invalidObjProperty2) {
        if (isIpsObjectPartContainer(invalidObjProperty1, invalidObjProperty2)) {
            IpsObjectPartContainer ipsObjectContainer2 = ((IpsObjectPartContainer)invalidObjProperty2.getObject());
            IpsObjectPartContainer ipsObjectContainer1 = ((IpsObjectPartContainer)invalidObjProperty1.getObject());

            if (isDifferentIpsObject(ipsObjectContainer2, ipsObjectContainer1)) {
                return createTextForDiffITypes(ipsObjectContainer2);
            } else if (isDifferentIpsObjectPartContainer(ipsObjectContainer2, ipsObjectContainer1)) {
                return createTextForDiffIpsObjPartContainer(invalidObjProperty1, invalidObjProperty2);
            }
        }
        return StringUtils.EMPTY;
    }

    private boolean isDifferentIpsObject(IpsObjectPartContainer ipsObjectContainer2,
            IpsObjectPartContainer ipsObjectContainer1) {
        return !(ipsObjectContainer1.getIpsObject().equals(ipsObjectContainer2.getIpsObject()));
    }

    private boolean isDifferentIpsObjectPartContainer(IpsObjectPartContainer ipsObjectContainer2,
            IpsObjectPartContainer ipsObjectContainer1) {
        return !(ipsObjectContainer1.getClass().equals(ipsObjectContainer2.getClass()));
    }

    private String createTextForDiffITypes(IpsObjectPartContainer ipsObjectContainer2) {
        return MessageFormat.format(Messages.DuplicatePropertyNameValidator_msg_DifferentElementsAndITypes,
                getObjectKindNameSingular(ipsObjectContainer2), ipsObjectContainer2.getIpsObject().getName());
    }

    private String createTextForDiffIpsObjPartContainer(ObjectProperty invalidObjProperty1,
            ObjectProperty invalidObjProperty2) {
        return MessageFormat.format(Messages.DuplicatePropertyNameValidator_msg_DifferentElementsSameType,
                StringUtils.capitalize(getObjectKindNamePlural(invalidObjProperty1)),
                getObjectKindNamePlural(invalidObjProperty2));
    }

    private boolean isIpsObjectPartContainer(ObjectProperty invalidObjProperty1, ObjectProperty invalidObjProperty2) {
        return invalidObjProperty1.getObject() instanceof IpsObjectPartContainer
                && invalidObjProperty2.getObject() instanceof IpsObjectPartContainer;
    }

    protected String getObjectKindNamePlural(ObjectProperty invalidObjProperty) {
        IIpsObjectPartContainer objectPartContainer = ((IIpsObjectPartContainer)invalidObjProperty.getObject());
        if (objectPartContainer instanceof IAttribute) {
            return Messages.DuplicatePropertyNameValidator_PluralAttribute;
        }
        if (objectPartContainer instanceof IAssociation) {
            return Messages.DuplicatePropertyNameValidator_PluralAssociation;
        }
        if (objectPartContainer instanceof IMethod) {
            return Messages.DuplicatePropertyNameValidator_PluralMethod;
        }
        if (objectPartContainer instanceof IProductCmptType) {
            return Messages.DuplicatePropertyNameValidator_ProductCmptTypeItself;
        }
        return Messages.DuplicatePropertyNameValidator_PluralElement;
    }

    protected String getObjectKindNameSingular(IpsObjectPartContainer objectPartContainer) {
        if (objectPartContainer instanceof IAttribute) {
            return Messages.DuplicatePropertyNameValidator_SingularAttribute;
        }
        if (objectPartContainer instanceof IAssociation) {
            return Messages.DuplicatePropertyNameValidator_SingularAssociation;
        }
        if (objectPartContainer instanceof IMethod) {
            return Messages.DuplicatePropertyNameValidator_SingularMethod;
        }
        return Messages.DuplicatePropertyNameValidator_SingularElement;
    }

    /**
     * In some cases there are duplicate object properties that are valid, for example in case of
     * constraining associations or detail-to-master associations. This method checks if the
     * duplicate properties can be ignored.
     * 
     * @param duplicateObjectProperties The array of duplicated properties
     * @return <code>true</code> if the duplication could be ignored, <code>false</code> to not
     *             ignore.
     */
    protected boolean ignore(IType currentType, ObjectProperty[] duplicateObjectProperties) {
        if (!isOnePropertyInThisType(currentType, duplicateObjectProperties)) {
            return true;
        }
        if (!checkAssociationAndType(duplicateObjectProperties)) {
            return false;
        }
        if (ignoreConstrainingAssociation(duplicateObjectProperties)) {
            return true;
        }
        return ignoreDuplicateDetailToMasterAssociations(duplicateObjectProperties);
    }

    private boolean isOnePropertyInThisType(IType currentType, ObjectProperty[] duplicateObjectProperties) {
        for (ObjectProperty objectProperty : duplicateObjectProperties) {
            if (objectProperty.getObject() instanceof IIpsObjectPartContainer) {
                IIpsObjectPartContainer part = (IIpsObjectPartContainer)objectProperty.getObject();
                if (part.getIpsObject().equals(currentType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * We can ignore the duplicateObjectProperties, if the first association is a constraining
     * association because we already checked that this is the only association in the current type.
     * The other associations are checked in supertype.
     * 
     * @return <code>true</code> to ignore the the duplicated properties, <code>false</code> to not
     *             ignore (move on)
     */
    private boolean ignoreConstrainingAssociation(ObjectProperty[] duplicateObjectProperties) {
        return ((IAssociation)duplicateObjectProperties[0].getObject()).isConstrain();
    }

    /**
     * Check that only IAssociations are in the array and that no other object but the first one is
     * in the same type. These are fast validations in the first iteration, for poor performance
     * validations we have a second iteration
     * 
     * @return <code>true</code> if these objectProperties may be ignored (depending on further
     *             testing), false if we cannot ignore
     */
    private boolean checkAssociationAndType(ObjectProperty[] objectProperties) {
        IType typeToValidate = null;
        for (ObjectProperty property : objectProperties) {
            if (!(property.getObject() instanceof IAssociation)) {
                return false;
            }
            IAssociation association = (IAssociation)property.getObject();
            if (typeToValidate == null) {
                // first get the type of the first association. This is the type we want to validate
                typeToValidate = association.getType();
                if (typeToValidate == null) {
                    return false;
                }
            } else {
                // if there is another property with the same name in this type, do not ignore
                if (typeToValidate.equals(association.getType())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * The detail-to-master association that is a subset of a derived union association could have
     * the same name as the corresponding derived union association
     * 
     * @param duplicateObjectProperties the ObjectProperties to check
     * @return true to ignore this property
     */
    private boolean ignoreDuplicateDetailToMasterAssociations(ObjectProperty[] duplicateObjectProperties) {
        for (ObjectProperty objectProperty : duplicateObjectProperties) {
            if (!(objectProperty.getObject() instanceof IPolicyCmptTypeAssociation)) {
                return false;
            }
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)objectProperty.getObject();
            if (!association.getAssociationType().isCompositionDetailToMaster()) {
                return false;
            }
        }
        return checkNotInverseofDerivedUnion(duplicateObjectProperties);
    }

    private boolean checkNotInverseofDerivedUnion(ObjectProperty[] objectProperties) {
        int index = 0;
        boolean foundNotInverseOfDerivedUnion = false;

        for (ObjectProperty property : objectProperties) {
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)property.getObject();
            try {
                IPolicyCmptType target = association.findTargetPolicyCmptType(getIpsProject());
                if (target == null) {
                    return false;
                }
                // shared associations must have the same name
                boolean isNotInverseOfDerivedUnion = checkNotInverseOfDerivedUnion(association);
                if (isNotInverseOfDerivedUnion && foundNotInverseOfDerivedUnion) {
                    // there could be only one association that is no inverse of a derived union
                    // in type hierarchy! (FIPS-459)
                    return false;
                } else {
                    foundNotInverseOfDerivedUnion = foundNotInverseOfDerivedUnion || isNotInverseOfDerivedUnion;
                }
                // the target of the association have to be covariant with the other
                // associations
                for (int i = index; i < objectProperties.length; i++) {
                    IPolicyCmptTypeAssociation nextAssociation = (IPolicyCmptTypeAssociation)objectProperties[i]
                            .getObject();
                    IPolicyCmptType nextTarget = nextAssociation.findTargetPolicyCmptType(getIpsProject());
                    if ((nextTarget == null) || !target.isSubtypeOrSameType(nextTarget, getIpsProject())) {
                        return false;
                    }
                }
            } catch (IpsException e) {
                IpsLog.log(e);
                return false;
            }
            index++;
        }

        return true;
    }

    private boolean checkNotInverseOfDerivedUnion(IPolicyCmptTypeAssociation association) {
        return !association.isInverseOfDerivedUnion() && !association.isSharedAssociation();
    }

    @Override
    protected boolean visit(IType currentType) {
        addAttributes(currentType);
        addAssociations(currentType);
        addMatchingType(currentType);
        return true;
    }

    private void addMatchingType(IType currentType) {
        IType matchingType = getMatchingType(currentType);
        if (matchingType != null) {
            addMatchingAttributes(matchingType);
        }
    }

    protected abstract IType getMatchingType(IType currentType);

    private void addMatchingAttributes(IType matchingType) {
        for (IAttribute attribute : matchingType.getAttributes()) {
            add(attribute.getName(), new ObjectProperty(attribute, IAssociation.PROPERTY_NAME));
        }
    }

    private void addAttributes(IType currentType) {
        for (IAttribute attr : currentType.getAttributes()) {
            if (!attr.isOverwrite()) {
                add(attr.getName(), new ObjectProperty(attr, IIpsElement.PROPERTY_NAME));
            }
        }
    }

    private void addAssociations(IType currentType) {
        for (IAssociation ass : currentType.getAssociations()) {
            if (ass.is1ToMany() && !ass.getTargetRoleSingular().equalsIgnoreCase(ass.getTargetRolePlural())) {
                // target role plural only check if is many association
                add(ass.getTargetRolePlural(), new ObjectProperty(ass, IAssociation.PROPERTY_TARGET_ROLE_PLURAL));
            }
            // always check target role singular
            add(ass.getTargetRoleSingular(), new ObjectProperty(ass, IAssociation.PROPERTY_TARGET_ROLE_SINGULAR));
        }
    }

    protected void add(String originalPropertyName, ObjectProperty wrapper) {
        String propertyName = originalPropertyName.toLowerCase();
        Object objInMap = properties.get(propertyName);
        if (objInMap == null) {
            properties.put(propertyName, new ObjectProperty[] { wrapper });
            return;
        }
        if (objInMap instanceof ObjectProperty[]) {
            ObjectProperty[] objects = (ObjectProperty[])objInMap;
            int i = objects.length;
            ObjectProperty[] objectsCopy = Arrays.copyOf(objects, i + 1);
            objectsCopy[i] = wrapper;
            properties.put(propertyName, objectsCopy);

            if (i == 1) {
                // there is already an object with this name
                duplicateProperties.add(propertyName);
            }
        }
    }

    protected Map<String, ObjectProperty[]> getProperties() {
        return properties;
    }
}
