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

public enum Operations {

    // plus operation
    PlusDecimal,
    PlusPrimitiveInt,
    PlusInteger,
    PlusMoney,

    // minus operation
    MinusDecimal,
    MinusInteger,
    MinusPrimitiveInt,
    MinusMoney,

    // add operation
    AddIntInt,
    AddDecimalInt,
    AddIntDecimal,
    AddDecimalInteger,
    AddIntegerDecimal,
    AddDecimalDecimal,
    AddMoneyMoney,
    AddStringString,

    // subtract operation
    SubtractIntInt,
    SubtractDecimalDecimal,
    SubtractMoneyMoney,

    // multiply operation
    MultiplyIntInt,
    MultiplyDecimalMoney,
    MultiplyMoneyDecimal,
    MultiplyIntegerMoney,
    MultiplyDecimalDecimal,

    // divide operation
    DivideDecimalDecimal,
    DivideMoneyDecimal,

    // greater than operation
    GreaterThanDecimalDecimal,
    GreaterThanMoneyMoney,

    // greater than or equal operation
    GreaterThanOrEqualDecimalDecimal,
    GreaterThanOrEqualMoneyMoney,

    // less than operation
    LessThanDecimalDecimal,
    LessThanMoneyMoney,

    // less than or equal operation
    LessThanOrEqualDecimalDecimal,
    LessThanOrEqualMoneyMoney,

    // equals operation
    EqualsPrimtiveInt,
    EqualsPrimtiveBoolean,
    EqualsDecimal,
    EqualsMoney,
    EqualsString,
    EqualsInstance,

    // not equals operation
    NotEqualsDecimal,
    NotEqualsMoney,

    // parenthesis operation
    ParenthesisInt,
    ParenthesisDecimal,
    ParenthesisMoney,
    ParenthesisString;

}
