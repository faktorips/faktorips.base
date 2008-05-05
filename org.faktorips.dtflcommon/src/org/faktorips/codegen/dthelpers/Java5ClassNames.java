/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.codegen.dthelpers;

/**
 * Qualified and unqualified class names for classes in Java 5 projects. They can not be retrieved
 * via <code>getClass().getName()</code> because a reference from old code to those projects is
 * not desired.
 * 
 * @author Daniel Hohenberger
 */
public interface Java5ClassNames {

    public static final String ValuesetPackage = "org.faktorips.valueset.java5";
    public static final String OrderedValueSet_UnqualifiedName = "OrderedValueSet";
    public static final String OrderedValueSet_QualifiedName = ValuesetPackage+"."+OrderedValueSet_UnqualifiedName;
    public static final String DefaultRange_UnqualifiedName = "DefaultRange";
    public static final String DefaultRange_QualifiedName = ValuesetPackage+"."+DefaultRange_UnqualifiedName;
    public static final String DecimalRange_UnqualifiedName = "DecimalRange";
    public static final String DecimalRange_QualifiedName = ValuesetPackage+"."+DecimalRange_UnqualifiedName;
    public static final String DoubleRange_UnqualifiedName = "DoubleRange";
    public static final String DoubleRange_QualifiedName = ValuesetPackage+"."+DoubleRange_UnqualifiedName;
    public static final String IntegerRange_UnqualifiedName = "IntegerRange";
    public static final String IntegerRange_QualifiedName = ValuesetPackage+"."+IntegerRange_UnqualifiedName;
    public static final String LongRange_UnqualifiedName = "LongRange";
    public static final String LongRange_QualifiedName = ValuesetPackage+"."+LongRange_UnqualifiedName;
    public static final String MoneyRange_UnqualifiedName = "MoneyRange";
    public static final String MoneyRange_QualifiedName = ValuesetPackage+"."+MoneyRange_UnqualifiedName;

    public static final String RuntimePackage = "org.faktorips.runtime.internal.java5";
    public static final String ProductComponentGeneration_UnqualifiedName = "ProductComponentGeneration";
    public static final String ProductComponentGeneration_QualifiedName = RuntimePackage+"."+ProductComponentGeneration_UnqualifiedName;

}
