/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * An editor page that allows to edit the {@link ILabel}s of an {@link ILabeledElement}.
 */
class LabelPage extends IpsObjectEditorPage {

    final static String PAGEID = "Label"; //$NON-NLS-1$

    LabelPage(IpsObjectEditor editor) {
        super(editor, PAGEID, Messages.LabelPage_label);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, false));
        new LabelSection(getIpsObject(), formBody, toolkit);
    }

    private static class LabelSection extends IpsSection {

        private final ILabeledElement labeledElement;

        private LabelEditComposite labelEditComposite;

        private LabelSection(ILabeledElement labeledElement, Composite parent, UIToolkit toolkit) {
            super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

            this.labeledElement = labeledElement;

            initControls();
            setText(Messages.LabelSection_label);
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            labelEditComposite = new LabelEditComposite(client, labeledElement);
        }

        @Override
        protected void performRefresh() {
            if (labeledElement == null) {
                return;
            }
            labelEditComposite.refresh();
        }

    }

}
