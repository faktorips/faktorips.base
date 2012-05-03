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

package org.faktorips.devtools.core.ui.controls.chooser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.message.MessageList;

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
    }

    public void init(AbstractSubsetChooserModel model) {
        this.model = model;
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                model.getValueDatatype());
        initTableColumns(preDefinedValuesTableViewer, ctrlFactory);
        initTableColumns(resultingValuesTableViewer, ctrlFactory);

        preDefinedValuesTableViewer.setContentProvider(createContentProvider(ListAccessor.SOURCE));
        resultingValuesTableViewer.setContentProvider(createContentProvider(ListAccessor.RESULT));

        preDefinedValuesTableViewer.setInput(model);
        resultingValuesTableViewer.setInput(model);

        model.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(AbstractSubsetChooserModel.PROPERTY_RESULTING_VALUES)) {
                    resultingValuesTableViewer.refresh();
                }
                if (evt.getPropertyName().equals(AbstractSubsetChooserModel.PROPERTY_PREDEFINED_VALUES)) {
                    preDefinedValuesTableViewer.refresh();
                }
                IStructuredSelection selection = (IStructuredSelection)resultingValuesTableViewer.getSelection();
                if (selection.getFirstElement() != null) {
                    resultingValuesTableViewer.reveal(selection.getFirstElement());
                }
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
        IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
        if (selection.isEmpty()) {
            return new ArrayList<ListChooserValue>();
        }
        TypedSelection<ListChooserValue> typedSel = new TypedSelection<ListChooserValue>(ListChooserValue.class,
                selection, selection.size());
        return new ArrayList<ListChooserValue>(typedSel.getElements());
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

    private class ContentProvider implements IStructuredContentProvider {

        private final ListAccessor listAccessor;

        public ContentProvider(ListAccessor listAccessor) {
            this.listAccessor = listAccessor;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof AbstractSubsetChooserModel) {
                AbstractSubsetChooserModel model = (AbstractSubsetChooserModel)inputElement;
                return listAccessor.getList(model).toArray();
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
            ListChooserValue value = (ListChooserValue)cell.getElement();

            String formattedString = IpsUIPlugin.getDefault().getDatatypeFormatter()
                    .formatValue(model.getValueDatatype(), value.getValue());
            cell.setText(formattedString);
        }

    }

    private class ErrorColumnLabelProvider extends CellLabelProvider {

        @Override
        public void update(ViewerCell cell) {
            ListChooserValue value = (ListChooserValue)cell.getElement();
            MessageList messages = model.validateValue(value);
            cell.setImage(IpsUIPlugin.getImageHandling().getImage(
                    IpsProblemOverlayIcon.getOverlay(messages.getSeverity()), false));
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

    public Composite getChooserComposite() {
        return uiBuilder.getChooserComposite();
    }

    public void setSourceLabel(String label) {
        uiBuilder.setSourceLabel(label);
    }

    public void setTargetLabel(String label) {
        uiBuilder.setSourceLabel(label);
    }

}
