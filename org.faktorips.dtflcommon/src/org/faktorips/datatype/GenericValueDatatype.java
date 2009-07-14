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

package org.faktorips.datatype;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.NullObjectSupport;

/**
 * The generic value datatype is used to make a Java class (representing a value)
 * available as value datatype. Instead of writing a special class implementing the datatype 
 * interface, an instance of this class (or more precisely of one of it's subclasses) is used.
 * <p>
 * This allows to defined a datatype based on a Java class by declaring it, instead of writing code.
 * 
 * @author Jan Ortmann
 */
public abstract class GenericValueDatatype implements ValueDatatype {
    
    private final static String MSGCODE_PREFIX = "GENERIC DATATYPE-";
    public final static String MSGCODE_JAVACLASS_NOT_FOUND = MSGCODE_PREFIX + "Java class not found";
    public final static String MSGCODE_GETVALUE_METHOD_NOT_FOUND = MSGCODE_PREFIX + "getValue() Method not found";
    public final static String MSGCODE_ISPARSABLE_METHOD_NOT_FOUND = MSGCODE_PREFIX + "isParsable() Method not found";
    public final static String MSGCODE_TOSTRING_METHOD_NOT_FOUND = MSGCODE_PREFIX + "toString() Method not found";
    public final static String MSGCODE_SPECIALCASE_NULL_NOT_FOUND = MSGCODE_PREFIX + "Special case null not found";
    public final static String MSGCODE_SPECIALCASE_NULL_IS_NOT_NULL = MSGCODE_PREFIX + "Special case null is not null";
    

    private String qualifiedName;
    private String valueOfMethodName = "valueOf";
    private String isParsableMethodName = "isParsable";
    private String toStringMethodName = "toString";
    
    private boolean nullObjectDefined = false;
    private String nullObjectId = null;
    
    protected Method valueOfMethod;
    protected Method isParsableMethod;
    protected Method toStringMethod;
    
    public GenericValueDatatype() {
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return null;
    }

    /**
     * Returns the class represented by this datatype. If the class can't be found, 
     * <code>null</code> is returned. In this case the <code>validate()</code> method
     * returns a message list containing an error message.
     */
    public abstract Class getAdaptedClass();
    
    /**
     * Returns the name of the class represented by this datatype.
     * This method must return a value, even if the class itself can't be found.
     */
    public abstract String getAdaptedClassName();
    
    /**
     * {@inheritDoc}
     */
    public MessageList checkReadyToUse() {
        MessageList list = new MessageList();
        if (getAdaptedClass()==null) {
            String text = "The Java class represented by the datatype can't be found. (Classname: " + getAdaptedClassName() + "). "
                    + "Either the class is not on the classpath or the resource it is stored in is out of sync. See error log for more details.";
            list.add(Message.newError(MSGCODE_JAVACLASS_NOT_FOUND, text));
            return list;
        }
        if (isParsableMethodName!=null) {
            try {
                getIsParsableMethod();
            } catch (RuntimeException e) {
                String text = "The Java class hasn't got a method " + getIsParsableMethodName() + "(String)";
                list.add(Message.newError(MSGCODE_ISPARSABLE_METHOD_NOT_FOUND, text));
            }
        }
        if (toStringMethodName!=null) {
            try {
                getToStringMethod();
            } catch (RuntimeException e) {
                String text = "The Java class hasn't got a method " + getToStringMethodName() + "(Object)";
                list.add(Message.newError(MSGCODE_TOSTRING_METHOD_NOT_FOUND, text));
            }
        }
        try {
            getValueOfMethod();
        } catch (RuntimeException e) {
            String text = "The Java class hasn't got a method " + getValueOfMethodName() + "(String)";
            list.add(Message.newError(MSGCODE_GETVALUE_METHOD_NOT_FOUND, text));
            return list;
        }
        if (nullObjectDefined) {
            try {
                Object value = getValue(nullObjectId);
                if (value instanceof NullObjectSupport) {
                    if (!((NullObjectSupport)value).isNull()) {
                        String text = "The string " + nullObjectId + " does not represent the special null value.";
                        list.add(Message.newError(MSGCODE_SPECIALCASE_NULL_IS_NOT_NULL, text));
                    }
                }
            } catch (RuntimeException e) {
                String text = "The null value string " + nullObjectId + " is not a value defined by the datatype.";
                list.add(Message.newError(MSGCODE_SPECIALCASE_NULL_NOT_FOUND, text));
            }
        }
        return list;
    }
    
    public String getIsParsableMethodName() {
        return isParsableMethodName;
    }

    public void setIsParsableMethodName(String isParsableMethodName) {
        this.isParsableMethodName = isParsableMethodName;
        isParsableMethod = null;
    }

    /**
     * Returns <code>true</code> if the datatype has a special instance representing
     * null. (This is known as the NullObject pattern). 
     */
    public boolean hasNullObject() {
        return nullObjectDefined;
    }

    /**
     * Sets if this datatype is an application of the NullObject pattern.
     */
    public void setNullObjectDefined(boolean flag) {
        this.nullObjectDefined = flag;
    }
    
    /**
     * Returns the String identification of the special NullObject.
     */
    public String getNullObjectId() {
        return nullObjectId;
    }

    /**
     * Sets the String identification of the special NullObject.
     */
    public void setNullObjectId(String specialNullValueId) {
        this.nullObjectId = specialNullValueId;
    }

    public String getValueOfMethodName() {
        return valueOfMethodName;
    }

    public void setValueOfMethodName(String valueOfMethodName) {
        this.valueOfMethodName = valueOfMethodName;
        valueOfMethod = null;
    }

    public String getToStringMethodName() {
        return toStringMethodName;
    }

    public void setToStringMethodName(String toStringMethodName) {
        this.toStringMethodName = toStringMethodName;
        toStringMethod = null;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype getWrapperType() {
        return null;
    }

    public boolean isParsable(String value) {
        if (value==null) {
            return true;
        }
        getIsParsableMethod();
        if (isParsableMethod!=null) {
            try {
                Object o = isParsableMethod.invoke(null, new Object[]{value});
                return ((Boolean)o).booleanValue();
            } catch (Exception e) {
                throw new RuntimeException("Error executing method " + isParsableMethod);
            }
        }
        try {
            getValueOfMethod().invoke(null, new Object[]{value});
            return true; // getValue() has executed without exception, the value can be parsed.
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error executing method " + valueOfMethod, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error executing method " + valueOfMethod, e);
        } catch (InvocationTargetException e) {
            return false; // getValue() has thrown an exception, the value can't be parsed.
        }
    }
    
    protected Method getIsParsableMethod() {
        if (isParsableMethod==null && isParsableMethodName!=null) {
            try {
                isParsableMethod = getAdaptedClass().getMethod(isParsableMethodName, new Class[]{String.class});
                if (isParsableMethod==null) {
                    throw new NullPointerException();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Can't get the method isParsable(String), Class: " + getAdaptedClassName() + ", Methodname: " + isParsableMethodName, t);
            }
        }
        return isParsableMethod;
    }

    public Object getValue(String value) {
        if (!nullObjectDefined && value==null) {
            return null;
        }
        try {
            return getValueOfMethod().invoke(null, new Object[]{value});
        } catch (Exception e) {
            throw new RuntimeException("Error invoking method to get the value " + valueOfMethod, e);
        }
    }
    
    protected Method getValueOfMethod() {
        if (valueOfMethodName!=null && valueOfMethod==null) {
            try {
                valueOfMethod = getAdaptedClass().getMethod(valueOfMethodName, new Class[]{String.class});
                if (valueOfMethod==null) {
                    throw new NullPointerException();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Can't get valueOfMethod(String), Class: " + getAdaptedClass() + ", Methodname: " + valueOfMethodName, t);
            }
        }
        return valueOfMethod;
    }

    public String valueToString(Object value) {
        getToStringMethod();
        if (toStringMethod==null) {
            if (value==null) {
                return null;
            }
            return value.toString();
        }
        try {
            return (String)toStringMethod.invoke(value, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error executing method " + toStringMethod);
        }
    }

    protected Method getToStringMethod() {
        if (toStringMethod==null && toStringMethodName!=null) {
            try {
                toStringMethod = getAdaptedClass().getMethod(toStringMethodName, new Class[0]);
                if (toStringMethod==null) {
                    throw new NullPointerException();
                }
            } catch (Throwable t) {
                throw new RuntimeException("Can't get method toString(String), Class: " + getAdaptedClass() + ", Methodname: " + toStringMethodName, t);
            }
        }
        return toStringMethod;
    }

    public boolean isNull(String value) {
        if (value==null) {
            return true;
        }
        if (!nullObjectDefined) {
            return false;
        }
        return value.equals(getValue(nullObjectId));
    }

    public String getName() {
        return StringUtil.unqualifiedName(qualifiedName);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this==o) {
            return true;
        }
        if (!(o instanceof Datatype)) {
            return false;
        }
        return getQualifiedName().equals(((Datatype)o).getQualifiedName());
    }
    
    public boolean isVoid() {
        return false;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isValueDatatype() {
        return true;
    }

    public String getJavaClassName() {
        return getAdaptedClass().getName();
    }
    
    // TODO pk: this cannot be right
    public int compareTo(Object o) {
        return 0;
    }
    
    public String toString() {
        return qualifiedName;
    }
    
    protected void clearCache() {
        isParsableMethod = null;
        valueOfMethod = null;
        toStringMethod = null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean areValuesEqual(String valueA, String valueB) {
        
        if(valueA == null && valueB == null){
            return true;
        }
        if(valueA == null && valueB != null ||
           valueA != null && valueB == null){
            return false;
        }
        return getValue(valueA).equals(getValue(valueB));
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String valueA, String valueB) throws UnsupportedOperationException {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("The class " + getAdaptedClassName() + " does not implement " + Comparable.class.getName());
        }

        return ((Comparable)getValue(valueA)).compareTo(valueB);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsCompare() {
        return Comparable.class.isAssignableFrom(this.getAdaptedClass());
    }

    /**
     * {@inheritDoc}
     */
	public boolean isImmutable() {
		return true;
	}

    /**
     * {@inheritDoc}
     */
	public boolean isMutable() {
		return false;
	}

    
}
