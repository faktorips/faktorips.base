/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.function.Function;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.ButtonField;
import org.faktorips.devtools.core.ui.controller.fields.MessageDecoration;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.EnumControl;
import org.faktorips.devtools.core.ui.controls.EnumRefControl;
import org.faktorips.devtools.core.ui.controls.EnumTypeRefControl;
import org.faktorips.devtools.core.ui.controls.FilteredIpsObjectTypeRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.core.ui.controls.Radiobutton;
import org.faktorips.devtools.core.ui.controls.TableContentsRefControl;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.devtools.core.ui.controls.TestCaseTypeRefControl;
import org.faktorips.devtools.core.ui.controls.contentproposal.ICachedContentProposalProvider;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.devtools.model.INamedValue;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.Severity;

/**
 * A toolkit to create controls with a common look and feel.
 */
public class UIToolkit {

    public static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_MULTILINE_HEIGHT = 60;
    private static final String READONLY_FOREGROUND_COLOR = "READONLY_FOREGROUND_COLOR"; //$NON-NLS-1$
    private static final String READONLY_BACKGROUND_COLOR = "READONLY_BACKGROUND_COLOR"; //$NON-NLS-1$
    private static final String DATA_CHANGEABLE = "dataChangeable"; //$NON-NLS-1$
    private static final String ENABLED = "enabled"; //$NON-NLS-1$

    private static final int FOREGROUND_COLOR_DISABLED = SWT.COLOR_DARK_GRAY;
    private static final int FOREGROUND_COLOR_ENABLED = SWT.COLOR_LIST_FOREGROUND;
    private static final int BACKGROUND_COLOR_DISABLED = SWT.COLOR_LIST_BACKGROUND;
    private static final int BACKGROUND_COLOR_ENABLED = SWT.COLOR_LIST_BACKGROUND;

    private FormToolkit formToolkit;

    /**
     * Creates a new toolkit.
     */
    public UIToolkit(FormToolkit formToolkit) {
        this.formToolkit = formToolkit;
    }

    /**
     * Adjusts the control so that the data shown in it can be either edited or not according to the
     * given changeable value. If this control is a composite all its descendants (recursively) are
     * adjusted as well.
     */
    public void setDataChangeable(Control c, boolean changeable) {
        if (c == null || !isMarkedAsEnabled(c)) {
            return;
        }
        if (c instanceof IDataChangeableReadWriteAccess) {
            ((IDataChangeableReadWriteAccess)c).setDataChangeable(changeable);
            return;
        }
        c.setData(DATA_CHANGEABLE, changeable);
        updateEnabledState(c, changeable);
        // note: this has to be the last if statement as other controls might derive from
        // composite
        setDataChangeableChildren(c, changeable);
    }

    private void updateEnabledState(Control c, boolean changeable) {
        if (c instanceof Text) {
            setEnabled((Text)c, changeable);
            return;
        }
        if (c instanceof Label) {
            if (formToolkit == null) {
                // grayed label text only in dialogs
                setForegroundColor(c, changeable);
            }
            return;
        }
        if (c instanceof Group) {
            setForegroundColor(c, changeable);
        }
        if (isEnabledRelevant(c)) {
            setControlEnabled(c, changeable);
        }
    }

    /**
     * In case a combo is disabled it can neither show a tool-tip nor display its contents (if they
     * are too long). In that case use its parent to display the content as a tool-tip.
     */
    private void setTooltipIfNecessary(Control c, boolean changeable) {
        if (c instanceof Combo && !changeable) {
            Combo combo = (Combo)c;
            combo.getParent().setToolTipText(combo.getText());
        }
    }

    private void setDataChangeableChildren(Control c, boolean changeable) {
        if (c instanceof Composite) {
            Control[] children = ((Composite)c).getChildren();
            for (Control element : children) {
                setDataChangeable(element, changeable);
            }
        }
    }

    /**
     * Checks whether the given control's data are changeable according to
     * {@link #setDataChangeable(Control, boolean)}. In contrast to
     * {@link #setDataChangeable(Control, boolean)} this method could only checks the given control
     * and do not check any child.
     * 
     * @return <code>true</code> if the data seems to be changeable, that means it is not set to be
     *         read-only by {@link #setDataChangeable(Control, boolean)}. That does not necessarily
     *         means that the control is enabled. For example a {@link Text} control may be editable
     *         (by checking {@link Text#getEditable()} but disabled by checking
     *         {@link Text#isEnabled()}.
     */
    public boolean isDataChangeable(Control c) {
        if (c == null) {
            return false;
        } else if (c instanceof IDataChangeableReadAccess) {
            return ((IDataChangeableReadAccess)c).isDataChangeable();
        }
        Object dataChangeable = c.getData(DATA_CHANGEABLE);
        return dataChangeable instanceof Boolean ? Boolean.TRUE.equals(dataChangeable) : true;
    }

    /**
     * Setting the enabled state of a control and all its child controls recursively. In contrast to
     * normal SWT enabled state, we do not set Text controls to enabled=false because we'd like to
     * select and copy text also in disabled text controls.
     * 
     * @param c The control that should be set to enabled or disabled
     * @param enabled <code>true</code> to mark the control as enabled, <code>false</code> to
     *            disable.
     */
    public void setEnabled(Control c, boolean enabled) {
        if (c == null || isEnabled(c) == enabled) {
            return;
        }
        c.setData(ENABLED, enabled);
        updateEnabledState(c, enabled);
        setEnabledChildren(c, enabled);
    }

    /**
     * Setting a text control to enabled by changing the editable state and changing the text color
     * to the disabled color. In contrast to real disabling the text stay selectable and better
     * readable.
     */
    public void setEnabled(Text text, boolean enabled) {
        text.setEditable(enabled);
        setForegroundColor(text, enabled);
        if (formToolkit == null) {
            // grayed text background only in dialogs
            setBackgroundColor(text, enabled);
        }
    }

    private static boolean isEnabledRelevant(Control c) {
        return c instanceof Checkbox || c instanceof Combo || c instanceof Button || c instanceof ToolBar;
    }

    private void setControlEnabled(Control c, boolean changeable) {
        c.setEnabled(changeable);
        setTooltipIfNecessary(c, changeable);
    }

    private void setEnabledChildren(Control c, boolean changeable) {
        if (c instanceof Composite) {
            Control[] children = ((Composite)c).getChildren();
            for (Control element : children) {
                setEnabled(element, changeable);
            }
        }
    }

    public boolean isEnabled(Control c) {
        if (c != null && !c.isDisposed()) {
            return c.isEnabled() && isMarkedAsEnabled(c);
        } else {
            return false;
        }
    }

    private boolean isMarkedAsEnabled(Control c) {
        if (c != null && !c.isDisposed()) {
            return BooleanUtils.isNotFalse((Boolean)c.getData(ENABLED));
        } else {
            return false;
        }
    }

    private void setForegroundColor(Control control, boolean changeable) {
        setColor(control, true, changeable);
    }

    private void setBackgroundColor(Control control, boolean changeable) {
        setColor(control, false, changeable);
    }

    private void setColor(Control control, boolean foreground, boolean changeable) {
        Color color = null;
        if (formToolkit == null) {
            if (!changeable) {
                color = control.getDisplay()
                        .getSystemColor(foreground ? FOREGROUND_COLOR_DISABLED : BACKGROUND_COLOR_DISABLED);
            } else {
                color = control.getDisplay()
                        .getSystemColor(foreground ? FOREGROUND_COLOR_ENABLED : BACKGROUND_COLOR_ENABLED);
            }
        } else {
            color = getColorFromFormToolkit(changeable, foreground);
        }
        if (foreground) {
            control.setForeground(color);
        } else {
            control.setBackground(color);
        }
    }

    private Color getColorFromFormToolkit(boolean changeable, boolean foreground) {
        if (changeable) {
            return foreground ? formToolkit.getColors().getForeground() : formToolkit.getColors().getBackground();
        }
        String key = foreground ? READONLY_FOREGROUND_COLOR : READONLY_BACKGROUND_COLOR;
        Color color = formToolkit.getColors().getColor(key);
        if (color == null) {
            return formToolkit.getColors().createColor(key, formToolkit.getColors()
                    .getSystemColor((foreground ? FOREGROUND_COLOR_DISABLED : BACKGROUND_COLOR_DISABLED)));
            // color will be disposed by the FormColors#colorRegistry
        }
        return formToolkit.getColors().getColor(key);
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
        Composite composite = createGridComposite(parent, numColumns, equalSize, margin,
                new GridData(GridData.FILL_BOTH));
        return composite;
    }

    /**
     * Creates a new composite with a grid layout and no borders.
     */
    public Composite createGridComposite(Composite parent,
            int numColumns,
            boolean equalSize,
            boolean margin,
            GridData gridData) {
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
        layout.horizontalSpacing = 10;
        newComposite.setLayout(layout);
        newComposite.setLayoutData(gridData);
        return newComposite;
    }

    /**
     * Creates a new sash form with a grid layout and no borders.
     */
    public SashForm createSashForm(Composite parent, int numColumns, boolean equalSize, boolean margin) {
        return createSashForm(parent, numColumns, equalSize, margin, new GridData(GridData.FILL_BOTH));
    }

    public SashForm createSashForm(Composite parent,
            int numColumns,
            boolean equalSize,
            boolean margin,
            GridData gridData) {
        SashForm newSashForm = new SashForm(parent, SWT.NONE);
        if (formToolkit != null) {
            formToolkit.adapt(newSashForm, true, true);
        }
        GridLayout layout = new GridLayout(numColumns, equalSize);
        if (!margin) {
            layout.marginHeight = 0;
            layout.marginWidth = 0;
        }
        layout.horizontalSpacing = 10;
        newSashForm.setLayout(layout);
        newSashForm.setLayoutData(gridData);
        return newSashForm;
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
     * Creates a new label with the given text.
     * 
     * @param parent The parent composite.
     * @param text The text to display.
     * @param grab <code>true</code> to let the label grab more space then initial preferred, if
     *            available.
     */
    public Label createLabel(Composite parent, String text, boolean grab) {
        Label label = createLabel(parent, text);
        grabHorizontalSpace(label, grab);
        grabVerticalSpace(label, grab);
        return label;
    }

    /**
     * Assumes the control has a GridData object as layout data. Sets "grabExcessHorizontalSpace" to
     * the given value.
     */
    public void grabHorizontalSpace(Control c, boolean grab) {
        GridData gd = (GridData)c.getLayoutData();
        gd.grabExcessHorizontalSpace = grab;
    }

    /**
     * Assumes the control has a GridData object as layout data. Sets "grabExcessVerticalSpace" to
     * the given value.
     */
    public void grabVerticalSpace(Control c, boolean grab) {
        GridData gd = (GridData)c.getLayoutData();
        gd.grabExcessVerticalSpace = grab;
    }

    /**
     * Creates a new label.
     */
    public Label createLabel(Composite parent, String text) {
        return createLabel(parent, text, SWT.NONE);
    }

    public Label createLabel(Composite parent, String text, int style) {
        return createLabel(parent, text, style, new GridData(SWT.FILL, SWT.CENTER, false, false));
    }

    public Label createLabel(Composite parent, String text, int style, GridData gridData) {
        Label newLabel;
        if (formToolkit != null) {
            newLabel = formToolkit.createLabel(parent, text, style);
        } else {
            newLabel = new Label(parent, style);
            // newLabel.setBackground(parent.getBackground());
            if (text != null) {
                newLabel.setText(text);
            }
        }
        newLabel.setLayoutData(gridData);
        return newLabel;
    }

    /**
     * Creates a new form label with margin.
     */
    public Label createFormLabel(Composite parent, String text) {
        Composite c = createComposite(parent);
        c.setLayoutData(new GridData(GridData.FILL, SWT.CENTER, false, false));
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.marginHeight = 1;
        layout.marginWidth = 0;
        c.setLayout(layout);
        Label newLabel = this.createLabel(c, text);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
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

    public Control createHorizontalSpacer(Composite parent, int width) {
        Composite spacer = new Composite(parent, SWT.NONE);
        spacer.setLayout(new GridLayout());
        GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL);
        data.heightHint = 1;
        data.widthHint = width;
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
        newLink.setLayoutData(new GridData(SWT.FILL | SWT.CENTER));
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

    public Text createTextAppendStyle(Composite parent, int style) {
        if (formToolkit != null) {
            return createText(parent, style);
        } else {
            return createText(parent, SWT.SINGLE | SWT.BORDER | style);
        }
    }

    public Text createText(Composite parent) {
        if (formToolkit != null) {
            return createText(parent, SWT.NONE);
        } else {
            return createText(parent, SWT.SINGLE | SWT.BORDER);
        }
    }

    public StyledText createStyledTextAppendStyle(Composite parent, int style) {
        if (formToolkit != null) {
            return createStyledText(parent, style);
        } else {
            return createStyledText(parent, SWT.SINGLE | SWT.BORDER | style);
        }
    }

    public StyledText createStyledText(Composite parent, int style) {
        StyledText newText;
        if (formToolkit != null) {
            newText = new StyledText(parent,
                    formToolkit.getBorderStyle() | SWT.BORDER | style | formToolkit.getOrientation());
            newText.setForeground(formToolkit.getColors().getForeground());
            newText.setBackground(formToolkit.getColors().getBackground());
        } else {
            newText = new StyledText(parent, style);
        }
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = DEFAULT_WIDTH;
        newText.setLayoutData(gridData);
        return newText;
    }

    public Text createMultilineText(Composite parent) {
        Text newText;
        if (formToolkit != null) {
            newText = formToolkit.createText(parent, null, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
        } else {
            newText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
        }
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = DEFAULT_MULTILINE_HEIGHT;
        gridData.widthHint = DEFAULT_WIDTH;
        newText.setLayoutData(gridData);

        return newText;
    }

    /**
     * Does not have an implementation for use with {@link FormToolkit}!
     * 
     * @param parent Reference to the container widget.
     * @return New instance with default setup.
     */
    public StyledText createStyledMultilineText(Composite parent) {
        StyledText newText;
        newText = new StyledText(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = DEFAULT_MULTILINE_HEIGHT;
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
     * 
     * Use {@link Button} and {@link ButtonField} instead. {@link ButtonField} also allows inverting
     * the checked state of a check box.
     * 
     * @deprecated as of FIPS 3.11
     */
    @Deprecated
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

    /**
     * Creates a new push button.
     */
    public Button createButton(Composite parent, String text, int style) {
        if (formToolkit != null) {
            return formToolkit.createButton(parent, text, style);
        }
        Button newButton = new Button(parent, style);
        newButton.setText(text);
        return newButton;
    }

    /**
     * Creates a new toggle button.
     */
    public Button createToggleButton(Composite parent, String text) {
        if (formToolkit != null) {
            return formToolkit.createButton(parent, text, SWT.TOGGLE);
        }
        Button newButton = new Button(parent, SWT.TOGGLE);
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
    public IpsPckFragmentRootRefControl createPdPackageFragmentRootRefControl(Composite parent,
            boolean onlySourceRoots) {
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
     * Creates a new FilteredIpsObjectTypeRefControl.
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
     * Creates a new Combo-Box. Note that the FormToolkit does not support Combos, so the appearance
     * of this Combo-Box is NOT similar to the other FormToolkit-Controls.
     */
    public Combo createCombo(Composite parent) {
        Combo newCombo = new Combo(parent, SWT.READ_ONLY);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = DEFAULT_WIDTH;
        newCombo.setLayoutData(gridData);
        return newCombo;
    }

    /**
     * @deprecated use {@link #createCombo(Composite)} and
     *             {@link BindingContext#bindContent(Combo, Object, String, Class)} to set the list
     *             of valid values instead.
     */
    @Deprecated
    public <E extends Enum<E>> Combo createCombo(Composite parent, Class<E> enumType) {
        E[] values = enumType.getEnumConstants();
        return createCombo(parent, values);
    }

    /**
     * @deprecated use {@link #createCombo(Composite)} and
     *             {@link BindingContext#bindContent(Combo, Object, String, Enum[])} to set the list
     *             of valid values instead.
     */
    @Deprecated
    public <E extends Enum<E>> Combo createCombo(Composite parent, E[] values) {
        return createCombo(parent, values, INamedValue::getName);
    }

    private <E> Combo createCombo(Composite parent,
            E[] values,
            Function<? super E, String> toString) {
        Combo newCombo = createCombo(parent);
        String[] names = Arrays.stream(values).map(toString).toArray(String[]::new);
        newCombo.setItems(names);
        return newCombo;
    }

    /**
     * Creates a combo containing the given <code>EnumDatatype</code>'s values as items. The
     * formatting is done based on the user's preferences.
     * 
     * @see org.faktorips.devtools.core.IpsPreferences#getNamedDataTypeDisplay()
     */
    public Combo createCombo(Composite parent, EnumDatatype datatype) {
        Combo newCombo = createCombo(parent);
        if (datatype.isSupportingNames()) {
            String[] ids = datatype.getAllValueIds(true);
            ArrayList<String> nameList = new ArrayList<>(ids.length);
            for (String id : ids) {
                String formatedText = IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(datatype, id);
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
            setComboValues(newCombo, new String[] { IpsPlugin.getDefault().getIpsPreferences().getNullPresentation(),
                    trueRepresentation, falseRepresentation });
        } else {
            setComboValues(newCombo, new String[] { trueRepresentation, falseRepresentation });
        }
        return newCombo;
    }

    public Composite createCheckboxSetForBoolean(Composite parent,
            String trueRepresentation,
            String falseRepresentation) {
        Composite newComposite = createGridComposite(parent, 3, true, true);
        createCheckbox(newComposite, trueRepresentation);
        createCheckbox(newComposite, falseRepresentation);
        createCheckbox(newComposite, IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        return newComposite;
    }

    /**
     * Replaces all occurrences of <code>null</code> in the values by the defined null
     * representation. The result is set as items to the given combo.
     * 
     * @param combo The combo to set the values.
     * @param comboValues The values to set.
     */
    private void setComboValues(Combo combo, String[] comboValues) {
        String[] values = Arrays.copyOf(comboValues, comboValues.length);
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
                String formatedText = IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(dataType,
                        enumValueSet.getValue(i));
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

    /**
     * @deprecated use {@link #createRadioButtonGroup(Composite, String, int, LinkedHashMap)}
     *             instead
     */
    @Deprecated
    public RadioButtonGroup<?> createRadiobuttonGroup(Composite parent, int style, String text) {
        return new RadioButtonGroup<>(parent, style, text, this);
    }

    /**
     * Creates and returns a new {@link RadioButtonGroup} with the provided group text, number of
     * columns and options.
     * 
     * @param groupText the text that will be shown as title of the group
     * @param numberColumns the number of radio buttons that will be horizontally placed beside each
     *            other
     * @param options the options the user can choose from. The map associates each value with it's
     *            label. For each option, a radio button is created
     */
    public <T> RadioButtonGroup<T> createRadioButtonGroup(Composite parent,
            String groupText,
            int numberColumns,
            LinkedHashMap<T, String> options) {

        return new RadioButtonGroup<>(parent, groupText, numberColumns, options, this);
    }

    /**
     * Creates a new {@link RadioButtonGroup} without creating a {@link Group}. The radio buttons
     * are created directly in the parent composite. You have the choice if the parent composite
     * should be a group, a native composite or any other kind of composite. You also have to
     * specify the layout of the parent yourself.
     * 
     * @param parent The parent composite for the radio buttons
     * @param options the options the user can choose from. The map associates each value with it's
     *            label. For each option, a radio button is created
     */
    public <T> RadioButtonGroup<T> createRadioButtonGroup(Composite parent, LinkedHashMap<T, String> options) {
        Composite newComposite = createGridComposite(parent, options.size(), false, false,
                new GridData(SWT.LEAD, SWT.TOP, false, false));
        ((GridLayout)newComposite.getLayout()).horizontalSpacing = 20;
        return new RadioButtonGroup<>(newComposite, options, this);
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

    public EnumControl createEnumControl(Composite parent) {
        return new EnumControl(parent, this);
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
    public static int convertToJFaceSeverity(Severity severity) {
        switch (severity) {
            case ERROR:
                return IMessageProvider.ERROR;
            case INFO:
                return IMessageProvider.INFORMATION;
            case WARNING:
                return IMessageProvider.WARNING;
            case NONE:
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

    /**
     * Attaches a {@link ContentProposalAdapter} to the given control, thereby adding content
     * proposal support.
     * 
     * @param control The control to add content proposal support to
     * @param contentAdapter Specifies how to set and retrieve data of the control
     * @param proposalProvider Provides content proposals as appropriate to the control's current
     *            content
     * @param labelProvider Specifies how the content proposals are shown to the user
     */
    public void attachContentProposalAdapter(Control control,
            IControlContentAdapter contentAdapter,
            IContentProposalProvider proposalProvider,
            ILabelProvider labelProvider) {

        KeyStroke keyStroke = null;
        try {
            keyStroke = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
        } catch (final ParseException e) {
            throw new IllegalArgumentException("KeyStroke \"Ctrl+Space\" could not be parsed.", e); //$NON-NLS-1$
        }
        ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(control, contentAdapter,
                proposalProvider, keyStroke, null);
        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        contentProposalAdapter.setLabelProvider(labelProvider);
    }

    /**
     * Attaches a {@link ContentProposalAdapter} to the given control, thereby adding content
     * proposal support.
     * 
     * @param control The control to add content proposal support to
     * @param proposalProvider Provides content proposals as appropriate to the control's current
     *            content
     * @param labelProvider Specifies how the content proposals are shown to the user
     */
    public void attachContentProposalAdapter(Control control,
            IContentProposalProvider proposalProvider,
            ILabelProvider labelProvider) {
        attachContentProposalAdapter(control, proposalProvider, ContentProposalAdapter.PROPOSAL_REPLACE, labelProvider);
    }

    /**
     * Attaches a {@link ContentProposalAdapter} to the given control, thereby adding content
     * proposal support.
     * 
     * @param control The control to add content proposal support to
     * @param proposalProvider Provides content proposals as appropriate to the control's current
     *            content
     * @param proposalAcceptanceStyle The style of the acceptance:
     *            {@link ContentProposalAdapter#PROPOSAL_IGNORE},
     *            {@link ContentProposalAdapter#PROPOSAL_REPLACE} or
     *            {@link ContentProposalAdapter#PROPOSAL_INSERT}
     * @param labelProvider Specifies how the content proposals are shown to the user. May be null
     *            to simply provide {@link ContentProposal#getLabel()}
     * @return The created {@link ContentProposalAdapter}
     */
    public ContentProposalAdapter attachContentProposalAdapter(Control control,
            IContentProposalProvider proposalProvider,
            int proposalAcceptanceStyle,
            ILabelProvider labelProvider) {
        KeyStroke keyStroke = null;
        try {
            keyStroke = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
        } catch (final ParseException e) {
            throw new IllegalArgumentException("KeyStroke \"Ctrl+Space\" could not be parsed.", e); //$NON-NLS-1$
        }
        ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(control, new TextContentAdapter(),
                proposalProvider, keyStroke, null);
        contentProposalAdapter.setProposalAcceptanceStyle(proposalAcceptanceStyle);
        contentProposalAdapter.setLabelProvider(labelProvider);
        if (proposalProvider instanceof ICachedContentProposalProvider) {
            final ICachedContentProposalProvider cachedContentProposalProvider = (ICachedContentProposalProvider)proposalProvider;
            contentProposalAdapter.addContentProposalListener(new IContentProposalListener2() {

                @Override
                public void proposalPopupOpened(ContentProposalAdapter adapter) {
                    cachedContentProposalProvider.clearCache();
                }

                @Override
                public void proposalPopupClosed(ContentProposalAdapter adapter) {
                    // nothing to do
                }
            });
        }
        // if the control is located in a section, we need to provide this section to the
        // ControlDecoration, otherwise the decoration would also appear when section is closed
        Composite section = getSectionClientArea(control.getParent());
        ControlDecoration controlDecoration = new ControlDecoration(control, SWT.TOP | SWT.LEFT, section);
        FieldDecoration decoration = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
        controlDecoration.setImage(decoration.getImage());
        controlDecoration.setDescriptionText(decoration.getDescription());
        controlDecoration.setShowOnlyOnFocus(true);
        return contentProposalAdapter;
    }

    /**
     * Create a new {@link MessageDecoration} for the given {@link Control}. Use the returned
     * {@link MessageDecoration} to show error markers next to the control.
     * <p>
     * Note: The position of the marker is on the left-bottom side of the control. Use
     * {@link #createMessageDecoration(Control, int)} to specify another position.
     * 
     * @param control the control on which to install the cue
     */
    public MessageDecoration createMessageDecoration(Control control) {
        return createMessageDecoration(control, SWT.LEFT | SWT.BOTTOM);
    }

    /**
     * Create a new {@link MessageDecoration} for the given {@link Control}. Use the returned
     * {@link MessageDecoration} to show error markers next to the control.
     * 
     * @param control the control on which to install the cue
     * @param position use {@link SWT#TOP}, {@link SWT#CENTER} or {@link SWT#BOTTOM} to trigger the
     *            position of the marker
     */
    public MessageDecoration createMessageDecoration(Control control, int position) {
        return new MessageDecoration(control, position, getSectionClientArea(control.getParent()));
    }

    private Composite getSectionClientArea(Composite composite) {
        if (composite != null && composite.getParent() instanceof Section) {
            return composite;
        } else if (composite != null) {
            return getSectionClientArea(composite.getParent());
        } else {
            return null;
        }
    }

    /**
     * Draws borders around all controls in the given composite. A border is drawn also for the
     * composite itself if its parent is also configured to paint borders. This affects text-,
     * combo-, tree-, table- and child composite-controls in the given composite. Does nothing if
     * the form-toolkit is <code>null</code>.
     * <p>
     * This method is used when developing a form UI, e.g. the product component editor.
     * <p>
     * Note that the composite requires at least one pixel of space to actually draw the border. The
     * border is drawn <em>around</em> the controls, not inside of them. Make sure the layout
     * ensures this, e.g. by using grid layout with a marginWidth and marginHeight of at least 1.
     * 
     */
    public void paintBordersForComposite(Composite composite) {
        if (getFormToolkit() != null) {
            composite.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
            getFormToolkit().paintBordersFor(composite);
        }
    }

    /**
     * Draws borders around the given text field. Does nothing if the form-toolkit is
     * <code>null</code>.
     * <p>
     * This method is used when developing a form UI, e.g. the product component editor.
     * <p>
     * Note that the text's parent composite requires at least one pixel of space to actually draw
     * the border. The border is drawn <em>around</em> the text control, not inside of it. Make sure
     * the layout ensures this, e.g. by using grid layout with a marginWidth and marginHeight of at
     * least 1.
     * 
     */
    public void paintBorderFor(Text text) {
        if (getFormToolkit() != null) {
            text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
            getFormToolkit().paintBordersFor(text.getParent());
        }
    }

}
