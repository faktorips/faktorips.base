/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.internal.InternationalString;
import org.faktorips.values.LocalizedString;
import org.junit.Test;

public class InternationalStringPresentationObjectTest {

    @Test
    public void shouldNotifyListener() throws Exception {
        IInternationalString internationalString = spy(new InternationalString());

        InternationalStringPresentationObject presentationObject = new InternationalStringPresentationObject(
                internationalString);

        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        presentationObject.addPropertyChangeListener(listener);

        Locale german = Locale.GERMAN;
        presentationObject.setLocale(german);
        verify(listener).propertyChange(any(PropertyChangeEvent.class));

        reset(listener);

        String text = "anyText";
        presentationObject.setText(text);
        verify(internationalString).add(new LocalizedString(german, text));
        verify(listener).propertyChange(any(PropertyChangeEvent.class));

        reset(listener);

        Locale english = Locale.ENGLISH;
        String englishText = "englishText";
        presentationObject.setLocale(english);
        presentationObject.setText(englishText);
        verify(internationalString).add(new LocalizedString(english, englishText));
        verify(listener, times(2)).propertyChange(any(PropertyChangeEvent.class));
    }

}
