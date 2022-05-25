/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.AbstractPrimitiveDatatype;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.fl.operations.AddDecimalDecimal;
import org.faktorips.fl.operations.AddDecimalInt;
import org.faktorips.fl.operations.AddDecimalInteger;
import org.faktorips.fl.operations.AddIntDecimal;
import org.faktorips.fl.operations.AddIntInt;
import org.faktorips.fl.operations.AddIntegerDecimal;
import org.faktorips.fl.operations.AddMoneyMoney;
import org.faktorips.fl.operations.AddStringString;
import org.faktorips.fl.operations.CompareToComparableDatatype;
import org.faktorips.fl.operations.DivideDecimalDecimal;
import org.faktorips.fl.operations.DivideMoneyDecimal;
import org.faktorips.fl.operations.EqualsObjectDatatype;
import org.faktorips.fl.operations.EqualsPrimtiveType;
import org.faktorips.fl.operations.GreaterThanDecimalDecimal;
import org.faktorips.fl.operations.GreaterThanMoneyMoney;
import org.faktorips.fl.operations.GreaterThanOrEqualDecimalDecimal;
import org.faktorips.fl.operations.GreaterThanOrEqualMoneyMoney;
import org.faktorips.fl.operations.LessThanDecimalDecimal;
import org.faktorips.fl.operations.LessThanMoneyMoney;
import org.faktorips.fl.operations.LessThanOrEqualDecimalDecimal;
import org.faktorips.fl.operations.LessThanOrEqualMoneyMoney;
import org.faktorips.fl.operations.MinusDecimal;
import org.faktorips.fl.operations.MinusInteger;
import org.faktorips.fl.operations.MinusMoney;
import org.faktorips.fl.operations.MinusPrimitiveInt;
import org.faktorips.fl.operations.MultiplyDecimalDecimal;
import org.faktorips.fl.operations.MultiplyDecimalMoney;
import org.faktorips.fl.operations.MultiplyIntInt;
import org.faktorips.fl.operations.MultiplyIntegerMoney;
import org.faktorips.fl.operations.MultiplyMoneyDecimal;
import org.faktorips.fl.operations.NotEqualsObjectDatatype;
import org.faktorips.fl.operations.ParenthesisDecimal;
import org.faktorips.fl.operations.ParenthesisInt;
import org.faktorips.fl.operations.ParenthesisMoney;
import org.faktorips.fl.operations.ParenthesisString;
import org.faktorips.fl.operations.PlusDecimal;
import org.faktorips.fl.operations.PlusInteger;
import org.faktorips.fl.operations.PlusMoney;
import org.faktorips.fl.operations.PlusPrimitiveInt;
import org.faktorips.fl.operations.SubtractDecimalDecimal;
import org.faktorips.fl.operations.SubtractIntInt;
import org.faktorips.fl.operations.SubtractMoneyMoney;
import org.faktorips.runtime.Message;

/**
 * This {@link ExprCompiler} implementation generates {@link JavaCodeFragment Java source code}.
 * <p>
 * This class is not thread safe!
 */
public class JavaExprCompiler extends ExprCompiler<JavaCodeFragment> {

    /**
     * Creates a new compiler. Messages returned by the compiler are generated using the default
     * locale.
     */
    public JavaExprCompiler() {
        this(Locale.getDefault());
    }

    /**
     * Creates a new compiler.
     * 
     * @param locale The locale that is used to generate locale dependent messages.
     */
    public JavaExprCompiler(Locale locale) {
        super(locale, new DefaultIdentifierResolver(), ConversionCodeGenerator.getDefault(),
                new DefaultDatatypeHelperProvider());
    }

    @Override
    protected void registerDefaults() {

        // plus operation
        register(new PlusDecimal());
        register(new PlusPrimitiveInt());
        register(new PlusInteger());
        register(new PlusMoney());

        // minus operation
        register(new MinusDecimal());
        register(new MinusInteger());
        register(new MinusPrimitiveInt());
        register(new MinusMoney());

        // add operation
        register(new AddIntInt());
        register(new AddDecimalDecimal());
        register(new AddMoneyMoney());
        register(new AddDecimalInt());
        register(new AddIntDecimal());
        register(new AddDecimalInteger());
        register(new AddIntegerDecimal());
        register(new AddStringString());

        // subtract operation
        register(new SubtractIntInt());
        register(new SubtractDecimalDecimal());
        register(new SubtractMoneyMoney());

        // multiply operation
        register(new MultiplyIntInt());
        register(new MultiplyDecimalMoney());
        register(new MultiplyMoneyDecimal());
        register(new MultiplyIntegerMoney());
        register(new MultiplyDecimalDecimal());

        // divide operation
        register(new DivideDecimalDecimal());
        register(new DivideMoneyDecimal());

        // greater than operation
        register(new GreaterThanDecimalDecimal());
        register(new GreaterThanMoneyMoney());
        register(new CompareToComparableDatatype(BinaryOperation.GREATER_THAN, LocalDateDatatype.DATATYPE));

        // greater than or equal operation
        register(new GreaterThanOrEqualDecimalDecimal());
        register(new GreaterThanOrEqualMoneyMoney());
        register(new CompareToComparableDatatype(BinaryOperation.GREATER_THAN_OR_EQUAL, LocalDateDatatype.DATATYPE));

        // less than operation
        register(new LessThanDecimalDecimal());
        register(new LessThanMoneyMoney());
        register(new CompareToComparableDatatype(BinaryOperation.LESSER_THAN, LocalDateDatatype.DATATYPE));

        // less than or equal operation
        register(new LessThanOrEqualDecimalDecimal());
        register(new LessThanOrEqualMoneyMoney());
        register(new LessThanOrEqualMoneyMoney());
        register(new CompareToComparableDatatype(BinaryOperation.LESSER_THAN_OR_EQUAL, LocalDateDatatype.DATATYPE));

        // equals operation
        register(new EqualsPrimtiveType(Datatype.PRIMITIVE_INT));
        register(new EqualsPrimtiveType(Datatype.PRIMITIVE_BOOLEAN));
        register(new EqualsObjectDatatype(Datatype.DECIMAL));
        register(new EqualsObjectDatatype(Datatype.MONEY));
        register(new EqualsObjectDatatype(AnyDatatype.INSTANCE));

        // not equals operation
        register(new NotEqualsObjectDatatype(Datatype.DECIMAL));
        register(new NotEqualsObjectDatatype(Datatype.MONEY));
        register(new EqualsObjectDatatype(AnyDatatype.INSTANCE));

        // parenthesis operation
        register(new ParenthesisInt());
        register(new ParenthesisDecimal());
        register(new ParenthesisMoney());
        register(new ParenthesisString());
    }

    @Override
    protected JavaCodeFragment convertPrimitiveToWrapper(Datatype resultType, JavaCodeFragment codeFragment) {
        if (resultType instanceof AbstractPrimitiveDatatype) {
            AbstractPrimitiveDatatype primitiveDatatype = (AbstractPrimitiveDatatype)resultType;
            ValueDatatype wrapperType = primitiveDatatype.getWrapperType();
            return ConversionCodeGenerator.getDefault().getConversionCode(primitiveDatatype, wrapperType, codeFragment);
        } else {
            return codeFragment;
        }
    }

    @Override
    protected ParseTreeVisitor<JavaCodeFragment> newParseTreeVisitor() {
        return new JavaParseTreeVisitor(this);
    }

    @Override
    protected AbstractCompilationResult<JavaCodeFragment> newCompilationResultImpl(Message message) {
        return new CompilationResultImpl(message);
    }

    @Override
    protected AbstractCompilationResult<JavaCodeFragment> newCompilationResultImpl(JavaCodeFragment sourcecode,
            Datatype datatype) {
        return new CompilationResultImpl(sourcecode, datatype);
    }

    @Override
    public DatatypeHelper getDatatypeHelper(Datatype type) {
        return (DatatypeHelper)super.getDatatypeHelper(type);
    }
}
