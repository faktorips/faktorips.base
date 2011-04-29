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

package org.faktorips.devtools.core.internal.model;

public class ValueDatatypeDefinition {

    private String className;
    private String qualifiedName;
    private String valueOfMethodName = "valueOf"; //$NON-NLS-1$
    private String isParsableMethodName = null;
    private String valueToStringMethodName = null;
    private String nullValueId = null;

    public ValueDatatypeDefinition() {
        super();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getIsParsableMethodName() {
        return isParsableMethodName;
    }

    public void setIsParsableMethodName(String isParsableMethodName) {
        this.isParsableMethodName = isParsableMethodName;
    }

    public String getNullValueId() {
        return nullValueId;
    }

    public void setNullValueId(String nullValueId) {
        this.nullValueId = nullValueId;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getValueOfMethodName() {
        return valueOfMethodName;
    }

    public void setValueOfMethodName(String valueOfMethodName) {
        this.valueOfMethodName = valueOfMethodName;
    }

    public String getValueToStringMethodName() {
        return valueToStringMethodName;
    }

    public void setValueToStringMethodName(String valueToStringMethodName) {
        this.valueToStringMethodName = valueToStringMethodName;
    }

}
