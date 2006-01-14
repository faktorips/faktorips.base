package org.faktorips.devtools.core.internal.model;

public class ValueDatatypeDefinition {

    private String className;
    private String qualifiedName;
    private String valueOfMethodName = "valueOf";
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
