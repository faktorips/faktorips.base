package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ArrayDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.Decimal;


/**
 * The function: Decimal sum(Decimal[])
 */
public class SumDecimal extends AbstractFlFunction {
    
    /**
     * Constructs a sum() function with the given name.
     * 
     * @param name The function name.
     * 
     * @throws IllegalArgumentException if name is <code>null</code>.
     */
    public SumDecimal(String name, String description) {
        super(name, description, Datatype.DECIMAL, new Datatype[] {new ArrayDatatype(Datatype.DECIMAL, 1)});
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.FlFunction#compile(org.faktorips.codegen.JavaCodeFragment[])
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.length(argResults, 1);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class);
        fragment.append(".sum(");
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(")");
        return new CompilationResultImpl(fragment, getType());
    }
    
}
