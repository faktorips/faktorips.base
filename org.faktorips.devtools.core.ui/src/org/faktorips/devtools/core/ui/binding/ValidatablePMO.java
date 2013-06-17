/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.binding;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

public abstract class ValidatablePMO extends PresentationModelObject implements Validatable {

    private Map<ObjectProperty, ObjectProperty> objectPropertyMapping = new HashMap<ObjectProperty, ObjectProperty>();

    public Map<ObjectProperty, ObjectProperty> getObjectPropertyMapping() {
        return objectPropertyMapping;
    }

    protected ObjectPropertyMappingDefinition mapValidationMessagesFor(ObjectProperty objectProperty) {
        return new ObjectPropertyMappingDefinition(this, objectProperty);
    }

    protected void addMapping(ObjectProperty fromProperty, ObjectProperty toProperty) {
        objectPropertyMapping.put(fromProperty, toProperty);
    }

    public void clearObjectPropertyMappings() {
        objectPropertyMapping.clear();
    }

    protected MessageList createCopyAndMapObjectProperties(MessageList messageList) {
        return messageList.createCopy(getObjectPropertyMapping());
    }

    @Override
    public boolean isValid() throws CoreException {
        return isValid(getIpsProject());
    }

    @Override
    public boolean isValid(IIpsProject ipsProject) throws CoreException {
        return !validate(ipsProject).containsErrorMsg();
    }

    @Override
    public int getValidationResultSeverity() throws CoreException {
        return getValidationResultSeverity(getIpsProject());
    }

    @Override
    public int getValidationResultSeverity(IIpsProject ipsProject) throws CoreException {
        return validate(ipsProject).getSeverity();
    }

    @Override
    public abstract MessageList validate(IIpsProject ipsProject) throws CoreException;

    @Override
    public abstract IIpsProject getIpsProject();

    /**
     * Utility class that allows writing easily readable mapping definitions. e.g.
     * <tt>pmo.mapValidationMessagesFor(modelProperty).to(uiProperty);</tt>
     * 
     * 
     * @author Stefan Widmaier
     */
    public class ObjectPropertyMappingDefinition {
    
        private final ObjectProperty fromProperty;
        private final ValidatablePMO validatablePmo;
    
        public ObjectPropertyMappingDefinition(ValidatablePMO validatablePmo, ObjectProperty objectProperty) {
            this.validatablePmo = validatablePmo;
            this.fromProperty = objectProperty;
        }
    
        public void to(ObjectProperty toProperty) {
            validatablePmo.addMapping(fromProperty, toProperty);
        }
    }

}
