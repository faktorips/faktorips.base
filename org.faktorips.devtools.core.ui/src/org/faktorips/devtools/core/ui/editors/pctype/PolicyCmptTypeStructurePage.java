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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.type.MethodsSection;

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
        new PolicyCmptTypeAttributesSection(this, (IPolicyCmptType)getIpsObject(), members, toolkit);
        new PolicyCmptTypeAssociationsSection(this, (IPolicyCmptType)getIpsObject(), members, toolkit);
        new MethodsSection((IPolicyCmptType)getIpsObject(), members, toolkit);
        new RulesSection((IPolicyCmptType)getIpsObject(), members, toolkit);
    }

    @Override
    protected void createContentForSplittedStructurePage(Composite formBody, UIToolkit toolkit) {
        createPersistenceTypeInfoSectionIfNecessary(formBody, toolkit);

        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new PolicyCmptTypeAttributesSection(this, (IPolicyCmptType)getIpsObject(), members, toolkit);
        new PolicyCmptTypeAssociationsSection(this, (IPolicyCmptType)getIpsObject(), members, toolkit);
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
        final ContentsChangeListener changeListener = new ContentsChangeListener() {
            @Override
            public void contentsChanged(ContentChangeEvent event) {
                if (getIpsObject() == null) {
                    return;
                }
                if (getPartControl().isVisible()
                        && event.getEventType() == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED
                        && event.getIpsSrcFile().equals(getIpsObject().getIpsSrcFile())) {
                    refresh();
                }
            }
        };
        getIpsObject().getIpsModel().addChangeListener(changeListener);
        getPartControl().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                IIpsModel model = IpsPlugin.getDefault().getIpsModel();
                if (model != null) {
                    model.removeChangeListener(changeListener);
                }
            }
        });
    }

}
