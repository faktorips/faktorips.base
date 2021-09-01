/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.extensions;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.testcase.ITocTreeFromDependencyManagerLoader;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.extensions.LazyCollectionExtension;

public class TocTreeFromDependencyManagerExtension extends
        LazyCollectionExtension<ITocTreeFromDependencyManagerLoader, List<ITocTreeFromDependencyManagerLoader>> {

    public TocTreeFromDependencyManagerExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                ITocTreeFromDependencyManagerLoader.EXTENSION_POINT_ID_NEW_PRODUCT_DEFINITION_OPERATION,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                ITocTreeFromDependencyManagerLoader.class,
                ArrayList::new,
                ($, participant, list) -> list.add(participant));
    }

}
