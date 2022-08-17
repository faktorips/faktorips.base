/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.chooser;

import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.runtime.MessageList;

/**
 * Viewer for SubsetChooser controls. Responsible for event-handling (listeners) and creating
 * viewers for the respective controls. Instances of this class must be initialized with an
 * {@link AbstractSubsetChooserModel} by calling {@link #init(AbstractSubsetChooserModel)}.
 * 
 * @author Stefan Widmaier
 */
public class SubsetChooserViewer {

    private AbstractSubsetChooserModel model;
    private SubsetChooserBuilder uiBuilder;
    private TableViewer preDefinedValuesTableViewer;
    private TableViewer resultingValuesTableViewer;

    /**
     * Creates a new subset chooser. {@link #init(AbstractSubsetChooserModel)} must be called
     * afterwards.
     * 
     * @param parent The parent control.
     */
    public SubsetChooserViewer(Composite parent, UIToolkit toolkit) {
        uiBuilder = new SubsetChooserBuilder();
        uiBuilder.createUI(parent, toolkit);
        initViewers();
        setUpListeners();
        addDisposeListener(parent);
    }

    private void addDisposeListener(Composite parent) {
        parent.addDisposeListener(e -> model.dispose());
    }

    protected void initViewers() {
        preDefinedValuesTableViewer = new TableViewer(uiBuilder.getPreDefinedValuesTable());
        resultingValuesTableViewer = new TableViewer(uiBuilder.getResultingValuesTable());

        ColumnViewerToolTipSupport.enableFor(preDefinedValuesTableViewer, ToolTip.NO_RECREATE);
        ColumnViewerToolTipSupport.enableFor(resultingValuesTableViewer, ToolTip.NO_RECREATE);
    }

    protected void setUpListeners() {
        uiBuilder.getAddSelectedButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedValuesFromPreDefinedToResulting();
            }
        });
        uiBuilder.getRemoveSelectedButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedValuesFromResultingToPredefined();
            }
        });
        uiBuilder.getAddAllButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveAllValuesFromPreDefinedToResulting();
            }

        });
        uiBuilder.getRemoveAllButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveAllValuesFromResultingToPreDefined();
            }

        });
        uiBuilder.getUpButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedValuesUp();
            }

        });
        uiBuilder.getDownButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedValuesDown();
            }

        });
        resultingValuesTableViewer.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                moveSelectedValuesFromResultingToPredefined();
            }
        });
        preDefinedValuesTableViewer.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                moveSelectedValuesFromPreDefinedToResulting();
            }
        });

        resultingValuesTableViewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { SubsetChooserTransfer.getInstance() },
                new SubsetChooserDragSourceAdapter(resultingValuesTableViewer, () -> model));

        preDefinedValuesTableViewer.addDragSupport(DND.DROP_MOVE,
                new Transfer[] { SubsetChooserTransfer.getInstance() },
                new SubsetChooserDragSourceAdapter(preDefinedValuesTableViewer, () -> model));

        resultingValuesTableViewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { SubsetChooserTransfer.getInstance() },
                new SubsetChooserDropListener(resultingValuesTableViewer));

        preDefinedValuesTableViewer.addDropSupport(DND.DROP_MOVE,
                new Transfer[] { SubsetChooserTransfer.getInstance() },
                new SubsetChooserDropListener(preDefinedValuesTableViewer));
    }

    public void init(AbstractSubsetChooserModel model) {
        this.model = model;
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(model.getValueDatatype());
        initTableColumns(preDefinedValuesTableViewer, ctrlFactory);
        initTableColumns(resultingValuesTableViewer, ctrlFactory);

        preDefinedValuesTableViewer.setContentProvider(createContentProvider(ListAccessor.SOURCE));
        resultingValuesTableViewer.setContentProvider(createContentProvider(ListAccessor.RESULT));

        preDefinedValuesTableViewer.setInput(model);
        resultingValuesTableViewer.setInput(model);

        model.addPropertyChangeListener(evt -> {
            if (AbstractSubsetChooserModel.PROPERTY_RESULTING_VALUES.equals(evt.getPropertyName())) {
                resultingValuesTableViewer.refresh();
            }
            if (AbstractSubsetChooserModel.PROPERTY_PREDEFINED_VALUES.equals(evt.getPropertyName())) {
                preDefinedValuesTableViewer.refresh();
            }
            IStructuredSelection selection = (IStructuredSelection)resultingValuesTableViewer.getSelection();
            if (selection.getFirstElement() != null) {
                resultingValuesTableViewer.reveal(selection.getFirstElement());
            }
        });
    }

    private void initTableColumns(TableViewer tableViewer, ValueDatatypeControlFactory ctrlFactory) {
        TableViewerColumn errorColumn = new TableViewerColumn(tableViewer, ctrlFactory.getDefaultAlignment());
        TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, ctrlFactory.getDefaultAlignment());
        errorColumn.setLabelProvider(createErrorColumnLabelProvider());
        valueColumn.setLabelProvider(createValueColumnLabelProvider());
    }

    private void moveSelectedValuesFromPreDefinedToResulting() {
        model.moveValuesFromPreDefinedToResulting(getSelectedValues(preDefinedValuesTableViewer));
    }

    private void moveSelectedValuesFromResultingToPredefined() {
        model.moveValuesFromResultingToPredefined(getSelectedValues(resultingValuesTableViewer));
    }

    private void moveAllValuesFromPreDefinedToResulting() {
        model.moveAllValuesFromPreDefinedToResulting();
    }

    private void moveAllValuesFromResultingToPreDefined() {
        model.moveAllValuesFromResultingToPreDefined();
    }

    private void moveSelectedValuesUp() {
        model.moveUp(getSelectedValues(resultingValuesTableViewer));
    }

    private void moveSelectedValuesDown() {
        model.moveDown(getSelectedValues(resultingValuesTableViewer));
    }

    public TableViewer getTargetViewer() {
        return resultingValuesTableViewer;
    }

    private List<ListChooserValue> getSelectedValues(TableViewer tableViewer) {
        return TypedSelection.createAtLeast(ListChooserValue.class, tableViewer.getSelection(), 0).getElements();
    }

    protected IContentProvider createContentProvider(ListAccessor type) {
        return new ContentProvider(type);
    }

    protected CellLabelProvider createValueColumnLabelProvider() {
        return new ValueColumnLabelProvider();
    }

    protected CellLabelProvider createErrorColumnLabelProvider() {
        return new ErrorColumnLabelProvider();
    }

    public Composite getChooserComposite() {
        return uiBuilder.getChooserComposite();
    }

    public void setSourceLabel(String label) {
        uiBuilder.setSourceLabel(label);
    }

    public void setTargetLabel(String label) {
        uiBuilder.setTargetLabel(label);
    }

    public enum ListAccessor {
        SOURCE() {

            @Override
            List<ListChooserValue> getList(AbstractSubsetChooserModel model) {
                return model.getPreDefinedValues();
            }

        },
        RESULT {

            @Override
            List<ListChooserValue> getList(AbstractSubsetChooserModel model) {
                return model.getResultingValues();
            }
        };

        abstract List<ListChooserValue> getList(AbstractSubsetChooserModel model);
    }

    private static class ContentProvider implements IStructuredContentProvider {

        private final ListAccessor listAccessor;

        public ContentProvider(ListAccessor listAccessor) {
            this.listAccessor = listAccessor;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof AbstractSubsetChooserModel) {
                return listAccessor.getList((AbstractSubsetChooserModel)inputElement).toArray();
            }
            return new Object[0];
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do
        }

    }

    private class ValueColumnLabelProvider extends CellLabelProvider {

        @Override
        public void update(ViewerCell cell) {
            ListChooserValue listChooserValue = (ListChooserValue)cell.getElement();

            String value = listChooserValue.getValue();
            String formattedString = value == null ? IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()
                    : IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(model.getValueDatatype(), value);
            cell.setText(formattedString);
        }

    }

    private class ErrorColumnLabelProvider extends CellLabelProvider {

        @Override
        public void update(ViewerCell cell) {
            ListChooserValue value = (ListChooserValue)cell.getElement();
            MessageList messages = model.validateValue(value);
            cell.setImage(IpsUIPlugin.getImageHandling()
                    .getImage(IpsProblemOverlayIcon.getOverlay(messages.getSeverity()), false));
        }

        @Override
        public String getToolTipText(Object element) {
            MessageList messages = model.validateValue((ListChooserValue)element);
            if (messages.isEmpty()) {
                return super.getToolTipText(element);
            } else {
                return messages.getMessageWithHighestSeverity().getText();
            }
        }

        @Override
        public Point getToolTipShift(Object object) {
            return new Point(5, 5);
        }

        @Override
        public int getToolTipDisplayDelayTime(Object object) {
            return 100;
        }

        @Override
        public int getToolTipTimeDisplayed(Object object) {
            return 5000;
        }

    }

}
