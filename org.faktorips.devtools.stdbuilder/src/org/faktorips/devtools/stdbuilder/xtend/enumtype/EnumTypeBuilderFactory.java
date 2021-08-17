/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.enumtype;

import org.faktorips.devtools.stdbuilder.IIpsArtefactBuilderFactory;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class EnumTypeBuilderFactory implements IIpsArtefactBuilderFactory {

    @Override
    public EnumTypeBuilder createBuilder(StandardBuilderSet builderSet) {
        return new EnumTypeBuilder(builderSet, builderSet.getGeneratorModelContext(), builderSet.getModelService());
    }

}
