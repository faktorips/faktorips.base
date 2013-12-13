/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controller.fields.ButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
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

    /** The RefControl for supertype. */
    private EnumTypeRefControl supertypeRefControl;

    /** The UI check box for the <tt>abstract</tt> property. */
    private Button isAbstractCheckbox;

    /** The UI check box for the <tt>extensible</tt> property. */
    private Button extensibleCheckbox;

    /** The UI control for the <tt>enumContentPackageFragment</tt> property */
    private TextField enumContentNameControl;

    /** The UI text for <tt>identifierBoundary</tt> property */
    private Text boundaryText;

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
        extFactory = new ExtensionPropertyControlFactory(enumType);

        setGrabVerticalSpace(false);
        initControls();
        setText(Messages.EnumTypeGeneralInfoSection_title);
        enumType.getIpsModel().addChangeListener(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        enumType.getIpsModel().removeChangeListener(this);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createLabelEditColumnComposite(client);

        createSuperclassLink(toolkit, composite);
        createSupertypeRefControl(toolkit, composite);
        createIsAbstractCheckbox(toolkit, composite);
        createExtensibleCheckbox(toolkit, composite);
        createEnumContentSpecificationAndBoundary(composite, toolkit);
        registerFocusHandling();
        createExtensionControl(toolkit, composite);
    }

    private void createSuperclassLink(UIToolkit toolkit, Composite composite) {
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
    }

    private EnumTypeRefControl createSupertypeRefControl(UIToolkit toolkit, Composite composite) {
        supertypeRefControl = toolkit.createEnumTypeRefControl(enumType.getIpsProject(), composite, true);
        supertypeRefControl.setCurrentEnumType(enumType);
        getBindingContext().bindContent(supertypeRefControl, enumType, IEnumType.PROPERTY_SUPERTYPE);
        return supertypeRefControl;
    }

    private Button createIsAbstractCheckbox(UIToolkit toolkit, Composite composite) {
        toolkit.createFormLabel(composite, Messages.EnumTypeGeneralInfoSection_labelAbstract);
        isAbstractCheckbox = toolkit.createButton(composite, "", SWT.CHECK); //$NON-NLS-1$
        getBindingContext().bindContent(isAbstractCheckbox, enumType, IEnumType.PROPERTY_ABSTRACT);
        return isAbstractCheckbox;
    }

    private void createExtensibleCheckbox(UIToolkit toolkit, Composite composite) {
        toolkit.createFormLabel(composite, Messages.EnumTypeGeneralInfoSection_labelExtensible);
        extensibleCheckbox = toolkit.createButton(composite, "", SWT.CHECK); //$NON-NLS-1$
        ButtonField extensibleButtonField = new ButtonField(extensibleCheckbox);
        getBindingContext().bindContent(extensibleButtonField, enumType, IEnumType.PROPERTY_EXTENSIBLE);
        getBindingContext().bindEnabled(extensibleCheckbox, new Pmo(), Pmo.PROPERTY_EXTENSIBLE_CHECKBOX);
    }

    private void createEnumContentSpecificationAndBoundary(Composite composite, UIToolkit toolkit) {
        Composite newComposite = createEnumContentSpecification(composite, toolkit);
        createEnumContentBoundary(toolkit, newComposite);
        bind();
    }

    private Composite createEnumContentSpecification(Composite composite, UIToolkit toolkit) {
        toolkit.createFormLabel(composite, Messages.EnumTypeGeneralInfoSection_labelEnumContentPackageFragment);

        Composite newComposite = new Composite(composite, 0);
        newComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        newComposite.setLayout(layout);

        Text text = toolkit.createText(newComposite);
        enumContentNameControl = new TextField(text);
        return newComposite;
    }

    private void createEnumContentBoundary(UIToolkit toolkit, Composite newComposite) {
        Label label = toolkit.createFormLabel(newComposite, Messages.EnumTypeGeneralInfoSection_IdentifierBoundary);
        GridData gridData = new GridData(SWT.RIGHT, SWT.END, true, false);
        gridData.horizontalIndent = 50;
        label.setLayoutData(gridData);

        boundaryText = toolkit.createText(newComposite);
        boundaryText.setToolTipText(Messages.EnumTypeGeneralInfoSection_IdentifierBoundaryTooltipText);
    }

    private void bind() {
        getBindingContext().bindContent(enumContentNameControl, enumType, IEnumType.PROPERTY_ENUM_CONTENT_NAME);
        getBindingContext().bindEnabled(enumContentNameControl.getControl(), new Pmo(),
                Pmo.PROPERTY_ENUM_CONTENT_NAME_CONTROL);
        getBindingContext().bindContent(boundaryText, enumType, IEnumType.PROPERTY_IDENTIFIER_BOUNDARY);
        getBindingContext().bindEnabled(boundaryText, new Pmo(), Pmo.PROPERTY_BOUNDARY_TEXT);
    }

    private void registerFocusHandling() {
        addFocusControl(supertypeRefControl);
        addFocusControl(isAbstractCheckbox);
        addFocusControl(extensibleCheckbox);
        addFocusControl(enumContentNameControl.getTextControl());
    }

    private void createExtensionControl(UIToolkit toolkit, Composite composite) {
        extFactory.createControls(composite, toolkit, enumType);
        extFactory.bind(getBindingContext());
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        // Return if the content changed was not the EnumType to be edited.
        if (!(event.getIpsSrcFile().equals(enumType.getIpsSrcFile()))) {
            return;
        }

        try {
            propertyChanged();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void propertyChanged() throws CoreException {
        /*
         * Initialize enumeration content field if none has been specified yet and the values are
         * not part of the model.
         */
        if (!enumContentNameControl.getTextControl().isDisposed()) {
            if ((enumType.isExtensible()) && enumContentNameControl.getText().length() == 0) {
                enumContentNameControl.setText(enumType.getQualifiedName());
            }
        }

        // Create an EnumLiteralNameAttribute if the EnumType does not have one but needs one.
        if (!enumType.isAbstract() && !(enumType.containsEnumLiteralNameAttribute())) {
            IEnumLiteralNameAttribute newEnumLiteralNameAttribute = enumType.newEnumLiteralNameAttribute();
            IEnumAttribute nameAttribute = enumType.findUsedAsNameInFaktorIpsUiAttribute(enumType.getIpsProject());
            if (nameAttribute != null) {
                newEnumLiteralNameAttribute.setDefaultValueProviderAttribute(nameAttribute.getName());
            }
        }
    }

    public class Pmo extends IpsObjectPartPmo {
        public static final String PROPERTY_EXTENSIBLE_CHECKBOX = "extensibleCheckboxEnabled"; //$NON-NLS-1$
        public static final String PROPERTY_ENUM_CONTENT_NAME_CONTROL = "enumContentNameControlEnabled"; //$NON-NLS-1$
        public static final String PROPERTY_BOUNDARY_TEXT = "boundaryTextEnabled"; //$NON-NLS-1$

        public boolean isExtensibleCheckboxEnabled() {
            return !enumType.isAbstract();
        }

        public boolean isEnumContentNameControlEnabled() {
            return !(enumType.isAbstract()) && (enumType.isExtensible());
        }

        public boolean isBoundaryTextEnabled() {
            return isIdentifierBoundaryTextEnabled();
        }

        private boolean isIdentifierBoundaryTextEnabled() {
            boolean supportCompare = false;
            ValueDatatype datatype = getIdentifierAttributeDatatype();
            if (datatype != null) {
                supportCompare = datatype.supportsCompare();
            }
            return enumType.isExtensible() && supportCompare && !enumType.isAbstract();
        }

        private ValueDatatype getIdentifierAttributeDatatype() {
            IEnumAttribute identiferAttribute = enumType.findIdentiferAttribute(enumType.getIpsProject());
            ValueDatatype datatype = null;
            if (identiferAttribute != null) {
                try {
                    datatype = identiferAttribute.findDatatype(enumType.getIpsProject());
                } catch (CoreException e) {
                    new CoreRuntimeException(e);
                }
            }
            return datatype;
        }
    }
}
