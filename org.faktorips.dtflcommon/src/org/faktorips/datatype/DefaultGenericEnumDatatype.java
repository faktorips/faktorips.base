package org.faktorips.datatype;

public class DefaultGenericEnumDatatype extends GenericEnumDatatype {

    private Class adaptedClass;
    
    public DefaultGenericEnumDatatype(Class adaptedClass) {
        super();
        this.adaptedClass = adaptedClass;
    }

    public Class getAdaptedClass() {
        return adaptedClass;
    }

    public String getAdaptedClassName() {
        return adaptedClass.getName();
    }

}
