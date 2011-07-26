/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.enumtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.EnumTypeRefControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.wizards.enumtype.EnumTypePage;
import org.faktorips.util.ArgumentCheck;

/**
 * The general info section for the <tt>EnumTypeEditor</tt> provides UI controls to edit the
 * <em>superEnumType</em> property, the <em>abstract</em> property and the
 * <em>valuesArePartOfModel</em> property of an <tt>IEnumType</tt>.
 * <p>
 * It is part of the <tt>EnumTypeStructurePage</tt>.
 * 
 * @see EnumTypeEditor
 * @see EnumTypePage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeGeneralInfoSection extends IpsSection implements ContentsChangeListener {

    private static final String ID = "org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeGeneralInfoSection"; //$NON-NLS-1$

    /** The <tt>IEnumType</tt> the editor is currently editing. */
    private IEnumType enumType;

    /** The extension property control factory that may extend the controls. */
    private ExtensionPropertyControlFactory extFactory;

    /** The UI check box for the <tt>valuesArePartOfModelCheckbox</tt> property. */
    private Checkbox valuesArePartOfModelCheckbox;

    /** The UI control for the <tt>enumContentPackageFragment</tt> property */
    private TextField enumContentNameControl;

    /**
     * Creates a new <tt>EnumTypeGeneralInfoSection</tt>.
     * 
     * @param enumType The <tt>IEnumType</tt> the <tt>EnumTypeEditor</tt> is currently editing.
     * @param parent The parent UI composite to attach this info section to.
     * @param toolkit The UI toolkit to be used to create new UI elements.
     * 
     * @throws NullPointerException If <tt>enumType</tt> is <tt>null</tt>.
     */
    public EnumTypeGeneralInfoSection(IEnumType enumType, Composite parent, UIToolkit toolkit) {
        super(ID, parent, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(enumType);

        this.enumType = enumType;
        extFactory = new ExtensionPropertyControlFactory(enumType.getClass());

        initControls();
        setText(Messages.EnumTypeGeneralInfoSection_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        Hyperlink link = toolkit.createHyperlink(composite, Messages.EnumTypeGeneralInfoSection_linkSuperclass);
        link.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent event) {
                try {
                    IEnumType supertype = enumType.findSuperEnumType(enumType.getIpsProject());
                    if (supertype != null) {
                        IpsUIPlugin.getDefault().openEditor(supertype);
                    }
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Supertype
        EnumTypeRefControl supertypeRefControl = toolkit.createEnumTypeRefControl(enumType.getIpsProject(), composite,
                true);
        supertypeRefControl.setCurrentEnumType(enumType);
        bindingContext.bindContent(supertypeRefControl, enumType, IEnumType.PROPERTY_SUPERTYPE);

        // Abstract
        toolkit.createFormLabel(composite, Messages.EnumTypeGeneralInfoSection_labelAbstract);
        Checkbox abstractCheckbox = toolkit.createCheckbox(composite);
        bindingContext.bindContent(abstractCheckbox, enumType, IEnumType.PROPERTY_ABSTRACT);

        // Values are part of model
        toolkit.createFormLabel(composite, Messages.EnumTypeGeneralInfoSection_labelContainingValues);
        valuesArePartOfModelCheckbox = toolkit.createCheckbox(composite, true);
        valuesArePartOfModelCheckbox.setEnabled(!(enumType.isAbstract()));
        bindingContext.bindContent(valuesArePartOfModelCheckbox, enumType, IEnumType.PROPERTY_CONTAINING_VALUES);

        // EnumContent specification
        toolkit.createFormLabel(composite, Messages.EnumTypeGeneralInfoSection_labelEnumContentPackageFragment);
        Text text = toolkit.createText(composite);
        enumContentNameControl = new TextField(text);
        enumContentNameControl.getTextControl()
                .setEnabled(!(enumType.isAbstract()) && !(enumType.isContainingValues()));
        bindingContext.bindContent(enumContentNameControl, enumType, IEnumType.PROPERTY_ENUM_CONTENT_NAME);

        // Register controls for focus handling
        addFocusControl(supertypeRefControl);
        addFocusControl(abstractCheckbox);
        addFocusControl(valuesArePartOfModelCheckbox);
        addFocusControl(enumContentNameControl.getTextControl());

        // Extension properties
        extFactory.createControls(composite, toolkit, enumType);
        extFactory.bind(bindingContext);
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        // Return if the content changed was not the EnumType to be edited.
        if (!(event.getIpsSrcFile().equals(enumType.getIpsSrcFile()))) {
            return;
        }

        try {
            switch (event.getEventType()) {
                case ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED:
                    wholeContentChanged();
                    //$FALL-THROUGH$

                case ContentChangeEvent.TYPE_PROPERTY_CHANGED:
                    propertyChanged();
                    break;
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void propertyChanged() throws CoreException {
        /*
         * Initialize enumeration content field if none has been specified yet and the values are
         * not part of the model.
         */
        if (!(enumType.isContainingValues()) && enumContentNameControl.getText().length() == 0) {
            enumContentNameControl.setText(enumType.getQualifiedName());
        }

        // Create an EnumLiteralNameAttribute if the EnumType does not have one but needs one.
        if (enumType.isCapableOfContainingValues()) {
            if (!(enumType.containsEnumLiteralNameAttribute())) {
                IEnumLiteralNameAttribute newEnumLiteralNameAttribute = enumType.newEnumLiteralNameAttribute();
                IEnumAttribute nameAttribute = enumType.findUsedAsNameInFaktorIpsUiAttribute(enumType.getIpsProject());
                if (nameAttribute != null) {
                    newEnumLiteralNameAttribute.setDefaultValueProviderAttribute(nameAttribute.getName());
                }

            }
        }
    }

    private void wholeContentChanged() {
        valuesArePartOfModelCheckbox.setEnabled(!(enumType.isAbstract()));
        enumContentNameControl.getTextControl()
                .setEnabled(!(enumType.isAbstract()) && !(enumType.isContainingValues()));
    }

}
