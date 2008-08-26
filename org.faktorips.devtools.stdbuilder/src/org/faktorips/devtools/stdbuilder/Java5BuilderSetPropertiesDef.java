/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;

/**
 * An implementation of the {@link IIpsBuilderSetPropertyDef} interface specific for Java 5 properties. The default value for the enablement
 * depends on the java project setting.
 * 
 * @author Peter Erzberger
 */
public class Java5BuilderSetPropertiesDef implements IIpsBuilderSetPropertyDef {

    private String name;
    private String label;
    private String description;
    
    /**
     * Returns "true" if the java project setting are greater equals to 1.5 and false otherwise.
     */
    public String getDefaultValue(IIpsProject ipsProject) {
        return Boolean.toString(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject));
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns "false".
     */
    public String getDisableValue(IIpsProject ipsProject) {
        return Boolean.toString(false);
    }

    public Object[] getDiscreteValues() {
        return new Object[0];
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
    
    public boolean hasDiscreteValues() {
        return false;
    }

    public IStatus initialize(IIpsModel ipsModel, Map properties) {
        name = (String)properties.get("name"); //$NON-NLS-1$
        label = (String)properties.get("label");
        description = (String)properties.get("description"); //$NON-NLS-1$
        return null;
    }

    /**
     * Returns <code>true</code> if the java project setting are greater equals to 1.5 and false otherwise.
     */
    public boolean isAvailable(IIpsProject ipsProject) {
        return ComplianceCheck.isComplianceLevelAtLeast5(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public Object parseValue(String value) {
        return Boolean.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    public Message validateValue(String value) {
        try{
            Boolean.valueOf(value);
            return null;
        } catch(Exception e){
            String text = NLS.bind(Messages.Java5BuilderSetPropertiesDef_0, value);
            return new Message("", text, Message.ERROR); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getType() {
        // FIXME: need access to nested class PropertyDefEnumDatatype in IpsBuilderSetPropertyDef
        //  for now only boolean fields are taken into account, BROKEN!
        return ValueDatatype.BOOLEAN;
    }

}
