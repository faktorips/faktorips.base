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
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.util.TextRegion;

/**
 * The parameter node represents an parameter access. The resulting {@link Datatype} is the type of
 * the parameter that was specified in formula signature.
 * 
 * @author dirmeier
 */
public class ParameterNode extends IdentifierNode {

    private final IParameter parameter;
    private final IIpsProject ipsProject;

    ParameterNode(IParameter parameter, TextRegion textRegion, IIpsProject ipsProject) {
        super(parameter.findDatatype(ipsProject), textRegion);
        this.parameter = parameter;
        this.ipsProject = ipsProject;
    }

    public IParameter getParameter() {
        return parameter;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

}
