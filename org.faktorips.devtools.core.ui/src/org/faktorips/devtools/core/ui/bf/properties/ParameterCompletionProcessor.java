/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
                result
                        .add(new CompletionProposal(parameter.getName(), 0, documentOffset, parameter.getName()
                                .length()));
            }
        }
    }

}
