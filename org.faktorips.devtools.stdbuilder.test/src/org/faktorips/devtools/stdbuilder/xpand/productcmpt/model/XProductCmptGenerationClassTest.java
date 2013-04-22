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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XProductCmptGenerationClassTest {

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IProductCmptType superType;

    @Mock
    private IProductCmptType superSuperType;

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private IIpsProject ipsProject;

    private XProductCmptGenerationClass xProductCmptGenerationClass;

    @Before
    public void initMocks() throws CoreException {
        when(productCmptType.getIpsProject()).thenReturn(ipsProject);
        when(productCmptType.findSupertype(ipsProject)).thenReturn(superType);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);
    }

    @Before
    public void createXProductCmptGenerationClass() throws Exception {
        xProductCmptGenerationClass = new XProductCmptGenerationClass(productCmptType, modelContext, modelService);
    }

    @Test
    public void testGetClassHierarchy_productCmptGenerationClass() throws Exception {
        XProductCmptGenerationClass xSuperType = mock(XProductCmptGenerationClass.class);
        XProductCmptGenerationClass xSuperSuperType = mock(XProductCmptGenerationClass.class);
        when(modelService.getModelNode(superType, XProductCmptGenerationClass.class, modelContext)).thenReturn(
                xSuperType);
        when(modelService.getModelNode(superSuperType, XProductCmptGenerationClass.class, modelContext)).thenReturn(
                xSuperSuperType);

        Set<XProductCmptGenerationClass> superclasses = xProductCmptGenerationClass.getClassHierarchy();
        assertEquals(3, superclasses.size());
        assertThat(superclasses, hasItems(xSuperType, xSuperSuperType));
    }

    @Test
    public void testOptionalFormulars() {

        final IProductCmptTypeMethod superMandatoryMethod = mock(IProductCmptTypeMethod.class);
        when(superMandatoryMethod.isFormulaOptional()).thenReturn(false);
        when(superMandatoryMethod.isOverloadsFormula()).thenReturn(false);

        final IProductCmptTypeMethod superOptionalMethod = mock(IProductCmptTypeMethod.class);
        when(superOptionalMethod.isFormulaOptional()).thenReturn(true);
        when(superOptionalMethod.isOverloadsFormula()).thenReturn(false);

        final IProductCmptTypeMethod optionalOverloadedMethod = mock(IProductCmptTypeMethod.class);
        when(optionalOverloadedMethod.isFormulaOptional()).thenReturn(true);
        when(optionalOverloadedMethod.isOverloadsFormula()).thenReturn(true);

        XProductCmptGenerationClass xSuperType = new XProductCmptGenerationClass(superType, modelContext, modelService) {

            @Override
            public Set<XMethod> getMethods() {
                Set<XMethod> methods = new HashSet<XMethod>(Arrays.asList(new XMethod(superOptionalMethod,
                        modelContext, modelService), new XMethod(superMandatoryMethod, modelContext, modelService),
                        new XMethod(optionalOverloadedMethod, modelContext, modelService)));
                return methods;
            }

        };

        Set<XMethod> optionalFormulas = xSuperType.getOptionalFormulas();

        assertEquals(1, optionalFormulas.size());
        XMethod method = optionalFormulas.iterator().next();
        assertEquals(superOptionalMethod, method.getMethod());
    }
}
