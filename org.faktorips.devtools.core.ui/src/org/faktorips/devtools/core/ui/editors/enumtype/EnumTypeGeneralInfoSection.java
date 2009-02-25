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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.EnumTypeRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * The general info section for the enum type editor provides ui controls to edit the superEnumType
 * property, the abstract property and the valuesArePartOfModel property.
 * 
 * @author Alexander Weickmann
 */
public class EnumTypeGeneralInfoSection extends IpsSection {

    /** The enum type the editor is currently editing. */
    private IEnumType enumType;

    /** The extension property control factory that may extend the controls. */
    private ExtensionPropertyControlFactory extFactory;

    /**
     * Creates a new <code>EnumTypeGeneralInfoSection</code> using the specified parameters.
     * 
     * @param enumType The enum type the enum type editor is currently editing.
     * @param parent The parent ui composite to attach this info section to.
     * @param toolkit The ui toolkit to be used to create new ui elements.
     * 
     * @throws NullPointerException If enumType is <code>null</code>.
     */
    public EnumTypeGeneralInfoSection(IEnumType enumType, Composite parent, UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);

        ArgumentCheck.notNull(enumType);

        this.enumType = enumType;
        this.extFactory = new ExtensionPropertyControlFactory(enumType.getClass());

        initControls();
        setText(Messages.EnumTypeGeneralInfoSection_title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        Hyperlink link = toolkit.createHyperlink(composite, Messages.EnumTypeGeneralInfoSection_linkSuperclass);
        link.addHyperlinkListener(new HyperlinkAdapter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IEnumType supertype = enumType.findSuperEnumType();
                    if (supertype != null) {
                        IpsUIPlugin.getDefault().openEditor(supertype);
                    }
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });

        EnumTypeRefControl supertypeRefControl = toolkit.createEnumTypeRefControl(enumType.getIpsProject(), composite,
                true);
        supertypeRefControl.setCurrentEnumType(enumType);
        bindingContext.bindContent(supertypeRefControl, enumType, IEnumType.PROPERTY_SUPERTYPE);

        Composite checkBoxesComposite = toolkit.createLabelEditColumnComposite(client);

        toolkit.createFormLabel(checkBoxesComposite, Messages.EnumTypeGeneralInfoSection_labelAbstract);
        Checkbox abstractCheckbox = toolkit.createCheckbox(checkBoxesComposite);
        bindingContext.bindContent(abstractCheckbox, enumType, IEnumType.PROPERTY_ABSTRACT);

        toolkit.createFormLabel(checkBoxesComposite, Messages.EnumTypeGeneralInfoSection_labelValuesArePartOfModel);
        Checkbox valuesArePartOfModelCheckbox = toolkit.createCheckbox(checkBoxesComposite);
        bindingContext.bindContent(valuesArePartOfModelCheckbox, enumType, IEnumType.PROPERTY_VALUES_ARE_PART_OF_MODEL);

        // Register controls for focus handling
        addFocusControl(supertypeRefControl);
        addFocusControl(abstractCheckbox);
        addFocusControl(valuesArePartOfModelCheckbox);

        // Create extension properties
        extFactory.createControls(checkBoxesComposite, toolkit, enumType);
        extFactory.bind(bindingContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

}
