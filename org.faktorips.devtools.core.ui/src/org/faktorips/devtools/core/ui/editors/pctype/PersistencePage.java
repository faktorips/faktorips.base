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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * 
 * 
 * @author Roman Grutza
 */
public class PersistencePage extends IpsObjectEditorPage {

    private final static String PAGE_ID = "PersistencePage"; //$NON-NLS-1$

    public PersistencePage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, "Persistence");
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, true));
        PersistentTypeInfoSection persistenceSection = new PersistentTypeInfoSection((IPolicyCmptType)getIpsObject(),
                formBody, toolkit);
        persistenceSection.setExpanded(true);

        new PersistentAttributeSection((IPolicyCmptType)getIpsObject(), formBody, toolkit);
        new PersistentAssociationSection((IPolicyCmptType)getIpsObject(), formBody, toolkit);
    }

}
