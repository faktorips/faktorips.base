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

	private String getAllValuesMethodName = "";
	
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
        DefaultGenericEnumDatatype datatype =new DefaultGenericEnumDatatype(getAdaptedClass());
        datatype.setGetAllValuesMethodName(getAllValuesMethodName);
        return datatype.getAllValueIds();
    }
 
    /**
     * Sets the name of the method that provides all values of the datatype.
     */
    public void setAllValuesMethodName(String getAllValuesMethodName){
    	this.getAllValuesMethodName = getAllValuesMethodName;
    }
}
