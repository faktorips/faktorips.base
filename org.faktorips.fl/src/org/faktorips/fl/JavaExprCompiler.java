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

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.codegen.CodeGenUtil;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.fl.operations.AddDecimalDecimal;
import org.faktorips.fl.operations.AddDecimalInt;
import org.faktorips.fl.operations.AddDecimalInteger;
import org.faktorips.fl.operations.AddIntDecimal;
import org.faktorips.fl.operations.AddIntInt;
import org.faktorips.fl.operations.AddIntegerDecimal;
import org.faktorips.fl.operations.AddMoneyMoney;
import org.faktorips.fl.operations.AddStringString;
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
import org.faktorips.fl.parser.ParseException;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.fl.parser.TokenMgrError;
import org.faktorips.util.message.Message;

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
        register(new AddDecimalInt());
        register(new AddIntDecimal());
        register(new AddDecimalInteger());
        register(new AddIntegerDecimal());
        register(new AddDecimalDecimal());
        register(new AddMoneyMoney());
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

        // greater than or equal operation
        register(new GreaterThanOrEqualDecimalDecimal());
        register(new GreaterThanOrEqualMoneyMoney());

        // less than operation
        register(new LessThanDecimalDecimal());
        register(new LessThanMoneyMoney());

        // less than or equal operation
        register(new LessThanOrEqualDecimalDecimal());
        register(new LessThanOrEqualMoneyMoney());

        // equals operation
        register(new EqualsPrimtiveType(Datatype.PRIMITIVE_INT));
        register(new EqualsPrimtiveType(Datatype.PRIMITIVE_BOOLEAN));
        register(new EqualsObjectDatatype(Datatype.DECIMAL));
        register(new EqualsObjectDatatype(Datatype.MONEY));
        register(new EqualsObjectDatatype(Datatype.STRING));
        register(new EqualsObjectDatatype(AnyDatatype.INSTANCE));

        // not equals operation
        register(new NotEqualsObjectDatatype(Datatype.DECIMAL));
        register(new NotEqualsObjectDatatype(Datatype.MONEY));

        // parenthesis operation
        register(new ParenthesisInt());
        register(new ParenthesisDecimal());
        register(new ParenthesisMoney());
        register(new ParenthesisString());
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(String expr) {
        SimpleNode rootNode;
        // parse the expression
        try {
            rootNode = parse(expr);
        } catch (ParseException pe) {
            return parseExceptionToResult(pe);
            // CSOFF: IllegalCatch
        } catch (Exception pe) {
            // CSON: IllegalCatch
            return new CompilationResultImpl(Message.newError(INTERNAL_ERROR,
                    LOCALIZED_STRINGS.getString(INTERNAL_ERROR, getLocale())));
        } catch (TokenMgrError e) {
            String text = LOCALIZED_STRINGS.getString(LEXICAL_ERROR, getLocale(), e.getMessage());
            return new CompilationResultImpl(Message.newError(LEXICAL_ERROR, text));
        }
        // parse ok, generate the sourcecode via the visitor visiting the parse tree
        CompilationResultImpl result;
        try {
            ParseTreeVisitor<JavaCodeFragment> visitor = new JavaParseTreeVisitor(this);
            result = (CompilationResultImpl)rootNode.jjtAccept(visitor, null);
            // CSOFF: IllegalCatch
        } catch (Exception pe) {
            // CSON: IllegalCatch
            return new CompilationResultImpl(Message.newError(INTERNAL_ERROR,
                    LOCALIZED_STRINGS.getString(INTERNAL_ERROR, getLocale())));
        }
        if (result.failed()) {
            return result;
        }
        try {
            Datatype resultType = result.getDatatype();
            if (!getEnsureResultIsObject() || !resultType.isPrimitive()) {
                return result;
            }
            // convert primitive to wrapper object
            JavaCodeFragment converted = CodeGenUtil.convertPrimitiveToWrapper(resultType, result.getCodeFragment());
            AbstractCompilationResult<JavaCodeFragment> finalResult = new CompilationResultImpl(converted,
                    ((ValueDatatype)resultType).getWrapperType());
            finalResult.addIdentifiersUsed(result.getIdentifiersUsedAsSet());
            return finalResult;
            // CSOFF: IllegalCatch
        } catch (Exception pe) {
            // CSON: IllegalCatch
            return new CompilationResultImpl(Message.newError(INTERNAL_ERROR,
                    LOCALIZED_STRINGS.getString(INTERNAL_ERROR, getLocale())));
        }
    }

    @Override
    protected CompilationResult<JavaCodeFragment> parseExceptionToResult(ParseException e) {
        String expected = ""; //$NON-NLS-1$
        for (int[] expectedTokenSequence : e.expectedTokenSequences) {
            expected += e.tokenImage[expectedTokenSequence[0]] + " "; //$NON-NLS-1$
        }
        Object[] replacements = new Object[] { e.currentToken.next.toString(),
                new Integer(e.currentToken.next.beginLine), new Integer(e.currentToken.next.beginColumn), expected };
        return new CompilationResultImpl(Message.newError(SYNTAX_ERROR,
                LOCALIZED_STRINGS.getString(SYNTAX_ERROR, getLocale(), replacements)));
    }
}
