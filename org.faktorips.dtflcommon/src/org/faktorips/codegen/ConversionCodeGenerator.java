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

package org.faktorips.codegen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.codegen.conversion.BigDecimalToDecimalCg;
import org.faktorips.codegen.conversion.BooleanToPrimitiveBooleanCg;
import org.faktorips.codegen.conversion.DecimalToBigDecimalCg;
import org.faktorips.codegen.conversion.DecimalToDoubleCg;
import org.faktorips.codegen.conversion.DecimalToIntegerCg;
import org.faktorips.codegen.conversion.DoubleToDecimalCg;
import org.faktorips.codegen.conversion.GregorianCalendarToLocalDateCg;
import org.faktorips.codegen.conversion.GregorianCalendarToLocalDateTimeCg;
import org.faktorips.codegen.conversion.IntegerToBigDecimalCg;
import org.faktorips.codegen.conversion.IntegerToDecimalCg;
import org.faktorips.codegen.conversion.IntegerToLongCg;
import org.faktorips.codegen.conversion.IntegerToPrimitiveIntCg;
import org.faktorips.codegen.conversion.LocalDateTimeToGregorianCalendarCg;
import org.faktorips.codegen.conversion.LocalDateToGregorianCalendarCg;
import org.faktorips.codegen.conversion.LongToBigDecimalCg;
import org.faktorips.codegen.conversion.LongToDecimalCg;
import org.faktorips.codegen.conversion.LongToIntegerCg;
import org.faktorips.codegen.conversion.LongToPrimitiveLongCg;
import org.faktorips.codegen.conversion.PrimitiveBooleanToBooleanCg;
import org.faktorips.codegen.conversion.PrimitiveIntToBigDecimalCg;
import org.faktorips.codegen.conversion.PrimitiveIntToDecimalCg;
import org.faktorips.codegen.conversion.PrimitiveIntToIntegerCg;
import org.faktorips.codegen.conversion.PrimitiveIntToLongCg;
import org.faktorips.codegen.conversion.PrimitiveIntToPrimitiveLongCg;
import org.faktorips.codegen.conversion.PrimitiveLongToBigDecimalCg;
import org.faktorips.codegen.conversion.PrimitiveLongToLongCg;
import org.faktorips.codegen.conversion.PrimitiveLongToPrimitiveIntCg;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;
import org.faktorips.util.ArgumentCheck;

/**
 * The ConversionCodeGenerator extends the ConversionMatrix with the ability to generate the Java
 * source code needed to convert the value of a given data type to another (if the conversion is
 * possible).
 */
public class ConversionCodeGenerator<T extends CodeFragment> implements ConversionMatrix {

    private FromToConversionMap<T> fromToConversionMap = new FromToConversionMap<T>();

    /**
     * Returns a default ConversionCodeGenerator that contains the following conversions.
     * <p>
     * <ul>
     * <li>Primitive boolean to Boolean</li>
     * <li>Boolean to primitive boolean</li>
     * <li>Primitive int to Integer</li>
     * <li>Integer to primitive int</li>
     * <li>Primitive int to Decimal</li>
     * <li>Integer to Decimal</li>
     * </ul>
     */
    public static final ConversionCodeGenerator<JavaCodeFragment> getDefault() {
        ConversionCodeGenerator<JavaCodeFragment> ccg = new ConversionCodeGenerator<JavaCodeFragment>();
        ccg.add(new BooleanToPrimitiveBooleanCg());
        ccg.add(new DecimalToIntegerCg());
        ccg.add(new IntegerToBigDecimalCg());
        ccg.add(new IntegerToDecimalCg());
        ccg.add(new IntegerToPrimitiveIntCg());
        ccg.add(new IntegerToLongCg());
        ccg.add(new LongToBigDecimalCg());
        ccg.add(new LongToDecimalCg());
        ccg.add(new LongToPrimitiveLongCg());
        ccg.add(new LongToIntegerCg());
        ccg.add(new PrimitiveBooleanToBooleanCg());
        ccg.add(new PrimitiveIntToBigDecimalCg());
        ccg.add(new PrimitiveIntToDecimalCg());
        ccg.add(new PrimitiveIntToIntegerCg());
        ccg.add(new PrimitiveIntToLongCg());
        ccg.add(new PrimitiveIntToPrimitiveLongCg());
        ccg.add(new PrimitiveLongToBigDecimalCg());
        ccg.add(new PrimitiveLongToLongCg());
        ccg.add(new PrimitiveLongToPrimitiveIntCg());
        ccg.add(new BigDecimalToDecimalCg());
        ccg.add(new DecimalToBigDecimalCg());
        ccg.add(new DoubleToDecimalCg());
        ccg.add(new DecimalToDoubleCg());
        ccg.add(new LocalDateToGregorianCalendarCg());
        ccg.add(new LocalDateTimeToGregorianCalendarCg());
        ccg.add(new GregorianCalendarToLocalDateCg());
        ccg.add(new GregorianCalendarToLocalDateTimeCg());
        return ccg;
    }

    public void add(SingleConversionCg<T> conversion) {
        addConversionToMap(conversion);
    }

    private void addConversionToMap(SingleConversionCg<T> conversion) {
        fromToConversionMap.add(conversion);
    }

    public boolean canConvert(Datatype from, Datatype to) {
        if (from.equals(to)) {
            return true;
        }
        if (to instanceof AnyDatatype) {
            return true;
        }
        if (existSingleConversionWithPrimitives(from, to)) {
            return true;
        }
        SingleConversionCg<T> singleConversionCode = getSingleConversionCode(from, to);
        if (singleConversionCode != null) {
            return true;
        }
        return false;
    }

    private SingleConversionCg<T> getSingleConversionCode(Datatype from, Datatype to) {
        return fromToConversionMap.get(from, to);
    }

    /**
     * Returns the Java source code that converts a value of {@link Datatype} <code>from</code> to a
     * value of {@link Datatype} <code>to</code> if possible. Returns null if the conversion is not
     * possible.
     * 
     * @param from The data type to convert from.
     * @param to The data type to convert to.
     * @param fromValue A Java source code fragment containing an expression that evaluates to a
     *            value of {@link Datatype} from.
     */
    public T getConversionCode(Datatype from, Datatype to, T fromValue) {
        if (from == null || to == null) {
            return null;
        }
        if (from.equals(to)) {
            return fromValue;
        }
        if (to instanceof AnyDatatype) {
            return fromValue;
        }
        if (existSingleConversionWithPrimitives(from, to)) {
            if (areFromToPrimitiveAndHaveSingleConversionCode(from, to)) {
                SingleConversionCg<T> singleConversionCode = getSingleConversionCodeWithPrimitives(from, to);
                return singleConversionCode.getConversionCode(fromValue);
            }
            if (from.isPrimitive() || to.isPrimitive()) {
                SingleConversionCg<T> singleConversionCg = getSingleConversionCodeWithOnePrimitive(from, to);
                return singleConversionCg.getConversionCode(fromValue);
            }
        }
        if (getSingleConversionCode(from, to) != null) {
            return getSingleConversionCode(from, to).getConversionCode(fromValue);
        }
        return null;
    }

    private boolean existSingleConversionWithPrimitives(Datatype from, Datatype to) {
        if (isSingleConversionEmpty(from, to)) {
            if (getSingleConversionCode(getWrapedPrimitiveDatatype(from), getWrapedPrimitiveDatatype(to)) != null) {
                return true;
            }
            if (getSingleConversionCode(getWrapedPrimitiveDatatype(from), to) != null) {
                return true;
            }
            if (getSingleConversionCode(from, getWrapedPrimitiveDatatype(to)) != null) {
                return true;
            }
        }
        return false;
    }

    private SingleConversionCg<T> getSingleConversionCodeWithPrimitives(Datatype from, Datatype to) {
        return getSingleConversionCode(getWrapedPrimitiveDatatype(from), getWrapedPrimitiveDatatype(to));
    }

    private SingleConversionCg<T> getSingleConversionCodeWithOnePrimitive(Datatype from, Datatype to) {
        return from.isPrimitive() ? getSingleConversionCode(getWrapedPrimitiveDatatype(from), to)
                : getSingleConversionCode(from, getWrapedPrimitiveDatatype(to));
    }

    private boolean areFromToPrimitiveAndHaveSingleConversionCode(Datatype from, Datatype to) {
        return from.isPrimitive() && to.isPrimitive() && getSingleConversionCodeWithPrimitives(from, to) != null;
    }

    private boolean isSingleConversionEmpty(Datatype from, Datatype to) {
        return getSingleConversionCode(from, to) == null;
    }

    private Datatype getWrapedPrimitiveDatatype(Datatype primitiveDatatype) {
        if (primitiveDatatype.equals(Datatype.PRIMITIVE_INT)) {
            return Datatype.INTEGER;
        }
        if (primitiveDatatype.equals(Datatype.PRIMITIVE_LONG)) {
            return Datatype.LONG;
        }
        return null;
    }

    private static class FromToConversionMap<T extends CodeFragment> {

        private final Map<Datatype, Map<Datatype, SingleConversionCg<T>>> internalMap = new ConcurrentHashMap<Datatype, Map<Datatype, SingleConversionCg<T>>>();

        /**
         * Return single conversion code generator given from-datatype and to-datatype
         * 
         * @param from The datatype which will be converted.
         * @param to The datatype which will be converted to.
         * @return single conversion code {@link SingleConversionCg}
         */
        public SingleConversionCg<T> get(Datatype from, Datatype to) {
            if (from == null || to == null) {
                return null;
            }
            Map<Datatype, SingleConversionCg<T>> fromMap = getMapValueOfFromDatatype(from);
            SingleConversionCg<T> singleConversionCg = getMapValueOfToDatatype(to, fromMap);
            return singleConversionCg;
        }

        /**
         * Adds a single conversion code generator to the mapping
         * 
         * @param singleConversionCg The single conversion code generator to be added.
         */
        public void add(SingleConversionCg<T> singleConversionCg) {
            Datatype from = singleConversionCg.getFrom();
            Datatype to = singleConversionCg.getTo();
            Map<Datatype, SingleConversionCg<T>> innerMap = new ConcurrentHashMap<Datatype, SingleConversionCg<T>>();
            innerMap.put(to, singleConversionCg);

            if (ifMappingExist(from, innerMap)) {
                getInternalMap().get(from).put(to, singleConversionCg);
            } else {
                getInternalMap().put(from, innerMap);
            }
        }

        /**
         * Returns the mapping.
         * 
         * @return internalMap
         */
        public Map<Datatype, Map<Datatype, SingleConversionCg<T>>> getInternalMap() {
            return internalMap;
        }

        private boolean ifMappingExist(Datatype from, Map<Datatype, SingleConversionCg<T>> innerMap) {
            return getInternalMap().containsKey(from) && !getInternalMap().containsValue(innerMap);
        }

        private Map<Datatype, SingleConversionCg<T>> getMapValueOfFromDatatype(Datatype from) {
            Map<Datatype, SingleConversionCg<T>> fromMap = getInternalMap().get(from);
            return fromMap;
        }

        private SingleConversionCg<T> getMapValueOfToDatatype(Datatype to, Map<Datatype, SingleConversionCg<T>> fromMap) {
            ArgumentCheck.notNull(fromMap);
            SingleConversionCg<T> singleConversionCg = fromMap.get(to);
            return singleConversionCg;
        }
    }

}
