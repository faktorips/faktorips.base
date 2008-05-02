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

}
