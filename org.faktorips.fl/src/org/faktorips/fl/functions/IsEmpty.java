/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;

/**
 * 
 * @author Jan Ortmann
 */
public class IsEmpty extends AbstractFlFunction {

    public final static String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "ISEMPTY"; //$NON-NLS-1$

    public IsEmpty(String name, String description) {
        super(name, description, FunctionSignatures.IsEmpty);
    }

    /**
     * {@inheritDoc}
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        Datatype argType = argResults[0].getDatatype();
        if (argType.isPrimitive()) {
            // values of primitive types can never be null
            return new CompilationResultImpl("false", Datatype.PRIMITIVE_BOOLEAN);
        }
        JavaCodeFragment code = new JavaCodeFragment();
        if (argType instanceof ListOfTypeDatatype) {
            code.append(argResults[0].getCodeFragment());
            code.append(".isEmpty()");
        } else if (argType.hasNullObject()) {
            BaseDatatypeHelper<JavaCodeFragment> helper = getCompiler().getDatatypeHelper(argType);
            code.append(helper.nullExpression());
            code.append(".equals(");
            code.append(argResults[0].getCodeFragment());
            code.append(")");
        } else {
            code.append(argResults[0].getCodeFragment());
            code.append("==null");
        }
        return new CompilationResultImpl(code, Datatype.PRIMITIVE_BOOLEAN);
    }

}
