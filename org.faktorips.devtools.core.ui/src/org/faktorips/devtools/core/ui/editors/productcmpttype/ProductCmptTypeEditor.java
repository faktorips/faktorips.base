/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) d�rfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung � Version 0.1 (vor Gr�ndung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;


/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeEditor extends TypeEditor {


    /** 
     * {@inheritDoc}
     */
    protected String getUniformPageTitle() {
        return Messages.ProductCmptTypeEditor_title + getIpsObject().getName();
    }

    /**
     * {@inheritDoc}
     */
    protected void addAllInOneSinglePage() throws PartInitException {
        addPage(new StructurePage(this, false));
    }

    /**
     * {@inheritDoc}
     */
    protected void addSplittedInMorePages() throws PartInitException {
        addPage(new StructurePage(this, true));
        addPage(new BehaviourPage(this));
    }
}
