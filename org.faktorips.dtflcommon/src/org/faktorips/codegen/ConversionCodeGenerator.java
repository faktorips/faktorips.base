/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.codegen;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.codegen.conversion.BooleanToPrimitiveBooleanCg;
import org.faktorips.codegen.conversion.DecimalToIntegerCg;
import org.faktorips.codegen.conversion.IntegerToDecimalCg;
import org.faktorips.codegen.conversion.IntegerToLongCg;
import org.faktorips.codegen.conversion.IntegerToPrimitiveIntCg;
import org.faktorips.codegen.conversion.LongToDecimalCg;
import org.faktorips.codegen.conversion.LongToIntegerCg;
import org.faktorips.codegen.conversion.LongToPrimitiveLongCg;
import org.faktorips.codegen.conversion.PrimitiveBooleanToBooleanCg;
import org.faktorips.codegen.conversion.PrimitiveIntToDecimalCg;
import org.faktorips.codegen.conversion.PrimitiveIntToIntegerCg;
import org.faktorips.codegen.conversion.PrimitiveIntToLongCg;
import org.faktorips.codegen.conversion.PrimitiveIntToPrimitiveLongCg;
import org.faktorips.codegen.conversion.PrimitiveLongToLongCg;
import org.faktorips.codegen.conversion.PrimitiveLongToPrimitiveIntCg;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;

/**
 * The ConversionCodeGenerator extends the ConversionMatrix with the ability to generate the Java
 * sourcecode needed to convert the value of a given datatype to another (if the conversion is
 * possible).
 */
public class ConversionCodeGenerator implements ConversionMatrix {

    /**
     * Returns a default ConversionCodeGenerator that contains the following conversions.
     * <p>
     * <ul>
     * <li>Primitve boolean to Boolean</li>
     * <li>Boolean to primitive boolean</li>
     * <li>Primitive int to Integer</li>
     * <li>Integer to primitive int</li>
     * <li>Primitive int to Decimal</li>
     * <li>Integer to Decimal</li>
     * </ul>
     */
    public final static ConversionCodeGenerator getDefault() {
        ConversionCodeGenerator ccg = new ConversionCodeGenerator();
        ccg.add(new BooleanToPrimitiveBooleanCg());
        ccg.add(new DecimalToIntegerCg());
        ccg.add(new IntegerToDecimalCg());
        ccg.add(new IntegerToPrimitiveIntCg());
        ccg.add(new IntegerToLongCg());
        ccg.add(new LongToDecimalCg());
        ccg.add(new LongToPrimitiveLongCg());
        ccg.add(new LongToIntegerCg());
        ccg.add(new PrimitiveBooleanToBooleanCg());
        ccg.add(new PrimitiveIntToDecimalCg());
        ccg.add(new PrimitiveIntToIntegerCg());
        ccg.add(new PrimitiveIntToLongCg());
        ccg.add(new PrimitiveIntToPrimitiveLongCg());
        ccg.add(new PrimitiveLongToLongCg());
        ccg.add(new PrimitiveLongToPrimitiveIntCg());
        return ccg;
    }

    /** List of single conversion code generators. */
    private List<SingleConversionCg> conversions = new ArrayList<SingleConversionCg>();

    /**
     * Creates a new instance.
     */
    public ConversionCodeGenerator() {

    }

    public void add(SingleConversionCg conversion) {
        conversions.add(conversion);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canConvert(Datatype from, Datatype to) {
        if (from.equals(to)) {
            return true;
        }
        if (to instanceof AnyDatatype) {
            return true;
        }
        for (SingleConversionCg singleConversionCg : conversions) {
            SingleConversionCg cg = (SingleConversionCg)singleConversionCg;
            if (cg.getFrom().equals(from) && cg.getTo().equals(to)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the Java sourcecode that converts a value of Datatype <code>from</code> to a value of
     * Datatype <code>to</code> if possible. Returns null if the conversion is not possible.
     * 
     * @param from The datatype to convert from.
     * @param to The datatype to convert to.
     * @param fromValue A Java sourcecode fragment containing an expression that evaluates to a
     *            value of Datatype from.
     */
    public JavaCodeFragment getConversionCode(Datatype from, Datatype to, JavaCodeFragment fromValue) {
        if (from.equals(to)) {
            return fromValue;
        }
        if (to instanceof AnyDatatype) {
            return fromValue;
        }
        for (SingleConversionCg singleConversionCg : conversions) {
            SingleConversionCg cg = (SingleConversionCg)singleConversionCg;
            if (cg.getFrom().equals(from) && cg.getTo().equals(to)) {
                return cg.getConversionCode(fromValue);
            }
        }
        return null;
    }

}
