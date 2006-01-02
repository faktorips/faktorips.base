package org.faktorips.codegen.dthelpers;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.DefaultEnumValue;
import org.faktorips.datatype.EnumType;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultEnumTypeHelper implements DatatypeHelper {

    private Class enumValueClass;
    private EnumType enumType;
    private String valueOfMethodname;
    
    public DefaultEnumTypeHelper(Class enumValueClass, String getEnumTypeMethod, String valueOfMethodname) {
        ArgumentCheck.isSubclassOf(enumValueClass, DefaultEnumValue.class);
        ArgumentCheck.notNull(getEnumTypeMethod);
        ArgumentCheck.notNull(valueOfMethodname);
        this.enumValueClass = enumValueClass;
        this.valueOfMethodname = valueOfMethodname;
        try {
            Method method = enumValueClass.getMethod(getEnumTypeMethod, new Class[0]);
            enumType = (EnumType)method.invoke(null, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error trying to get EnumType for " + enumValueClass.getName(), e);
        }
    }

    /**
     * Overridden Method.
     * @see org.faktorips.codegen.DatatypeHelper#getDatatype()
     */
    public Datatype getDatatype() {
        return enumType;
    }
    
	/**
	 * Overridden Method.
	 * 
	 * @see org.faktorips.codegen.DatatypeHelper#setDatatype(org.faktorips.datatype.Datatype)
	 */
	public void setDatatype(Datatype datatype) {
		this.enumType = (EnumType)datatype;
	}

    /**
     * Overridden Method.
     * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
     */
    public JavaCodeFragment newInstance(String value) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(enumValueClass);
        fragment.append('.');
        fragment.append(valueOfMethodname);
        fragment.append('(');
        fragment.appendQuoted(value);
        fragment.append(')');
        return fragment;
    }

	/* (non-Javadoc)
	 * @see org.faktorips.codegen.DatatypeHelper#newInstanceFromExpression(java.lang.String)
	 */
	public JavaCodeFragment newInstanceFromExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
        	return nullExpression();
        }		
        JavaCodeFragment fragment = new JavaCodeFragment();
		fragment.appendClassName(StringUtils.class);
		fragment.append(".isEmpty(");		
		fragment.append(expression);
		fragment.append(") ? ");
		fragment.append(nullExpression());
		fragment.append(" : ");        
        fragment.appendClassName(enumValueClass);
        fragment.append('.');
        fragment.append(valueOfMethodname);
        fragment.append('(');
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

	/* (non-Javadoc)
	 * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
	 */
	public JavaCodeFragment nullExpression() {
		return newInstance("");
	}
	

    /* (non-Javadoc)
     * @see org.faktorips.codegen.DatatypeHelper#getRangeJavaClassName()
     */
    public String getRangeJavaClassName() {
        return null;
    }	

}
