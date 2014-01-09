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

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;

/**
 * Small helper class which provides the {@link DeltaType} and its parent, the
 * {@link IPropertyValueContainerToTypeDelta}. This class is necessary for the
 * {@link DeltaContentProvider} and {@link DeltaLabelProvider}.
 */
class DeltaTypeWrapper {
    private final DeltaType type;
    private final IPropertyValueContainerToTypeDelta delta;

    DeltaTypeWrapper(DeltaType type, IPropertyValueContainerToTypeDelta delta) {
        this.type = type;
        this.delta = delta;
    }

    DeltaType getDeltaType() {
        return type;
    }

    IPropertyValueContainerToTypeDelta getDelta() {
        return delta;
    }
}