/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class IpsObjectPartChangeRefreshHelperTest {

    @Mock
    private ContentChangeEvent event;
    private IpsObjectPartChangeRefreshHelper helper;
    @Mock
    private IpsModel ipsModel;
    @Mock
    private IIpsObject ipsObject;
    @Mock
    private Viewer viewer;
    @Mock
    private Control control;
    private static SingletonMockHelper singletonHelper = new SingletonMockHelper();

    @Before
    public void setUp() throws Exception {
        singletonHelper.setSingletonInstance(IpsModel.class, ipsModel);
        when(viewer.getControl()).thenReturn(control);

        helper = new IpsObjectPartChangeRefreshHelper(ipsObject, viewer);
        // no init(), deliberately
    }

    @Test
    public void testRefreshIfSelectedPartChanged() throws Exception {
        when(event.isAffected(ipsObject)).thenReturn(true);

        helper.handleEvent(event);

        verify(viewer).refresh();
    }

    @Test
    public void testNotRefreshIfPartNotAffected() throws Exception {
        when(event.isAffected(ipsObject)).thenReturn(false);

        helper.handleEvent(event);

        verify(viewer, never()).refresh();
    }

    @Test
    public void testInit() throws Exception {
        helper.init();
        verify(ipsModel).addChangeListener(any(ContentsChangeListener.class));
    }

    @Test
    public void testDispose() throws Exception {
        helper.init();

        helper.dispose();

        verify(ipsModel).removeChangeListener(any(ContentsChangeListener.class));
    }

    @Test
    public void testCreateAndInit_nullArgument() throws Exception {
        assertNull(IpsObjectPartChangeRefreshHelper.createAndInit(null, null));
        assertNull(IpsObjectPartChangeRefreshHelper.createAndInit(ipsObject, null));
        assertNull(IpsObjectPartChangeRefreshHelper.createAndInit(null, viewer));
    }

    @Test
    public void testCreateAndInit() throws Exception {
        assertNotNull(IpsObjectPartChangeRefreshHelper.createAndInit(ipsObject, viewer));
    }

    @AfterClass
    public static void tearDown() {
        singletonHelper.reset();
    }

}
