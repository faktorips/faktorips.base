/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;

/**
 * 
 * @author Jan Ortmann
 */
public class TestContentsChangeListener implements ContentsChangeListener {

    private int count;

    private ContentChangeEvent lastEvent;

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        this.lastEvent = event;
        count++;

    }

    public int getNumOfEventsReceived() {
        return count;
    }

    public ContentChangeEvent getLastEvent() {
        return lastEvent;
    }

}
