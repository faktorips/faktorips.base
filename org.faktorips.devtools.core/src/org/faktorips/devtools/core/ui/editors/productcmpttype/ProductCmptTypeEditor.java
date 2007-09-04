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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;


/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeEditor extends IpsObjectEditor {

    /**
     * {@inheritDoc}
     */
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        addPage(new StructurePage(this));
    }

    /**
     * {@inheritDoc}
     */
    protected String getUniformPageTitle() {
        return null;
    }

}
