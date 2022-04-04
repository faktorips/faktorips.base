/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.DatatypeComparator;
import org.faktorips.util.StringUtil;
import org.faktorips.values.NullObjectSupport;

/**
 * The generic value datatype is used to make a Java class (representing a value) available as value
 * datatype. Instead of writing a special class implementing the datatype interface, an instance of
 * this class (or more precisely of one of it's subclasses) is used.
 * <p>
 * This allows to defined a datatype based on a Java class by declaring it, instead of writing code.
 * 
 * @author Jan Ortmann
 */
public abstract class GenericValueDatatype implements ValueDatatype {

    public static final String MSGCODE_PREFIX = "GENERIC DATATYPE-"; //$NON-NLS-1$
    public static final String MSGCODE_JAVACLASS_NOT_FOUND = MSGCODE_PREFIX + "Java class not found"; //$NON-NLS-1$
    public static final String MSGCODE_GETVALUE_METHOD_NOT_FOUND = MSGCODE_PREFIX + "getValue() Method not found"; //$NON-NLS-1$
    public static final String MSGCODE_ISPARSABLE_METHOD_NOT_FOUND = MSGCODE_PREFIX + "isParsable() Method not found"; //$NON-NLS-1$
    public static final String MSGCODE_TOSTRING_METHOD_NOT_FOUND = MSGCODE_PREFIX + "toString() Method not found"; //$NON-NLS-1$
    public static final String MSGCODE_SPECIALCASE_NULL_NOT_FOUND = MSGCODE_PREFIX + "Special case null not found"; //$NON-NLS-1$
    public static final String MSGCODE_SPECIALCASE_NULL_IS_NOT_NULL = MSGCODE_PREFIX + "Special case null is not null"; //$NON-NLS-1$

    private Method valueOfMethod;
    private Method isParsableMethod;
    private Method toStringMethod;

    private String qualifiedName;
    private String valueOfMethodName = "valueOf"; //$NON-NLS-1$
    private String isParsableMethodName = "isParsable"; //$NON-NLS-1$
    private String toStringMethodName = "toString"; //$NON-NLS-1$

    private boolean nullObjectDefined = false;
    private String nullObjectId = null;

    @Override
    public String getDefaultValue() {
        return null;
    }

    /**
     * Returns the class represented by this datatype. If the class can't be found,
     * <code>null</code> is returned. In this case the <code>validate()</code> method returns a
     * message list containing an error message.
     */
    public abstract Class<?> getAdaptedClass();

    /**
     * Returns the name of the class represented by this datatype. This method must return a value,
     * even if the class itself can't be found.
     */
    public abstract String getAdaptedClassName();

    @Override
    public MessageList checkReadyToUse() {
        MessageList list = new MessageList();
        if (getAdaptedClass() == null) {
            String text = "The Java class represented by the datatype can't be found. (Classname: " //$NON-NLS-1$
                    + getAdaptedClassName() + "). " //$NON-NLS-1$
                    + "Either the class is not on the classpath or the resource it is stored in is out of sync. See error log for more details."; //$NON-NLS-1$
            list.add(Message.newError(MSGCODE_JAVACLASS_NOT_FOUND, text));
            return list;
        }
        checkParsableMethodName(list);
        checkStringMethodName(list);
        checkNullObjectDefined(list);
        try {
            getValueOfMethod();
            // CSOFF: Illegal Catch
        } catch (RuntimeException e) {
            // CSON: Illegal Catch
            String text = "The Java class hasn't got a method " + getValueOfMethodName() + "(String)"; //$NON-NLS-1$ //$NON-NLS-2$
            list.add(Message.newError(MSGCODE_GETVALUE_METHOD_NOT_FOUND, text));
            return list;
        }
        DatatypeValidation.checkMethod(list, valueOfMethod, true, getAdaptedClass());
        return list;
    }

    private void checkParsableMethodName(MessageList list) {
        if (isParsableMethodName != null) {
            try {
                getIsParsableMethod();
                // CSOFF: Illegal Catch
            } catch (RuntimeException e) {
                // CSON: Illegal Catch
                String text = "The Java class hasn't got a method " + getIsParsableMethodName() + "(String)"; //$NON-NLS-1$ //$NON-NLS-2$
                list.add(Message.newError(MSGCODE_ISPARSABLE_METHOD_NOT_FOUND, text));
            }
            DatatypeValidation.checkMethod(list, isParsableMethod, false, Boolean.class);
        }
    }

    private void checkStringMethodName(MessageList list) {
        if (toStringMethodName != null) {
            try {
                getToStringMethod();
                // CSOFF: Illegal Catch
            } catch (RuntimeException e) {
                // CSON: Illegal Catch
                String text = "The Java class hasn't got a method " + getToStringMethodName() + "(Object)"; //$NON-NLS-1$ //$NON-NLS-2$
                list.add(Message.newError(MSGCODE_TOSTRING_METHOD_NOT_FOUND, text));
            }
        }
    }

    private void checkNullObjectDefined(MessageList list) {
        if (nullObjectDefined) {
            try {
                Object value = getValue(nullObjectId);
                if (value instanceof NullObjectSupport) {
                    if (!((NullObjectSupport)value).isNull()) {
                        String text = "The string " + nullObjectId + " does not represent the special null value."; //$NON-NLS-1$ //$NON-NLS-2$
                        list.add(Message.newError(MSGCODE_SPECIALCASE_NULL_IS_NOT_NULL, text));
                    }
                }
                // CSOFF: Illegal Catch
            } catch (RuntimeException e) {
                // CSON: Illegal Catch
                String text = "The null value string " + nullObjectId + " is not a value defined by the datatype."; //$NON-NLS-1$ //$NON-NLS-2$
                list.add(Message.newError(MSGCODE_SPECIALCASE_NULL_NOT_FOUND, text));
            }
        }
    }

    public String getIsParsableMethodName() {
        return isParsableMethodName;
    }

    public void setIsParsableMethodName(String isParsableMethodName) {
        this.isParsableMethodName = isParsableMethodName;
        isParsableMethod = null;
    }

    /**
     * Returns <code>true</code> if the datatype has a special instance representing null. (This is
     * known as the NullObject pattern).
     */
    @Override
    public boolean hasNullObject() {
        return nullObjectDefined;
    }

    /**
     * Sets if this datatype is an application of the NullObject pattern.
     */
    public void setNullObjectDefined(boolean flag) {
        nullObjectDefined = flag;
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
        nullObjectId = specialNullValueId;
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

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    @Override
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        getIsParsableMethod();
        if (isParsableMethod != null) {
            try {
                Object o = isParsableMethod.invoke(null, value);
                return ((Boolean)o).booleanValue();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error executing method " + isParsableMethod); //$NON-NLS-1$
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error executing method " + isParsableMethod); //$NON-NLS-1$
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Error executing method " + isParsableMethod); //$NON-NLS-1$
            }
        }
        try {
            getValueOfMethod().invoke(null, value);
            // getValue() has executed without exception, the value can be parsed.
            return true;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error executing method " + valueOfMethod, e); //$NON-NLS-1$
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error executing method " + valueOfMethod, e); //$NON-NLS-1$
        } catch (InvocationTargetException e) {
            // getValue() has thrown an exception, the value can't be parsed.
            return false;
        }
    }

    protected Method getIsParsableMethod() {
        if (isParsableMethodName != null && isParsableMethod == null) {
            // @formatter:off
            isParsableMethod = findMethodWithCharSequenceSubclassParameter(isParsableMethodName)
                    .orElseThrow(() -> new RuntimeException("Can't get isParsable-method, Class: " + getAdaptedClass() //$NON-NLS-1$
                            + ", Methodname: " + isParsableMethodName)); //$NON-NLS-1$
            // @formatter:on
        }
        return isParsableMethod;
    }

    @Override
    public Object getValue(String value) {
        if (!nullObjectDefined && value == null) {
            return null;
        }
        try {
            return getValueOfMethod().invoke(null, value);
            // CSOFF: Illegal Catch
        } catch (Exception e) {
            // CSON: Illegal Catch
            throw new RuntimeException("Error invoking method to get the value " + valueOfMethod, e); //$NON-NLS-1$
        }
    }

    protected Method getValueOfMethod() {
        if (valueOfMethodName != null && valueOfMethod == null) {
            // @formatter:off
            valueOfMethod = findMethodWithCharSequenceSubclassParameter(valueOfMethodName)
                    .orElseThrow(() -> new RuntimeException("Can't get valueOf-method, Class: " + getAdaptedClass() //$NON-NLS-1$
                            + ", Methodname: " + valueOfMethodName)); //$NON-NLS-1$
            // @formatter:on
        }
        return valueOfMethod;
    }

    private Optional<Method> findMethodWithCharSequenceSubclassParameter(String methodname) {
        return Arrays.stream(getAdaptedClass().getMethods()).filter(m -> methodname.equals(m.getName()))
                .filter(m -> m.getParameterCount() == 1)
                .filter(m -> CharSequence.class.isAssignableFrom(m.getParameterTypes()[0])).findFirst();
    }

    public String valueToString(Object value) {
        getToStringMethod();
        if (toStringMethod == null) {
            if (value == null) {
                return null;
            }
            return value.toString();
        }
        try {
            return (String)toStringMethod.invoke(value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error executing method " + toStringMethod); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error executing method " + toStringMethod); //$NON-NLS-1$
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error executing method " + toStringMethod); //$NON-NLS-1$
        }
    }

    protected Method getToStringMethod() {
        if (toStringMethod == null && toStringMethodName != null) {
            try {
                toStringMethod = getAdaptedClass().getMethod(toStringMethodName);
                if (toStringMethod == null) {
                    throw new NullPointerException();
                }
                // CSOFF: Illegal Catch
            } catch (Exception e) {
                // CSON: Illegal Catch
                throw new RuntimeException("Can't get method toString(String), Class: " + getAdaptedClass() //$NON-NLS-1$
                        + ", Methodname: " + toStringMethodName); //$NON-NLS-1$
            }
        }
        return toStringMethod;
    }

    @Override
    public boolean isNull(String value) {
        if (value == null) {
            return true;
        }
        if (!nullObjectDefined) {
            return false;
        }
        return value.equals(getValue(nullObjectId));
    }

    @Override
    public boolean isEnum() {
        return this instanceof EnumDatatype;
    }

    @Override
    public String getName() {
        return StringUtil.unqualifiedName(qualifiedName);
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Datatype)) {
            return false;
        }
        return getQualifiedName().equals(((Datatype)o).getQualifiedName());
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    /**
     * Returns the java class that is represented by this generic datatype
     */
    public String getJavaClassName() {
        return getAdaptedClass().getName();
    }

    @Override
    public int compareTo(Datatype o) {
        return DatatypeComparator.doCompare(this, o);
    }

    @Override
    public String toString() {
        return qualifiedName;
    }

    protected void clearCache() {
        isParsableMethod = null;
        valueOfMethod = null;
        toStringMethod = null;
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {

        if (valueA == null && valueB == null) {
            return true;
        }
        if (valueA == null && valueB != null || valueA != null && valueB == null) {
            return false;
        }
        return getValue(valueA).equals(getValue(valueB));
    }

    @Override
    public int compare(String valueA, String valueB) {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("The class " + getAdaptedClassName() + " does not implement " //$NON-NLS-1$ //$NON-NLS-2$
                    + Comparable.class.getName());
        }

        @SuppressWarnings("unchecked")
        // GenericValueDatatype does not support generics.
        Comparable<Object> value = (Comparable<Object>)getValue(valueA);
        Object value2 = getValue(valueB);
        return value.compareTo(value2);
    }

    @Override
    public boolean supportsCompare() {
        return Comparable.class.isAssignableFrom(getAdaptedClass());
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

}
