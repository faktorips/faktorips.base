/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.ICategorisableElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class IpsObjectPartCategoryHelperTest extends AbstractIpsPluginTest {

    private IIpsObjectPart ipsObjectPart;

    private IpsObjectPartCategoryHelper categoryHelper;

    @Override
    @Before
    public void setUp() throws CoreException {
        getIpsModel().addChangeListener(this);

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        ipsObjectPart = productCmptType.newProductCmptTypeAttribute();

        categoryHelper = new IpsObjectPartCategoryHelper(ipsObjectPart);
    }

    @Override
    protected void tearDownExtension() throws Exception {
        getIpsModel().removeChangeListener(this);
    }

    @Test
    public void testSetCategory() {
        categoryHelper.setCategory("foo");
        assertEquals("foo", categoryHelper.getCategory());
        assertPropertyChangedEvent(ipsObjectPart, ICategorisableElement.PROPERTY_CATEGORY, "", "foo");
    }

    @Test
    public void testInitPropertiesFromXml() {
        Element element = mock(Element.class);
        when(element.getAttribute(ICategorisableElement.PROPERTY_CATEGORY)).thenReturn("foo");

        categoryHelper.initPropertiesFromXml(element);

        assertEquals("foo", categoryHelper.getCategory());
    }

    @Test
    public void testPropertiesToXml() {
        Element element = mock(Element.class);
        categoryHelper.setCategory("foo");

        categoryHelper.propertiesToXml(element);

        verify(element).setAttribute(ICategorisableElement.PROPERTY_CATEGORY, "foo");
    }

}
