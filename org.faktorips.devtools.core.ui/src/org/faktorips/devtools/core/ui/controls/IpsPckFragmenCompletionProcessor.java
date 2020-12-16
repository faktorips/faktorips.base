/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;

/**
 * Completion processor for ips packages.
 * 
 * @author Joerg Ortmann
 */
public class IpsPckFragmenCompletionProcessor extends AbstractCompletionProcessor {

    private IpsPckFragmentRefControl control;

    public IpsPckFragmenCompletionProcessor(IpsPckFragmentRefControl control) {
        this.control = control;
        setComputeProposalForEmptyPrefix(true);
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl,
            int documentOffset) {
        if (control != null) {
            IIpsPackageFragmentRoot ipsPckFragmentRoot = control.getIpsPckFragmentRoot();
            if (ipsPckFragmentRoot != null) {
                setIpsProject(ipsPckFragmentRoot.getIpsProject());
            }
        }
        return super.computeCompletionProposals(contentAssistSubjectControl, documentOffset);
    }

    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        IIpsPackageFragmentRoot ipsPckFragmentRoot = control.getIpsPckFragmentRoot();
        if (ipsPckFragmentRoot == null) {
            return;
        }
        matchPackages(ipsPckFragmentRoot.getIpsPackageFragments(), prefix, documentOffset, result);
    }
}
