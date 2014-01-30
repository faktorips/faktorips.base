/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

public class DuplicatePropertyNameValidator extends TypeHierarchyVisitor<IType> {

    /*
     * Map with property names as keys. For a unqiue property name, the map contains the object
     * (with the name) as value. If there are multiple properties with a name, the value is a list
     * containing all the objects with the same name.
     */
    private Map<String, ObjectProperty[]> properties = new HashMap<String, ObjectProperty[]>();
    private List<String> duplicateProperties = new ArrayList<String>();

    public DuplicatePropertyNameValidator(IIpsProject ipsProject) {
        super(ipsProject);
    }

    protected Message createMessage(String propertyName, ObjectProperty[] invalidObjProperties) {
        String text = NLS.bind(Messages.DuplicatePropertyNameValidator_msg, propertyName);
        return new Message(IType.MSGCODE_DUPLICATE_PROPERTY_NAME, text, Message.ERROR, invalidObjProperties);
    }

    public void addMessagesForDuplicates(MessageList messages) {
        for (String propertyName : duplicateProperties) {
            ObjectProperty[] duplicateObjProperties = properties.get(propertyName);
            if (!ignore(duplicateObjProperties)) {
                messages.add(createMessage(propertyName, duplicateObjProperties));
            }
        }
    }

    /**
     * In some cases there are duplicate object properties that are valid, for example in case of
     * constraining associations or detail-to-master associations. This method checks if the
     * duplicate properties can be ignored.
     * 
     * @param duplicateObjectProperties The array of duplicated properties
     * @return <code>true</code> if the duplication could be ignored, <code>false</code> to not
     *         ignore.
     */
    protected boolean ignore(ObjectProperty[] duplicateObjectProperties) {
        if (!checkAssociationAndType(duplicateObjectProperties)) {
            return false;
        }
        if (ignoreConstrainingAssociation(duplicateObjectProperties)) {
            return true;
        }
        return ignoreDuplicateDetailToMasterAssociations(duplicateObjectProperties);
    }

    /**
     * We can ignore the duplicateObjectProperties, if the first association is a constraining
     * association because we already checked that this is the only association in the current type.
     * The other associations are checked in supertype.
     * 
     * @return <code>true</code> to ignore the the duplicated properties, <code>false</code> to not
     *         ignore (move on)
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
     *         testing), false if we cannot ignore
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
                IPolicyCmptType target = association.findTargetPolicyCmptType(ipsProject);
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
                    IPolicyCmptType nextTarget = nextAssociation.findTargetPolicyCmptType(ipsProject);
                    if (nextTarget == null) {
                        return false;
                    }
                    if (!target.isSubtypeOrSameType(nextTarget, ipsProject)) {
                        return false;
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
            index++;
        }

        return true;
    }

    private boolean checkNotInverseOfDerivedUnion(IPolicyCmptTypeAssociation association) throws CoreException {
        return !association.isInverseOfDerivedUnion() && !association.isSharedAssociation();
    }

    @Override
    protected boolean visit(IType currentType) {
        Type currType = (Type)currentType;
        for (IAttribute attr : currType.getAttributesPartCollection()) {
            if (!attr.isOverwrite()) {
                add(attr.getName().toLowerCase(), new ObjectProperty(attr, IIpsElement.PROPERTY_NAME));
            }
        }
        for (IAssociation ass : currType.getAssociationPartCollection()) {
            if (ass.is1ToMany()) {
                // target role plural only check if is many association
                add(ass.getTargetRolePlural().toLowerCase(), new ObjectProperty(ass,
                        IAssociation.PROPERTY_TARGET_ROLE_PLURAL));
            }
            // always check target role singular
            add(ass.getTargetRoleSingular().toLowerCase(), new ObjectProperty(ass,
                    IAssociation.PROPERTY_TARGET_ROLE_SINGULAR));
        }
        return true;
    }

    protected void add(String propertyName, ObjectProperty wrapper) {
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
}
