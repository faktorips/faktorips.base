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

/**
 * Calculates the difference between two date values based on the ISMA 30/360 calendar.
 * 
 * 
 * 
 * @author dirmeier
 */
public class Days360 extends AbstractFlFunction {

    public Days360(String name, String description) {
        super(name, description, FunctionSignatures.DAYS360);
    }

    /**
     * ((d2.getYear() - d1.getYear()) * 360 + (d2.getMonthOfYear() - d1.getMonthOfYear()) * 30 +
     * (Math.min(d2.getDayOfMonth(), 30) - Math.min(d1.getDayOfMonth(), 30)))
     */
    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        Datatype datatype = getArgTypes()[0];
        DatatypeHelper datatypeHelper = getDatatypeHelper(datatype);
        if (datatypeHelper instanceof ILocalDateHelper) {
            ILocalDateHelper localDateHelper = (ILocalDateHelper)datatypeHelper;
            JavaCodeFragment d1 = argResults[0].getCodeFragment();
            JavaCodeFragment d2 = argResults[1].getCodeFragment();
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.append("((").append(d2);
            appendGetField(fragment, localDateHelper.getDateFieldEnumClass(), localDateHelper.getYearField());
            fragment.append(" - ").append(d1);
            appendGetField(fragment, localDateHelper.getDateFieldEnumClass(), localDateHelper.getYearField());
            fragment.append(") * 360 + ").append("(").append(d2);
            appendGetField(fragment, localDateHelper.getDateFieldEnumClass(), localDateHelper.getMonthOfYearField());
            fragment.append(" - ").append(d1);
            appendGetField(fragment, localDateHelper.getDateFieldEnumClass(), localDateHelper.getMonthOfYearField());
            fragment.append(") * 30 + ").append("(Math.min(").append(d2);
            appendGetField(fragment, localDateHelper.getDateFieldEnumClass(), localDateHelper.getDayOfMonthField());
            fragment.append(", 30)").append(" - ").append("Math.min(").append(d1);
            appendGetField(fragment, localDateHelper.getDateFieldEnumClass(), localDateHelper.getDayOfMonthField());
            fragment.append(", 30)))");
            return new CompilationResultImpl(fragment, getType());
        } else {
            String code = ExprCompiler.PREFIX + AbstractPeriodFunction.NO_PERIOD_SUPPORT;
            String text = Messages.INSTANCE.getString(code, datatype);
            Message msg = Message.newError(code, text);
            return new CompilationResultImpl(msg);
        }
    }

    private void appendGetField(JavaCodeFragment fragment, String dateFieldEnumClass, String field) {
        fragment.append(".get(").appendClassName(dateFieldEnumClass).append('.').append(field).append(')');
    }
}
