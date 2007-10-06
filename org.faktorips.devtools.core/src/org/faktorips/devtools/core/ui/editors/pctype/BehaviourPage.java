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

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.type.MethodsSection;


/**
 *
 */
public class BehaviourPage extends PctEditorPage {
    
    final static String PAGE_ID = "BehaviourPage"; //$NON-NLS-1$
    
    public BehaviourPage(PctEditor editor) {
        super(editor, PAGE_ID, Messages.BehaviourPage_title); 
    }

    /**
     * {@inheritDoc}
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		formBody.setLayout(createPageLayout(2, true));
		new MethodsSection(getPolicyCmptType(), formBody, toolkit);
		new RulesSection(getPolicyCmptType(), formBody, toolkit);
    }

}
