/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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


/**
 * A datatype that represents a Java class representing a value, for example java.lang.String.
 * 
 * @author Jan Ortmann
 */
public abstract class ValueClassDatatype extends ValueClassNameDatatype {

    private Class<?> clazz;

    public ValueClassDatatype(Class<?> clazz) {
        super(clazz.getName());
        this.clazz = clazz;
    }

    public ValueClassDatatype(Class<?> clazz, String name) {
        super(clazz.getName(), name);
        this.clazz = clazz;
    }

    public Class<?> getJavaClass() {
        return clazz;
    }

}
