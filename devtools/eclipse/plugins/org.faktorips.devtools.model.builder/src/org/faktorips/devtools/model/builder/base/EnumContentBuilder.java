/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.base;

import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * Builder for enum content XML files.
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
