/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;


/**
 *
 */
public abstract class PctEditorPage extends IpsObjectEditorPage {

    /**
     * @param editor
     * @param id
     * @param title
     */
    public PctEditorPage(PctEditor editor, String id, String tabPageName) {
        super(editor, id, tabPageName);
    }

    PctEditor getPctEditor() {
        return (PctEditor)getEditor();
    }
    
    IPolicyCmptType getPolicyCmptType() {
        return getPctEditor().getPolicyCmptType(); 
    }

}
