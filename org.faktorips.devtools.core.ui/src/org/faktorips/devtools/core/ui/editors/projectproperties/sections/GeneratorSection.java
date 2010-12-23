/*
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * *****************************************************************************
 */

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.preferencepages.BuilderSetPropertyEditingSupport;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

public class GeneratorSection extends IpsSection {
    private IIpsArtefactBuilderSetConfigModel builderSetConfig;
    private static final int COLUMNS_COUNT = 3;
    public static final int PROPERTY_NAME_COLUMN_INDEX = 0;
    public static final int PROPERTY_VALUE_COLUMN_INDEX = 1;
    public static final int PROPERTY_DESCRIPTION_COLUMN_INDEX = 2;
    private TableViewer tableViewer;
    private TableViewerColumn[] columns;
    private IIpsArtefactBuilderSetConfigModel builderSetConfigModel;
    private IIpsProject ipsProject;
    private String builderSetId;
    private ComboField builderSetComboField;
    private IIpsProjectProperties iIpsProjectProperties;

    public GeneratorSection(IIpsProject iIpsProject, IIpsArtefactBuilderSetConfigModel builderSetConfig,
            IIpsProjectProperties iIpsProjectProperties, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);
        this.builderSetConfig = builderSetConfig;
        this.ipsProject = iIpsProject;
        this.builderSetConfigModel = iIpsProjectProperties.getBuilderSetConfig();
        this.builderSetId = ipsProject.getProperties().getBuilderSetId();
        this.iIpsProjectProperties = iIpsProjectProperties;

        initControls();
        setText(Messages.Generator_title);
    }

    private void createBuilderSetCombo(Composite parent) {
        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        l.setText("Generator");

        Combo builderSetCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        builderSetCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        builderSetComboField = new ComboField(builderSetCombo);
        // builderSetComboField.addChangeListener(adapter);

        List<IIpsArtefactBuilderSetInfo> builderSetInfos = getBuilderSetInfos();
        String[] builderSetLabels = new String[builderSetInfos.size()];

        // indices out of bounds are ignored in Combo.select()
        int currentBuilderSetIndex = -1;
        for (int i = 0; i < builderSetInfos.size(); i++) {
            IIpsArtefactBuilderSetInfo info = builderSetInfos.get(i);
            String builderSetId = info.getBuilderSetId();
            if (iIpsProjectProperties.getBuilderSetId().equals(builderSetId)) {
                currentBuilderSetIndex = i;
            }
            builderSetLabels[i] = info.getBuilderSetLabel();
        }

        builderSetComboField.getCombo().setItems(builderSetLabels);
        builderSetComboField.getCombo().select(currentBuilderSetIndex);
    }

    private List<IIpsArtefactBuilderSetInfo> getBuilderSetInfos() {
        List<IIpsArtefactBuilderSetInfo> builderSetInfos = new ArrayList<IIpsArtefactBuilderSetInfo>();

        IIpsModel ipsModel = ipsProject.getIpsModel();
        IpsArtefactBuilderSetInfo.loadExtensions(Platform.getExtensionRegistry(), IpsPlugin.getDefault().getLog(),
                builderSetInfos, ipsModel);

        return builderSetInfos;
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        createBuilderSetCombo(client);
        Composite composite = toolkit.createGridComposite(client, 2, false, false);
        // Label label = new Label(composite, SWT.NONE);
        // label.setText(Messages.Generator_tableViewerLabel);
        //
        // final Table table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL |
        // SWT.SINGLE
        // | SWT.FULL_SELECTION);
        // table.setLinesVisible(true);
        // table.setHeaderVisible(true);
        // GridData gridData = new GridData(GridData.FILL_BOTH);
        // gridData.grabExcessHorizontalSpace = true;
        // gridData.grabExcessVerticalSpace = true;
        // gridData.horizontalSpan = 3;
        // table.setLayoutData(gridData);
        //
        // tableViewer = new TableViewer(table);
        // tableViewer.setContentProvider(new BuilderSetContentProvider());
        //
        // String[] columnNames = new String[COLUMNS_COUNT];
        // columnNames[PROPERTY_NAME_COLUMN_INDEX] = Messages.Generator_tableColumnLabel_Property;
        // columnNames[PROPERTY_VALUE_COLUMN_INDEX] = Messages.Generator_tableColumnLabel_Value;
        // columnNames[PROPERTY_DESCRIPTION_COLUMN_INDEX] = Messages.Generator_title;
        //
        // columns = new TableViewerColumn[columnNames.length];
        // for (int i = 0; i < columnNames.length; i++) {
        // columns[i] = new TableViewerColumn(tableViewer, SWT.LEFT, i);
        // columns[i].getColumn().setText(columnNames[i]);
        // }
        //
        // columns[PROPERTY_NAME_COLUMN_INDEX].setLabelProvider(new ColumnLabelProvider() {
        // @Override
        // public String getText(Object element) {
        // return ((IIpsBuilderSetPropertyDef)element).getLabel();
        // }
        // });
        // columns[PROPERTY_VALUE_COLUMN_INDEX].setLabelProvider(new
        // BuilderSetPropertyLabelProvider(ipsProject,
        // builderSetConfigModel));
        // columns[PROPERTY_DESCRIPTION_COLUMN_INDEX].setLabelProvider(new ColumnLabelProvider() {
        // private static final int TOOLTIP_LINE_LENGTH = 75;
        //
        // @Override
        // public String getText(Object element) {
        // return ((IIpsBuilderSetPropertyDef)element).getDescription();
        // }
        //
        // @Override
        // public String getToolTipText(Object element) {
        // if (element instanceof IIpsBuilderSetPropertyDef) {
        // IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
        // String description = propertyDef.getDescription();
        //                    String wrappedText = StringUtils.wrapText(description, TOOLTIP_LINE_LENGTH, "\n"); //$NON-NLS-1$
        // return wrappedText;
        // }
        //                return ""; //$NON-NLS-1$
        // }
        //
        // @Override
        // public Point getToolTipShift(Object object) {
        // return new Point(5, 5);
        // }
        //
        // @Override
        // public int getToolTipDisplayDelayTime(Object object) {
        // return 200;
        // }
        //
        // @Override
        // public int getToolTipTimeDisplayed(Object object) {
        // return 10000;
        // }
        // });
        //
        // TableViewerFocusCellManager focusCellManager = new
        // TableViewerFocusCellManager(tableViewer,
        // new FocusCellOwnerDrawHighlighter(tableViewer));
        //
        // ColumnViewerEditorActivationStrategy actSupport = new
        // ColumnViewerEditorActivationStrategy(tableViewer) {
        // @Override
        // protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
        // return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
        // || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
        // || event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
        // || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
        // }
        // };
        // TableViewerEditor.create(tableViewer, focusCellManager, actSupport,
        // ColumnViewerEditor.TABBING_HORIZONTAL
        // | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
        // | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        // sort property name column if clicked on column header
        // columns[PROPERTY_NAME_COLUMN_INDEX].getColumn().addSelectionListener(adapter);

        // updateBuilderSet();
        IIpsArtefactBuilderSetInfo builderSetInfo = IpsPlugin.getDefault().getIpsModel().getIpsArtefactBuilderSetInfo(
                builderSetId);
        if (builderSetInfo != null) {
            IIpsBuilderSetPropertyDef[] propertyDefinitions = builderSetInfo.getPropertyDefinitions();
            for (IIpsBuilderSetPropertyDef propertyDef : propertyDefinitions) {
                Label label = toolkit.createLabel(composite, propertyDef.getLabel());
                label.setToolTipText(propertyDef.getDescription());
                String propertyValue = builderSetConfigModel.getPropertyValue(propertyDef.getName());
                if (propertyValue == null || "".equals(propertyValue)) { //$NON-NLS-1$
                    // value not set in .ipsproject file, use default
                    propertyValue = propertyDef.getDefaultValue(ipsProject);
                } else if (propertyDef.getName().equals("loggingFrameworkConnector")) { //$NON-NLS-1$
                    // Special treatment of qualified names:
                    // Prevent the table column to be too wide by removing package information from
                    // the following type. The full qualified name is shown only when the combo box
                    // is opened.
                    propertyValue = StringUtil.unqualifiedName(propertyValue);
                }
                String[] values = propertyDef.getDiscreteValues();
                if (values.length != 0) {
                    Combo combo = toolkit.createCombo(composite);
                    combo.setItems(values);
                    int index = getIndex(propertyValue, values);
                    if (index >= 0) {
                        combo.select(index);
                    }
                } else {
                    Combo combo = toolkit.createComboForBoolean(composite, false, "true", "false");
                    if ("true".equals(propertyValue)) {
                        combo.select(0);
                    } else {
                        combo.select(1);
                    }
                }
                // ComboViewer dd = new ComboViewer(combo);
                // propertyValue
            }
        }
    }

    private int getIndex(String propertyname, String[] propertynames) {
        int index = -1;
        int i = 0;
        for (String name : propertynames) {
            if (propertyname.equals(name)) {
                index = i;
            }
            i++;
        }
        return index;
    }

    // if (propertyValue.equals("true") || propertyValue.equals("false")) {
    // createCombo(propertyValue, composite, toolkit);
    // } else {
    // createText(propertyValue, composite, toolkit);
    // }
    // }

    @Override
    protected void performRefresh() {
        // TODO Auto-generated method stub

    }

    public void saveProperties() {
        // builderSetConfig.setPropertyValue(propertyName, value);
    }

    /** Updates the page's widgets to reflect the model change */
    private void updateBuilderSet() {
        columns[PROPERTY_VALUE_COLUMN_INDEX].setEditingSupport(new BuilderSetPropertyEditingSupport(tableViewer,
                ipsProject, builderSetConfigModel, builderSetId));

        tableViewer.getTable().setSortDirection(SWT.UP);
        tableViewer.getTable().setSortColumn(columns[PROPERTY_NAME_COLUMN_INDEX].getColumn());
        tableViewer.setSorter(new BuilderSetPropertyDefSorter(ipsProject));
        tableViewer.setInput(builderSetId);

        ColumnViewerToolTipSupport.enableFor(tableViewer);
        updateColumnWidths();
    }

    private void updateColumnWidths() {
        columns[PROPERTY_NAME_COLUMN_INDEX].getColumn().pack();
        columns[PROPERTY_VALUE_COLUMN_INDEX].getColumn().pack();

        // avoid the horizontal scrollbar to be shown by decreasing the width of the
        // third tableViewerColumn by 5 pixels (taking a minimal width of 150 into account)
        int thirdColWidth = 150;
        thirdColWidth = Math.max(thirdColWidth, tableViewer.getControl().getSize().x - 5
                - columns[PROPERTY_NAME_COLUMN_INDEX].getColumn().getWidth()
                - columns[PROPERTY_VALUE_COLUMN_INDEX].getColumn().getWidth());

        columns[PROPERTY_DESCRIPTION_COLUMN_INDEX].getColumn().setWidth(thirdColWidth);
    }

    private final class BuilderSetPropertyDefSorter extends ViewerSorter {

        private IIpsProject ipsProject;

        public BuilderSetPropertyDefSorter(IIpsProject ipsProject) {
            ArgumentCheck.notNull(ipsProject);
            this.ipsProject = ipsProject;
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            int direction = ((TableViewer)viewer).getTable().getSortDirection();

            boolean firstIsModifiable = (e1 instanceof IIpsBuilderSetPropertyDef && ((IIpsBuilderSetPropertyDef)e1)
                    .isAvailable(ipsProject));
            boolean secondIsModifiable = (e2 instanceof IIpsBuilderSetPropertyDef && ((IIpsBuilderSetPropertyDef)e2)
                    .isAvailable(ipsProject));

            if (firstIsModifiable && secondIsModifiable) {
                String s1 = getPropertyByColumn((TableViewer)viewer, (IIpsBuilderSetPropertyDef)e1);
                String s2 = getPropertyByColumn((TableViewer)viewer, (IIpsBuilderSetPropertyDef)e2);
                if (direction == SWT.UP) {
                    return s1.compareTo(s2);
                } else {
                    return s2.compareTo(s1);
                }
            }
            if (firstIsModifiable) {
                return -1;
            }
            if (secondIsModifiable) {
                return 1;
            }

            // non-modifiable elements are unsorted at the end
            return 0;
        }

        private String getPropertyByColumn(TableViewer viewer, IIpsBuilderSetPropertyDef propertyDef) {
            if (viewer.getTable().getSortColumn() == columns[PROPERTY_NAME_COLUMN_INDEX].getColumn()) {
                return propertyDef.getName();
            }
            return "";
        }
    }
    /**
     * Restores the defaults for an IPS project's currently selected builder set.
     */
    // public void performDefaults() {
    // String builderSetLabel = (String)builderSetComboField.getValue();
    // if (builderSetLabel != null) {
    // builderSetId = getBuilderSetIdByLabel(builderSetLabel);
    // ipsProjectProperties = ipsProject.getProperties();
    // ipsProjectProperties.setBuilderSetId(builderSetId);
    //
    // IIpsArtefactBuilderSetInfo ipsArtefactBuilderSetInfo = IpsPlugin.getDefault().getIpsModel()
    // .getIpsArtefactBuilderSetInfo(builderSetId);
    // builderSetConfigModel = ipsArtefactBuilderSetInfo.createDefaultConfiguration(ipsProject);
    // ipsProjectProperties.setBuilderSetConfig(builderSetConfigModel);
    //
    // updateBuilderSet();
    // }
    // }
}
