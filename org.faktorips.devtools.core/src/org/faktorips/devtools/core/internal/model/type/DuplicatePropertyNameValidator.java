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

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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

public class DuplicatePropertyNameValidator extends TypeHierarchyVisitor {

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
            ObjectProperty[] invalidObjProperties = properties.get(propertyName);
            if (!ignoreDuplicatedInverseAssociationsForDerivedUnions(invalidObjProperties)) {
                messages.add(createMessage(propertyName, invalidObjProperties));
            }
        }
    }

    // TODO set to protected and refactor the test with mockito!!!
    /**
     * The detail-to-master association that is a subset of a derived union association could have
     * the same name as the corresponding derived union association
     * 
     * @param objectProperties the ObjectProperties to check
     * @return true to ignore this property
     */
    public boolean ignoreDuplicatedInverseAssociationsForDerivedUnions(ObjectProperty[] objectProperties) {
        IType typeToValidate = null;
        int index = 0;
        // check that only IPolicyCmptTypeAssociations are in the array and that no other object but
        // the first one is in the same type
        for (ObjectProperty property : objectProperties) {
            if (!(property.getObject() instanceof IPolicyCmptTypeAssociation)) {
                return false;
            }
            if (typeToValidate == null) {
                // first get the type of the first association. This is the type we want to validate
                typeToValidate = ((IPolicyCmptTypeAssociation)objectProperties[0].getObject()).getType();
                if (typeToValidate == null) {
                    return false;
                }
            } else {
                if (typeToValidate.equals(((IPolicyCmptTypeAssociation)property.getObject()).getType())) {
                    return false;
                }
            }
        }
        for (ObjectProperty property : objectProperties) {
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)property.getObject();
            // every association have to be a Detail-TO-Master association
            if (!association.getAssociationType().isCompositionDetailToMaster()) {
                return false;
            }
            try {
                IPolicyCmptType target = association.findTargetPolicyCmptType(ipsProject);
                if (target == null) {
                    return false;
                }
                // the target of the association have to be covariant with the other associations
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

    @Override
    protected boolean visit(IType currentType) throws CoreException {
        Type currType = (Type)currentType;
        for (Iterator<? extends IAttribute> it = currType.getIteratorForAttributes(); it.hasNext();) {
            IAttribute attr = it.next();
            if (!attr.isOverwrite()) {
                add(attr.getName().toLowerCase(), new ObjectProperty(attr, IIpsElement.PROPERTY_NAME));
            }
        }
        for (Iterator<? extends IAssociation> it = currType.getIteratorForAssociations(); it.hasNext();) {
            IAssociation ass = it.next();
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
