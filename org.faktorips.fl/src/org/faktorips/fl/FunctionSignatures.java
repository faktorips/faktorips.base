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

import static org.faktorips.datatype.Datatype.BOOLEAN;
import static org.faktorips.datatype.Datatype.DECIMAL;
import static org.faktorips.datatype.Datatype.DOUBLE;
import static org.faktorips.datatype.Datatype.INTEGER;
import static org.faktorips.datatype.Datatype.MONEY;
import static org.faktorips.datatype.Datatype.PRIMITIVE_BOOLEAN;
import static org.faktorips.datatype.Datatype.PRIMITIVE_INT;
import static org.faktorips.datatype.Datatype.PRIMITIVE_LONG;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;

/**
 * A list of all function signatures that are supported by the formula language.
 */
public enum FunctionSignatures {
    Abs(DECIMAL, new Datatype[] { DECIMAL }),
    And(PRIMITIVE_BOOLEAN, PRIMITIVE_BOOLEAN),
    Exists(PRIMITIVE_BOOLEAN, new Datatype[] { AnyDatatype.INSTANCE }),
    If(AnyDatatype.INSTANCE, new Datatype[] { PRIMITIVE_BOOLEAN, AnyDatatype.INSTANCE, AnyDatatype.INSTANCE }),
    IsEmpty(PRIMITIVE_BOOLEAN, new Datatype[] { AnyDatatype.INSTANCE }),
    MaxDecimal(DECIMAL, new Datatype[] { DECIMAL, DECIMAL }),
    MaxDouble(DOUBLE, new Datatype[] { DOUBLE, DOUBLE }),
    MaxInt(PRIMITIVE_INT, new Datatype[] { PRIMITIVE_INT, PRIMITIVE_INT }),
    MaxLong(PRIMITIVE_LONG, new Datatype[] { PRIMITIVE_LONG, PRIMITIVE_LONG }),
    MaxMoney(MONEY, new Datatype[] { MONEY, MONEY }),
    MinDecimal(DECIMAL, new Datatype[] { DECIMAL, DECIMAL }),
    MinDouble(DOUBLE, new Datatype[] { DOUBLE, DOUBLE }),
    MinInt(PRIMITIVE_INT, new Datatype[] { PRIMITIVE_INT, PRIMITIVE_INT }),
    MinLong(PRIMITIVE_LONG, new Datatype[] { PRIMITIVE_LONG, PRIMITIVE_LONG }),
    MinMoney(MONEY, new Datatype[] { MONEY, MONEY }),
    Not(PRIMITIVE_BOOLEAN, new Datatype[] { PRIMITIVE_BOOLEAN }),
    NotBoolean(BOOLEAN, new Datatype[] { BOOLEAN }),
    Or(PRIMITIVE_BOOLEAN, PRIMITIVE_BOOLEAN),
    Round(DECIMAL, new Datatype[] { DECIMAL, PRIMITIVE_INT }),
    RoundDown(DECIMAL, new Datatype[] { DECIMAL, PRIMITIVE_INT }),
    RoundUp(DECIMAL, new Datatype[] { DECIMAL, PRIMITIVE_INT }),
    SumBeanArrayPropertyFct(AnyDatatype.INSTANCE, new Datatype[] { AnyDatatype.INSTANCE, AnyDatatype.INSTANCE }),
    SumDecimal(DECIMAL, new Datatype[] { new ArrayOfValueDatatype(DECIMAL, 1) }),
    WholeNumber(INTEGER, new Datatype[] { DECIMAL });

    private final Datatype type;
    private final Datatype[] argTypes;
    private final boolean hasVarArgs;

    private FunctionSignatures(Datatype type, Datatype argType) {
        this.type = type;
        this.hasVarArgs = true;
        this.argTypes = new Datatype[] { argType };
    }

    private FunctionSignatures(Datatype type, Datatype[] argTypes) {
        this.type = type;
        this.hasVarArgs = false;
        this.argTypes = argTypes;
    }

    public Datatype getType() {
        return type;
    }

    public Datatype[] getArgTypes() {
        return argTypes;
    }

    public boolean hasVarArgs() {
        return hasVarArgs;
    }
}
