/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.properties;

import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;

/**
 * A completion processor that completes the parameter names of the parameters of a business
 * function.
 * 
 * @author Peter Erzberger
 */
public class ParameterCompletionProcessor extends AbstractCompletionProcessor {

    private IBusinessFunction businessFunction;

    public void setBusinessFunction(IBusinessFunction businessFunction) {
        this.businessFunction = businessFunction;
    }

    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        String match = prefix.toLowerCase();
        for (IParameterBFE parameter : businessFunction.getParameterBFEs()) {
            Datatype datatype = parameter.findDatatype();
            if (!(datatype instanceof IType)) {
                continue;
            }
            if (parameter.getName().startsWith(match)) {
                result.add(new CompletionProposal(parameter.getName(), 0, documentOffset, parameter.getName().length()));
            }
        }
    }

}
