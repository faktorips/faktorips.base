/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumexport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumExportPageTest {
    @Mock
    private IStructuredSelection selection;
    @Mock
    private EnumRefControl exportedIpsObjectControl;
    @Mock
    private IEnumContent enumContent;
    @Mock
    private IEnumType enumType;

    @InjectMocks
    private EnumExportPage enumExportPage;

    @Before
    public void setUp() throws Exception {
        Field exportedIpsObjectControlField = IpsObjectExportPage.class.getDeclaredField("exportedIpsObjectControl");
        exportedIpsObjectControlField.setAccessible(true);
        exportedIpsObjectControlField.set(enumExportPage, exportedIpsObjectControl);
    }

    @Test
    public void testValidateObjectToExport_ok() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumContent);
        when(enumContent.exists()).thenReturn(true);
        when(enumContent.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(enumContent.findEnumType(any(IIpsProject.class))).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(new MessageList());

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(nullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_noName() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("");

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_noEnumContent() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_noEnumContentExists() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumContent);

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_enumContentInvalid() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumContent);
        when(enumContent.exists()).thenReturn(true);
        when(enumContent.validate(any(IIpsProject.class))).thenReturn(new MessageList(Message.newError("foo", "bar")));

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_enumInvalid() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(new MessageList(Message.newError("foo", "bar")));

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_enumAbstract() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(enumType.isAbstract()).thenReturn(true);

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_enumTypeInvalid() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumContent);
        when(enumContent.exists()).thenReturn(true);
        when(enumContent.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(enumContent.findEnumType(any(IIpsProject.class))).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(new MessageList(Message.newError("foo", "bar")));

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(nullValue()));
        assertThat(enumExportPage.getMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessageType(), is(IMessageProvider.WARNING));
    }

    @Test
    public void testValidateObjectToExport_enumTypeInvalidVersionFormat() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumContent);
        when(enumContent.exists()).thenReturn(true);
        when(enumContent.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(enumContent.findEnumType(any(IIpsProject.class))).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(
                new MessageList(Message.newError(IIpsObjectPartContainer.MSGCODE_INVALID_VERSION_FORMAT, "foobar")));

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(nullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_enumTypeNull() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(new MessageList());

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_enumType_tooManyAttributes() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(enumType.findEnumType(any(IIpsProject.class))).thenReturn(enumType);
        when(enumType.getEnumAttributesCountIncludeSupertypeCopies(anyBoolean())).thenReturn(1 + Short.MAX_VALUE);

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_enumContent_tooManyAttributes() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumContent);
        when(enumContent.exists()).thenReturn(true);
        when(enumContent.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(enumContent.findEnumType(any(IIpsProject.class))).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(enumType.getEnumAttributesCountIncludeSupertypeCopies(anyBoolean())).thenReturn(1 + Short.MAX_VALUE);

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_enumContent_tooManyAttributes_withInvalidEnumType() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyEnum");
        when(exportedIpsObjectControl.findEnum()).thenReturn(enumContent);
        when(enumContent.exists()).thenReturn(true);
        when(enumContent.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(enumContent.findEnumType(any(IIpsProject.class))).thenReturn(enumType);
        when(enumType.exists()).thenReturn(true);
        when(enumType.validate(any(IIpsProject.class))).thenReturn(new MessageList(Message.newError("foo", "bar")));
        when(enumType.getEnumAttributesCountIncludeSupertypeCopies(anyBoolean())).thenReturn(1 + Short.MAX_VALUE);

        enumExportPage.validateObjectToExport();

        assertThat(enumExportPage.getErrorMessage(), is(notNullValue()));
        assertThat(enumExportPage.getMessage(), is(notNullValue()));
    }
}
