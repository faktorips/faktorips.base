package org.faktorips.codegen;

import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;

/**
 *
 */
public class DatatypeConverter implements ConversionMatrix {

    /**
     * 
     */
    public DatatypeConverter() {
        super();
        // TODO Auto-generated constructor stub
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.ConversionMatrix#canConvert(org.faktorips.datatype.Datatype, org.faktorips.datatype.Datatype)
     */
    public boolean canConvert(Datatype from, Datatype to) {
        // TODO Auto-generated method stub
        return false;
    }

}
