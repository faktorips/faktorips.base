package org.faktorips.codegen;

import org.faktorips.datatype.Datatype;

/**
 * A registry for DatatypeHelper.
 */
public interface DatatypeHelperRegistry {

    /**
     * Returns the helper for the indicated datatype.
     * Returns null if no helper is registered for the datatype.
     */
    public DatatypeHelper getHelper(Datatype datatype);
    
    /**
     * Registers the datatype helper.
     * @throws IllegalArgumentException if helper is null.
     */
    public void register(DatatypeHelper helper);
}
