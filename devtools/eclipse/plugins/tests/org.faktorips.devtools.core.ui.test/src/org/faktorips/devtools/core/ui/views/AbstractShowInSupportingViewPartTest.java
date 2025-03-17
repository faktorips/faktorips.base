/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.ShowInContext;
import org.faktorips.devtools.core.ui.editors.IpsArchiveEditorInput;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.junit.Before;
import org.junit.Test;

public class AbstractShowInSupportingViewPartTest {

    private AbstractShowInSupportingViewPart viewPart;

    @Before
    public void setUp() {
        viewPart = new AbstractShowInSupportingViewPart() {

            @Override
            public void setFocus() {
                // nothing to do
            }

            @Override
            public void createPartControl(Composite parent) {
                // nothing to do
            }

            @Override
            protected boolean show(IAdaptable adaptable) {
                return true;
            }

            @Override
            protected ISelection getSelection() {
                return null;
            }
        };
    }

    @Test
    public void testShowSelectionInContext() {
        IStructuredSelection selection = mock(IStructuredSelection.class);
        Object object = new Object();
        IAdaptable adaptable = mock(IAdaptable.class);
        IAdaptable[] adaptables = { adaptable };
        when(selection.getFirstElement()).thenReturn(object, adaptable, adaptables);

        ShowInContext context = new ShowInContext(null, selection);

        assertFalse(viewPart.show(context));
        assertTrue(viewPart.show(context));
        assertTrue(viewPart.show(context));
    }

    @Test
    public void testShowFileEditorInputInContext() {

        IFileEditorInput input = mock(IFileEditorInput.class);
        IFile file = mock(IFile.class);
        when(input.getFile()).thenReturn(null, file);

        ShowInContext context = new ShowInContext(input, null);

        assertFalse(viewPart.show(context));
        assertTrue(viewPart.show(context));
    }

    @Test
    public void testShowIpsArchiveEditorInputInContext() {

        IpsArchiveEditorInput input = mock(IpsArchiveEditorInput.class);
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(input.getIpsSrcFile()).thenReturn(null, srcFile);

        ShowInContext context = new ShowInContext(input, null);

        assertFalse(viewPart.show(context));
        assertTrue(viewPart.show(context));
    }

    @Test
    public void testShowNothingDefinedInContext() {
        ISelection selection = mock(ISelection.class);
        IEditorInput input = mock(IEditorInput.class);

        ShowInContext context = new ShowInContext(input, selection);

        assertFalse(viewPart.show(context));
    }
}
