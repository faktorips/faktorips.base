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

import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * Adapter that allows to use any Java class (that is not a value) as datatype. Typical use for such
 * datatypes are classes like MessageList or CalculationResult that are used as collection
 * parameters in methods.
 * 
 * @author Jan Ortmann
 */
public class JavaClass2DatatypeAdaptor extends AbstractDatatype {

    private String qualifiedName;
    private String javaClassName;

    public JavaClass2DatatypeAdaptor(String javaClassName) {
        this(javaClassName, javaClassName);
    }

    public JavaClass2DatatypeAdaptor(String name, String javaClassName) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(javaClassName);
        qualifiedName = name;
        this.javaClassName = javaClassName;
    }

    public JavaClass2DatatypeAdaptor(String name, Class<?> clazz) {
        this(name, clazz.getName());
    }

    public JavaClass2DatatypeAdaptor(Class<?> clazz) {
        this(clazz.getName());
    }

    public String getJavaClassName() {
        return javaClassName;
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
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return false;
    }

}
