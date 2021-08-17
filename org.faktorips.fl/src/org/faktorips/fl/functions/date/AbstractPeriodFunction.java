/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions.date;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.ILocalDateHelper;
import org.faktorips.codegen.dthelpers.ILocalDateHelper.Period;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.fl.functions.AbstractFlFunction;
import org.faktorips.fl.functions.Messages;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractPeriodFunction extends AbstractFlFunction {

    static final String NO_PERIOD_SUPPORT = "NO_PERIOD_SUPPORT";
    private Period period;

    public AbstractPeriodFunction(String name, String description, FunctionSignatures functionSignature,
            Period period) {
        super(name, description, functionSignature);
        this.period = period;
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        Datatype datatype = getArgTypes()[0];
        DatatypeHelper datatypeHelper = getDatatypeHelper(datatype);
        if (datatypeHelper instanceof ILocalDateHelper) {
            JavaCodeFragment fragment = ((ILocalDateHelper)datatypeHelper).getPeriodCode(
                    argResults[0].getCodeFragment(), argResults[1].getCodeFragment(), period);
            return new CompilationResultImpl(fragment, getType());
        } else {
            String code = ExprCompiler.PREFIX + NO_PERIOD_SUPPORT;
            String text = Messages.INSTANCE.getString(code, datatype);
            Message msg = Message.newError(code, text);
            return new CompilationResultImpl(msg);
        }
    }

}