/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableexport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableExportPageTest {
    @Mock
    private IStructuredSelection selection;
    @Mock
    private TableContentsRefControl exportedIpsObjectControl;
    @Mock
    private ITableContents tableContents;
    @Mock
    private ITableStructure tableStructure;
    @Mock
    private IIpsProject ipsProject;

    @InjectMocks
    private TableExportPage tableExportPage;

    @Before
    public void setUp() throws Exception {
        Field exportedIpsObjectControlField = IpsObjectExportPage.class.getDeclaredField("exportedIpsObjectControl");
        exportedIpsObjectControlField.setAccessible(true);
        exportedIpsObjectControlField.set(tableExportPage, exportedIpsObjectControl);
        when(tableContents.getIpsProject()).thenReturn(ipsProject);
        when(tableStructure.getIpsProject()).thenReturn(ipsProject);
    }

    @Test
    public void testValidateObjectToExport_ok() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");
        when(exportedIpsObjectControl.findTableContents()).thenReturn(tableContents);
        when(tableContents.exists()).thenReturn(true);
        when(tableContents.findTableStructure(any(IIpsProject.class))).thenReturn(tableStructure);
        when(tableStructure.exists()).thenReturn(true);
        when(tableStructure.validate(any(IIpsProject.class))).thenReturn(new MessageList());

        tableExportPage.validateObjectToExport();

        assertThat(tableExportPage.getErrorMessage(), is(nullValue()));
        assertThat(tableExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_noName() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("");

        tableExportPage.validateObjectToExport();

        assertThat(tableExportPage.getErrorMessage(), is(Messages.TableExportPage_msgContentsEmpty));
        assertThat(tableExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_noTableContents() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");

        tableExportPage.validateObjectToExport();

        assertThat(tableExportPage.getErrorMessage(), is(Messages.TableExportPage_msgNonExisitingContents));
        assertThat(tableExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_tableContentsDoesNotExist() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");
        when(exportedIpsObjectControl.findTableContents()).thenReturn(tableContents);

        tableExportPage.validateObjectToExport();

        assertThat(tableExportPage.getErrorMessage(), is(Messages.TableExportPage_msgNonExisitingContents));
        assertThat(tableExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_noTableStructure() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");
        when(exportedIpsObjectControl.findTableContents()).thenReturn(tableContents);
        when(tableContents.exists()).thenReturn(true);

        tableExportPage.validateObjectToExport();

        assertThat(tableExportPage.getErrorMessage(), is(Messages.TableExportPage_msgNonExisitingStructure));
        assertThat(tableExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_noTableStructureExists() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");
        when(exportedIpsObjectControl.findTableContents()).thenReturn(tableContents);
        when(tableContents.exists()).thenReturn(true);
        when(tableContents.findTableStructure(any(IIpsProject.class))).thenReturn(tableStructure);

        tableExportPage.validateObjectToExport();

        assertThat(tableExportPage.getErrorMessage(), is(Messages.TableExportPage_msgNonExisitingStructure));
        assertThat(tableExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_structureInvalid() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");
        when(exportedIpsObjectControl.findTableContents()).thenReturn(tableContents);
        when(tableContents.exists()).thenReturn(true);
        when(tableContents.findTableStructure(ipsProject)).thenReturn(tableStructure);
        when(tableStructure.exists()).thenReturn(true);
        when(tableStructure.validate(ipsProject)).thenReturn(
                new MessageList(Message.newError("foo", "bar")));

        tableExportPage.validateObjectToExport();

        assertThat(tableExportPage.getErrorMessage(), is(nullValue()));
        assertThat(tableExportPage.getMessage(), is(Messages.TableExportPage_msgStructureNotValid));
        assertThat(tableExportPage.getMessageType(), is(IMessageProvider.WARNING));
    }

    @Test
    public void testValidateObjectToExport_structureInvalidVersionFormat() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");
        when(exportedIpsObjectControl.findTableContents()).thenReturn(tableContents);
        when(tableContents.exists()).thenReturn(true);
        when(tableContents.findTableStructure(any(IIpsProject.class))).thenReturn(tableStructure);
        when(tableStructure.exists()).thenReturn(true);
        when(tableStructure.validate(any(IIpsProject.class))).thenReturn(
                new MessageList(Message.newError(IIpsObjectPartContainer.MSGCODE_INVALID_VERSION_FORMAT, "foobar")));

        tableExportPage.validateObjectToExport();

        assertThat(tableExportPage.getErrorMessage(), is(nullValue()));
        assertThat(tableExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_tooManyColumns() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");
        when(exportedIpsObjectControl.findTableContents()).thenReturn(tableContents);
        when(tableContents.exists()).thenReturn(true);
        when(tableContents.findTableStructure(any(IIpsProject.class))).thenReturn(tableStructure);
        when(tableStructure.exists()).thenReturn(true);
        when(tableStructure.validate(any(IIpsProject.class))).thenReturn(new MessageList());
        when(tableStructure.getNumOfColumns()).thenReturn(1 + Short.MAX_VALUE);

        tableExportPage.validateObjectToExport();

        String msg = NLS
                .bind(org.faktorips.devtools.model.tablecontents.Messages.TableExportOperation_errStructureTooMuchColumns,
                        new Object[] { 1 + Short.MAX_VALUE, "tableStructure", Short.MAX_VALUE });
        assertThat(tableExportPage.getErrorMessage(), is(msg));
        assertThat(tableExportPage.getMessage(), is(nullValue()));
    }

    @Test
    public void testValidateObjectToExport_tooManyColumns_withInvalidStructure() throws Exception {
        when(exportedIpsObjectControl.getText()).thenReturn("MyTable");
        when(exportedIpsObjectControl.findTableContents()).thenReturn(tableContents);
        when(tableContents.exists()).thenReturn(true);
        when(tableContents.findTableStructure(any(IIpsProject.class))).thenReturn(tableStructure);
        when(tableStructure.exists()).thenReturn(true);
        when(tableStructure.validate(any(IIpsProject.class))).thenReturn(
                new MessageList(Message.newError("foo", "bar")));
        when(tableStructure.getNumOfColumns()).thenReturn(1 + Short.MAX_VALUE);

        tableExportPage.validateObjectToExport();

        String msg = NLS
                .bind(org.faktorips.devtools.model.tablecontents.Messages.TableExportOperation_errStructureTooMuchColumns,
                        new Object[] { 1 + Short.MAX_VALUE, "tableStructure", Short.MAX_VALUE });
        assertThat(tableExportPage.getErrorMessage(), is(msg));
        assertThat(tableExportPage.getMessage(), is(Messages.TableExportPage_msgStructureNotValid));
        assertThat(tableExportPage.getMessageType(), is(IMessageProvider.WARNING));
    }

}
