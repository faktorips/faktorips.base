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

package org.faktorips.codegen.dthelpers;

import org.faktorips.valueset.DefaultRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.ValueSet;

/**
 * Qualified and unqualified class names for classes in Java 5 projects. They can not be retrieved
 * via <code>getClass().getName()</code> because a reference from old code to those projects is not
 * desired.
 * 
 * @author Daniel Hohenberger
 */
public interface Java5ClassNames {

    String ValueSet_QualifiedName = ValueSet.class.getName();
    String OrderedValueSet_QualifiedName = OrderedValueSet.class.getName();
    String DefaultRange_QualifiedName = DefaultRange.class.getName();
    String RuntimePackage = "org.faktorips.runtime"; //$NON-NLS-1$
    String ILink_UnqualifiedName = "IProductComponentLink"; //$NON-NLS-1$
    String ILink_QualifiedName = RuntimePackage + "." + ILink_UnqualifiedName; //$NON-NLS-1$

    String RuntimePackageInternal = "org.faktorips.runtime.internal"; //$NON-NLS-1$
    String ReadOnlyBinaryRangeTreeKeyType_UnqualifiedName = "ReadOnlyBinaryRangeTree.KeyType"; //$NON-NLS-1$
    String ReadOnlyBinaryRangeTreeKeyType_QualifiedName = RuntimePackageInternal + "." //$NON-NLS-1$
            + ReadOnlyBinaryRangeTreeKeyType_UnqualifiedName;
    String Link_UnqualifiedName = "ProductComponentLink"; //$NON-NLS-1$
    String Link_QualifiedName = RuntimePackageInternal + "." + Link_UnqualifiedName; //$NON-NLS-1$

}
