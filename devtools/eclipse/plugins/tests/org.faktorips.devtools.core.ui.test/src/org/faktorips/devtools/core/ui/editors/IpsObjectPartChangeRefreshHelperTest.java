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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

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

    private MockitoSession mockito;
    private static SingletonMockHelper singletonHelper = new SingletonMockHelper();

    @BeforeEach
    public void setUp() throws Exception {
        mockito = createMocks(this);
        singletonHelper.setSingletonInstance(IpsModel.class, ipsModel);
        lenient().when(viewer.getControl()).thenReturn(control);

        helper = new IpsObjectPartChangeRefreshHelper(ipsObject, viewer);
        // no init(), deliberately
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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

    @AfterAll
    public static void tearDownAll() {
        singletonHelper.reset();
    }

}
