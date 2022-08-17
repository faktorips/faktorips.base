/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.extensions.LazyCollectionExtension;

public class AdditionalDeepCopyWizardPageExtensions
        extends LazyCollectionExtension<IAdditionalDeepCopyWizardPage, List<IAdditionalDeepCopyWizardPage>> {

    public AdditionalDeepCopyWizardPageExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                IAdditionalDeepCopyWizardPage.EXTENSION_POINT_ID_DEEP_COPY_WIZARD,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IAdditionalDeepCopyWizardPage.class,
                ArrayList::new,
                ($, participant, list) -> list.add(participant));
    }

}
