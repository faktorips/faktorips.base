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

package org.faktorips.devtools.core.ui.search.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.EqualitySearchOperator;
import org.faktorips.devtools.core.ui.search.product.conditions.types.EqualitySearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperator;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductAttributeConditionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductSearchQueryConditionMatcherTest {

    @Mock
    private ProductSearchPresentationModel searchModel;

    @Mock
    private ISearchOperator searchOperatorHit;

    @Mock
    private ISearchOperator searchOperatorMiss;

    @Mock
    private IIpsSrcFile srcFileHit;

    @Mock
    private IProductCmpt productCmptHit;

    @Mock
    private IIpsSrcFile srcFileGenerationHit;

    @Mock
    private IProductCmpt productCmptGenerationHit;

    @Mock
    private IProductCmptGeneration generationHit;

    @Mock
    private IProductCmptGeneration generationMiss;

    @Mock
    private IIpsSrcFile srcFileMiss;

    @Mock
    private IProductCmpt productCmptMiss;

    private List<ISearchOperator> searchOperators;

    private Set<IIpsSrcFile> matchingFiles;

    @Before
    public void setUp() throws CoreException {
        searchOperators = Arrays.asList(searchOperatorHit);
        matchingFiles = new HashSet<IIpsSrcFile>(Arrays.asList(srcFileMiss, srcFileHit, srcFileGenerationHit));

        when(srcFileHit.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);
        when(srcFileMiss.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);
        when(srcFileGenerationHit.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        when(srcFileHit.getIpsObject()).thenReturn(productCmptHit);
        when(srcFileMiss.getIpsObject()).thenReturn(productCmptMiss);
        when(srcFileGenerationHit.getIpsObject()).thenReturn(productCmptGenerationHit);

        when(productCmptGenerationHit.getProductCmptGenerations()).thenReturn(
                Arrays.asList(generationMiss, generationHit));

        when(searchOperatorHit.check(productCmptHit)).thenReturn(true);
        when(searchOperatorHit.check(generationHit)).thenReturn(true);
        when(searchOperatorHit.check(productCmptMiss)).thenReturn(false);

        when(searchOperatorMiss.check(productCmptHit)).thenReturn(false);
        when(searchOperatorMiss.check(generationHit)).thenReturn(false);
        when(searchOperatorMiss.check(productCmptMiss)).thenReturn(false);
    }

    @Test
    public void testGetResults() throws CoreException {
        ProductSearchQueryConditionMatcher matcher = new ProductSearchQueryConditionMatcher(searchModel,
                searchOperators);

        Set<IProductPartsContainer> results = matcher.getResults(matchingFiles);

        assertEquals(2, results.size());
        assertTrue(results.contains(productCmptHit));
        assertTrue(results.contains(generationHit));
    }

    @Test
    public void testGetResultsAllMisses() throws CoreException {
        searchOperators = Arrays.asList(searchOperatorHit, searchOperatorMiss);

        ProductSearchQueryConditionMatcher matcher = new ProductSearchQueryConditionMatcher(searchModel,
                searchOperators);

        Set<IProductPartsContainer> results = matcher.getResults(matchingFiles);

        assertTrue(results.toString(), results.isEmpty());
    }

    @Test
    public void testSearchOperators() {
        ProductSearchConditionPresentationModel invalidCPM = mock(ProductSearchConditionPresentationModel.class);
        when(invalidCPM.isValid()).thenReturn(false);

        ProductSearchConditionPresentationModel validCPM = mock(ProductSearchConditionPresentationModel.class);
        when(validCPM.isValid()).thenReturn(true);
        when(validCPM.getConditionType()).thenReturn(new ProductAttributeConditionType());
        when(validCPM.getOperatorType()).thenReturn(EqualitySearchOperatorType.EQUALITY);
        IntegerDatatype datatype = new IntegerDatatype();
        when(validCPM.getValueDatatype()).thenReturn(datatype);
        String argumentValue = "argument";
        when(validCPM.getArgument()).thenReturn(argumentValue);

        List<ProductSearchConditionPresentationModel> conditionPresentationModels = Arrays.asList(invalidCPM, validCPM);

        when(searchModel.getProductSearchConditionPresentationModels()).thenReturn(conditionPresentationModels);

        ProductSearchQueryConditionMatcher matcher = new ProductSearchQueryConditionMatcher(searchModel);

        List<ISearchOperator> createdSearchOperators = matcher.getSearchOperators();

        assertEquals(1, createdSearchOperators.size());

        assertTrue(createdSearchOperators.get(0) instanceof EqualitySearchOperator);

        EqualitySearchOperator searchOperator = (EqualitySearchOperator)createdSearchOperators.get(0);
        assertEquals(EqualitySearchOperatorType.EQUALITY, searchOperator.getSearchOperatorType());
        assertEquals(argumentValue, searchOperator.getArgument());
        assertEquals(datatype, searchOperator.getValueDatatype());
    }
}
