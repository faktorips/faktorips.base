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
    PlusDecimal(BinaryOperation.PLUS, DECIMAL),
    /**
     * + int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    PlusPrimitiveInt(BinaryOperation.PLUS, PRIMITIVE_INT),
    /**
     * + Integer
     * 
     * @see Datatype#INTEGER
     */
    PlusInteger(BinaryOperation.PLUS, INTEGER),
    /**
     * + Money
     * 
     * @see Datatype#MONEY
     */
    PlusMoney(BinaryOperation.PLUS, MONEY),

    // minus operation
    /**
     * - Decimal
     * 
     * @see Datatype#DECIMAL
     */
    MinusDecimal(BinaryOperation.MINUS, DECIMAL),
    /**
     * - Integer
     * 
     * @see Datatype#INTEGER
     */
    MinusInteger(BinaryOperation.MINUS, INTEGER),
    /**
     * - int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    MinusPrimitiveInt(BinaryOperation.MINUS, PRIMITIVE_INT),
    /**
     * - Money
     * 
     * @see Datatype#MONEY
     */
    MinusMoney(BinaryOperation.MINUS, MONEY),

    // add operation
    /**
     * int + int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    AddIntInt(PRIMITIVE_INT, BinaryOperation.PLUS, PRIMITIVE_INT),
    /**
     * Decimal + int
     * 
     * @see Datatype#PRIMITIVE_INT
     * @see Datatype#DECIMAL
     */
    AddDecimalInt(DECIMAL, BinaryOperation.PLUS, PRIMITIVE_INT),
    /**
     * int + Decimal
     * 
     * @see Datatype#PRIMITIVE_INT
     * @see Datatype#DECIMAL
     */
    AddIntDecimal(PRIMITIVE_INT, BinaryOperation.PLUS, DECIMAL),
    /**
     * Decimal + Integer
     * 
     * @see Datatype#INTEGER
     * @see Datatype#DECIMAL
     */
    AddDecimalInteger(DECIMAL, BinaryOperation.PLUS, INTEGER),
    /**
     * Integer + Decimal
     * 
     * @see Datatype#INTEGER
     * @see Datatype#DECIMAL
     */
    AddIntegerDecimal(INTEGER, BinaryOperation.PLUS, DECIMAL),
    /**
     * Decimal + Decimal
     * 
     * @see Datatype#DECIMAL
     */
    AddDecimalDecimal(DECIMAL, BinaryOperation.PLUS, DECIMAL),
    /**
     * Money + Money
     * 
     * @see Datatype#MONEY
     */
    AddMoneyMoney(MONEY, BinaryOperation.PLUS, MONEY),
    /**
     * String + String
     * 
     * @see Datatype#STRING
     */
    AddStringString(STRING, BinaryOperation.PLUS, STRING),

    // subtract operation
    /**
     * int - int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    SubtractIntInt(PRIMITIVE_INT, BinaryOperation.MINUS, PRIMITIVE_INT),
    /**
     * Decimal - Decimal
     * 
     * @see Datatype#DECIMAL
     */
    SubtractDecimalDecimal(DECIMAL, BinaryOperation.MINUS, DECIMAL),
    /**
     * Money - Money
     * 
     * @see Datatype#MONEY
     */
    SubtractMoneyMoney(MONEY, BinaryOperation.MINUS, MONEY),

    // multiply operation
    /**
     * int * int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    MultiplyIntInt(PRIMITIVE_INT, BinaryOperation.MULTIPLY, PRIMITIVE_INT),
    /**
     * Decimal * Money
     * 
     * @see Datatype#DECIMAL
     * @see Datatype#MONEY
     */
    MultiplyDecimalMoney(DECIMAL, BinaryOperation.MULTIPLY, MONEY),
    /**
     * Money * Decimal
     * 
     * @see Datatype#DECIMAL
     * @see Datatype#MONEY
     */
    MultiplyMoneyDecimal(MONEY, BinaryOperation.MULTIPLY, DECIMAL),
    /**
     * Integer * Money
     * 
     * @see Datatype#INTEGER
     * @see Datatype#MONEY
     */
    MultiplyIntegerMoney(INTEGER, BinaryOperation.MULTIPLY, MONEY),
    /**
     * Decimal * Decimal
     * 
     * @see Datatype#DECIMAL
     */
    MultiplyDecimalDecimal(DECIMAL, BinaryOperation.MULTIPLY, DECIMAL),

    // divide operation
    /**
     * Decimal / Decimal
     * 
     * @see Datatype#DECIMAL
     */
    DivideDecimalDecimal(DECIMAL, BinaryOperation.DIVIDE, DECIMAL),
    /**
     * Money / Decimal
     * 
     * @see Datatype#MONEY
     * @see Datatype#DECIMAL
     */
    DivideMoneyDecimal(MONEY, BinaryOperation.DIVIDE, DECIMAL),

    // greater than operation
    /**
     * Decimal &gt; Decimal
     * 
     * @see Datatype#DECIMAL
     */
    GreaterThanDecimalDecimal(DECIMAL, BinaryOperation.GREATER_THAN, DECIMAL),
    /**
     * Money &gt; Money
     * 
     * @see Datatype#MONEY
     */
    GreaterThanMoneyMoney(MONEY, BinaryOperation.GREATER_THAN, MONEY),

    // greater than or equal operation
    /**
     * Decimal &ge; Decimal
     * 
     * @see Datatype#DECIMAL
     */
    GreaterThanOrEqualDecimalDecimal(DECIMAL, BinaryOperation.GREATER_THAN_OR_EQUAL, DECIMAL),
    /**
     * Money &ge; Money
     * 
     * @see Datatype#MONEY
     */
    GreaterThanOrEqualMoneyMoney(MONEY, BinaryOperation.GREATER_THAN_OR_EQUAL, MONEY),

    // less than operation
    /**
     * Decimal &lt; Decimal
     * 
     * @see Datatype#DECIMAL
     */
    LessThanDecimalDecimal(DECIMAL, BinaryOperation.LESSER_THAN, DECIMAL),
    /**
     * Money &lt; Money
     * 
     * @see Datatype#MONEY
     */
    LessThanMoneyMoney(MONEY, BinaryOperation.LESSER_THAN, MONEY),

    // less than or equal operation
    /**
     * Decimal &le; Decimal
     * 
     * @see Datatype#DECIMAL
     */
    LessThanOrEqualDecimalDecimal(DECIMAL, BinaryOperation.LESSER_THAN_OR_EQUAL, DECIMAL),
    /**
     * Money &le; Money
     * 
     * @see Datatype#MONEY
     */
    LessThanOrEqualMoneyMoney(MONEY, BinaryOperation.LESSER_THAN_OR_EQUAL, MONEY),

    // equals operation
    /**
     * int = int
     * 
     * @see Datatype#PRIMITIVE_INT
     */
    EqualsPrimtiveInt(PRIMITIVE_INT, BinaryOperation.EQUAL, PRIMITIVE_INT),
    /**
     * boolean = boolean
     * 
     * @see Datatype#PRIMITIVE_BOOLEAN
     */
    EqualsPrimtiveBoolean(PRIMITIVE_BOOLEAN, BinaryOperation.EQUAL, PRIMITIVE_BOOLEAN),
    /**
     * Decimal = Decimal
     * 
     * @see Datatype#DECIMAL
     */
    EqualsDecimal(DECIMAL, BinaryOperation.EQUAL, DECIMAL),
    /**
     * Money = Money
     * 
     * @see Datatype#MONEY
     */
    EqualsMoney(MONEY, BinaryOperation.EQUAL, MONEY),
    /**
     * String = String
     * 
     * @see Datatype#STRING
     */
    EqualsString(STRING, BinaryOperation.EQUAL, STRING),
    /**
     * Object = Object
     * 
     * @see AnyDatatype
     */
    EqualsInstance(AnyDatatype.INSTANCE, BinaryOperation.EQUAL, AnyDatatype.INSTANCE),

    // not equals operation
    /**
     * Decimal != Decimal
     * 
     * @see Datatype#DECIMAL
     */
    NotEqualsDecimal(DECIMAL, BinaryOperation.NOT_EQUAL, DECIMAL),
    /**
     * Money != Money
     * 
     * @see Datatype#MONEY
     */
    NotEqualsMoney(MONEY, BinaryOperation.NOT_EQUAL, MONEY),

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
