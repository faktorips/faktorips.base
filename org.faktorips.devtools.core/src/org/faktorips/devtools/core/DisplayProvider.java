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

package org.faktorips.devtools.core;

import org.eclipse.swt.widgets.Display;

/**
 * Provides access to the display.
 * <p>
 * This class is especially designed for projects that do not want to depend on eclipse.ui but
 * nevertheless need to run operations in the user-interface thread.
 * 
 * @author Alexander Weickmann
 */
public final class DisplayProvider {

    public static Display getDisplay() {
        return Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
    }

    public static void syncExec(Runnable runnable) {
        getDisplay().syncExec(runnable);
    }

    private DisplayProvider() {
        // Prohibit instantiation
    }

}
