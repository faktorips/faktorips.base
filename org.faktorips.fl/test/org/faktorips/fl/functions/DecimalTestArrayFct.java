package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ArrayDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.values.Decimal;

/**
 * A function for testing purposes that returns a constant decimal array. 
 *  
 * @author Jan Ortmann
 */
public class DecimalTestArrayFct extends AbstractFlFunction {

    public final static String name = "DECIMALTESTARRAY";
    
    // the values that will be returned by the function.
    private Decimal[] values;
    
    public DecimalTestArrayFct() {
        super(name, "", new ArrayDatatype(Datatype.DECIMAL, 1), new Datatype[]{});
    }

    public void setValues(Decimal[] values) {
        this.values = values;
    }
    
    /**
     * Overridden Method.
     *
     * @see org.faktorips.fl.FlFunction#compile(org.faktorips.fl.CompilationResult[])
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (values==null) {
            fragment.append("null");
            return new CompilationResultImpl(fragment, getType());
        }
        fragment.append("new ");
        fragment.appendClassName(Datatype.DECIMAL.getJavaClassName());
        fragment.append("[] {");
        for (int i=0; i<values.length; i++) {
            if (i>0) {
                fragment.append(", ");
            }
            fragment.append("Decimal.valueOf(");
            fragment.appendQuoted(values[i].toString());
            fragment.append(')');
        }
        fragment.append("}");
        return new CompilationResultImpl(fragment, getType());
    }

}
