/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import java.util.List;

import org.faktorips.devtools.model.internal.productcmpt.IDeepCopyOperationFixup;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IDeepCopyOperationFixup}-{@link List}-supplier for all implementations of the extension
 * point {@value IDeepCopyOperationFixup#EXTENSION_POINT_ID_DEEP_COPY_OPERATION}.
 */
public class DeepCopyOperationFixupExtensions extends LazyListExtension<IDeepCopyOperationFixup> {

    public DeepCopyOperationFixupExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                IDeepCopyOperationFixup.EXTENSION_POINT_ID_DEEP_COPY_OPERATION,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IDeepCopyOperationFixup.class);
    }

}
