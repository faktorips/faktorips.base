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

package org.faktorips.devtools.stdbuilder.xpand.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XAttributeTest {

    @Mock
    private ModelService modelService;
    @Mock
    private GeneratorModelContext modelContext;
    @Mock
    private IPolicyCmptTypeAttribute attribute;
    @Mock
    private IIpsProject ipsProject;

    @Test
    public void getJavaClassName_primitive() {
        XPolicyAttribute xAttribute = setUpXAttributeWithDatatypeHelper(DatatypeHelper.PRIMITIVE_INTEGER);

        String javaClassName = xAttribute.getJavaClassName();
        verify(xAttribute, never()).addImport(anyString());
        assertEquals("int", javaClassName);
    }

    @Test
    public void getJavaClassName_type() {
        XPolicyAttribute xAttribute = setUpXAttributeWithDatatypeHelper(DatatypeHelper.DECIMAL);

        String javaClassName = xAttribute.getJavaClassName();
        verify(xAttribute).addImport("org.faktorips.values.Decimal");
        assertEquals("Decimal", javaClassName);
    }

    private XPolicyAttribute setUpXAttributeWithDatatypeHelper(DatatypeHelper helper) {
        try {
            when(attribute.getIpsProject()).thenReturn(ipsProject);
            when(ipsProject.findDatatypeHelper(anyString())).thenReturn(helper);
            XPolicyAttribute xAttribute = spy(new XPolicyAttribute(attribute, modelContext, modelService));
            return xAttribute;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
