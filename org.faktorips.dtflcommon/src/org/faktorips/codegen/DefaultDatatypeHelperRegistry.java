package org.faktorips.codegen;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.datatype.Datatype;


/**
 * A default DatatypeHelperRegistry. 
 */
public class DefaultDatatypeHelperRegistry implements DatatypeHelperRegistry {
    
    private Map helpers = new HashMap(20);
    
    /**
     * Returns a new empty registry.
     */
    public final static DatatypeHelperRegistry newEmptyRegistry() {
        return new DefaultDatatypeHelperRegistry();
    }

    /**
     * Overridden method.
     * @see org.faktorips.codegen.DatatypeHelperRegistry#getHelper(org.faktorips.datatype.Datatype)
     */
    public DatatypeHelper getHelper(Datatype datatype) {
        return (DatatypeHelper)helpers.get(datatype);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.DatatypeHelperRegistry#register(org.faktorips.codegen.DatatypeHelper)
     */
    public void register(DatatypeHelper helper) {
        helpers.put(helper.getDatatype(), helper);
    }

}
