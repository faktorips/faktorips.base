/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.XmlContentFileCopyBuilder;

public class TableContentBuilder extends XmlContentFileCopyBuilder {

    public TableContentBuilder(DefaultBuilderSet builderSet) {
        super(IpsObjectType.TABLE_CONTENTS, builderSet);
    }

    @Override
    public String getName() {
        return "TableContentBuilder";
    }

}
