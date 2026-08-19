/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.scope.IIpsSearchScope;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductSearchQueryTest {

    private static final String PRODUCT_CMPT_TYPE_NAME = "ProductCmptType";

    @Mock
    private ProductSearchPresentationModel model;

    @Mock
    private ProductSearchConditionPresentationModel validCondition;

    @Mock
    private ProductSearchConditionPresentationModel invalidCondition;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IIpsSearchScope scope;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProject ipsProject2;

    @Mock
    private IProductCmptType productCmptType;

    private ProductSearchQuery query;

    @BeforeEach
    public void setUp() {
        lenient().when(validCondition.isValid()).thenReturn(true);
        lenient().when(invalidCondition.isValid()).thenReturn(false);

        lenient().when(model.getSearchScope()).thenReturn(scope);
        lenient().when(model.getProductCmptType()).thenReturn(productCmptType);

        lenient().when(productCmptType.getQualifiedName()).thenReturn(PRODUCT_CMPT_TYPE_NAME);

        lenient().when(ipsModel.getIpsProjects()).thenReturn(new IIpsProject[] { ipsProject, ipsProject2 });

        lenient().when(ipsProject.findProductCmptType(PRODUCT_CMPT_TYPE_NAME)).thenReturn(productCmptType);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsOnlyTypeNameSearch() {
        query = new ProductSearchQuery(model, ipsModel);

        when(model.getProductSearchConditionPresentationModels()).thenReturn(
                new ArrayList<>(), Arrays.asList(invalidCondition),
                Arrays.asList(invalidCondition, validCondition));

        assertTrue(query.isOnlyTypeNameSearch());
        assertTrue(query.isOnlyTypeNameSearch());
        assertFalse(query.isOnlyTypeNameSearch());
    }

    @Test
    public void testGetSelectedSrcFiles() {
        query = new ProductSearchQuery(model, ipsModel);

        IIpsSrcFile wrongObjectType = mock(IIpsSrcFile.class);
        when(wrongObjectType.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsSrcFile wrongProductCmptType = mock(IIpsSrcFile.class);
        when(wrongProductCmptType.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        IIpsSrcFile selectedSrcFile = mock(IIpsSrcFile.class);
        when(selectedSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        Set<IIpsSrcFile> selectedFiles = new HashSet<>(Arrays.asList(wrongObjectType, wrongProductCmptType,
                selectedSrcFile));

        when(scope.getSelectedIpsSrcFiles()).thenReturn(selectedFiles);
        when(ipsProject.findAllProductCmptSrcFiles(productCmptType, true)).thenReturn(
                new IIpsSrcFile[] { selectedSrcFile });

        Set<IIpsSrcFile> selectedSrcFiles = query.getSelectedSrcFiles();

        assertEquals(1, selectedSrcFiles.size());
        assertTrue(selectedSrcFiles.contains(selectedSrcFile));
    }

    @Test
    public void testGetSelectedSrcFilesNoSelectedFiles() {
        query = new ProductSearchQuery(model, ipsModel);

        when(scope.getSelectedIpsSrcFiles()).thenReturn(new HashSet<>());

        Set<IIpsSrcFile> selectedSrcFiles = query.getSelectedSrcFiles();

        assertTrue(selectedSrcFiles.isEmpty());
    }
}
