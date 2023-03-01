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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SelectionProviderIntermediateTest {

    @Test
    public void testSetSelectionProviderDelegate() throws Exception {
        SelectionProviderIntermediate intermediate = new SelectionProviderIntermediate();

        ISelection selection = mock(ISelection.class);
        ISelectionChangedListener selectionListener = mock(ISelectionChangedListener.class);
        ISelectionChangedListener postSelectionListener = mock(ISelectionChangedListener.class);

        intermediate.addSelectionChangedListener(selectionListener);
        intermediate.addPostSelectionChangedListener(postSelectionListener);

        ISelectionProvider newDelegate = mock(ISelectionProvider.class);
        when(newDelegate.getSelection()).thenReturn(selection);

        intermediate.setSelectionProviderDelegate(newDelegate);

        ArgumentCaptor<ISelectionChangedListener> intermediateChangeListenerCapture = ArgumentCaptor
                .forClass(ISelectionChangedListener.class);
        ArgumentCaptor<ISelectionChangedListener> intermediatePostChangeListenerCapture = ArgumentCaptor
                .forClass(ISelectionChangedListener.class);

        verify(selectionListener).selectionChanged(any(SelectionChangedEvent.class));
        verify(postSelectionListener).selectionChanged(any(SelectionChangedEvent.class));
        verify(newDelegate).addSelectionChangedListener(intermediateChangeListenerCapture.capture());
        verify(newDelegate, times(2)).getSelection();
        verifyNoMoreInteractions(newDelegate);
        assertEquals(selection, intermediate.getSelection());

        // set new delegate
        reset(selectionListener);
        reset(postSelectionListener);
        reset(newDelegate);
        reset(selection);

        IPostSelectionProvider newPostDelegate = mock(IPostSelectionProvider.class);
        ISelection postSelection = mock(ISelection.class);
        when(newPostDelegate.getSelection()).thenReturn(postSelection);

        intermediate.setSelectionProviderDelegate(newPostDelegate);

        verify(selectionListener, times(2)).selectionChanged(any(SelectionChangedEvent.class));
        verify(postSelectionListener, times(2)).selectionChanged(any(SelectionChangedEvent.class));
        verify(newPostDelegate).addSelectionChangedListener(intermediateChangeListenerCapture.getValue());
        verify(newPostDelegate).addPostSelectionChangedListener(intermediatePostChangeListenerCapture.capture());
        verify(newPostDelegate, times(2)).getSelection();
        verifyNoMoreInteractions(newPostDelegate);
        assertEquals(postSelection, intermediate.getSelection());

        verify(newDelegate).removeSelectionChangedListener(intermediateChangeListenerCapture.getValue());
        verifyNoMoreInteractions(newDelegate);

        // set no delegate
        reset(selectionListener);
        reset(postSelectionListener);
        reset(newDelegate);
        reset(newPostDelegate);
        reset(selection);

        intermediate.setSelectionProviderDelegate(null);
        verify(newPostDelegate).removeSelectionChangedListener(intermediateChangeListenerCapture.getValue());
        verify(newPostDelegate).removePostSelectionChangedListener(intermediatePostChangeListenerCapture.getValue());
        verifyNoMoreInteractions(newPostDelegate);
    }

    @Test
    public void testSelectionChanged() throws Exception {
        SelectionProviderIntermediate intermediate = new SelectionProviderIntermediate();

        ISelection selection = mock(ISelection.class);
        ISelectionChangedListener selectionListener = mock(ISelectionChangedListener.class);
        ISelectionChangedListener postSelectionListener = mock(ISelectionChangedListener.class);

        IPostSelectionProvider newDelegate = mock(IPostSelectionProvider.class);
        when(newDelegate.getSelection()).thenReturn(selection);

        intermediate.setSelectionProviderDelegate(newDelegate);

        intermediate.addSelectionChangedListener(selectionListener);
        intermediate.addPostSelectionChangedListener(postSelectionListener);

        intermediate.fireSelectionChanged(selection);

        ArgumentCaptor<SelectionChangedEvent> eventCapture = ArgumentCaptor.forClass(SelectionChangedEvent.class);
        ArgumentCaptor<SelectionChangedEvent> postEventCapture = ArgumentCaptor.forClass(SelectionChangedEvent.class);

        verify(selectionListener).selectionChanged(eventCapture.capture());
        assertEquals(selection, eventCapture.getValue().getSelection());
        verifyNoInteractions(postSelectionListener);

        reset(selectionListener);

        intermediate.firePostSelectionChanged(selection);

        verify(postSelectionListener).selectionChanged(postEventCapture.capture());
        assertEquals(selection, eventCapture.getValue().getSelection());
        verifyNoInteractions(selectionListener);
    }
}
