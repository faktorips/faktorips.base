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

package org.faktorips.devtools.core.ui.controls;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.message.MessageList;

/**
 * Control which displays two lists, seperated by four buttons. The left list contains the source
 * values, the right one the target values. The buttons between the both lists allow to take values
 * from one list to the other.
 * <p>
 * To the right of the target list, two buttons allow to modify the sort order of the items in this
 * list.
 * 
 * @author Thorsten Guenther
 */
public abstract class ListChooser extends Composite implements IDataChangeableReadWriteAccess {

    private static final int DATA_COLUMN = 1;
    private static final int IMG_COLUMN = 0;

    private Label sourceLabel;
    private Label targetLabel;

    private UIToolkit toolkit;
    private TableViewer source;
    private Table sourceTable;
    private TableViewer target;
    private Table targetTable;
    private Button addSelected;
    private Button addAll;
    private Button removeSelected;
    private Button removeAll;
    private Button up;
    private Button down;

    private boolean dataChangeable;

    /**
     * Creates a new list chooser.
     * 
     * @param parent The parent control.
     * @param toolkit Toolkit to easily create the UI.
     * @param sourceContent All values which should show up in the source list.
     * @param targetContent All values which should show up in the target list.
     */
    public ListChooser(Composite parent, UIToolkit toolkit) {
        super(parent, SWT.NONE);
        this.toolkit = toolkit;

        setLayout(new GridLayout(4, false));

        sourceLabel = toolkit.createLabel(this, Messages.ListChooser_labelAvailableValues);
        toolkit.createLabel(this, ""); //$NON-NLS-1$
        targetLabel = toolkit.createLabel(this, Messages.ListChooser_lableChoosenValues);
        toolkit.createLabel(this, ""); //$NON-NLS-1$

        TableLayoutComposite srcParent = new TableLayoutComposite(this, SWT.NONE);
        srcParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sourceTable = new Table(srcParent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        sourceTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        newTableColumns(sourceTable, srcParent);
        source = new TableViewer(sourceTable);
        addChooseButtons();

        TableLayoutComposite targetParent = new TableLayoutComposite(this, SWT.NONE);
        targetParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        targetTable = new Table(targetParent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.horizontalIndent = 5;
        targetTable.setLayoutData(layoutData);
        newTableColumns(targetTable, targetParent);
        target = new TableViewer(targetTable);
        addMoveButtons();

        source.setContentProvider(new ContentProvider());
        source.setLabelProvider(new TableLabelProvider());
        target.setContentProvider(new ContentProvider());
        target.setLabelProvider(new TableLabelProvider());

        addSelected.addSelectionListener(new ChooseListener(sourceTable, targetTable, false));
        removeSelected.addSelectionListener(new ChooseListener(targetTable, sourceTable, false));
        addAll.addSelectionListener(new ChooseListener(sourceTable, targetTable, true));
        removeAll.addSelectionListener(new ChooseListener(targetTable, sourceTable, true));

        new MessageService(source);
        new MessageService(target);
    }

    public TableViewer getTargetViewer() {
        return target;
    }

    private void newTableColumns(Table parent, TableLayoutComposite parentLayouter) {
        parentLayouter.addColumnData(new ColumnPixelData(20, false)); // message image
        parentLayouter.addColumnData(new ColumnWeightData(100, true));
        new TableColumn(parent, SWT.NONE).setResizable(false);
        new TableColumn(parent, SWT.NONE).setResizable(false);
    }

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
     * Set the content of the source-list.
     */
    protected void setSourceContent(String[] srcContent) {
        source.setInput(srcContent);
    }

    /**
     * Set the content of the target-list.
     */
    protected void setTargetContent(String[] targetContent) {
        target.setInput(targetContent);
    }

    /**
     * Returns all values contained in the target list. The order of the items in the array is the
     * order the user has choosen.
     */
    public String[] getTargetContent() {
        return (String[])target.getInput();
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

        up.addSelectionListener(new MoveListener());
        down.addSelectionListener(new MoveListener());
    }

    /**
     * This method is called when new values are added to the target list.
     * 
     * @param values The new values.
     */
    public abstract void valuesAdded(String[] values);

    /**
     * This method is called when values are removed from the target list.
     * 
     * @param values The removed values.
     */
    public abstract void valuesRemoved(String[] values);

    /**
     * This method is called, when the order of items has changed in the target list.
     * 
     * @param text The text presentation of the value moved.
     * @param oldIndex The index, the value was located.
     * @param newIndex The index, the value is moved to.
     * @param up <code>true</code> if the value is moved upwards, <code>false</code> otherwise.
     */
    public abstract void valueMoved(String text, int oldIndex, int newIndex, boolean up);

    public abstract MessageList getMessagesFor(String value);

    private void notify(Table modified, String[] values, boolean removed) {
        if (modified == sourceTable) {
            return;
        }

        if (removed) {
            valuesRemoved(values);
        } else {
            valuesAdded(values);
        }
    }

    private String[] getData(TableItem[] items) {
        String[] result = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            result[i] = items[i].getText(DATA_COLUMN);
        }
        return result;
    }

    /**
     * Listener to handle the modification of object-order in target list.
     * 
     * @author Thorsten Guenther
     */
    private class MoveListener implements SelectionListener {

        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e) {
            int[] selected = targetTable.getSelectionIndices();
            int[] newSelection = new int[selected.length];

            if (selected.length == 0) {
                return;
            }

            Arrays.sort(selected);

            if (e.getSource() == up) {
                if (selected[0] == 0) {
                    // allready at top
                    return;
                }
                for (int i = 0; i < selected.length; i++) {
                    String newTxt = targetTable.getItem(selected[i]).getText(DATA_COLUMN);
                    String oldTxt = targetTable.getItem(selected[i] - 1).getText(DATA_COLUMN);

                    valueMoved(newTxt, selected[i], selected[i] - 1, true);
                    targetTable.getItem(selected[i] - 1).setText(DATA_COLUMN, newTxt);
                    targetTable.getItem(selected[i]).setText(DATA_COLUMN, oldTxt);
                    newSelection[i] = selected[i] - 1;
                    selected[i]--;
                }
            } else {
                if (selected[selected.length - 1] == targetTable.getItemCount() - 1) {
                    // allready at bottom
                    return;
                }

                for (int i = 0; i < selected.length; i++) {
                    String newTxt = targetTable.getItem(selected[i]).getText(DATA_COLUMN);
                    String oldTxt = targetTable.getItem(selected[i] + 1).getText(DATA_COLUMN);

                    valueMoved(newTxt, selected[i], selected[i] + 1, false);
                    targetTable.getItem(selected[i] + 1).setText(DATA_COLUMN, newTxt);
                    targetTable.getItem(selected[i]).setText(DATA_COLUMN, oldTxt);
                    newSelection[i] = selected[i] + 1;
                    selected[i]++;
                }
            }
            target.setInput(getData(targetTable.getItems()));
            targetTable.setSelection(newSelection);
        }

        /**
         * {@inheritDoc}
         */
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

    }

    /**
     * Listener to handle the move of values from source to target and vice versa.
     * 
     * @author Thorsten Guenther
     */
    private class ChooseListener implements SelectionListener {

        private Table src;
        private Table trgt;
        private boolean moveAll;

        /**
         * Creates a new listener to handle modifcations at the given lists.
         * 
         * @param source The source where to remove the objects
         * @param target The target where to put the objects
         * @param all Flag to respect the selection (<code>false</code>) or take all values (
         *            <code>true</code>).
         */
        public ChooseListener(Table source, Table target, boolean all) {
            src = source;
            trgt = target;
            moveAll = all;
        }

        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e) {
            String[] toMove;
            if (moveAll) {
                toMove = getData(src.getItems());
                src.removeAll();
            } else {
                toMove = getData(src.getSelection());
                src.remove(src.getSelectionIndices());
            }
            String old[] = getData(trgt.getItems());
            String toAdd[] = new String[toMove.length + old.length];
            System.arraycopy(old, 0, toAdd, 0, old.length);
            for (int i = old.length; i < toAdd.length; i++) {
                toAdd[i] = toMove[i - old.length];
            }

            if (trgt == sourceTable) {
                source.setInput(toAdd);
                target.setInput(getData(src.getItems()));
            } else {
                target.setInput(toAdd);
                source.setInput(getData(src.getItems()));
            }

            ListChooser.this.notify(src, toMove, true);
            ListChooser.this.notify(trgt, toMove, false);
        }

        /**
         * {@inheritDoc}
         */
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

    }

    private class ContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            return (String[])inputElement;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex != IMG_COLUMN) {
                return null;
            }
            MessageList messages = getMessagesFor((String)element);
            if (!messages.isEmpty()) {
                return IpsUIPlugin.getImageHandling()
                        .getImage(IpsProblemOverlayIcon.getOverlay(messages.getSeverity()));
            }
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == IMG_COLUMN) {
                return ""; //$NON-NLS-1$
            }
            return element.toString();
        }

    }

    private class MessageService extends TableMessageHoverService {

        /**
         * @param viewer
         */
        public MessageService(TableViewer list) {
            super(list);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected MessageList getMessagesFor(Object element) throws CoreException {
            return ListChooser.this.getMessagesFor((String)element);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    /**
     * {@inheritDoc}
     */
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
