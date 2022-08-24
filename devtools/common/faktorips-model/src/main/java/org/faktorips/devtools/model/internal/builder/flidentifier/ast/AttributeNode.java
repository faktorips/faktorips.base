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
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;

/**
 * This node represents an attribute of an {@link IType}, The resulting {@link Datatype} is a
 * {@link ValueDatatype}. In case of this identifier part was called on a list of {@link IType} the
 * resulting {@link Datatype} will be {@link ListOfTypeDatatype} with a {@link ValueDatatype} as
 * basis type.
 * 
 * @author dirmeier
 */
public class AttributeNode extends IdentifierNode {

    private final IAttribute attribute;

    private final boolean defaultValueAccess;

    private final IIpsProject ipsProject;

    AttributeNode(IAttribute attribute, boolean defaultValueAccess, boolean listOfTypes, IIpsProject ipsProject,
            TextRegion textRegion) {
        super(attribute.findDatatype(ipsProject), listOfTypes, textRegion);
        this.attribute = attribute;
        this.defaultValueAccess = defaultValueAccess;
        this.ipsProject = ipsProject;
    }

    public IAttribute getAttribute() {
        return attribute;
    }

    public boolean isDefaultValueAccess() {
        return defaultValueAccess;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

}
