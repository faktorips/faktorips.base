/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.contentproposal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsSrcFileContentProposalProviderTest {

    @Test
    public void testGetProposal_emptyProposal() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IpsObjectType ipsObjectType = mock(IpsObjectType.class);

        when(ipsProject.findIpsSrcFiles(ipsObjectType)).thenReturn(new IIpsSrcFile[0]);

        IpsSrcFileContentProposalProvider contentProposalProvider = new IpsSrcFileContentProposalProvider(ipsProject,
                ipsObjectType);
        IContentProposal[] proposals = contentProposalProvider.getProposals("", 0);
        assertEquals(0, proposals.length);
    }

    @Test
    public void testGetProposal_someProposal() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IpsObjectType ipsObjectType = mock(IpsObjectType.class);

        IIpsSrcFile[] srcFiles = new IIpsSrcFile[2];
        srcFiles[0] = mock(IIpsSrcFile.class);
        when(srcFiles[0].exists()).thenReturn(true);
        srcFiles[1] = mock(IIpsSrcFile.class);
        when(srcFiles[1].exists()).thenReturn(true);
        when(ipsProject.findIpsSrcFiles(ipsObjectType)).thenReturn(srcFiles);

        IpsSrcFileContentProposalProvider contentProposalProvider = new IpsSrcFileContentProposalProvider(ipsProject,
                ipsObjectType);
        IContentProposal[] proposals = contentProposalProvider.getProposals("", 0);
        assertEquals(2, proposals.length);
        assertEquals(srcFiles[0], ((IpsSrcFileContentProposal)proposals[0]).getIpsSrcFile());
        assertEquals(srcFiles[1], ((IpsSrcFileContentProposal)proposals[1]).getIpsSrcFile());
    }

    @Test
    public void testGetProposal_filtered() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IpsObjectType ipsObjectType = mock(IpsObjectType.class);

        final IIpsSrcFile[] srcFiles = new IIpsSrcFile[2];
        srcFiles[0] = mock(IIpsSrcFile.class);
        when(srcFiles[0].exists()).thenReturn(true);
        srcFiles[1] = mock(IIpsSrcFile.class);
        when(srcFiles[1].exists()).thenReturn(true);
        when(ipsProject.findIpsSrcFiles(ipsObjectType)).thenReturn(srcFiles);

        IpsSrcFileContentProposalProvider contentProposalProvider = new IpsSrcFileContentProposalProvider(ipsProject,
                ipsObjectType);

        contentProposalProvider.setFilter(toTest -> {
            if (toTest == srcFiles[0]) {
                return true;
            } else {
                return false;
            }
        });

        IContentProposal[] proposals = contentProposalProvider.getProposals("", 0);
        assertEquals(1, proposals.length);
        assertEquals(srcFiles[0], ((IpsSrcFileContentProposal)proposals[0]).getIpsSrcFile());
    }
}
