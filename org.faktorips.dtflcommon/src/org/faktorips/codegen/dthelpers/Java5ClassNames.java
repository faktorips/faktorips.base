/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
