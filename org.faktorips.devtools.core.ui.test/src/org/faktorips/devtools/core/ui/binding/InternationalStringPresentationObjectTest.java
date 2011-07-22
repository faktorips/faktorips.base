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

package org.faktorips.devtools.core.ui.binding;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.IInternationalString;
import org.junit.Test;

public class InternationalStringPresentationObjectTest {

    @Test
    public void shouldNotifyListener() throws Exception {
        IInternationalString internationalString = mock(IInternationalString.class);

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
