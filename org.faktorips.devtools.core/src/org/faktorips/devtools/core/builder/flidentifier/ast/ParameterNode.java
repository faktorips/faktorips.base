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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Region;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameter;

/**
 * The parameter node represents an parameter access. The resulting {@link Datatype} is the type of
 * the parameter that was specified in formula signature.
 * 
 * @author dirmeier
 */
public class ParameterNode extends IdentifierNode {

    private final IParameter parameter;
    private final IIpsProject ipsProject;

    ParameterNode(IParameter parameter, Region region, IIpsProject ipsProject) throws CoreException {
        super(parameter.findDatatype(ipsProject), region);
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
