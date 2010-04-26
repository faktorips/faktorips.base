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

package org.faktorips.devtools.core.ui.preferencepages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
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
import org.faktorips.devtools.core.util.StringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

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

    // mapping of builderSetId -> builderSetConfigModel
    private HashMap<String, IIpsArtefactBuilderSetConfigModel> builderSetModels;

    // to detect changes made in dialog or outside of eclipse
    private long ipsprojectFileTimeStamp;
    private String builderSettingsSnapshot;

    private boolean hasPropertyNameColumnSorter = true;
    private BuilderSetPropertyDefSorter builderSetPropertyDefSorter;

    private TableViewer tableViewer;
    private ComboField builderSetComboField;
    private TableViewerColumn[] columns;
    private Composite mainComposite;

    /**
     * @param ipsProject IPS project whose builder set configuration is to be altered. If ipsProject
     *            is null a NullPointerException is thrown.
     */
    public BuilderSetContainer(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        this.ipsProject = ipsProject;
        this.builderSetId = ipsProject.getProperties().getBuilderSetId();
        this.ipsProjectProperties = ipsProject.getProperties();
        this.builderSetConfigModel = ipsProjectProperties.getBuilderSetConfig();
        this.builderSetModels = new HashMap<String, IIpsArtefactBuilderSetConfigModel>();
        initializeTimeStamps();
    }

    /**
     * Returns a Control with the purpose to provide a GUI for configuring an IPS project's builder
     * set.
     * 
     * @param parent The parent Composite
     * @return The created Control for configuring an IPS builder set
     */
    public Control createContents(Composite parent) {
        mainComposite = new Composite(parent, SWT.NONE);
        BuilderSetAdapter adapter = new BuilderSetAdapter();
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        mainComposite.setLayout(layout);

        createBuilderSetCombo(mainComposite, adapter);

        Label label = new Label(mainComposite, SWT.HORIZONTAL | SWT.BAR);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        label.setLayoutData(gd);

        label = new Label(mainComposite, SWT.NONE);
        label.setText(Messages.BuilderSetContainer_tableViewerLabel);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        label.setLayoutData(gd);

        createTableViewer(mainComposite, adapter);

        return mainComposite;
    }

    private void createBuilderSetCombo(Composite parent, BuilderSetAdapter adapter) {
        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        l.setText(Messages.BuilderSetContainer_builderSetComboLabel);

        Combo builderSetCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        builderSetCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
        builderSetComboField = new ComboField(builderSetCombo);
        builderSetComboField.addChangeListener(adapter);

        List<IIpsArtefactBuilderSetInfo> builderSetInfos = getBuilderSetInfos(ipsProject);
        String[] builderSetLabels = new String[builderSetInfos.size()];

        // indices out of bounds are ignored in Combo.select()
        int currentBuilderSetIndex = -1;
        for (int i = 0; i < builderSetInfos.size(); i++) {
            IIpsArtefactBuilderSetInfo info = builderSetInfos.get(i);
            String builderSetId = info.getBuilderSetId();
            if (ipsProjectProperties.getBuilderSetId().equals(builderSetId)) {
                currentBuilderSetIndex = i;
            }
            builderSetLabels[i] = info.getBuilderSetLabel();
        }

        builderSetComboField.getCombo().setItems(builderSetLabels);
        builderSetComboField.getCombo().select(currentBuilderSetIndex);
    }

    private void createTableViewer(Composite parent, BuilderSetAdapter adapter) {
        final Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE
                | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 3;
        table.setLayoutData(gridData);

        tableViewer = new TableViewer(table);
        tableViewer.setContentProvider(new BuilderSetContentProvider());

        String[] columnNames = new String[COLUMNS_COUNT];
        columnNames[PROPERTY_NAME_COLUMN_INDEX] = Messages.BuilderSetContainer_tableColumnLabel_Property;
        columnNames[PROPERTY_VALUE_COLUMN_INDEX] = Messages.BuilderSetContainer_tableColumnLabel_Value;
        columnNames[PROPERTY_DESCRIPTION_COLUMN_INDEX] = Messages.BuilderSetContainer_tableColumnLabel_Description;

        columns = new TableViewerColumn[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columns[i] = new TableViewerColumn(tableViewer, SWT.LEFT, i);
            columns[i].getColumn().setText(columnNames[i]);
        }

        columns[PROPERTY_NAME_COLUMN_INDEX].setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((IIpsBuilderSetPropertyDef)element).getLabel();
            }
        });
        columns[PROPERTY_VALUE_COLUMN_INDEX].setLabelProvider(new BuilderSetPropertyLabelProvider(ipsProject,
                builderSetConfigModel));
        columns[PROPERTY_DESCRIPTION_COLUMN_INDEX].setLabelProvider(new ColumnLabelProvider() {
            private static final int TOOLTIP_LINE_LENGTH = 75;

            @Override
            public String getText(Object element) {
                return ((IIpsBuilderSetPropertyDef)element).getDescription();
            }

            @Override
            public String getToolTipText(Object element) {
                if (element instanceof IIpsBuilderSetPropertyDef) {
                    IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
                    String description = propertyDef.getDescription();
                    String wrappedText = StringUtils.wrapText(description, TOOLTIP_LINE_LENGTH, "\n"); //$NON-NLS-1$
                    return wrappedText;
                }
                return ""; //$NON-NLS-1$
            }

            @Override
            public Point getToolTipShift(Object object) {
                return new Point(5, 5);
            }

            @Override
            public int getToolTipDisplayDelayTime(Object object) {
                return 200;
            }

            @Override
            public int getToolTipTimeDisplayed(Object object) {
                return 10000;
            }
        });

        TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tableViewer,
                new FocusCellOwnerDrawHighlighter(tableViewer));

        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(tableViewer) {
            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
                        || event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
            }
        };
        TableViewerEditor.create(tableViewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        // sort property name column if clicked on column header
        columns[PROPERTY_NAME_COLUMN_INDEX].getColumn().addSelectionListener(adapter);

        updateBuilderSet();
    }

    private String getBuilderSetIdByLabel(String builderSetLabel) {
        String builderSetId = null;

        IIpsArtefactBuilderSetInfo[] ipsArtefactBuilderSetInfos = IpsPlugin.getDefault().getIpsModel()
                .getIpsArtefactBuilderSetInfos();
        for (IIpsArtefactBuilderSetInfo ipsArtefactBuilderSetInfo : ipsArtefactBuilderSetInfos) {
            if (builderSetLabel.equals(ipsArtefactBuilderSetInfo.getBuilderSetLabel())) {
                return ipsArtefactBuilderSetInfo.getBuilderSetId();
            }
        }
        return builderSetId;
    }

    private IpsArtefactBuilderSetInfo getBuilderSetInfo(String builderSetId) {
        List<IIpsArtefactBuilderSetInfo> builderSetInfos = getBuilderSetInfos(ipsProject);
        for (IIpsArtefactBuilderSetInfo iIpsArtefactBuilderSetInfo : builderSetInfos) {
            IpsArtefactBuilderSetInfo info = (IpsArtefactBuilderSetInfo)iIpsArtefactBuilderSetInfo;
            if (builderSetId.equals(info.getBuilderSetId())) {
                return info;
            }
        }
        return null;
    }

    /**
     * Initializes this dialog with the given IPS project. Throws a NullPointerException if
     * ipsProject is null.
     * 
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

    private void initializeTimeStamps() {
        IFile file = ipsProject.getProject().getFile(".ipsproject"); //$NON-NLS-1$
        ipsprojectFileTimeStamp = file.getModificationStamp();
        builderSettingsSnapshot = getEncodedSettings();
    }

    private String getEncodedSettings() {
        StringBuffer buf = new StringBuffer();
        buf.append(ipsProjectProperties.getBuilderSetId()).append(';');
        String[] propertyNames = builderSetConfigModel.getPropertyNames();
        for (String propertyName : propertyNames) {
            buf.append(propertyName).append('=');
            buf.append(builderSetConfigModel.getPropertyValue(propertyName)).append(';');
        }
        return buf.toString();
    }

    private List<IIpsArtefactBuilderSetInfo> getBuilderSetInfos(IIpsProject project) {
        List<IIpsArtefactBuilderSetInfo> builderSetInfos = new ArrayList<IIpsArtefactBuilderSetInfo>();

        IIpsModel ipsModel = ipsProject.getIpsModel();
        IpsArtefactBuilderSetInfo.loadExtensions(Platform.getExtensionRegistry(), IpsPlugin.getDefault().getLog(),
                builderSetInfos, ipsModel);

        return builderSetInfos;
    }

    /**
     * @return True if changes were made in this dialog
     */
    public boolean hasChangesInDialog() {
        if (builderSetConfigModel == null) {
            return false;
        }
        String currentDialogSettings = getEncodedSettings();
        return (!currentDialogSettings.equals(builderSettingsSnapshot));
    }

    /**
     * @return true True if the .ipsproject file was modified outside of eclipse since this dialog
     *         was opened.
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
     * Saves the current settings made in this dialog to an IPS project's configuration file
     * (.ipsproject).
     * 
     * @return True if save was successful, false otherwise.
     */
    public boolean saveToIpsProjectFile() {
        try {
            validateBuilderSetConfig();
            ipsProjectProperties.setBuilderSetConfig(builderSetConfigModel);
            ipsProject.setProperties(ipsProjectProperties);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }

    private void validateBuilderSetConfig() {
        String builderSetId = getBuilderSetIdByLabel(builderSetComboField.getText());
        IpsArtefactBuilderSetInfo info = getBuilderSetInfo(builderSetId);
        builderSetConfigModel.validate(ipsProject, info);
    }

    private void sortByPropertyName() {
        int direction = (tableViewer.getTable().getSortDirection() == SWT.DOWN) ? SWT.UP : SWT.DOWN;
        tableViewer.getTable().setSortDirection(direction);
        tableViewer.getTable().setSortColumn(columns[PROPERTY_NAME_COLUMN_INDEX].getColumn());
        tableViewer.refresh();

        if (hasPropertyNameColumnSorter == false) {
            hasPropertyNameColumnSorter = true;
            builderSetPropertyDefSorter = new BuilderSetPropertyDefSorter(ipsProject);
            tableViewer.setSorter(builderSetPropertyDefSorter);
        }
    }

    // save model for old builderSetId, since builderSetPropertyDefs could have been changed
    // and we want to restore the values if this builderSet is selected again
    private void changeBuilderSet(String oldBuilderSetId, String newBuilderSetId) {
        if (oldBuilderSetId != null && builderSetConfigModel != null) {
            builderSetModels.put(oldBuilderSetId, builderSetConfigModel);
        }

        // restore old model if available, else create a new one
        builderSetConfigModel = builderSetModels.get(newBuilderSetId);
        if (builderSetConfigModel == null) {
            IpsArtefactBuilderSetInfo info = getBuilderSetInfo(newBuilderSetId);
            builderSetConfigModel = info.createDefaultConfiguration(ipsProject);
        }

        builderSetId = newBuilderSetId;
        ipsProjectProperties.setBuilderSetId(builderSetId);
        ipsProjectProperties.setBuilderSetConfig(builderSetConfigModel);

        columns[PROPERTY_VALUE_COLUMN_INDEX].setEditingSupport(new BuilderSetPropertyEditingSupport(tableViewer,
                ipsProject, builderSetConfigModel, builderSetId));
        columns[PROPERTY_VALUE_COLUMN_INDEX].setLabelProvider(new BuilderSetPropertyLabelProvider(ipsProject,
                builderSetConfigModel));

        tableViewer.setInput(builderSetId);

        updateColumnWidths();
    }

    // Widget action handling
    private final class BuilderSetAdapter implements ValueChangeListener, SelectionListener {

        public void valueChanged(FieldValueChangedEvent event) {
            String builderSetLabel = (String)event.field.getValue();
            String newBuilderSetId = getBuilderSetIdByLabel(builderSetLabel);
            changeBuilderSet(builderSetId, newBuilderSetId);
        }

        public void widgetSelected(SelectionEvent e) {
            if (ipsProject != null && e.widget == columns[PROPERTY_NAME_COLUMN_INDEX].getColumn()) {
                sortByPropertyName();
            }
        }

        public void widgetDefaultSelected(SelectionEvent e) { /* ignore */
        }
    }

    // Sorter for IIpsBuilderSetPropertyDef class. Note that only "Property name" is taken into
    // account (value, description, ... are omitted).
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
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Content provider for Builder Set Property Definitions. Returns property definitions using a
     * builder set id.
     * 
     * @author Roman Grutza
     */
    private static class BuilderSetContentProvider implements IStructuredContentProvider {
        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            IIpsArtefactBuilderSetInfo builderSetInfo = IpsPlugin.getDefault().getIpsModel()
                    .getIpsArtefactBuilderSetInfo(inputElement.toString());
            if (builderSetInfo != null) {
                return builderSetInfo.getPropertyDefinitions();
            }
            return null;
        }
    }

    /**
     * Column label provider for the value of a Builder Set property
     * 
     * @author Roman Grutza
     */
    private class BuilderSetPropertyLabelProvider extends ColumnLabelProvider {
        private IIpsArtefactBuilderSetConfigModel model;
        private IIpsProject ipsProject;

        BuilderSetPropertyLabelProvider(IIpsProject ipsProject,
                IIpsArtefactBuilderSetConfigModel ipsArtefactBuilderSetConfigModel) {
            this.ipsProject = ipsProject;
            this.model = ipsArtefactBuilderSetConfigModel;
        }

        @Override
        public String getText(Object element) {
            if (element instanceof IIpsBuilderSetPropertyDef) {
                IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
                String propertyValue = model.getPropertyValue(propertyDef.getName());
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
                return propertyValue;
            }
            return ""; //$NON-NLS-1$
        }

        @Override
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
    }

}
