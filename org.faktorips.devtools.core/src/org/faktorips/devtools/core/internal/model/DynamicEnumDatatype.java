package org.faktorips.devtools.core.internal.model;

import org.faktorips.datatype.DefaultGenericEnumDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * A dynamic enum datatype. See the super class for more detais.
 * 
 * @author Jan Ortmann
 */
public class DynamicEnumDatatype extends DynamicValueDatatype implements EnumDatatype {

    public DynamicEnumDatatype(IIpsProject ipsProject) {
        super(ipsProject);
    }

    /**
     * Overridden.
     */
    public String[] getAllValueIds() {
        if (getAdaptedClass()==null) {
            throw new RuntimeException("Datatype " + getQualifiedName() + ", Class " + getAdaptedClassName() + " not found.");
        }
        return new DefaultGenericEnumDatatype(getAdaptedClass()).getAllValueIds();
    }

}
