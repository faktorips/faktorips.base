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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.type.MethodsSection;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;

/**
 * The structure page contain the general information section, the attributes section and the
 * relations section.
 */
class PolicyCmptTypeStructurePage extends PolicyCmptTypeEditorPage {

    public PolicyCmptTypeStructurePage(PolicyCmptTypeEditor editor, boolean twoSectionsWhenTrueOtherwiseFour) {
        super(editor, twoSectionsWhenTrueOtherwiseFour, Messages.StructurePage_title, "PolicyCmptTypeStructurePage"); //$NON-NLS-1$
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        super.createPageContent(formBody, toolkit);
        registerContentsChangeListener();
    }

    @Override
    protected void createContentForSingleStructurePage(Composite formBody, UIToolkit toolkit) {
        createPersistenceTypeInfoSectionIfNecessary(formBody, toolkit);

        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new PolicyCmptTypeAttributesSection((IPolicyCmptType)getIpsObject(), members, getSite(), toolkit);
        new PolicyCmptTypeAssociationsSection((IPolicyCmptType)getIpsObject(), members, getSite(), toolkit);
        new MethodsSection((IPolicyCmptType)getIpsObject(), members, getSite(), toolkit);
        new RulesSection((IPolicyCmptType)getIpsObject(), members, getSite(), toolkit);
    }

    @Override
    protected void createContentForSplittedStructurePage(Composite formBody, UIToolkit toolkit) {
        createPersistenceTypeInfoSectionIfNecessary(formBody, toolkit);

        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new PolicyCmptTypeAttributesSection((IPolicyCmptType)getIpsObject(), members, getSite(), toolkit);
        new PolicyCmptTypeAssociationsSection((IPolicyCmptType)getIpsObject(), members, getSite(), toolkit);
    }

    @Override
    protected void createGeneralPageInfoSection(Composite formBody, UIToolkit toolkit) {
        new GeneralInfoSection((IPolicyCmptType)getIpsObject(), formBody, toolkit);
    }

    private void createPersistenceTypeInfoSectionIfNecessary(Composite formBody, UIToolkit toolkit) {
        if (!getIpsObject().getIpsProject().isPersistenceSupportEnabled()) {
            return;
        }
        new PersistentTypeInfoSection((IPolicyCmptType)getIpsObject(), formBody, toolkit);
    }

    // package level access, need this functionality also in PersistencePage
    void registerContentsChangeListener() {
        /*
         * Refreshing the page after a change in the PolicyCmptType occurred is necessary since
         * there is a dependency from attributes that are displayed in the GeneralInfoSection and
         * the attributes respectively IpsPart that are displayed in the other sections.
         */
        final ContentsChangeListener changeListener = event -> {
            if (getIpsObject() == null) {
                return;
            }
            if (getPartControl().isVisible()
                    && event.getEventType() == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED
                    && event.getIpsSrcFile().equals(getIpsObject().getIpsSrcFile())) {
                refresh();
            }
        };
        getIpsObject().getIpsModel().addChangeListener(changeListener);
        getPartControl().addDisposeListener($ -> {
            IIpsModel model = IIpsModel.get();
            if (model != null) {
                model.removeChangeListener(changeListener);
            }
        });
    }

}
