package org.faktorips.codegen;

import org.faktorips.codegen.dthelpers.PrimitiveBooleanHelper;
import org.faktorips.codegen.dthelpers.PrimitiveIntegerHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;

/**
 * A collection of util methods related to sourcecode generation.
 */
public class CodeGenUtil {
    
    public final static JavaCodeFragment convertPrimitiveToWrapper (Datatype type, JavaCodeFragment expression) {
        if (type instanceof PrimitiveBooleanDatatype) {
            return new PrimitiveBooleanHelper((PrimitiveBooleanDatatype)type).toWrapper(expression); 
        }
        if (type instanceof PrimitiveIntegerDatatype) {
            return new PrimitiveIntegerHelper((PrimitiveIntegerDatatype)type).toWrapper(expression); 
        }
        throw new IllegalArgumentException("Can't convert dataype " + type);
    }

    private CodeGenUtil() {
    }

}
