package org.faktorips.devtools.core.builder;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.Datatype;

/**
 * The Java naming convention determines for how published interfaces and their implementation
 * are named and how getter and setter are named.
 * 
 * @author Jan Ortmann
 */
public class JavaNamingConvention {

    /**
     * Standard Java naming convention. Interfaces have the name of the conecpt and the
     * implementation has the suffix 'Impl'.
     */
    public final static JavaNamingConvention JAVA_STANDARD = new JavaNamingConvention(0);

    /**
     * Standard Eclipse naming convention. Interfaces have the prefix 'I' and the
     * implementation has the concept name.
     */
    public final static JavaNamingConvention ECLIPSE_STANDARD = new JavaNamingConvention(1);
    
    private int type;
    
    private JavaNamingConvention(int type) {
        this.type = type;
    }

    /**
     * Returns the unqualified name of the published interface for the given concept name.
     */
    public String getPublishedInterfaceName(String name) {
        if (type==1) {
            return "I" + name;
        } else {
            return name;
        }
    }
    
    /**
     * Returns the unqualified name of the implementation class for the given concept name.
     */
    public String getImplementationClassName(String name) {
        if (type==1) {
            return name;
        } else {
            return name + "Impl";
        }
    }
    
    /**
     * Returns the name of the member variable for a property.
     */
    public String getMemberVarName(String propertyName) {
    	return StringUtils.uncapitalise(propertyName);
    }
    
    /**
     * Returns the name of the member variable for a multi value property.
     */
    public String getMultiValueMemberVarName(String propertyNamePlural) {
    	return StringUtils.uncapitalise(propertyNamePlural);
    }
    
    /**
     * Returns the method name for the method with that the property is read. 
     */
    public String getGetterMethodName(String propertyName, Datatype datatype) {
    	if (datatype.equals(Datatype.BOOLEAN) || datatype.equals(Datatype.PRIMITIVE_BOOLEAN)) {
    		return "is" + StringUtils.capitalise(propertyName);
    	}
		return "get" + StringUtils.capitalise(propertyName);
    }
    
    /**
     * Returns the method name for the method with that a multie value property is read. 
     */
    public String getMultiValueGetterMethodName(String propertyNamePlural) {
		return "get" + StringUtils.capitalise(propertyNamePlural);
    }
    
    /**
     * Returns the method name for the method with that the property is set. 
     */
    public String getSetterMethodName(String propertyName, Datatype datatype) {
		return "set" + StringUtils.capitalise(propertyName);
    }
    
    /**
     * Returns the modifier used for public interface methods. 
     */
    public int getModifierForPublicInterfaceMethod() {
    	return Modifier.PUBLIC;
    }
}
