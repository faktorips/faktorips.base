/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.junit.Test;

public class ProductStructureCounterTest extends AbstractIpsPluginTest {

    @Test
    public void testCountProductsAndReferences_EmptyStructure() {
        IProductCmptTreeStructure mockStructure = mock(IProductCmptTreeStructure.class);
        when(mockStructure.toSet(false)).thenReturn(new HashSet<>());

        ProductStructureCounts counts = ProductStructureCounter.countProductsAndReferences(mockStructure);
        assertThat(counts.productCount(), is(0));
        assertThat(counts.relationCount(), is(0));
    }

    @Test
    public void testCountProductsAndReferences_NullStructure() {
        ProductStructureCounts counts = ProductStructureCounter.countProductsAndReferences(null);
        assertThat(counts.productCount(), is(0));
        assertThat(counts.relationCount(), is(0));
    }

    @Test
    public void testCountProductsAndReferences_ProductsOnly() {
        IProductCmptTreeStructure mockStructure = createMockStructure(3, 0);

        ProductStructureCounts counts = ProductStructureCounter.countProductsAndReferences(mockStructure);
        assertThat(counts.productCount(), is(3));
        assertThat(counts.relationCount(), is(0));
    }

    @Test
    public void testCountProductsAndReferences_RelationsOnly() {
        IProductCmptTreeStructure mockStructure = createMockStructure(0, 3);

        ProductStructureCounts counts = ProductStructureCounter.countProductsAndReferences(mockStructure);
        assertThat(counts.productCount(), is(0));
        assertThat(counts.relationCount(), is(3));
    }

    @Test
    public void testCountProductsAndReferences_ProductAndRelation() {
        IProductCmptTreeStructure mockStructure = createMockStructure(31, 3);

        ProductStructureCounts counts = ProductStructureCounter.countProductsAndReferences(mockStructure);
        assertThat(counts.productCount(), is(31));
        assertThat(counts.relationCount(), is(3));
    }

    private IProductCmptTreeStructure createMockStructure(int productCount, int relationCount) {
        IProductCmptTreeStructure mockStructure = mock(IProductCmptTreeStructure.class);
        Set<IProductCmptStructureReference> mockReferences = new HashSet<>();

        for (int i = 0; i < productCount; i++) {
            IProductCmptReference mockRef = mock(IProductCmptReference.class);
            when(mockRef.getChildren()).thenReturn(new IProductCmptStructureReference[0]);
            mockReferences.add(mockRef);
        }

        for (int i = 0; i < relationCount; i++) {
            IProductCmptTypeAssociationReference mockRef = mock(IProductCmptTypeAssociationReference.class);
            when(mockRef.getChildren())
                    .thenReturn(new IProductCmptStructureReference[] { mock(IProductCmptStructureReference.class) });
            mockReferences.add(mockRef);
        }

        when(mockStructure.toSet(false)).thenReturn(mockReferences);
        return mockStructure;
    }
}
