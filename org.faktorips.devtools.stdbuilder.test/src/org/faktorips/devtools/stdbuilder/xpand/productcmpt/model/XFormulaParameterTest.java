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
        setUpXParameterWithDatatype(Datatype.VOID);
        String className = xParameter.getTypeClassName();

        assertEquals("void", className);
        verify(xParameter, never()).addImport(anyString());
    }

    @Test
    public void returnValueClassNameForValueDatatype() {
        setUpXParameterWithDatatype(Datatype.DECIMAL);
        doReturn("").when(xParameter).getClassNameForDatatypeOrThrowException(any(Datatype.class));

        xParameter.getTypeClassName();
        verify(xParameter).getClassNameForDatatypeOrThrowException(Datatype.DECIMAL);
        verify(xParameter).addImport(anyString());
    }

    @Test
    public void returnValueClassNameForValueDatatype2() throws CoreException {
        setUpXParameterWithDatatype(Datatype.DECIMAL);

        doReturn(ipsProject).when(xParameter).getIpsProject();
        when(ipsProject.findDatatypeHelper(anyString())).thenReturn(DatatypeHelper.DECIMAL);

        String typeClassName = xParameter.getTypeClassName();
        assertEquals("Decimal", typeClassName);
        verify(xParameter).addImport("org.faktorips.values.Decimal");
    }

    @Test
    public void returnValueClassNameForPolicyCmptType() {
        setUpXParameterWithDatatype(polCmptType);

        when(xClass.getQualifiedName(any(BuilderAspect.class))).thenReturn("package.polQName");
        when(modelService.getModelNode(polCmptType, XClass.class, context)).thenReturn(xClass);

        String typeClassName = xParameter.getTypeClassName();
        verify(xClass).getQualifiedName(any(BuilderAspect.class));
        verify(xParameter).addImport("package.polQName");
        assertEquals("polQName", typeClassName);
    }

    @Test
    public void returnValueClassNameForProductCmptType() {
        setUpXParameterWithDatatype(prodCmptType);

        when(xClass.getQualifiedName(any(BuilderAspect.class))).thenReturn("package.prodQName");
        when(modelService.getModelNode(prodCmptType, XClass.class, context)).thenReturn(xClass);

        String typeClassName = xParameter.getTypeClassName();
        verify(xClass).getQualifiedName(any(BuilderAspect.class));
        verify(xParameter).addImport("package.prodQName");
        assertEquals("prodQName", typeClassName);
    }

    private void setUpXParameterWithDatatype(Datatype datatype) {
        xParameter = spy(xParameter);
        doReturn(datatype).when(xParameter).getDatatype();
    }
}
