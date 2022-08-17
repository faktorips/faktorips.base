/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.treestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProductCmptReferenceTest {

    private ProductCmptReference cmptReference;
    private GregorianCalendar today;
    private GregorianCalendar yesterday;
    private GregorianCalendar tomorow;

    @Mock
    private IProductCmptTreeStructure structure;
    @Mock
    private ProductCmptTypeAssociationReference parent;
    @Mock
    private IProductCmpt cmpt;
    @Mock
    private IProductCmptLink link;
    @Mock
    private IProductCmptGeneration productCmptGeneration;
    @Mock
    private ProductCmptReference childProductCmptReference;
    @Mock
    private IProductCmpt childCmpt;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    protected void setUpBasic() {
        try {
            cmptReference = new ProductCmptReference(structure, parent, cmpt, link);
        } catch (CycleInProductStructureException e) {
            throw new RuntimeException(e);
        }
        today = new GregorianCalendar();
        today = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH));
        yesterday = new GregorianCalendar(today.get(GregorianCalendar.YEAR), today.get(GregorianCalendar.MONTH),
                today.get(GregorianCalendar.DAY_OF_MONTH) - 1);
        tomorow = new GregorianCalendar(today.get(GregorianCalendar.YEAR), today.get(GregorianCalendar.MONTH),
                today.get(GregorianCalendar.DAY_OF_MONTH) + 1);

        IProductCmptReference[] productCmptReferences = new ProductCmptReference[] { childProductCmptReference };
        when(structure.getChildProductCmptReferences(cmptReference)).thenReturn(productCmptReferences);
        when(structure.getValidAt()).thenReturn(today);
    }

    protected void setUpWithValidGeneration() {
        setUpBasic();
        when(cmpt.getGenerationEffectiveOn(structure.getValidAt())).thenReturn(productCmptGeneration);
    }

    @Test
    public void testGetValidToFromChild() {
        setUpWithValidGeneration();
        when(productCmptGeneration.getValidTo()).thenReturn(today);
        when(childProductCmptReference.getValidTo()).thenReturn(yesterday);
        assertEquals(yesterday, cmptReference.getValidTo());
    }

    @Test
    public void testGetValidToGenerationIsNull() {
        setUpWithValidGeneration();
        when(productCmptGeneration.getValidTo()).thenReturn(null);
        when(childProductCmptReference.getValidTo()).thenReturn(yesterday);
        assertEquals(yesterday, cmptReference.getValidTo());
    }

    @Test
    public void testGetValidToChildIsNull() {
        setUpWithValidGeneration();
        when(productCmptGeneration.getValidTo()).thenReturn(today);
        when(childProductCmptReference.getValidTo()).thenReturn(null);
        assertEquals(today, cmptReference.getValidTo());
    }

    @Test
    public void testGetValidToAllIsNull() {
        setUpWithValidGeneration();
        when(productCmptGeneration.getValidTo()).thenReturn(null);
        when(childProductCmptReference.getValidTo()).thenReturn(null);
        assertNull(cmptReference.getValidTo());
    }

    @Test
    public void testGetValidToChildAfter() {
        setUpWithValidGeneration();
        when(productCmptGeneration.getValidTo()).thenReturn(today);
        when(childProductCmptReference.getValidTo()).thenReturn(tomorow);
        assertEquals(today, cmptReference.getValidTo());
    }

    @Test
    public void testGetValidToWithoutGeneration() {
        setUpBasic();
        when(cmpt.getGenerationEffectiveOn(structure.getValidAt())).thenReturn(null);
        when(cmptReference.getValidTo()).thenReturn(today);

        assertEquals(today, cmptReference.getValidTo());
    }

    @Test
    public void testGetValidToWithoutGenerationInChildren() {
        setUpWithValidGeneration();
        when(childCmpt.getGenerationEffectiveOn(structure.getValidAt())).thenReturn(null);
        when(childProductCmptReference.getValidTo()).thenReturn(today);

        assertEquals(today, cmptReference.getValidTo());
    }
}
