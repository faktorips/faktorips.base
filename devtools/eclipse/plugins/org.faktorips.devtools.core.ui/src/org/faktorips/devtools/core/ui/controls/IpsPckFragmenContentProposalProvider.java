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

import java.util.stream.Stream;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractPrefixContentProposalProvider;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * Content proposal provider for IPS packages.
 */
public class IpsPckFragmenContentProposalProvider extends AbstractPrefixContentProposalProvider {

    private IpsPckFragmentRefControl control;

    public IpsPckFragmenContentProposalProvider(IpsPckFragmentRefControl control) {
        this.control = control;
    }

    @Override
    public IContentProposal[] getProposals(String prefix) {
        IIpsPackageFragmentRoot ipsPckFragmentRoot = control.getIpsPckFragmentRoot();
        if (ipsPckFragmentRoot != null) {
            String lowerPrefix = prefix.toLowerCase();
            return Stream.of(ipsPckFragmentRoot.getIpsPackageFragments())
                    .map(IIpsPackageFragment::getName)
                    .filter(s -> s.length() > 0)
                    // don't show default package,
                    // the default package could be entered by leaving the edit field empty
                    .filter(s -> s.toLowerCase().startsWith(lowerPrefix))
                    .map(ContentProposal::new)
                    .toArray(IContentProposal[]::new);
        }
        return EMPTY_PROPOSALS;
    }
}
