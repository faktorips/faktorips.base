/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype;

import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * Adapter that allows to use any Java class (that is not a value) as datatype. Typical use for such
 * datatypes are classes like MessageList or CalculationResult that are used as collection
 * parameters in methods.
 * 
 * @author Jan Ortmann
 */
public class JavaClass2DatatypeAdaptor<T> extends AbstractDatatype {

    private String qualifiedName;
    private String javaClassName;

    public JavaClass2DatatypeAdaptor(String javaClassName) {
        this(javaClassName, javaClassName);
    }

    public JavaClass2DatatypeAdaptor(String name, String javaClassName) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(javaClassName);
        this.qualifiedName = name;
        this.javaClassName = javaClassName;
    }

    public JavaClass2DatatypeAdaptor(String name, Class<T> clazz) {
        this(name, clazz.getName());
    }

    public JavaClass2DatatypeAdaptor(Class<T> clazz) {
        this(clazz.getName());
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public String getName() {
        return StringUtil.unqualifiedName(qualifiedName);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isValueDatatype() {
        return false;
    }

}
