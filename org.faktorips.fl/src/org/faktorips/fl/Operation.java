/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import static org.faktorips.datatype.Datatype.DECIMAL;
import static org.faktorips.datatype.Datatype.INTEGER;
import static org.faktorips.datatype.Datatype.MONEY;
import static org.faktorips.datatype.Datatype.PRIMITIVE_BOOLEAN;
import static org.faktorips.datatype.Datatype.PRIMITIVE_INT;
import static org.faktorips.datatype.Datatype.STRING;

import java.util.Arrays;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;

/**
 * Lists all operations the formula language supports.
 */
public enum Operation {

    // plus operation
    /**
     * + Decimal
     * 
     * @see Datatype#DECIMAL
     */
    PlusDecimal("+", DECIMAL),
    /**
     * + int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    PlusPrimitiveInt("+", PRIMITIVE_INT),
    /**
     * + Integer
     * 
     * @see Datatype#INTEGER
     */
    PlusInteger("+", INTEGER),
    /**
     * + Money
     * 
     * @see Datatype#MONEY
     */
    PlusMoney("+", MONEY),

    // minus operation
    /**
     * - Decimal
     * 
     * @see Datatype#DECIMAL
     */
    MinusDecimal("-", DECIMAL),
    /**
     * - Integer
     * 
     * @see Datatype#INTEGER
     */
    MinusInteger("-", INTEGER),
    /**
     * - int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    MinusPrimitiveInt("-", PRIMITIVE_INT),
    /**
     * - Money
     * 
     * @see Datatype#MONEY
     */
    MinusMoney("-", MONEY),

    // add operation
    /**
     * int + int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    AddIntInt(PRIMITIVE_INT, "+", PRIMITIVE_INT),
    /**
     * Decimal + int
     * 
     * @see Datatype#PRIMITIVE_INT
     * @see Datatype#DECIMAL
     */
    AddDecimalInt(DECIMAL, "+", PRIMITIVE_INT),
    /**
     * int + Decimal
     * 
     * @see Datatype#PRIMITIVE_INT
     * @see Datatype#DECIMAL
     */
    AddIntDecimal(PRIMITIVE_INT, "+", DECIMAL),
    /**
     * Decimal + Integer
     * 
     * @see Datatype#INTEGER
     * @see Datatype#DECIMAL
     */
    AddDecimalInteger(DECIMAL, "+", INTEGER),
    /**
     * Integer + Decimal
     * 
     * @see Datatype#INTEGER
     * @see Datatype#DECIMAL
     */
    AddIntegerDecimal(INTEGER, "+", DECIMAL),
    /**
     * Decimal + Decimal
     * 
     * @see Datatype#DECIMAL
     */
    AddDecimalDecimal(DECIMAL, "+", DECIMAL),
    /**
     * Money + Money
     * 
     * @see Datatype#MONEY
     */
    AddMoneyMoney(MONEY, "+", MONEY),
    /**
     * String + String
     * 
     * @see Datatype#STRING
     */
    AddStringString(STRING, "+", STRING),

    // subtract operation
    /**
     * int - int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    SubtractIntInt(PRIMITIVE_INT, "-", PRIMITIVE_INT),
    /**
     * Decimal - Decimal
     * 
     * @see Datatype#DECIMAL
     */
    SubtractDecimalDecimal(DECIMAL, "-", DECIMAL),
    /**
     * Money - Money
     * 
     * @see Datatype#MONEY
     */
    SubtractMoneyMoney(MONEY, "-", MONEY),

    // multiply operation
    /**
     * int * int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    MultiplyIntInt(PRIMITIVE_INT, "*", PRIMITIVE_INT),
    /**
     * Decimal * Money
     * 
     * @see Datatype#DECIMAL
     * @see Datatype#MONEY
     */
    MultiplyDecimalMoney(DECIMAL, "*", MONEY),
    /**
     * Money * Decimal
     * 
     * @see Datatype#DECIMAL
     * @see Datatype#MONEY
     */
    MultiplyMoneyDecimal(MONEY, "*", DECIMAL),
    /**
     * Integer * Money
     * 
     * @see Datatype#INTEGER
     * @see Datatype#MONEY
     */
    MultiplyIntegerMoney(INTEGER, "*", MONEY),
    /**
     * Decimal * Decimal
     * 
     * @see Datatype#DECIMAL
     */
    MultiplyDecimalDecimal(DECIMAL, "*", DECIMAL),

    // divide operation
    /**
     * Decimal / Decimal
     * 
     * @see Datatype#DECIMAL
     */
    DivideDecimalDecimal(DECIMAL, "/", DECIMAL),
    /**
     * Money / Decimal
     * 
     * @see Datatype#MONEY
     * @see Datatype#DECIMAL
     */
    DivideMoneyDecimal(MONEY, "/", DECIMAL),

    // greater than operation
    /**
     * Decimal > Decimal
     * 
     * @see Datatype#DECIMAL
     */
    GreaterThanDecimalDecimal(DECIMAL, ">", DECIMAL),
    /**
     * Money > Money
     * 
     * @see Datatype#MONEY
     */
    GreaterThanMoneyMoney(MONEY, ">", MONEY),

    // greater than or equal operation
    /**
     * Decimal >= Decimal
     * 
     * @see Datatype#DECIMAL
     */
    GreaterThanOrEqualDecimalDecimal(DECIMAL, ">=", DECIMAL),
    /**
     * Money >= Money
     * 
     * @see Datatype#MONEY
     */
    GreaterThanOrEqualMoneyMoney(MONEY, ">=", MONEY),

    // less than operation
    /**
     * Decimal < Decimal
     * 
     * @see Datatype#DECIMAL
     */
    LessThanDecimalDecimal(DECIMAL, "<", DECIMAL),
    /**
     * Money < Money
     * 
     * @see Datatype#MONEY
     */
    LessThanMoneyMoney(MONEY, "<", MONEY),

    // less than or equal operation
    /**
     * Decimal <= Decimal
     * 
     * @see Datatype#DECIMAL
     */
    LessThanOrEqualDecimalDecimal(DECIMAL, "<=", DECIMAL),
    /**
     * Money <= Money
     * 
     * @see Datatype#MONEY
     */
    LessThanOrEqualMoneyMoney(MONEY, "<=", MONEY),

    // equals operation
    /**
     * int = int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    EqualsPrimtiveInt(PRIMITIVE_INT, "=", PRIMITIVE_INT),
    /**
     * boolean = boolean
     * 
     * @see Datatype#PRIMITIVE_BOOLEAN
     */
    EqualsPrimtiveBoolean(PRIMITIVE_BOOLEAN, "=", PRIMITIVE_BOOLEAN),
    /**
     * Decimal = Decimal
     * 
     * @see Datatype#DECIMAL
     */
    EqualsDecimal(DECIMAL, "=", DECIMAL),
    /**
     * Money = Money
     * 
     * @see Datatype#MONEY
     */
    EqualsMoney(MONEY, "=", MONEY),
    /**
     * String = String
     * 
     * @see Datatype#STRING
     */
    EqualsString(STRING, "=", STRING),
    /**
     * Object = Object
     * 
     * @see AnyDatatype
     */
    EqualsInstance(AnyDatatype.INSTANCE, "=", AnyDatatype.INSTANCE),

    // not equals operation
    /**
     * Decimal != Decimal
     * 
     * @see Datatype#DECIMAL
     */
    NotEqualsDecimal(DECIMAL, "!=", DECIMAL),
    /**
     * Money != Money
     * 
     * @see Datatype#MONEY
     */
    NotEqualsMoney(MONEY, "!=", MONEY),

    // parenthesis operation
    /**
     * (int)
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    ParenthesisInt("()", PRIMITIVE_INT),
    /**
     * (Decimal)
     * 
     * @see Datatype#DECIMAL
     */
    ParenthesisDecimal("()", DECIMAL),
    /**
     * (Money)
     * 
     * @see Datatype#MONEY
     */
    ParenthesisMoney("()", MONEY),
    /**
     * (String)
     * 
     * @see Datatype#STRING
     */
    ParenthesisString("()", STRING);

    private final String operator;
    private final Datatype[] operands;

    private Operation(String operator, Datatype... operands) {
        this.operator = operator;
        this.operands = operands;
    }

    private Operation(Datatype lhs, String operator, Datatype rhs) {
        this(operator, lhs, rhs);
    }

    /**
     * Returns the operator used by the operation.
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Returns the operand used if this is a unary operation.
     * 
     * @throws IllegalStateException if this is not a unary operation.
     */
    public Datatype getOperand() {
        if (operands.length != 1) {
            throw new IllegalStateException("The operation " + toString() + " is not a unary operation.");
        }
        return operands[0];
    }

    /**
     * Returns the left-hand-side operand used if this is a binary operation.
     * 
     * @throws IllegalStateException if this is not a binary operation.
     */
    public Datatype getLhs() {
        if (operands.length != 2) {
            throw new IllegalStateException("The operation " + toString() + " is not a binary operation.");
        }
        return operands[0];
    }

    /**
     * Returns the right-hand-side operand used if this is a binary operation.
     * 
     * @throws IllegalStateException if this is not a binary operation.
     */
    public Datatype getRhs() {
        if (operands.length != 2) {
            throw new IllegalStateException("The operation " + toString() + " is not a binary operation.");
        }
        return operands[1];
    }

    @Override
    public String toString() {
        return "\"" + operator + "\" " + Arrays.toString(operands);
    }
}
