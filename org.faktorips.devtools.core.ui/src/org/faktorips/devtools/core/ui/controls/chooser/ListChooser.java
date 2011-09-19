/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.util.message.MessageList;

/**
 * Control which displays two lists, separated by four buttons. The left list contains the source
 * values (predefined values), the right one the target values (or resulting values respectively).
 * The buttons between the both lists allow to take values from one list to the other.
 * <p>
 * To the right of the target list, two buttons allow to modify the sort order of the items in this
 * list.
 * 
 * @author Thorsten Guenther, Stefan Widmaier
 */
public abstract class ListChooser extends Composite implements IDataChangeableReadWriteAccess {

    protected static final int DATA_COLUMN = 1;
    protected static final int IMG_COLUMN = 0;

    private Label sourceLabel;
    private Label targetLabel;

    private UIToolkit toolkit;
    private TableViewer preDefinedValuesTableViewer;
    private TableViewer resultingValuesTableViewer;
    private Button addSelected;
    private Button addAll;
    private Button removeSelected;
    private Button removeAll;
    private Button up;
    private Button down;

    private boolean dataChangeable;

    private ListChooserModel model;

    /**
     * Creates a new list chooser.
     * 
     * @param parent The parent control.
     */
    public ListChooser(Composite parent) {
        super(parent, SWT.NONE);
    }

    public void initControl(UIToolkit toolkit) {
        this.toolkit = toolkit;

        setLayout(new GridLayout(4, false));

        sourceLabel = toolkit.createLabel(this, Messages.ListChooser_labelAvailableValues);
        toolkit.createLabel(this, ""); //$NON-NLS-1$
        targetLabel = toolkit.createLabel(this, Messages.ListChooser_lableChoosenValues);
        toolkit.createLabel(this, ""); //$NON-NLS-1$

        TableLayoutComposite srcParent = new TableLayoutComposite(this, SWT.NONE);
        srcParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Table sourceTable = new Table(srcParent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        sourceTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        newTableColumns(sourceTable, srcParent);
        preDefinedValuesTableViewer = new TableViewer(sourceTable);
        addChooseButtons();

        TableLayoutComposite targetParent = new TableLayoutComposite(this, SWT.NONE);
        targetParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Table targetTable = new Table(targetParent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.horizontalIndent = 5;
        targetTable.setLayoutData(layoutData);
        newTableColumns(targetTable, targetParent);
        resultingValuesTableViewer = new TableViewer(targetTable);

        addMoveButtons();

        addSelected.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveValuesFromPreDefinedToResulting();
            }
        });
        removeSelected.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveValuesFromResultingToPredefined();
            }
        });
        addAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveAllValuesFromPreDefinedToResulting();
            }

        });
        removeAll.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveAllValuesFromResultingToPreDefined();
            }

        });
        resultingValuesTableViewer.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                moveValuesFromResultingToPredefined();
            }
        });
        preDefinedValuesTableViewer.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                moveValuesFromPreDefinedToResulting();
            }
        });

        new MessageService(preDefinedValuesTableViewer);
        new MessageService(resultingValuesTableViewer);
    }

    public final void init(List<String> predefValues, IEnumValueSet resultingValues) {
        model = new ListChooserModel(predefValues, resultingValues);

        preDefinedValuesTableViewer.setContentProvider(createContentProvider(SourceOrResult.SOURCE));
        preDefinedValuesTableViewer.setLabelProvider(createLabelProvider());
        resultingValuesTableViewer.setContentProvider(createContentProvider(SourceOrResult.RESULT));
        resultingValuesTableViewer.setLabelProvider(createLabelProvider());

        preDefinedValuesTableViewer.setInput(model);
        resultingValuesTableViewer.setInput(model);

        model.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                preDefinedValuesTableViewer.refresh();
                resultingValuesTableViewer.refresh();
                IStructuredSelection selection = (IStructuredSelection)resultingValuesTableViewer.getSelection();
                if (selection != null) {
                    resultingValuesTableViewer.reveal(selection.getFirstElement());
                }
            }
        });
    }

    private void moveValuesFromPreDefinedToResulting() {
        model.moveValuesFromPreDefinedToResulting(getSelectedValues(preDefinedValuesTableViewer));
    }

    private void moveValuesFromResultingToPredefined() {
        model.moveValuesFromResultingToPredefined(getSelectedValues(resultingValuesTableViewer));
    }

    private void moveAllValuesFromPreDefinedToResulting() {
        model.moveAllValuesFromPreDefinedToResulting();
    }

    private void moveAllValuesFromResultingToPreDefined() {
        model.moveAllValuesFromResultingToPreDefined();
    }

    private List<String> getSelectedValues(TableViewer tableViewer) {
        IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
        if (selection.size() == 0) {
            return new ArrayList<String>();
        }
        TypedSelection<ListChooserValue> typedSel = new TypedSelection<ListChooserValue>(ListChooserValue.class,
                selection, selection.size());
        ArrayList<String> result = new ArrayList<String>();
        for (ListChooserValue listChooserValue : typedSel.getElements()) {
            result.add(listChooserValue.getValue());
        }
        return result;
    }

    protected IContentProvider createContentProvider(SourceOrResult type) {
        return new ContentProvider(type);
    }

    public abstract MessageList getMessagesFor(String value);

    protected abstract ITableLabelProvider createLabelProvider();

    public TableViewer getTargetViewer() {
        return resultingValuesTableViewer;
    }

    protected abstract void newTableColumns(Table parent, TableLayoutComposite parentLayouter);

    /**
     * Sets the source section's label text.
     */
    public void setSourceLabel(String label) {
        sourceLabel.setText(label);
    }

    /**
     * Sets the targets section's label text.
     */
    public void setTargetLabel(String label) {
        targetLabel.setText(label);
    }

    /**
     * Add the buttons to take a value from left to right or vice versa.
     */
    private void addChooseButtons() {
        Composite root = new Composite(this, SWT.NONE);
        root.setLayout(new GridLayout(1, false));
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        layoutData.horizontalIndent = 5;
        root.setLayoutData(layoutData);

        addSelected = toolkit.createButton(root, ">"); //$NON-NLS-1$
        removeSelected = toolkit.createButton(root, "<"); //$NON-NLS-1$
        addAll = toolkit.createButton(root, ">>"); //$NON-NLS-1$
        removeAll = toolkit.createButton(root, "<<"); //$NON-NLS-1$

        addSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        removeSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        removeAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    }

    /**
     * Add the buttons to move a value in the target list up or down.
     */
    private void addMoveButtons() {
        Composite root = new Composite(this, SWT.NONE);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));

        up = toolkit.createButton(root, Messages.ListChooser_buttonUp);
        down = toolkit.createButton(root, Messages.ListChooser_buttonDown);

        up.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        down.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<String> selectedValues = getSelectedValues(resultingValuesTableViewer);
                model.moveUp(selectedValues);
                // resultingValuesTableViewer.setSelection(new StructuredSelection(selectedValues));
            }
        });
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<String> selectedValues = getSelectedValues(resultingValuesTableViewer);
                model.moveDown(selectedValues);
                // resultingValuesTableViewer.setSelection(new StructuredSelection(selectedValues));
            }
        });
    }

    private enum SourceOrResult {
        SOURCE() {

            @Override
            List<String> getList(ListChooserModel model) {
                return model.getPreDefinedValues();
            }

        },
        RESULT {

            @Override
            List<String> getList(ListChooserModel model) {
                return model.getResultingValues();
            }
        };

        abstract List<String> getList(ListChooserModel model);
    }

    private class ContentProvider implements IStructuredContentProvider {

        private final SourceOrResult type;

        public ContentProvider(SourceOrResult type) {
            this.type = type;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof ListChooserModel) {
                ListChooserModel model = (ListChooserModel)inputElement;
                List<String> strings = type.getList(model);
                ListChooserValue[] result = new ListChooserValue[strings.size()];
                int i = 0;
                for (String value : strings) {
                    result[i] = new ListChooserValue(value);
                    i++;
                }
                return result;
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

    private class MessageService extends TableMessageHoverService {

        public MessageService(TableViewer list) {
            super(list);
        }

        @Override
        protected MessageList getMessagesFor(Object element) throws CoreException {
            if (element instanceof ListChooserValue) {
                ListChooserValue value = (ListChooserValue)element;
                return ListChooser.this.getMessagesFor(value.getValue());
            } else {
                return new MessageList();
            }
        }
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;

        toolkit.setDataChangeable(addSelected, changeable);
        toolkit.setDataChangeable(removeSelected, changeable);
        toolkit.setDataChangeable(addAll, changeable);
        toolkit.setDataChangeable(removeAll, changeable);
        toolkit.setDataChangeable(up, changeable);
        toolkit.setDataChangeable(down, changeable);
    }
}
