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

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;

/**
 * A list of all function signatures that are supported by the formula language.
 */
public enum FunctionSignatures {
    Abs(Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL }),
    And(Datatype.PRIMITIVE_BOOLEAN, Datatype.PRIMITIVE_BOOLEAN),
    Exists(Datatype.PRIMITIVE_BOOLEAN, new Datatype[] { AnyDatatype.INSTANCE }),
    If(AnyDatatype.INSTANCE, new Datatype[] { Datatype.PRIMITIVE_BOOLEAN, AnyDatatype.INSTANCE, AnyDatatype.INSTANCE }),
    IsEmpty(Datatype.PRIMITIVE_BOOLEAN, new Datatype[] { AnyDatatype.INSTANCE }),
    MaxDecimal(Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL, Datatype.DECIMAL }),
    MaxDouble(Datatype.DOUBLE, new Datatype[] { Datatype.DOUBLE, Datatype.DOUBLE }),
    MaxInt(Datatype.PRIMITIVE_INT, new Datatype[] { Datatype.PRIMITIVE_INT, Datatype.PRIMITIVE_INT }),
    MaxLong(Datatype.PRIMITIVE_LONG, new Datatype[] { Datatype.PRIMITIVE_LONG, Datatype.PRIMITIVE_LONG }),
    MaxMoney(Datatype.MONEY, new Datatype[] { Datatype.MONEY, Datatype.MONEY }),
    MinDecimal(Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL, Datatype.DECIMAL }),
    MinDouble(Datatype.DOUBLE, new Datatype[] { Datatype.DOUBLE, Datatype.DOUBLE }),
    MinInt(Datatype.PRIMITIVE_INT, new Datatype[] { Datatype.PRIMITIVE_INT, Datatype.PRIMITIVE_INT }),
    MinLong(Datatype.PRIMITIVE_LONG, new Datatype[] { Datatype.PRIMITIVE_LONG, Datatype.PRIMITIVE_LONG }),
    MinMoney(Datatype.MONEY, new Datatype[] { Datatype.MONEY, Datatype.MONEY }),
    Not(Datatype.PRIMITIVE_BOOLEAN, new Datatype[] { Datatype.PRIMITIVE_BOOLEAN }),
    NotBoolean(Datatype.BOOLEAN, new Datatype[] { Datatype.BOOLEAN }),
    Or(Datatype.PRIMITIVE_BOOLEAN, Datatype.PRIMITIVE_BOOLEAN),
    Round(Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL, Datatype.PRIMITIVE_INT }),
    RoundDown(Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL, Datatype.PRIMITIVE_INT }),
    RoundUp(Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL, Datatype.PRIMITIVE_INT }),
    SumBeanArrayPropertyFct(AnyDatatype.INSTANCE, new Datatype[] { AnyDatatype.INSTANCE, AnyDatatype.INSTANCE }),
    SumDecimal(Datatype.DECIMAL, new Datatype[] { new ArrayOfValueDatatype(Datatype.DECIMAL, 1) }),
    WholeNumber(Datatype.INTEGER, new Datatype[] { Datatype.DECIMAL });

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
