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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.ui.controls.chooser.EnumValueSubsetChooserModel;
import org.faktorips.devtools.core.ui.controls.chooser.SubsetChooserViewer;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;

/**
 * A dialog that allows to specify a subset of values available in a given enum data type.
 * 
 * @author Jan Ortmann
 */
public class EnumSubsetEditDialog extends IpsPartEditDialog {
    /**
     * Prefix to store the settings (size/position) of this dialog, used together with the
     * attribute.
     */
    private static final String SETTINGS_KEY_PREFIX = "EnumSubsetEditDialog_"; //$NON-NLS-1$

    /** Initial width of the dialog. */
    private static final int INITIAL_WIDTH = 500;

    /** Initial height of the dialog. */
    private static final int INITIAL_HEIGHT = 400;

    private ValueDatatype valueDatatype;

    private boolean viewOnly;

    private final IEnumValueSetProvider enumValueSetProvider;

    public EnumSubsetEditDialog(IEnumValueSetProvider provider, ValueDatatype datatype, Shell parentShell,
            boolean viewOnly) {
        super(provider.getTargetConfigElement(), parentShell, Messages.PolicyAttributeEditDialog_editLabel, true);
        this.enumValueSetProvider = provider;
        valueDatatype = datatype;
        this.viewOnly = viewOnly;
        setDescriptionEnabled(!(viewOnly));
        enableDialogSizePersistence(SETTINGS_KEY_PREFIX, datatype.getQualifiedName(), new Point(INITIAL_WIDTH,
                INITIAL_HEIGHT), null);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.PolicyAttributeEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Control valueSetControl = createEnumValueSetChooser(c);
        GridData valueSetGridData = new GridData(GridData.FILL_BOTH);
        valueSetGridData.horizontalSpan = 2;
        valueSetControl.setLayoutData(valueSetGridData);
        valueSetControl.setEnabled(!viewOnly);
        return c;
    }

    private Composite createEnumValueSetChooser(Composite workArea) {
        SubsetChooserViewer viewer = new SubsetChooserViewer(workArea, getToolkit());
        EnumValueSubsetChooserModel model = new EnumValueSubsetChooserModel(
                enumValueSetProvider.getSourceEnumValueSet(), valueDatatype, (IEnumValueSet)enumValueSetProvider
                        .getTargetConfigElement().getValueSet());
        viewer.init(model);
        viewer.setSourceLabel(enumValueSetProvider.getSourceLabel());
        viewer.setTargetLabel(enumValueSetProvider.getTargetLabel());
        return viewer.getChooserComposite();
    }
}
