/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;

/**
 * An editor page that allows to edit the {@link ILabel}s and {@link IDescription}s of an
 * {@link IIpsObject}.
 */
class DocumentationPage extends IpsObjectEditorPage {

    private static final String PAGEID = "Documentation"; //$NON-NLS-1$

    DocumentationPage(IpsObjectEditor editor) {
        super(editor, PAGEID, Messages.IpsPartEditDialog_tabItemDocumentation);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, true));
        IIpsObject ipsObject = getIpsObject();
        if (ipsObject instanceof ILabeledElement) {
            new LabelSection((ILabeledElement)getIpsObject(), formBody, toolkit);
        }
        new DescriptionSection(getIpsObject(), formBody, toolkit);
        if (ipsObject instanceof IVersionControlledElement) {
            new VersionSection((IVersionControlledElement)getIpsObject(), formBody, toolkit);
            new DeprecationSection((IpsObjectPartContainer)getIpsObject(), formBody, toolkit);
        }
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
            labelEditComposite = new LabelEditComposite(client, labeledElement, toolkit);
        }

        @Override
        protected void performRefresh() {
            labelEditComposite.refresh();
        }

    }

    private static class DescriptionSection extends IpsSection {

        private final IDescribedElement describedElement;

        private DescriptionEditComposite descriptionEditComposite;

        private DescriptionSection(IDescribedElement describedElement, Composite parent, UIToolkit toolkit) {
            super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

            this.describedElement = describedElement;

            initControls();
            setText(Messages.DescriptionSection_description);
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            descriptionEditComposite = new DescriptionEditComposite(client, describedElement, toolkit,
                    getBindingContext());
        }

        @Override
        protected void performRefresh() {
            descriptionEditComposite.refresh();
        }

    }

    private static class VersionSection extends IpsSection {

        private final IVersionControlledElement versionElement;

        public VersionSection(IVersionControlledElement versionElement, Composite composite, UIToolkit toolkit) {
            super(composite, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
            this.versionElement = versionElement;

            initControls();
            setText(Messages.IpsPartEditDialog_groupVersion);
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            new VersionsComposite(client, versionElement, toolkit, getBindingContext());
        }

        @Override
        protected void performRefresh() {
            // nothing to do
        }
    }

    private static class DeprecationSection extends IpsSection {

        private final IpsObjectPartContainer deprecatedElement;

        private DeprecationEditComposite deprecationEditComposite;

        public DeprecationSection(IpsObjectPartContainer deprecatedElement, Composite composite,
                UIToolkit toolkit) {
            super(composite, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
            this.deprecatedElement = deprecatedElement;
            initControls();
            setText(Messages.DeprecationSection_label);
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            deprecationEditComposite = new DeprecationEditComposite(client, deprecatedElement,
                    toolkit,
                    getBindingContext());

        }

        @Override
        protected void performRefresh() {
            deprecationEditComposite.refresh();
        }
    }
}
