/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.util.TextRegion;

/**
 * The parameter node represents an parameter access. The resulting {@link Datatype} is the type of
 * the parameter that was specified in formula signature.
 * 
 * @author dirmeier
 */
public class ParameterNode extends IdentifierNode {

    private final IParameter parameter;
    private final IIpsProject ipsProject;

    ParameterNode(IParameter parameter, TextRegion textRegion, IIpsProject ipsProject) throws CoreException {
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

    @Override
    public String getText() {
        return parameter.getName();
    }

    @Override
    public String getDescription(MultiLanguageSupport multiLanguageSupport) {
        try {
            Datatype datatype = parameter.findDatatype(ipsProject);
            if (datatype instanceof IIpsElement) {
                return getDescription((IIpsElement)datatype, multiLanguageSupport);
            } else {
                return NLS.bind(Messages.ParameterNode_description, parameter.getName(), parameter.getDatatype());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
