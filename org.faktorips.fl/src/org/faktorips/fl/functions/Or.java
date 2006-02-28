package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;


/**
 * A function that provides a boolean or-operation and has the following signature <i>boolean OR(boolean...)</i>.  
 */
public class Or extends AbstractVarArgFunction {
    
    public Or(String name, String description) {
        super(name, description, Datatype.PRIMITIVE_BOOLEAN, Datatype.PRIMITIVE_BOOLEAN);
    }
    
    protected void compileInternal(CompilationResult returnValue, CompilationResult[] convertedArgs, JavaCodeFragment fragment) {
        for (int i = 0; i < convertedArgs.length; i++) {
            fragment.append(convertedArgs[i].getCodeFragment());
            
            if(i < convertedArgs.length - 1){
                fragment.append("||");
            }
        }
    }

}
