/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
