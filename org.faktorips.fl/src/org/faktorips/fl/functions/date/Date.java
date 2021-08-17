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
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.fl.functions.AbstractFlFunction;
import org.faktorips.fl.functions.Messages;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

public class Date extends AbstractFlFunction {

    public Date(String name, String description) {
        super(name, description, FunctionSignatures.DATE);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 3);
        JavaCodeFragment year = argResults[0].getCodeFragment();
        JavaCodeFragment month = argResults[1].getCodeFragment();
        JavaCodeFragment day = argResults[2].getCodeFragment();
        Datatype datatype = getType();
        DatatypeHelper datatypeHelper = getDatatypeHelper(datatype);
        if (datatypeHelper instanceof ILocalDateHelper) {
            JavaCodeFragment fragment = ((ILocalDateHelper)datatypeHelper).getDateInitialization(year, month, day);
            return new CompilationResultImpl(fragment, datatype);
        } else {
            String code = ExprCompiler.PREFIX + AbstractPeriodFunction.NO_PERIOD_SUPPORT;
            String text = Messages.INSTANCE.getString(code, datatype);
            Message msg = Message.newError(code, text);
            return new CompilationResultImpl(msg);
        }
    }

}
