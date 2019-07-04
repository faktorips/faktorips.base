/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewTableContentsOperationTest extends AbstractIpsPluginTest {

    @Mock
    private IProgressMonitor monitor;

    private IIpsProject ipsProject;

    private TableContents tableContents;

    private NewTableContentsPMO pmo = new NewTableContentsPMO();

    @Override
    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        ipsProject = newIpsProject();

        tableContents = newTableContents(ipsProject, "TestTableContent");
        tableContents.getIpsSrcFile().save(true, null);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        IpsPlugin.getDefault().getIpsPreferences().setWorkingMode(IpsPreferences.WORKING_MODE_EDIT);
    }

    @Test
    public void testFinishIpsSrcFile_setsTableStructure() throws CoreException {
        pmo.setSelectedStructure(newTableStructure(ipsProject, "TestTableStructure"));
        pmo.setEffectiveDate(new GregorianCalendar(2013, 0, 1));
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.finishIpsSrcFile(tableContents.getIpsSrcFile(), monitor);

        assertEquals("TestTableStructure", tableContents.getTableStructure());
        assertEquals(0, tableContents.getNumOfColumns());
    }

    @Test
    public void testFinishIpsSrcFile_createsColumnsForTableStructureColumns() throws CoreException {
        TableStructure tableStructure = newTableStructure(ipsProject, "TestTableStructure");
        pmo.setSelectedStructure(tableStructure);
        tableStructure.newColumn();
        tableStructure.newColumn();
        pmo.setEffectiveDate(new GregorianCalendar(2013, 0, 1));
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.finishIpsSrcFile(tableContents.getIpsSrcFile(), monitor);

        assertEquals("TestTableStructure", tableContents.getTableStructure());
        assertEquals(2, tableContents.getNumOfColumns());
    }

    @Test
    public void testFinishIpsSrcFile_setsValidFromForFirstGeneration() throws CoreException {
        pmo.setSelectedStructure(newTableStructure(ipsProject, "TestTableStructure"));
        pmo.setEffectiveDate(new GregorianCalendar(2013, 0, 1));
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.finishIpsSrcFile(tableContents.getIpsSrcFile(), monitor);

        ITableRows generation = tableContents.getTableRows();
        assertNotNull(generation);
    }

    @Test
    public void testFinishIpsSrcFile_createsNewRowIfOpenEditorIsSet() throws CoreException {
        TableStructure tableStructure = newTableStructure(ipsProject, "TestTableStructure");
        pmo.setSelectedStructure(tableStructure);
        pmo.setEffectiveDate(new GregorianCalendar(2013, 0, 1));
        pmo.setOpenEditor(true);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.finishIpsSrcFile(tableContents.getIpsSrcFile(), monitor);

        assertEquals("TestTableStructure", tableContents.getTableStructure());
        ITableRows generation = tableContents.getTableRows();
        assertEquals(1, generation.getNumOfRows());
    }

    @Test
    public void testFinishIpsSrcFile_createsNoNewRowIfOpenEditorIsNotSet() throws CoreException {
        TableStructure tableStructure = newTableStructure(ipsProject, "TestTableStructure");
        pmo.setSelectedStructure(tableStructure);
        pmo.setEffectiveDate(new GregorianCalendar(2013, 0, 1));
        pmo.setOpenEditor(false);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.finishIpsSrcFile(tableContents.getIpsSrcFile(), monitor);

        assertEquals("TestTableStructure", tableContents.getTableStructure());
        ITableRows generation = tableContents.getTableRows();
        assertEquals(0, generation.getNumOfRows());
    }

    @Test
    public void testPostProcess_doesNothingIfTableUsageNull() {
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.postProcess(tableContents.getIpsSrcFile(), monitor);

        verify(monitor, never()).worked(anyInt());
    }

    @Test
    public void testPostProcess_doesNothingIfTableUsageNotEditable() throws CoreException {
        ITableContentUsage tableUsage = mockTableUsage(false);
        pmo = mockPMO(tableUsage, true);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);
        IpsPlugin.getDefault().getIpsPreferences().setWorkingMode(IpsPreferences.WORKING_MODE_BROWSE);

        newTableContentsOperation.postProcess(tableContents.getIpsSrcFile(), monitor);

        verify(tableUsage, never()).setTableContentName(anyString());
    }

    @Test
    public void testPostProcess_setsTableContentNameOnProductCmptGeneration() throws CoreException {
        ITableContentUsage tableUsage = mockTableUsage(false);
        pmo = mockPMO(tableUsage, true);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.postProcess(tableContents.getIpsSrcFile(), monitor);

        verify(tableUsage).setTableContentName("TestTableContent");
    }

    @Test
    public void testPostProcess_setsTableContentNameOnProductCmpt() throws CoreException {
        ITableContentUsage tableUsage = mock(ITableContentUsage.class);
        IPropertyValueContainer propertyValueContainer = mock(IPropertyValueContainer.class);
        when(tableUsage.getPropertyValueContainer()).thenReturn(propertyValueContainer);
        when(propertyValueContainer.getIpsSrcFile()).thenReturn(newProductCmpt().getIpsSrcFile());
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(tableUsage.getIpsSrcFile()).thenReturn(ipsSrcFile);
        pmo = mockPMO(tableUsage, true);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.postProcess(tableContents.getIpsSrcFile(), monitor);

        verify(tableUsage).setTableContentName("TestTableContent");
    }

    private IProductCmpt newProductCmpt() throws CoreException {
        IProductCmpt productCmpt = newProductCmpt(ipsProject, "TestProductCmptNoGeneration");
        return productCmpt;
    }

    @Test
    public void testPostProcess_savesTableUsageIfNotDirtyAndAutosafeSet() throws CoreException {
        ITableContentUsage tableUsage = mockTableUsage(false);
        pmo = mockPMO(tableUsage, true);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.postProcess(tableContents.getIpsSrcFile(), monitor);

        verify(tableUsage.getIpsSrcFile()).save(true, monitor);
    }

    @Test
    public void testPostProcess_doesNotSaveTableUsageIfNotDirtyAndAutosafeNotSet() throws CoreException {
        ITableContentUsage tableUsage = mockTableUsage(false);
        pmo = mockPMO(tableUsage, false);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.postProcess(tableContents.getIpsSrcFile(), monitor);

        verify(tableUsage.getIpsSrcFile(), never()).save(true, monitor);
    }

    @Test
    public void testPostProcess_doesNotSaveTableUsageIfDirty() throws CoreException {
        ITableContentUsage tableUsage = mockTableUsage(true);
        pmo = mockPMO(tableUsage, true);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.postProcess(tableContents.getIpsSrcFile(), monitor);

        verify(tableUsage.getIpsSrcFile(), never()).save(true, monitor);
    }

    @Test(expected = CoreRuntimeException.class)
    public void testPostProcess_throwsCoreRuntimeExceptionIfSavingThrowsCoreException() throws CoreException {
        ITableContentUsage tableUsage = mockTableUsage(true);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.isDirty()).thenReturn(false);
        doThrow(new CoreException(new Status(IStatus.ERROR, "foo", "bar"))).when(ipsSrcFile).save(true, monitor);
        when(tableUsage.getIpsSrcFile()).thenReturn(ipsSrcFile);
        pmo = mockPMO(tableUsage, true);
        NewTableContentsOperation newTableContentsOperation = new NewTableContentsOperation(pmo);

        newTableContentsOperation.postProcess(tableContents.getIpsSrcFile(), monitor);
    }

    private IProductCmptGeneration newProductCmptGeneration() throws CoreException {
        IProductCmpt productCmpt = newProductCmpt(ipsProject, "TestProductCmpt");
        IProductCmptGeneration productCmptGeneration = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2013, 0, 1));
        return productCmptGeneration;
    }

    private NewTableContentsPMO mockPMO(ITableContentUsage tableUsage, boolean autosafe) {
        NewTableContentsPMO mockPmo = mock(NewTableContentsPMO.class);
        when(mockPmo.getAddToTableUsage()).thenReturn(tableUsage);
        when(mockPmo.isAutoSaveAddToFile()).thenReturn(autosafe);
        return mockPmo;
    }

    private ITableContentUsage mockTableUsage(boolean dirty) throws CoreException {
        ITableContentUsage tableUsage = mock(ITableContentUsage.class);
        IPropertyValueContainer propertyValueContainer = mock(IPropertyValueContainer.class);
        when(tableUsage.getPropertyValueContainer()).thenReturn(propertyValueContainer);
        when(propertyValueContainer.getIpsSrcFile()).thenReturn(newProductCmptGeneration().getIpsSrcFile());
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.isDirty()).thenReturn(dirty);
        when(tableUsage.getIpsSrcFile()).thenReturn(ipsSrcFile);
        return tableUsage;
    }
}
