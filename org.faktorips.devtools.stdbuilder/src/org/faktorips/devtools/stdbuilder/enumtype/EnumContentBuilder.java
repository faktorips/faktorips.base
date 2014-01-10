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

package org.faktorips.devtools.stdbuilder.enumtype;

import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.XmlContentFileCopyBuilder;

/**
 * Builder for enum content XML files.
 * 
 * @author dirmeier
 */
public class EnumContentBuilder extends XmlContentFileCopyBuilder {

    public EnumContentBuilder(DefaultBuilderSet builderSet) {
        super(IpsObjectType.ENUM_CONTENT, builderSet);
    }

    @Override
    public String getName() {
        return "EnumContentBuilder";
    }

}
