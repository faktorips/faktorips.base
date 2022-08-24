/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.ast;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;

/**
 * The index node is a special node that follows an {@link AssociationNode}. It represents an
 * identifier part that was suffixed with an index access. The resulting {@link Datatype} will
 * always be a subclass of {@link IType}.
 * 
 * @author dirmeier
 */
public class IndexNode extends IdentifierNode {

    private final int index;

    IndexNode(int index, IType targetType, TextRegion textRegion) {
        super(targetType, false, textRegion);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
