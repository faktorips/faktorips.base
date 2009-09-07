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

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.enums.EnumValue;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.controls.EnumTypeRefControl;
import org.faktorips.devtools.core.ui.controls.FilteredIpsObjectTypeRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.controls.Radiobutton;
import org.faktorips.devtools.core.ui.controls.RadiobuttonGroup;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.devtools.core.ui.controls.TestCaseTypeRefControl;
import org.faktorips.util.message.Message;

/**
 * A toolkit to create controls with a common look and feel.
 */
public class UIToolkit {
    private static final String READONLY_FOREGROUND_COLOR = "READONLY_FOREGROUND_COLOR"; //$NON-NLS-1$

    private FormToolkit formToolkit;

    public static final int DEFAULT_WIDTH = 100;

    /**
     * Adjusts the control so that the data shown in it can be either edited or not according to the
     * given changeable value. If this control is a composite all it's children (and recursivly
     * their children) are adjusted as well.
     */
    public void setDataChangeable(Control c, boolean changeable) {
        if (c == null) {
            return;
        }
        if (c instanceof IDataChangeableReadWriteAccess) {
            ((IDataChangeableReadWriteAccess)c).setDataChangeable(changeable);
            return;
        }
        if (c instanceof Text) {
            ((Text)c).setEditable(changeable);
            setForegroundColor((Text)c, changeable);
            return;
        }
        if (c instanceof Checkbox) {
            ((Checkbox)c).setEnabled(changeable);
            return;
        }
        if (c instanceof Combo) {
            ((Combo)c).setEnabled(changeable);
            return;
        }
        if (c instanceof Button) {
            ((Button)c).setEnabled(changeable);
            return;
        }
        // note: this has to be the last if statement as other controls might derive from
        // composite
        if (c instanceof Composite) {
            Control[] children = ((Composite)c).getChildren();
            for (int i = 0; i < children.length; i++) {
                setDataChangeable(children[i], changeable);
            }
        }
    }

    private void setForegroundColor(Text text, boolean changeable) {
        if (formToolkit == null) {
            // color will be implicit changed by OS
            return;
        }
        text.setForeground(getForegroundColorFromFormToolkit(changeable));
    }

    private Color getForegroundColorFromFormToolkit(boolean changeable) {
        if (changeable) {
            return formToolkit.getColors().getForeground();
        }
        Color color = formToolkit.getColors().getColor(READONLY_FOREGROUND_COLOR);
        if (color == null) {
            formToolkit.getColors().createColor(READONLY_FOREGROUND_COLOR,
                    formToolkit.getColors().getSystemColor(SWT.COLOR_DARK_GRAY));
            // color will be disposed by the FormColors#colorRegistry
        }
        return formToolkit.getColors().getColor(READONLY_FOREGROUND_COLOR);
    }

    /**
     * Creates a new toolkit.
     */
    public UIToolkit(FormToolkit formToolkit) {
        this.formToolkit = formToolkit;
    }

    /**
     * Returns <code>true</code> if this is toolkit used for forms, otherwise <code>false</code>.
     */
    public boolean isFormToolkit() {
        return formToolkit != null;
    }

    public FormToolkit getFormToolkit() {
        return formToolkit;
    }

    public GridLayout createNoMarginGridLayout(int numColumns, boolean equalSize) {
        GridLayout layout = new GridLayout(numColumns, equalSize);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        return layout;
    }

    /**
     * Creates a new composite with a grid layout and no borders.
     */
    public Composite createGridComposite(Composite parent, int numColumns, boolean equalSize, boolean margin) {
        Composite newComposite;
        if (formToolkit != null) {
            newComposite = formToolkit.createComposite(parent);
        } else {
            newComposite = new Composite(parent, SWT.NONE);
        }
        GridLayout layout = new GridLayout(numColumns, equalSize);
        if (!margin) {
            layout.marginHeight = 0;
            layout.marginWidth = 0;
        }
        newComposite.setLayout(layout);
        newComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return newComposite;
    }

    /**
     * Creates a new composite.
     */
    public Composite createComposite(Composite parent) {
        if (formToolkit != null) {
            return formToolkit.createComposite(parent);
        }
        return new Composite(parent, SWT.NONE);
    }

    /**
     * Creates a new composite with a two column grid layout.
     */
    public Composite createLabelEditColumnComposite(Composite parent) {
        Composite newComposite;
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 12;
        layout.verticalSpacing = 6;
        if (formToolkit != null) {
            newComposite = formToolkit.createComposite(parent);
            formToolkit.paintBordersFor(newComposite);
            layout.marginHeight = 2;
            layout.marginWidth = 2;
        } else {
            newComposite = new Composite(parent, SWT.NONE);
            // newComposite.setBackground(parent.getBackground());
            layout.marginHeight = 0;
            layout.marginWidth = 0;
        }
        newComposite.setLayout(layout);
        if (parent.getLayout() instanceof GridLayout) {
            newComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        return newComposite;
    }

    /**
     * Creates a new composite with a three column grid layout. First column for structure-label,
     * second column for input-label, third column for the input control.
     */
    public Composite createStructuredLabelEditColumnComposite(Composite parent) {
        Composite newComposite;
        GridLayout layout = new GridLayout(3, false);
        layout.horizontalSpacing = 12;
        if (formToolkit != null) {
            newComposite = formToolkit.createComposite(parent);
            formToolkit.paintBordersFor(newComposite);
            layout.marginHeight = 2;
            layout.marginWidth = 2;
        } else {
            newComposite = new Composite(parent, SWT.NONE);
            newComposite.setBackground(parent.getBackground());
            layout.marginHeight = 0;
            layout.marginWidth = 0;
        }
        newComposite.setLayout(layout);
        if (parent.getLayout() instanceof GridLayout) {
            newComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        return newComposite;
    }

    /**
     * Creates a new label with the given text.
     * 
     * @param parent The parent composite.
     * @param text The text to display.
     * @param grab <code>true</code> to let the label grab more space then initial preferred, if
     *            available.
     */
    public Label createLabel(Composite parent, String text, boolean grab) {
        Label label = createLabel(parent, text);
        ((GridData)label.getLayoutData()).grabExcessHorizontalSpace = grab;
        ((GridData)label.getLayoutData()).grabExcessVerticalSpace = grab;
        return label;
    }

    /**
     * Creates a new label.
     */
    public Label createLabel(Composite parent, String text) {
        Label newLabel;
        if (formToolkit != null) {
            newLabel = formToolkit.createLabel(parent, text);
        } else {
            newLabel = new Label(parent, SWT.NONE);
            // newLabel.setBackground(parent.getBackground());
            if (text != null) {
                newLabel.setText(text);
            }
        }
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END);
        newLabel.setLayoutData(gridData);
        return newLabel;
    }

    /**
     * Creates a new form label with margin.
     */
    public Label createFormLabel(Composite parent, String text) {
        Composite c = createComposite(parent);
        c.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END));
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.marginHeight = 1;
        layout.marginWidth = 0;
        c.setLayout(layout);
        Label newLabel = this.createLabel(c, text);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END);
        newLabel.setLayoutData(gridData);
        return newLabel;
    }

    /**
     * Creates a spacer-control.
     * 
     * @param parent The parent composite.
     * @param height The preferred height.
     * @param grab <code>true</code> to let the spacer grab available space (horizontal and
     *            vertical) to become bigger then the preferred size.
     */
    public Control createVerticalSpacer(Composite parent, int height, boolean grab) {
        if (grab) {
            return createVerticalSpacer(parent, height);
        } else {
            Control c = createVerticalSpacer(parent, height);
            ((GridData)c.getLayoutData()).grabExcessHorizontalSpace = false;
            ((GridData)c.getLayoutData()).grabExcessVerticalSpace = false;
            return c;
        }
    }

    public Control createVerticalSpacer(Composite parent, int height) {
        Composite spacer = new Composite(parent, SWT.NONE);
        spacer.setLayout(new GridLayout());
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.heightHint = height;
        data.widthHint = 1;
        spacer.setLayoutData(data);
        return spacer;
    }

    /**
     * Creates a new form label with margin.
     */
    public Hyperlink createHyperlink(Composite parent, String text) {
        Hyperlink newLink;
        if (formToolkit != null) {
            newLink = formToolkit.createHyperlink(parent, text, SWT.NONE);
        } else {
            throw new RuntimeException(
                    "Hyperlinks are only available for forms, use createLinks or createLinkOrHyperlink instead."); //$NON-NLS-1$
        }
        newLink.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END));
        return newLink;
    }

    public Text createText(Composite parent, int style) {
        Text newText;
        if (formToolkit != null) {
            newText = formToolkit.createText(parent, null, style);
        } else {
            newText = new Text(parent, style);
        }
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = DEFAULT_WIDTH;
        newText.setLayoutData(gridData);
        return newText;

    }

    public Text createText(Composite parent) {
        if (formToolkit != null) {
            return createText(parent, SWT.NONE);
        } else {
            return createText(parent, SWT.SINGLE | SWT.BORDER);
        }
    }

    public Text createMultilineText(Composite parent) {
        Text newText;
        if (formToolkit != null) {
            newText = formToolkit.createText(parent, null, SWT.MULTI | SWT.WRAP);
        } else {
            newText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP);
        }
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 60;
        gridData.widthHint = DEFAULT_WIDTH;
        newText.setLayoutData(gridData);

        return newText;
    }

    /**
     * Creates a new checkbox.
     */
    public Checkbox createCheckbox(Composite parent) {
        return new Checkbox(parent, this);
    }

    /**
     * Creates a new checkbox.
     */
    public Checkbox createCheckbox(Composite parent, boolean invertValue) {
        return new Checkbox(parent, this, invertValue);
    }

    /**
     * Creates a new checkbox.
     */
    public Checkbox createCheckbox(Composite parent, String text) {
        Checkbox checkbox = new Checkbox(parent, this);
        if (text != null) {
            checkbox.setText(text);
        }
        return checkbox;
    }

    /**
     * Creates a new radiobutton.
     */
    public Radiobutton createRadiobutton(Composite parent) {
        return new Radiobutton(parent, this);
    }

    /**
     * Creates a new radiobutton.
     */
    public Radiobutton createRadiobutton(Composite parent, String text) {
        Radiobutton radiobutton = new Radiobutton(parent, this);
        if (text != null) {
            radiobutton.setText(text);
        }
        return radiobutton;
    }

    /**
     * Creates a new push button.
     */
    public Button createButton(Composite parent, String text) {
        if (formToolkit != null) {
            return formToolkit.createButton(parent, text, SWT.PUSH);
        }
        Button newButton = new Button(parent, SWT.PUSH);
        newButton.setText(text);
        return newButton;
    }

    public Button createRadioButton(Composite parent, String text) {
        if (formToolkit != null) {
            return formToolkit.createButton(parent, text, SWT.RADIO);
        }
        Button newButton = new Button(parent, SWT.RADIO);
        newButton.setText(text);
        return newButton;
    }

    /**
     * Creates a new package fragment reference control.
     */
    public IpsPckFragmentRefControl createPdPackageFragmentRefControl(Composite parent) {
        return new IpsPckFragmentRefControl(parent, this);
    }

    /**
     * Creates a new package fragment reference control.
     */
    public IpsPckFragmentRefControl createPdPackageFragmentRefControl(IIpsPackageFragmentRoot root, Composite parent) {
        IpsPckFragmentRefControl control = createPdPackageFragmentRefControl(parent);
        control.setIpsPckFragmentRoot(root);
        return control;
    }

    /**
     * Creates a new package fragment reference control.
     */
    public IpsPckFragmentRootRefControl createPdPackageFragmentRootRefControl(Composite parent, boolean onlySourceRoots) {
        return new IpsPckFragmentRootRefControl(parent, onlySourceRoots, this);
    }

    /**
     * Creates a new PcTypeRefControl.
     */
    public PcTypeRefControl createPcTypeRefControl(IIpsProject project, Composite parent) {
        return new PcTypeRefControl(project, parent, this);
    }

    /**
     * Creates a new ProductCmptTypeRefControl.
     */
    public ProductCmptType2RefControl createProductCmptTypeRefControl(IIpsProject project,
            Composite parent,
            boolean excludeAbstractTypes) {
        return new ProductCmptType2RefControl(project, parent, this, excludeAbstractTypes);
    }

    /**
     * Creates a new QuestionIpsObjectTypeRefControl.
     */
    public FilteredIpsObjectTypeRefControl createFilteredIpsObjectTypeRefControl(IIpsProject project,
            Composite parent,
            String controlTitle,
            String controlDescription,
            IpsObjectType[] applicableTypes,
            boolean excludeAbstractTypes) {
        return new FilteredIpsObjectTypeRefControl(project, parent, this, controlTitle, controlDescription,
                applicableTypes, excludeAbstractTypes);
    }

    /**
     * Creates and returns a new <code>EnumTypeRefControl</code>.
     * 
     * @param ipsProject The ips project to search for enum types.
     * @param parent The parent ui composite.
     * @param chooseSuperEnumType Flag indicating whether the created control shall be used to
     *            choose a super enum type.
     */
    public EnumTypeRefControl createEnumTypeRefControl(IIpsProject ipsProject,
            Composite parent,
            boolean chooseSuperEnumType) {

        return new EnumTypeRefControl(ipsProject, parent, this, chooseSuperEnumType);
    }

    /**
     * Creates and returns a new <code>EnumRefControl</code> which allows to select either an
     * <code>IEnumType</code> or an <code>IEnumContent</code>.
     * 
     * @param ipsProject The ips project to search for enum types and contents.
     * @param parent The parent ui composite.
     * @param hideAbstract Flag indicating whether to hide abstract enum types.
     * @param hideNotContainingValues Flag indicating whether to hide enum types that are not
     *            containing values.
     */
    public EnumRefControl createEnumRefControl(IIpsProject ipsProject,
            Composite parent,
            boolean hideAbstract,
            boolean hideNotContainingValues) {
        return new EnumRefControl(ipsProject, parent, this, hideAbstract, hideNotContainingValues);
    }

    /**
     * Creates a new TableStructureRefControl.
     */
    public TableStructureRefControl createTableStructureRefControl(IIpsProject project, Composite parent) {
        return new TableStructureRefControl(project, parent, this);
    }

    /**
     * Creates a new TableContentsRefControl.
     */
    public TableContentsRefControl createTableContentsRefControl(IIpsProject project, Composite parent) {
        return new TableContentsRefControl(project, parent, this);
    }

    /**
     * Creates a new IpsProjectRefControl.
     */
    public IpsProjectRefControl createIpsProjectRefControl(Composite parent) {
        return new IpsProjectRefControl(parent, this);
    }

    /**
     * Creates a new DatatypeRefControl.
     */
    public DatatypeRefControl createDatatypeRefEdit(IIpsProject project, Composite parent) {
        return new DatatypeRefControl(project, parent, this);
    }

    /**
     * Creates a new Combo-Box. Note that the FormToolkit does not support Combos, so the appearence
     * of this Combo-Box is NOT similar to the oter FormToolkit-Controls.
     */
    public Combo createCombo(Composite parent) {
        Combo newCombo = new Combo(parent, SWT.READ_ONLY);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
        gridData.widthHint = DEFAULT_WIDTH;
        newCombo.setLayoutData(gridData);
        return newCombo;
    }

    public Combo createCombo(Composite parent, EnumType type) {
        return createCombo(parent, type.getValues());
    }

    public Combo createCombo(Composite parent, EnumValue[] values) {
        Combo newCombo = createCombo(parent);
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].getName();
        }
        newCombo.setItems(names);
        return newCombo;
    }

    /**
     * Creates a combo containing the given <code>EnumDatatype</code>'s values as items. The
     * formatting is done based on the user's preferences.
     * 
     * @see org.faktorips.devtools.core.IpsPreferences#formatValue(EnumDatatype, String)
     * @see org.faktorips.devtools.core.IpsPreferences#getEnumTypeDisplay()
     */
    public Combo createCombo(Composite parent, EnumDatatype datatype) {
        Combo newCombo = createCombo(parent);
        if (datatype.isSupportingNames()) {
            String[] ids = datatype.getAllValueIds(true);
            ArrayList<String> nameList = new ArrayList<String>(ids.length);
            for (int i = 0; i < ids.length; i++) {
                String formatedText = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(
                        datatype, ids[i]);
                nameList.add(formatedText);
            }
            setComboValues(newCombo, nameList.toArray(new String[ids.length]));
            return newCombo;
        }
        setComboValues(newCombo, datatype.getAllValueIds(true));
        return newCombo;
    }

    /**
     * Creates a combo containing the given <code>EnumDatatype</code>'s value IDs as items. It does
     * not add names to the combo even if the <code>EnumDatatype</code> supports names.
     */
    public Combo createIDCombo(Composite parent, EnumDatatype enumValues) {
        Combo newCombo = createCombo(parent);
        setComboValues(newCombo, enumValues.getAllValueIds(true));
        return newCombo;
    }

    public Combo createComboForBoolean(Composite parent,
            boolean inclNull,
            String trueRepresentation,
            String falseRepresentation) {
        Combo newCombo = createCombo(parent);
        if (inclNull) {
            setComboValues(newCombo, new String[] { null, trueRepresentation, falseRepresentation });
        } else {
            setComboValues(newCombo, new String[] { trueRepresentation, falseRepresentation });
        }
        return newCombo;
    }

    /**
     * Replaces all occurences of <code>null</code> in the values by the defined null
     * representation. The result is set as items to the given combo.
     * 
     * @param combo The combo to set the values.
     * @param values The values to set.
     */
    // pk 26-05-2009: this code needs to be refactored. the provided parameter array mustn't not be
    // modified
    // within this method.
    private void setComboValues(Combo combo, String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                values[i] = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            }
        }
        combo.setItems(values);
    }

    /**
     * Creates a new Combo and adds the values of the value set as items to it and returns it. If an
     * EnumDatatype is provided then the names for the values in the EnumValueSet are retrieved from
     * it and added as items combined with the id as the property defined to the Combo. If the
     * EnumValueSet doesn't base on an EnumDatatype or if just the value ids are to display in the
     * Combo null can be specified for the EnumDatatype parameter.
     */
    public Combo createCombo(Composite parent, IEnumValueSet enumValueSet, EnumDatatype dataType) {
        Combo newCombo = createCombo(parent);

        String[] values = new String[enumValueSet.size()];
        for (int i = 0; i < values.length; i++) {
            if (dataType != null) {
                String formatedText = IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(
                        dataType, enumValueSet.getValue(i));
                values[i] = formatedText;
            } else {
                values[i] = enumValueSet.getValue(i);
            }
        }

        setComboValues(newCombo, values);
        return newCombo;
    }

    public Group createGroup(Composite parent, String text) {
        return createGroup(parent, SWT.NONE, text);
    }

    public Group createGroup(Composite parent, int style, String text) {
        Group newGroup = new Group(parent, style);
        if (text != null) {
            newGroup.setText(text);
        }
        newGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        newGroup.setLayout(layout);

        return newGroup;
    }

    public RadiobuttonGroup createRadiobuttonGroup(Composite parent, int style, String text) {
        RadiobuttonGroup newRadiobuttonGroup = new RadiobuttonGroup(parent, style, this);
        Group newGroup = newRadiobuttonGroup.getGroup();
        if (text != null) {
            newGroup.setText(text);
        }
        newGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        newGroup.setLayout(layout);

        return newRadiobuttonGroup;
    }

    public Group createGridGroup(Composite parent, String text, int numOfColumns, boolean equalSize) {
        return createGridGroup(parent, SWT.NONE, text, numOfColumns, equalSize);
    }

    public Group createGridGroup(Composite parent, int style, String text, int numOfColumns, boolean equalSize) {
        Group newGroup = createGroup(parent, style, text);
        GridLayout layout = new GridLayout(numOfColumns, equalSize);
        newGroup.setLayout(layout);
        return newGroup;
    }

    public Label createHorizonzalLine(Composite parent) {
        Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return line;
    }

    public Table createTable(Composite parent, int style) {
        if (formToolkit != null) {
            return formToolkit.createTable(parent, style);
        }
        return new Table(parent, style);
    }

    /**
     * Creates a new TestCaseTypeRefControl.
     */
    public TestCaseTypeRefControl createTestCaseTypeRefControl(IIpsProject project, Composite parent) {
        return new TestCaseTypeRefControl(project, parent, this);
    }

    /**
     * Converts the severity constant from <code>{@link Message}</code> to the constant of
     * <code>{@link IMessageProvider}</code>.
     */
    // TODO move to Message when the class is moved to the core project
    public int convertToJFaceSeverity(int severity) {

        switch (severity) {
            case Message.ERROR:
                return IMessageProvider.ERROR;
            case Message.INFO:
                return IMessageProvider.INFORMATION;
            case Message.WARNING:
                return IMessageProvider.WARNING;
            case Message.NONE:
                return IMessageProvider.NONE;
        }
        return IMessageProvider.NONE;
    }

    /**
     * Disposes the toolkit.
     */
    public void dispose() {
        if (formToolkit != null) {
            formToolkit.dispose();
        }
    }

    /**
     * If the given Control has a {@link GridData} as LayoutData, the vertical span is set to the
     * given value (vSpan). Has no effect otherwise.
     */
    public void setHorizontalSpan(Control control, int vSpan) {
        if (control.getLayoutData() instanceof GridData) {
            ((GridData)control.getLayoutData()).horizontalSpan = vSpan;
        }
    }

    /**
     * If the given Control has a {@link GridData} as LayoutData, the width-hint is set to the given
     * value (width). Has no effect otherwise.
     */
    public void setWidthHint(Control control, int width) {
        if (control.getLayoutData() instanceof GridData) {
            ((GridData)control.getLayoutData()).widthHint = width;
        }
    }
}
