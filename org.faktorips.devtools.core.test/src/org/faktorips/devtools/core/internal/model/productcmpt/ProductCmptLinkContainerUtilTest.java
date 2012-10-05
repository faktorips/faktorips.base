/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptLinkContainerUtilTest {
    @Mock
    private IProductCmptLinkContainer linkContainer;
    @Mock
    private IProductCmpt target;
    @Mock
    private IProductCmptTypeAssociation association;
    @Mock
    private IIpsProject ipsProject;

    @Test
    public void testTargetNull() {
        target = null;
        assertCannotCreateLink();
    }

    @Test
    public void testAssociationNull() {
        association = null;
        assertCannotCreateLink();
    }

    @Test
    public void testSrcFileIsImmutable() {
        setUpSrcFileMutable(false);

        assertCannotCreateLink();
    }

    @Test
    public void testChangingOverTimeMismatch() {
        setUpSrcFileMutable(true);
        when(linkContainer.isContainerFor(association)).thenReturn(false);
        assertCannotCreateLink();
    }

    private void setUpSrcFileMutable(boolean mutable) {
        IProductCmpt prodCmpt = mock(IProductCmpt.class);
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(linkContainer.getProductCmpt()).thenReturn(prodCmpt);
        when(prodCmpt.getIpsSrcFile()).thenReturn(srcFile);
        when(srcFile.isMutable()).thenReturn(mutable);
    }

    private void assertCannotCreateLink() {
        assertFalse(canCreateLink());
    }

    private boolean canCreateLink() {
        try {
            return ProductCmptLinkContainerUtil.canCreateValidLink(linkContainer, target, association, ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
