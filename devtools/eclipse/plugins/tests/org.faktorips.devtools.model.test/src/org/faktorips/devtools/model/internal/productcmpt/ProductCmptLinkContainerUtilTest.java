/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ProductCmptLinkContainerUtilTest {
    @Mock
    private IProductCmptLinkContainer linkContainer;
    @Mock
    private IProductCmpt target;
    @Mock
    private IProductCmptTypeAssociation association;
    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IProductCmpt prodCmpt;

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
    public void testCanCreateLink() {
        setUpSrcFileMutable(true);
        when(linkContainer.isContainerFor(association)).thenReturn(true);
        IProductCmptType prodCmptType = mock(IProductCmptType.class);
        when(prodCmptType.isSubtypeOrSameType(prodCmptType, ipsProject)).thenReturn(true);
        when(target.findProductCmptType(ipsProject)).thenReturn(prodCmptType);
        when(association.findTarget(ipsProject)).thenReturn(prodCmptType);
        when(association.getMaxCardinality()).thenReturn(3);

        when(linkContainer.getIpsProject()).thenReturn(ipsProject);
        when(target.getIpsProject()).thenReturn(ipsProject);

        assertTrue(canCreateLink());
    }

    @Test
    public void testTargetNotWithinProjectStructure() {
        setUpSrcFileMutable(true);
        when(linkContainer.isContainerFor(association)).thenReturn(true);
        IProductCmptType prodCmptType = mock(IProductCmptType.class);
        when(prodCmptType.isSubtypeOrSameType(prodCmptType, ipsProject)).thenReturn(true);
        when(target.findProductCmptType(ipsProject)).thenReturn(prodCmptType);
        when(association.findTarget(ipsProject)).thenReturn(prodCmptType);
        when(association.getMaxCardinality()).thenReturn(3);

        when(linkContainer.getIpsProject()).thenReturn(ipsProject);

        IIpsProject notReferencedIpsProject = mock(IIpsProject.class);
        when(ipsProject.isReferencing(notReferencedIpsProject)).thenReturn(false);
        when(target.getIpsProject()).thenReturn(notReferencedIpsProject);

        assertCannotCreateLink();
    }

    @Test
    public void testChangingOverTimeMismatch() {
        setUpSrcFileMutable(true);
        assertCannotCreateLink();
    }

    private void setUpSrcFileMutable(boolean mutable) {
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(linkContainer.getProductCmpt()).thenReturn(prodCmpt);
        when(prodCmpt.getIpsSrcFile()).thenReturn(srcFile);
        when(srcFile.isMutable()).thenReturn(mutable);
    }

    private void assertCannotCreateLink() {
        assertFalse(canCreateLink());
    }

    private boolean canCreateLink() {
        return ProductCmptLinkContainerUtil.canCreateValidLink(linkContainer, target, association, ipsProject);
    }
}
