/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * An editor page that allows to edit the {@link IDescription}s of an {@link IDescribedElement}.
 */
class DescriptionPage extends IpsObjectEditorPage {

    final static String PAGEID = "Description"; //$NON-NLS-1$

    DescriptionPage(IpsObjectEditor editor) {
        super(editor, PAGEID, Messages.DescriptionPage_description);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        new DescriptionSection((IDescribedElement)getIpsObject(), formBody, toolkit);
    }

    private static class DescriptionSection extends IpsSection {

        private final IDescribedElement describedElement;

        private DescriptionSection(IDescribedElement describedElement, Composite parent, UIToolkit toolkit) {
            super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

            this.describedElement = describedElement;

            initControls();
            setText(Messages.DescriptionSection_description);
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            new DescriptionEditComposite(client, describedElement, toolkit);
        }

        @Override
        protected void performRefresh() {
            // Nothing to do
        }

    }

}
