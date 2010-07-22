/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Application for Faktor-IPS to be used with eclipse. Provides reduced functionality in comparison
 * with the use as plug-in within an eclipse running the IDE-Product. Used for department-workers.
 * 
 * @author Thorsten Guenther
 */
public class IpsApplication implements IPlatformRunnable {

    @Override
    public Object run(Object args) throws Exception {
        Display display = PlatformUI.createDisplay();
        Integer retValue = new Integer(PlatformUI.createAndRunWorkbench(display, new IpsWorkbenchAdvisor()));
        return retValue;
    }

}
