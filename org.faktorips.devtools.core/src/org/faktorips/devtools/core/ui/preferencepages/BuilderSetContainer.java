/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.util.ArgumentCheck;

/**
 * Dialog for configuring IPS builder sets of an IPS project.
 * 
 * @author Roman Grutza
 */
public class BuilderSetContainer {

    private static final int COLUMNS_COUNT = 3;
    public static final int PROPERTY_NAME_COLUMN_INDEX = 0;
    public static final int PROPERTY_VALUE_COLUMN_INDEX = 1;
    public static final int PROPERTY_DESCRIPTION_COLUMN_INDEX = 2;

    private IIpsProject ipsProject;
    private String builderSetId;
    private IIpsProjectProperties ipsProjectProperties;
    private IIpsArtefactBuilderSetConfigModel builderSetConfigModel;

    // to detect changes made in dialog or outside of eclipse
    private long ipsprojectFileTimeStamp;
    private String builderSettingsSnapshot;

    private TableViewer tableViewer;
    private ComboField builderSetComboField;
    private TableViewerColumn[] columns;

    /**
     * @param ipsProject IPS project whose builder set configuration is to be altered. If ipsProject
     *            is null a NullPointerException is thrown.
     */
    public BuilderSetContainer(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        this.ipsProject = ipsProject;
        this.builderSetId = ipsProject.getIpsArtefactBuilderSet().getId();
        this.ipsProjectProperties = ipsProject.getProperties();
        this.builderSetConfigModel = ipsProjectProperties.getBuilderSetConfig();
        initializeTimeStamps();
    }

    /**
     * Returns a Control whose purpose is to provide a GUI for configuring an IPS project's builder
     * set.
     * @param parent The parent Composite
     * @return The created Control for configuring an IPS builder set
     */
    public Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        createBuilderSetCombo(composite);

        Label label = new Label(composite, SWT.HORIZONTAL | SWT.BAR);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 20;
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);

        label = new Label(composite, SWT.NONE);
        label.setText("Builder set properties (click on cell in value column to edit a property value):");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);

        createTableViewer(composite);

        return composite;
    }

    private void createBuilderSetCombo(Composite parent) {
        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        l.setText("Builder Set:");

        Combo builderSetCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        builderSetCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        builderSetComboField = new ComboField(builderSetCombo);
        builderSetComboField.addChangeListener(new ValueChangeListener() {

            private String previousBuilderSetId;

            public void valueChanged(FieldValueChangedEvent event) {
                String builderSetLabel = (String)event.field.getValue();
                String builderSetId = getBuilderSetIdByLabel(builderSetLabel);

                // save model for previously selected builderSetId, since builderSetPropertyDefs
                // could have been changed
                // and we want to restore the values if this builderSet is selected again
                if (previousBuilderSetId != null) {
                    event.field.getControl().setData(previousBuilderSetId, builderSetConfigModel);
                }

                // restore old model if available, else create a new one
                Object o = event.field.getControl().getData(builderSetId);
                if (o instanceof IIpsArtefactBuilderSetConfigModel) {
                    builderSetConfigModel = (IIpsArtefactBuilderSetConfigModel)o;
                } else {
                    IpsArtefactBuilderSetInfo info = getBuilderSetInfo(builderSetId);
                    builderSetConfigModel = info.createDefaultConfiguration(ipsProject);
                }

                previousBuilderSetId = builderSetId;
                ipsProjectProperties.setBuilderSetId(builderSetId);
                ipsProjectProperties.setBuilderSetConfig(builderSetConfigModel);

                columns[PROPERTY_VALUE_COLUMN_INDEX].setEditingSupport(new BuilderSetPropertyEditingSupport(
                        tableViewer, ipsProject, builderSetConfigModel, builderSetId));
                tableViewer.setLabelProvider(new PropertyDefValueLabelProvider(ipsProject, builderSetConfigModel));
                tableViewer.setInput(builderSetId);

                updateColumnWidths();
            }
        });

        List builderSetInfos = getBuilderSetInfos(ipsProject);

        String[] builderSetLabels = new String[builderSetInfos.size()];

        int currentBuilderSetIndex = -1;
        for (int i = 0; i < builderSetInfos.size(); i++) {
            IIpsArtefactBuilderSetInfo info = (IIpsArtefactBuilderSetInfo)builderSetInfos.get(i);
            String builderSetId = info.getBuilderSetId();
            if (ipsProjectProperties.getBuilderSetId().equals(builderSetId)) {
                currentBuilderSetIndex = i;
            }
            builderSetLabels[i] = info.getBuilderSetLabel();
            builderSetComboField.getCombo().setData(builderSetLabels[i], builderSetId);
        }
        builderSetComboField.getCombo().setItems(builderSetLabels);
        builderSetComboField.getCombo().select(currentBuilderSetIndex);
    }
    
    private void createTableViewer(Composite parent) {
        final Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 2;
        table.setLayoutData(gridData);

        tableViewer = new TableViewer(table);
        tableViewer.setContentProvider(new BuilderSetContentProvider());
        
        String[] columnNames = new String[COLUMNS_COUNT];
        columnNames[PROPERTY_NAME_COLUMN_INDEX] = "Property";
        columnNames[PROPERTY_VALUE_COLUMN_INDEX] = "Value";
        columnNames[PROPERTY_DESCRIPTION_COLUMN_INDEX] = "Description";

        columns = new TableViewerColumn[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columns[i] = new TableViewerColumn(tableViewer, SWT.LEFT, i);
            columns[i].getColumn().setText(columnNames[i]);
        }

        TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer,
                new FocusCellOwnerDrawHighlighter(tableViewer));

        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED /*
                                                                                                 * &&
                                                                                                 * event.keyCode ==
                                                                                                 * SWT.CR
                                                                                                 */)
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
            }

        };
        TableViewerEditor.create(tableViewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        // sort property name column if clicked on column header
        columns[PROPERTY_NAME_COLUMN_INDEX].getColumn().addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                if (ipsProject != null && e.widget == columns[PROPERTY_NAME_COLUMN_INDEX].getColumn()) {
                    int direction = (tableViewer.getTable().getSortDirection() == SWT.DOWN) ? SWT.UP : SWT.DOWN;
                    tableViewer.getTable().setSortDirection(direction);
                    tableViewer.getTable().setSortColumn(columns[PROPERTY_NAME_COLUMN_INDEX].getColumn());
//                     tableViewer.setSorter(new BuilderSetPropertyDefSorter(ipsProject));
                }
            }
        });

        updateBuilderSet();
    }

    private String getBuilderSetIdByLabel(String builderSetLabel) {
        String builderSetId = null;

        IIpsArtefactBuilderSetInfo[] ipsArtefactBuilderSetInfos = IpsPlugin.getDefault().getIpsModel()
                .getIpsArtefactBuilderSetInfos();
        for (int i = 0; i < ipsArtefactBuilderSetInfos.length; i++) {
            if (builderSetLabel.equals(ipsArtefactBuilderSetInfos[i].getBuilderSetLabel())) {
                return ipsArtefactBuilderSetInfos[i].getBuilderSetId();
            }
        }
        return builderSetId;
    }

    private IpsArtefactBuilderSetInfo getBuilderSetInfo(String builderSetId) {
        List builderSetInfos = getBuilderSetInfos(ipsProject);
        for (Iterator iterator = builderSetInfos.iterator(); iterator.hasNext();) {
            IpsArtefactBuilderSetInfo info = (IpsArtefactBuilderSetInfo)iterator.next();
            if (builderSetId.equals(info.getBuilderSetId())) {
                return info;
            }
        }
        return null;
    }

    /**
     * Initializes this dialog with the given IPS project. Throws a NullPointerException if ipsProject is null.
     * @param ipsProject An IPS project
     */
    public void init(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        
        this.ipsProject = ipsProject;
        this.builderSetId = ipsProject.getIpsArtefactBuilderSet().getId();
        this.ipsProjectProperties = ipsProject.getProperties();
        this.builderSetConfigModel = ipsProjectProperties.getBuilderSetConfig();
        
        updateBuilderSet();
    }
    
    // Updates the page's widgets to reflect the model change
    private void updateBuilderSet() {
        columns[PROPERTY_VALUE_COLUMN_INDEX].setEditingSupport(new BuilderSetPropertyEditingSupport(tableViewer,
                ipsProject, builderSetConfigModel, builderSetId));

        tableViewer.setLabelProvider(new PropertyDefValueLabelProvider(ipsProject, builderSetConfigModel));
        tableViewer.getTable().setSortDirection(SWT.UP);
        tableViewer.getTable().setSortColumn(columns[PROPERTY_NAME_COLUMN_INDEX].getColumn());
        tableViewer.setSorter(new BuilderSetPropertyDefSorter(ipsProject));
        tableViewer.setInput(builderSetId);

        updateColumnWidths();
    }

    private void updateColumnWidths() {
        columns[PROPERTY_NAME_COLUMN_INDEX].getColumn().pack();
        columns[PROPERTY_VALUE_COLUMN_INDEX].getColumn().pack();
        columns[PROPERTY_DESCRIPTION_COLUMN_INDEX].getColumn().pack();
    }

    private void initializeTimeStamps() {
        IFile file = ipsProject.getProject().getFile(".ipsproject"); //$NON-NLS-1$
        ipsprojectFileTimeStamp = file.getModificationStamp();
        builderSettingsSnapshot = getEncodedSettings();
    }

    private String getEncodedSettings() {
        StringBuffer buf = new StringBuffer();
        buf.append(ipsProjectProperties.getBuilderSetId()).append(';');
        String[] propertyNames = builderSetConfigModel.getPropertyNames();
        for (int i = 0; i < propertyNames.length; i++) {
            buf.append(propertyNames[i]).append('=');
            buf.append(builderSetConfigModel.getPropertyValue(propertyNames[i])).append(";");
        }
        return buf.toString();
    }

    private List getBuilderSetInfos(IIpsProject project) {
        List builderSetInfos = new ArrayList();

        IIpsModel ipsModel = ipsProject.getIpsModel();
        IpsArtefactBuilderSetInfo.loadExtensions(Platform.getExtensionRegistry(), IpsPlugin.getDefault().getLog(),
                builderSetInfos, ipsModel);

        return builderSetInfos;
    }

    /**
     * @return True if changes were made in this dialog
     */
    public boolean hasChangesInDialog() {
        String currentDialogSettings = getEncodedSettings();
        return (!currentDialogSettings.equals(builderSettingsSnapshot));
    }

    /**
     * @return true True if the .ipsproject file was modified outside of eclipse since this dialog was opened.
     */
    public boolean hasChangesInIpsprojectFile() {
        IFile file = ipsProject.getProject().getFile(".ipsproject"); //$NON-NLS-1$
        return ipsprojectFileTimeStamp != file.getModificationStamp();
    }

    /**
     * Restores the defaults for an IPS project's currently selected builder set.
     */
    public void performDefaults() {
        String builderSetLabel = (String)builderSetComboField.getValue();
        if (builderSetLabel != null) {
            builderSetId = getBuilderSetIdByLabel(builderSetLabel);
            ipsProjectProperties = ipsProject.getProperties();
            ipsProjectProperties.setBuilderSetId(builderSetId);

            IIpsArtefactBuilderSetInfo ipsArtefactBuilderSetInfo = IpsPlugin.getDefault().getIpsModel()
                    .getIpsArtefactBuilderSetInfo(builderSetId);
            builderSetConfigModel = ipsArtefactBuilderSetInfo.createDefaultConfiguration(ipsProject);
            ipsProjectProperties.setBuilderSetConfig(builderSetConfigModel);

            updateBuilderSet();
        }
    }

    /**
     * Saves the current settings made in this dialog to an IPS project's configuration file (.ipsproject).
     * @return True if save was successful, false otherwise.
     */
    public boolean saveToIpsProjectFile() {
        try {
            ipsProjectProperties.setBuilderSetConfig(builderSetConfigModel);
            ipsProject.setProperties(ipsProjectProperties);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }

    /**
     * Sorter for IIpsBuilderSetPropertyDef class. Note that only "Property name" is taken into
     * account (value, description, ... are omitted).
     */
    private final class BuilderSetPropertyDefSorter extends ViewerSorter {

        // FIXME: use isAvailable() on a PropertyDef instead of using isOverriddenProperty()
        private List builderSetPropertyNames;

        public BuilderSetPropertyDefSorter(IIpsProject ipsProject) {
            IIpsArtefactBuilderSetConfigModel builderSetConfig = ipsProject.getProperties().getBuilderSetConfig();
            builderSetPropertyNames = Arrays.asList(builderSetConfig.getPropertyNames());
        }

        public int compare(Viewer viewer, Object e1, Object e2) {
            int direction = ((TableViewer)viewer).getTable().getSortDirection();

            String s1 = (e1 instanceof IIpsBuilderSetPropertyDef) ? getPropertyByColumn((TableViewer)viewer,
                    (IIpsBuilderSetPropertyDef)e1) : (String)e1;

            String s2 = (e2 instanceof IIpsBuilderSetPropertyDef) ? getPropertyByColumn((TableViewer)viewer,
                    (IIpsBuilderSetPropertyDef)e2) : (String)e2;

            // comparing a default property with a property defined in an .ipsproject file:
            // default property is always put to the end
            if ((!isOverriddenProperty(s1)) && (isOverriddenProperty(s2))) {
                return 1;
            }
            if ((isOverriddenProperty(s1)) && (!isOverriddenProperty(s2))) {
                return -1;
            }

            if (isOverriddenProperty(s1) && isOverriddenProperty(s2)) {
                if (direction == SWT.UP) {
                    return s1.compareTo(s2);
                } else {
                    return s2.compareTo(s1);
                }
            }

            return s1.compareTo(s2);
        }

        private boolean isOverriddenProperty(String propertyName) {
            return builderSetPropertyNames.contains(propertyName);
        }

        private String getPropertyByColumn(TableViewer viewer, IIpsBuilderSetPropertyDef propertyDef) {
            if (viewer.getTable().getSortColumn() == columns[PROPERTY_NAME_COLUMN_INDEX].getColumn()) {
                return propertyDef.getName();
            }
            return "";
        }
    }

    /**
     * Content provider for Builder Set Property Definitions. Returns property definitions using a builder set id.
     * @author Roman Grutza
     */
    private final class BuilderSetContentProvider implements IStructuredContentProvider {
        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof String) {
                IIpsArtefactBuilderSetInfo builderSetInfo = IpsPlugin.getDefault().getIpsModel()
                        .getIpsArtefactBuilderSetInfo((String)inputElement);
                return builderSetInfo.getPropertyDefinitions();
            }
            return null;
        }
    }

    /**
     * Label provider for Builder Set Property Definitions. 
     * @author Roman Grutza
     */
    private static class PropertyDefValueLabelProvider extends CellLabelProvider implements ITableLabelProvider,
            IColorProvider {
        private IIpsArtefactBuilderSetConfigModel model;
        private IIpsProject ipsProject;

        PropertyDefValueLabelProvider(IIpsProject ipsProject,
                IIpsArtefactBuilderSetConfigModel ipsArtefactBuilderSetConfigModel) {
            this.ipsProject = ipsProject;
            this.model = ipsArtefactBuilderSetConfigModel;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IIpsBuilderSetPropertyDef) {
                IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;

                switch (columnIndex) {
                    case BuilderSetContainer.PROPERTY_NAME_COLUMN_INDEX:
                        return propertyDef.getLabel();
                    case BuilderSetContainer.PROPERTY_VALUE_COLUMN_INDEX:
                        String propertyValue = model.getPropertyValue(propertyDef.getName());
                        if (propertyValue == null || "".equals(propertyValue)) {
                            // value not set in .ipsproject file, use default
                            propertyValue = propertyDef.getDefaultValue(ipsProject);
                        }
                        return propertyValue;
                    case BuilderSetContainer.PROPERTY_DESCRIPTION_COLUMN_INDEX:
                        return propertyDef.getDescription();
                }
            }
            return "";
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public Color getBackground(Object element) {
            Color bgColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
            if (element instanceof IIpsBuilderSetPropertyDef) {
                IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
                if (!propertyDef.isAvailable(ipsProject)) {
                    bgColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
                }
            }
            return bgColor;
        }

        public Color getForeground(Object element) {
            return null;
        }

        public void update(ViewerCell cell) {
        }
    }

}
