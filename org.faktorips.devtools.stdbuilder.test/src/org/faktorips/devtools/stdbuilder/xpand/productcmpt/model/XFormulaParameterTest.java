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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XFormulaParameterTest {

    @Mock
    private IParameter parameter;
    @Mock
    private GeneratorModelContext context;
    @Mock
    private ModelService modelService;
    @Mock
    private IIpsProject ipsProject;

    @Mock
    private PolicyCmptType polCmptType;
    @Mock
    private ProductCmptType prodCmptType;
    @Mock
    private XClass xClass;

    private XFormulaParameter xParameter;

    @Before
    public void setUp() {
        xParameter = new XFormulaParameter(parameter, context, modelService);
    }

    @Test
    public void returnVoidForVoidDatatype() {
        xParameter = spy(xParameter);
        doReturn(Datatype.VOID).when(xParameter).getDatatype();
        String className = xParameter.getTypeClassName();

        assertEquals("void", className);
        verify(xParameter, never()).addImport(anyString());
    }

    @Test
    public void returnValueClassNameForValueDatatype() {
        xParameter = spy(xParameter);
        doReturn(Datatype.DECIMAL).when(xParameter).getDatatype();
        doReturn("").when(xParameter).getClassNameForDatatypeOrThrowException(any(Datatype.class));

        xParameter.getTypeClassName();
        verify(xParameter).getClassNameForDatatypeOrThrowException(Datatype.DECIMAL);
        verify(xParameter).addImport(anyString());
    }

    @Test
    public void returnValueClassNameForValueDatatype2() throws CoreException {
        xParameter = spy(xParameter);
        doReturn(Datatype.DECIMAL).when(xParameter).getDatatype();

        doReturn(ipsProject).when(xParameter).getIpsProject();
        when(ipsProject.findDatatypeHelper(anyString())).thenReturn(DatatypeHelper.DECIMAL);

        String typeClassName = xParameter.getTypeClassName();
        assertEquals("Decimal", typeClassName);
        verify(xParameter).addImport("org.faktorips.values.Decimal");
    }

    @Test
    public void returnValueClassNameForPolicyCmptType() {
        xParameter = spy(xParameter);
        doReturn(polCmptType).when(xParameter).getDatatype();

        when(xClass.getQualifiedName(any(BuilderAspect.class))).thenReturn("polQName");
        when(modelService.getModelNode(polCmptType, XClass.class, context)).thenReturn(xClass);

        xParameter.getTypeClassName();
        verify(xClass).getQualifiedName(any(BuilderAspect.class));
        verify(xParameter).addImport("polQName");
    }

    @Test
    public void returnValueClassNameForProductCmptType() {
        xParameter = spy(xParameter);
        doReturn(prodCmptType).when(xParameter).getDatatype();

        when(xClass.getQualifiedName(any(BuilderAspect.class))).thenReturn("prodQName");
        when(modelService.getModelNode(prodCmptType, XClass.class, context)).thenReturn(xClass);

        xParameter.getTypeClassName();
        verify(xClass).getQualifiedName(any(BuilderAspect.class));
        verify(xParameter).addImport("prodQName");
    }
}
