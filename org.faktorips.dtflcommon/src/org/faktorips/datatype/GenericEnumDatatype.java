package org.faktorips.datatype;

import java.lang.reflect.Method;

/**
 * Generic enum datatype. See the superclass for more Details.
 * 
 * @author Jan Ortmann
 */
public abstract class GenericEnumDatatype extends GenericValueDatatype implements EnumDatatype {

    private String getAllValuesMethodName = "getAllValues";
    protected Method getAllValuesMethod;
    
    public GenericEnumDatatype() {
        super();
    }
    
    public String getGetAllValuesMethodName() {
        return getAllValuesMethodName;
    }

    public void setGetAllValuesMethodName(String getAllValuesMethodName) {
        this.getAllValuesMethodName = getAllValuesMethodName;
        getAllValuesMethod = null;
    }

    public String[] getAllValueIds() {
        try {
            Object[] values = (Object[])getGetAllValuesMethod().invoke(null, new Object[0]);
            String[] ids = new String[values.length];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = this.valueToString(values[i]);
            }
            return ids;
        } catch (Exception e) {
            throw new RuntimeException("Error invoking method " + valueOfMethod);
        }
    }

    Method getGetAllValuesMethod() {
        if (getAllValuesMethod==null && getAllValuesMethodName!=null) {
            try {
                getAllValuesMethod = getAdaptedClass().getMethod(getAllValuesMethodName, new Class[0]);
                if (getAllValuesMethod==null) {
                    throw new NullPointerException();
                }
            } catch (Exception e) {
                throw new RuntimeException("Can't get method getAllValues(), Class: " + getAdaptedClass() + ", Methodname: " + getAllValuesMethodName);
            }
        }
        return getAllValuesMethod;
    }

    
}
