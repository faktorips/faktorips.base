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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link ICustomValidation}-{@link List}-supplier for all implementations of the extension point
 * {@value ExtensionPoints#CUSTOM_VALIDATION}.
 */
@SuppressWarnings("rawtypes")
// can't use LazyListExtension, because of the <?> only on the collection but not on the Class
public class CustomValidationExtensions extends LazyCollectionExtension<ICustomValidation, List<ICustomValidation<?>>> {

    /**
     * Name of the attribute that holds the name of the validation class
     */
    public static final String ATTRIBUTE_VALIDATION_CLASS = "validationClass"; //$NON-NLS-1$

    public CustomValidationExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                ExtensionPoints.CUSTOM_VALIDATION,
                ATTRIBUTE_VALIDATION_CLASS,
                ICustomValidation.class,
                ArrayList::new,
                ($, containerType, list) -> list.add(containerType));
    }

}
