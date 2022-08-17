/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;

/**
 * Overview page for persistence properties of an {@link IPolicyCmptType}. The page consists of
 * three section showing the type´s persistence properties and the properties of its attributes and
 * associations.
 * 
 * @author Roman Grutza
 */
public class PersistencePage extends IpsObjectEditorPage {

    private static final String PAGE_ID = "PersistencePage"; //$NON-NLS-1$

    public PersistencePage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, "Persistence"); //$NON-NLS-1$
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, true));
        PersistentTypeInfoSection persistenceSection = new PersistentTypeInfoSection((IPolicyCmptType)getIpsObject(),
                formBody, toolkit);
        persistenceSection.setExpanded(true);
        persistenceSection.refresh();

        new PersistentAttributeSection((IPolicyCmptType)getIpsObject(), formBody, toolkit);
        new PersistentAssociationSection((IPolicyCmptType)getIpsObject(), formBody, toolkit);
    }

}
