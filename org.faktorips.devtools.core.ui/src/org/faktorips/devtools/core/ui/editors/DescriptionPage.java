/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
        super(editor, PAGEID, Messages.IpsPartEditDialog_tabItemDocumentation);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        new DescriptionSection(getIpsObject(), formBody, toolkit);
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

    }

}
