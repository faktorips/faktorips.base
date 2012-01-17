/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.contentproposal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.IFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
        srcFiles[1] = mock(IIpsSrcFile.class);
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
        srcFiles[1] = mock(IIpsSrcFile.class);
        when(ipsProject.findIpsSrcFiles(ipsObjectType)).thenReturn(srcFiles);

        IpsSrcFileContentProposalProvider contentProposalProvider = new IpsSrcFileContentProposalProvider(ipsProject,
                ipsObjectType);

        contentProposalProvider.setFilter(new IFilter() {

            @Override
            public boolean select(Object toTest) {
                if (toTest == srcFiles[0]) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        IContentProposal[] proposals = contentProposalProvider.getProposals("", 0);
        assertEquals(1, proposals.length);
        assertEquals(srcFiles[0], ((IpsSrcFileContentProposal)proposals[0]).getIpsSrcFile());
    }
}
