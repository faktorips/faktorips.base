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

package org.faktorips.devtools.stdbuilder.table;

import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.XmlContentFileCopyBuilder;

public class TableContentBuilder extends XmlContentFileCopyBuilder {

    public TableContentBuilder(DefaultBuilderSet builderSet) {
        super(IpsObjectType.TABLE_CONTENTS, builderSet);
    }

    @Override
    public String getName() {
        return "TableContentBuilde";
    }

}
