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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.EnumTypeRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * The general info section for the <code>EnumTypeEditor</code> provides ui controls to edit the
 * <em>superEnumType</em> property, the <em>abstract</em> property and the
 * <em>valuesArePartOfModel</em> property of an <code>IEnumType</code>.
 * <p>
 * It is part of the <code>EnumTypeStructurePage</code>.
 * 
 * @see EnumTypeEditor
 * @see EnumTypeStructurePage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeGeneralInfoSection extends IpsSection implements ContentsChangeListener {

    /** The enum type the editor is currently editing. */
    private IEnumType enumType;

    /** The extension property control factory that may extend the controls. */
    private ExtensionPropertyControlFactory extFactory;

    /** The ui checkbox for the valuesArePartOfModelCheckbox property. */
    private Checkbox valuesArePartOfModelCheckbox;

    /**
     * Creates a new <code>EnumTypeGeneralInfoSection</code>.
     * 
     * @param enumType The enum type the enum type editor is currently editing.
     * @param parent The parent ui composite to attach this info section to.
     * @param toolkit The ui toolkit to be used to create new ui elements.
     * 
     * @throws NullPointerException If <code>enumType</code> is <code>null</code>.
     */
    public EnumTypeGeneralInfoSection(final IEnumType enumType, Composite parent, UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);

        ArgumentCheck.notNull(enumType);

        this.enumType = enumType;
        this.extFactory = new ExtensionPropertyControlFactory(enumType.getClass());

        initControls();
        setText(Messages.EnumTypeGeneralInfoSection_title);

        enumType.getIpsModel().addChangeListener(this);
        addDisposeListener(new DisposeListener() {
            /**
             * {@inheritDoc}
             */
            public void widgetDisposed(DisposeEvent e) {
                enumType.getIpsModel().removeChangeListener(EnumTypeGeneralInfoSection.this);
            }
        });
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
                } catch (CoreException e) {
                    throw new RuntimeException(e);
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
        valuesArePartOfModelCheckbox = toolkit.createCheckbox(checkBoxesComposite);
        valuesArePartOfModelCheckbox.setEnabled(!(enumType.isAbstract()));
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

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        /*
         * Return if the content changed was not the enum type container to be edited.
         */
        if (!(event.getIpsSrcFile().equals(enumType.getIpsSrcFile()))) {
            return;
        }

        switch (event.getEventType()) {
            case ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED:
                try {
                    IEnumType enumType = (IEnumType)event.getIpsSrcFile().getIpsObject();
                    valuesArePartOfModelCheckbox.setEnabled(!(enumType.isAbstract()));
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
        }
    }

}
