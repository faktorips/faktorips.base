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

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

public class DuplicatePropertyNameValidator extends TypeHierarchyVisitor {

    // Map with property names as keys. For a unqiue property name, the map contains the object (with the name) as value.
    // If there are multiple properties with a name, the value is a list containing all the objects with the same name.
    private Map<String, Object> properties = new HashMap<String, Object>();
    private List<String> duplicateProperties = new ArrayList<String>();
    
    public DuplicatePropertyNameValidator(IIpsProject ipsProject) {
        super(ipsProject);
    }
    
    protected Message createMessage(String propertyName, ObjectProperty[] invalidObjProperties){
        String text = NLS.bind(Messages.DuplicatePropertyNameValidator_msg, propertyName);
        return new Message(IType.MSGCODE_DUPLICATE_PROPERTY_NAME, text, Message.ERROR, invalidObjProperties);
    }
    
    @SuppressWarnings("unchecked")
    public void addMessagesForDuplicates(MessageList messages) {
        for (String propertyName : duplicateProperties) {
            List<ObjectProperty> objects = (List<ObjectProperty>)properties.get(propertyName);
            ObjectProperty[] invalidObjProperties = (ObjectProperty[])objects.toArray(new ObjectProperty[objects.size()]);
            messages.add(createMessage(propertyName, invalidObjProperties));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean visit(IType currentType) throws CoreException {
        Type currType = (Type)currentType;
        for (Iterator<? extends IAttribute> it=currType.getIteratorForAttributes(); it.hasNext(); ) {
            IAttribute attr = (IAttribute)it.next();
            if (!attr.isOverwrite()) {
                add(attr.getName().toLowerCase(), new ObjectProperty(attr, IAttribute.PROPERTY_NAME));
            }
        }
        for (Iterator<? extends IAssociation> it=currType.getIteratorForAssociations(); it.hasNext(); ) {
            IAssociation ass = (IAssociation)it.next();
            //TODO it needs to be clarified if we should ask the builder set which naming conventions are used and instead of just
            //uncapitalize ask the naming convention how it is handled
            if (ass.is1ToMany()) {
                add(ass.getTargetRolePlural().toLowerCase(), new ObjectProperty(ass, IAssociation.PROPERTY_TARGET_ROLE_PLURAL));
            } else {
                add(ass.getTargetRoleSingular().toLowerCase(), new ObjectProperty(ass, IAssociation.PROPERTY_TARGET_ROLE_SINGULAR));
            }
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
    protected void add(String propertyName, ObjectProperty wrapper) {
        Object objInMap = properties.get(propertyName);
        if (objInMap==null) {
            properties.put(propertyName, wrapper);
            return;
        }
        if (objInMap instanceof List) {
            ((List<ObjectProperty>)objInMap).add(wrapper);
            return;
        }
        List<ObjectProperty> objects = new ArrayList<ObjectProperty>(2);
        objects.add((ObjectProperty)objInMap);
        objects.add(wrapper);
        properties.put(propertyName, objects);
        duplicateProperties.add(propertyName);
    }

}
