/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.ui.actions.IpsAction;

/**
 * Abstract base class for the actions used by the <code>EnumValuesSection</code>.
 * 
 * @see EnumValuesSection
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public abstract class AbstractEnumAction extends IpsAction {

    /** The table viewer linking the ui table widget with the model data */
    protected TableViewer tableViewer;

    /**
     * Creates the enum action.
     * 
     * @param tableViewer The table viewer linking the ui table widget with the model data.
     */
    public AbstractEnumAction(TableViewer tableViewer) {
        super(tableViewer);
        this.tableViewer = tableViewer;
    }

}
