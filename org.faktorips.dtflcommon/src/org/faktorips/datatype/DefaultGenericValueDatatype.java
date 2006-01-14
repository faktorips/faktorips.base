package org.faktorips.datatype;

import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * A generic value datatype that makes an <strong>existing</code> Java class  
 * (is is already loaded by the classloader) available as datatype.
 * 
 * @author Jan Ortmann
 */
public class DefaultGenericValueDatatype extends GenericValueDatatype {

    private Class adaptedClass;
    
    
    public DefaultGenericValueDatatype() {
        super();
    }

    public DefaultGenericValueDatatype(Class adaptedClass) {
        ArgumentCheck.notNull(adaptedClass);
        this.adaptedClass = adaptedClass;
        setQualifiedName(StringUtil.unqualifiedName(adaptedClass.getName()));
    }

    /**
     * Overridden.
     */
    public Class getAdaptedClass() {
        return adaptedClass;
    }

    /**
     * Overridden.
     */
    public String getAdaptedClassName() {
        return adaptedClass.getName();
    }

}
