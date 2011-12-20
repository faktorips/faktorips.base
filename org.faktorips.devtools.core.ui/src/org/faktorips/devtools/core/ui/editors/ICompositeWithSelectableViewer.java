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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.Viewer;

/**
 * This interface should be implemented by any composite holding a viewer that would provide its
 * selection to the editors selection provider. The {@link IpsObjectEditorPage} would search for
 * composites implementing this interface and register the necessary listeners to the given viewer
 * and its control.
 * 
 * @author dirmeier
 */
public interface ICompositeWithSelectableViewer {

    /**
     * Returns the viewer that would provide its selection to the editors selection provider.
     * 
     */
    public Viewer getViewer();

}
