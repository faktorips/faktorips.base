/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.table;

import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.stdbuilder.IIpsArtefactBuilderFactory;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class TableRowBuilderFactory implements IIpsArtefactBuilderFactory {

    @Override
    public IIpsArtefactBuilder createBuilder(StandardBuilderSet builderSet) {
        return new TableRowBuilder(builderSet, builderSet.getGeneratorModelContext(), builderSet.getModelService());
    }

}
