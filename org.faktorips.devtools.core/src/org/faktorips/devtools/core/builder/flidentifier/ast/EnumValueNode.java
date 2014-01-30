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

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.eclipse.jface.text.Region;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;

/**
 * The enum value node represents the value part of an enum access. It have to follow an
 * {@link EnumClassNode}. The resulting {@link Datatype} will be a {@link EnumDatatype}.
 * 
 * @author dirmeier
 */
public class EnumValueNode extends IdentifierNode {

    private final String enumValueName;

    EnumValueNode(String enumValueName, EnumDatatype datatype, Region region) {
        super(datatype, region);
        this.enumValueName = enumValueName;
    }

    public String getEnumValueName() {
        return enumValueName;
    }

    @Override
    public EnumDatatype getDatatype() {
        return (EnumDatatype)super.getDatatype();
    }

}
