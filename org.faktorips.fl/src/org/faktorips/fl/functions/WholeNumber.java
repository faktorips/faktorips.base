package org.faktorips.fl.functions;

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class WholeNumber extends AbstractFlFunction {

    /**
     */
    public WholeNumber(String name, String description) {
        super(name, description, Datatype.INTEGER, new Datatype[] {Datatype.DECIMAL});
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.FlFunction#compile(org.faktorips.codegen.JavaCodeFragment[])
     */
    public CompilationResult compile(CompilationResult[] argResults) {
        ArgumentCheck.length(argResults, 1);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.appendClassName(Integer.class);
        fragment.append('(');
        fragment.append(argResults[0].getCodeFragment());
        fragment.append(".setScale(0, ");
        fragment.appendClassName(BigDecimal.class);
        fragment.append(".ROUND_DOWN).intValue())");
        return new CompilationResultImpl(fragment, Datatype.INTEGER);
    }


}
