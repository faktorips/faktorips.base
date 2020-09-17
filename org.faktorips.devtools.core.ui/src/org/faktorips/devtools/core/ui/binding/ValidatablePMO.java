/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
     * <code>pmo.mapValidationMessagesFor(modelProperty).to(uiProperty);</code>
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
