/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.datatype;

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
