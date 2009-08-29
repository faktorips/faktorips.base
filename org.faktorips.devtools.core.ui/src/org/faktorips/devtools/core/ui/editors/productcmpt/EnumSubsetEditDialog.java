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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.ui.controller.fields.DefaultEditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controls.valuesets.EnumValueSetChooser;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 *
 */
public class EnumSubsetEditDialog extends IpsPartEditDialog {

    private IConfigElement configElement;
    private IEnumValueSet source;
    private ValueDatatype valueDatatype;

    // edit fields
    private DefaultEditField defaultValueField;

    private boolean viewOnly;

    public EnumSubsetEditDialog(IConfigElement configEl, EnumDatatype datatype, Shell parentShell, boolean viewOnly) {
        this(null, configEl, datatype, parentShell, viewOnly);
    }

    public EnumSubsetEditDialog(IEnumValueSet source, IConfigElement configEl, ValueDatatype datatype,
            Shell parentShell, boolean viewOnly) {
        super(configEl, parentShell, Messages.PolicyAttributeEditDialog_editLabel, true);
        configElement = configEl;
        this.source = source;
        valueDatatype = datatype;
        this.viewOnly = viewOnly;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.PolicyAttributeEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        createDescriptionTabItem(folder);
        super.setEnabledDescription(!viewOnly);

        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        createControlsForDefaultValue(workArea);

        Control valueSetControl = createEnumValueSetChooser(workArea);
        GridData valueSetGridData = new GridData(GridData.FILL_BOTH);
        valueSetGridData.horizontalSpan = 2;
        valueSetControl.setLayoutData(valueSetGridData);
        valueSetControl.setEnabled(!viewOnly);
        return c;
    }

    private void createControlsForDefaultValue(Composite workArea) {
        uiToolkit.createFormLabel(workArea, Messages.PolicyAttributeEditDialog_defaultValue);
        Combo combo = uiToolkit.createCombo(workArea);
        defaultValueField = new EnumValueSetField(combo, source, valueDatatype);
    }

    private Composite createEnumValueSetChooser(Composite workArea) {
        EnumValueSetChooser chooser = new Chooser(workArea);
        chooser.setSourceLabel(Messages.DefaultsAndRangesEditDialog_additionalValuesDefinedInModel);
        chooser.setTargetLabel(Messages.DefaultsAndRangesEditDialog_valueDefinedInProductCmpt);
        return chooser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(defaultValueField, IConfigElement.PROPERTY_VALUE);
    }

    class Chooser extends EnumValueSetChooser {

        public Chooser(Composite parent) {
            super(parent, uiToolkit, source, (IEnumValueSet)configElement.getValueSet(), valueDatatype, uiController);
        }

        @Override
        public MessageList getMessagesForValue(String valueId) {
            MessageList list = new MessageList();
            if (getSourceValueSet().containsValue(valueId)) {
                return list;
            }
            String text = NLS.bind(Messages.DefaultsAndRangesEditDialog_valueNotContainedInValueSet, valueId,
                    getSourceValueSet().toShortString());
            list.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return list;
        }

    }

}
