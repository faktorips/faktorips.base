/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsproject.AbstractIpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.util.QNameUtil;

/**
 * Dialog for changing the sort order of IIpsPackageFragments.
 */
public class IpsPackageSortDefDialog extends TrayDialog {

    private static final int RESTORE_BUTTON_ID = 42;
    private static final String SETTINGS_SECTION_SIZE = "size"; //$NON-NLS-1$
    private static final String SETTINGS_SIZE_X = "x"; //$NON-NLS-1$
    private static final String SETTINGS_SIZE_Y = "y"; //$NON-NLS-1$
    private static final int SETTINGS_DEFAULT_HEIGTH = 480;
    private static final int SETTINGS_DEFAULT_WIDTH = 640;

    private static String settingsFilename;

    private String title;

    private UIToolkit toolkit;
    private Button up;
    private Button down;
    private Composite container;

    private DialogSettings settings;
    private IIpsPackageFragment packageFragment;
    private SortOrder sortOrder;
    private TableViewer tableViewer;

    /**
     * New instance.
     * 
     * @param parentShell The active shell.
     * @param title Title of the dialog.
     * @param packageFragment The selected IIpsProject.
     */
    public IpsPackageSortDefDialog(Shell parentShell, String title, IIpsPackageFragment packageFragment) {
        super(parentShell);

        this.title = title;
        this.packageFragment = packageFragment;
        sortOrder = new SortOrder();

        toolkit = new UIToolkit(null);

        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.RESIZE | SWT.MAX);

        loadDialogSettings();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(title);

        container = (Composite)super.createDialogArea(parent);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

        // restore size
        int width = Math.max(settings.getInt(SETTINGS_SIZE_X), layoutData.heightHint);
        int height = Math.max(settings.getInt(SETTINGS_SIZE_Y), layoutData.widthHint);
        layoutData.widthHint = Math.max(width, layoutData.minimumWidth);
        layoutData.heightHint = Math.max(height, layoutData.minimumHeight);

        container.setLayoutData(layoutData);

        GridLayout layout = new GridLayout();
        container.setLayout(layout);

        createHeadline(container);
        createSortArea(container);

        Dialog.applyDialogFont(parent);

        return container;
    }

    /**
     * Create a headline with the chosen IpsProject name.
     */
    private void createHeadline(Composite parent) {

        Composite headline = toolkit.createComposite(parent);

        headline.setLayoutData(new GridData());

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        headline.setLayout(layout);

        toolkit.createLabel(headline,
                Messages.bind(Messages.IpsPackageSortDefDialog_headlineText, packageFragment.getName()));
    }

    /**
     * Create component for shifting IpsPackageFragments
     */
    private void createSortArea(Composite parent) {
        Composite sortComposite = toolkit.createComposite(parent);

        sortComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        sortComposite.setLayout(layout);

        createTableViewer(sortComposite);
        createUpDownButtons(sortComposite);
        udpateButtonEnablement();
        tableViewer.addSelectionChangedListener($ -> udpateButtonEnablement());
        tableViewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new DragSourceAdapter() {

                    @Override
                    public void dragStart(DragSourceEvent event) {
                        event.doit = getSelectedElements().size() > 0;
                    }
                });
        tableViewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new SortOrderDropListener(tableViewer, sortOrder::above, sortOrder::below, this::refresh));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, RESTORE_BUTTON_ID, Messages.IpsPackageSortDefDialog_restore, false);
        super.createButtonsForButtonBar(parent);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (RESTORE_BUTTON_ID == buttonId) {
            restorePressed();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    public void refresh() {
        tableViewer.refresh(false);
        udpateButtonEnablement();
    }

    /**
     * Enables or disables the buttons {@link #up} and {@link #down} according to the
     * {@link #tableViewer}'s current selection.
     * <p>
     * Both buttons are disabled if no element is selected or if the element represents a source
     * folder or packageFragmentRoot respectively.
     * <p>
     * For movable elements (PackageFragments/IpsSrcFiles) the button {@link #up} is disabled if the
     * fragment is the first in its category and cannot be moved further upwards, #down analogous.
     */
    private void udpateButtonEnablement() {
        List<IIpsElement> selectedElements = getSelectedElements();
        if (hasOnlyElementsOfOneCategory(selectedElements)) {
            up.setEnabled(!isFirstInCategory(selectedElements.get(0)));
            down.setEnabled(!isLastInCategory(selectedElements.get(selectedElements.size() - 1)));
        } else {
            up.setEnabled(false);
            down.setEnabled(false);
        }
    }

    private boolean isFirstInCategory(IIpsElement element) {
        return element instanceof IIpsPackageFragment && sortOrder.isFirst((IIpsPackageFragment)element)
                || element instanceof IIpsSrcFile && sortOrder.isFirst((IIpsSrcFile)element);
    }

    private boolean isLastInCategory(IIpsElement element) {
        return element instanceof IIpsPackageFragment && sortOrder.isLast((IIpsPackageFragment)element)
                || element instanceof IIpsSrcFile && sortOrder.isLast((IIpsSrcFile)element);
    }

    private static boolean hasOnlyElementsOfOneCategory(List<IIpsElement> selectedElements) {
        boolean hasPackage = false;
        boolean hasSrcFile = false;
        for (IIpsElement element : selectedElements) {
            if (element instanceof IIpsPackageFragment) {
                hasPackage = true;
                if (hasSrcFile) {
                    return false;
                }
            }
            if (element instanceof IIpsSrcFile) {
                hasSrcFile = true;
                if (hasPackage) {
                    return false;
                }
            }
        }
        return selectedElements.size() > 0;
    }

    private void createTableViewer(Composite sortComposite) {
        tableViewer = new TableViewer(sortComposite);
        tableViewer.setLabelProvider(new IpsPackageSortDefLabelProvider());
        tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tableViewer.setContentProvider(sortOrder);
        tableViewer.setInput(packageFragment);
    }

    /**
     * Create composite with up/buttons.
     */
    private void createUpDownButtons(Composite parent) {
        Composite upDownComposite = toolkit.createComposite(parent);

        upDownComposite.setLayoutData(new GridData());

        GridLayout layout = new GridLayout();
        upDownComposite.setLayout(layout);

        up = toolkit.createButton(upDownComposite, Messages.IpsPackageSortDefDialog_up);
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                upPressed();
            }
        });
        setButtonLayoutData(up);

        down = toolkit.createButton(upDownComposite, Messages.IpsPackageSortDefDialog_down);
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                downPressed();
            }
        });
        setButtonLayoutData(down);
    }

    /**
     * Handle Button <code>restore</code>.
     */
    protected void restorePressed() {
        sortOrder.restore();
        refresh();
    }

    /**
     * Handle Button <code>down</code>.
     */
    protected void downPressed() {
        sortOrder.down(getSelectedElements());
        refresh();
    }

    /**
     * Handle Button <code>up</code>.
     */
    protected void upPressed() {
        sortOrder.up(getSelectedElements());
        refresh();
    }

    @SuppressWarnings("unchecked")
    protected List<IIpsElement> getSelectedElements() {
        return ((IStructuredSelection)tableViewer.getSelection()).toList();
    }

    @Override
    protected void okPressed() {
        // write changes to filesystem.
        sortOrder.save();

        super.okPressed();
    }

    @Override
    public boolean close() {
        saveDialogSettings();

        if (toolkit != null) {
            toolkit.dispose();
        }

        return super.close();
    }

    /**
     * save dialog settings to file.
     */
    private void saveDialogSettings() {
        Point size = container.getSize();
        settings.put(SETTINGS_SIZE_X, size.x);
        settings.put(SETTINGS_SIZE_Y, size.y);

        try {
            settings.save(settingsFilename);
        } catch (IOException e) {
            // can't save - use defaults the next time
            IpsPlugin.log(e);
        }
    }

    /**
     * load dialog settings from file.
     */
    private void loadDialogSettings() {
        IPath path = IpsPlugin.getDefault().getStateLocation();
        settingsFilename = path.append("sortDefDialog.settings").toOSString(); //$NON-NLS-1$

        settings = new DialogSettings(SETTINGS_SECTION_SIZE);
        // set default size if no settings exists
        settings.put(SETTINGS_SIZE_X, SETTINGS_DEFAULT_WIDTH);
        settings.put(SETTINGS_SIZE_Y, SETTINGS_DEFAULT_HEIGTH);

        try {
            settings.load(settingsFilename);
        } catch (IOException e) {
            // cant read the settings, use defaults.
            // do not log the error - could be the first time we read the settings.
            /* IpsPlugin.log(e); */
        }
    }

    private static class SortOrderDropListener extends ViewerDropAdapter {

        private BiConsumer<IIpsElement, List<IIpsElement>> above;
        private BiConsumer<IIpsElement, List<IIpsElement>> below;
        private Runnable updateHandler;

        private SortOrderDropListener(TableViewer tableViewer, BiConsumer<IIpsElement, List<IIpsElement>> above,
                BiConsumer<IIpsElement, List<IIpsElement>> below, Runnable updateHandler) {
            super(tableViewer);
            this.above = above;
            this.below = below;
            this.updateHandler = updateHandler;
        }

        @Override
        public boolean validateDrop(Object target, int operation, TransferData transferData) {
            List<IIpsElement> selectedElements = getSelectedElements();
            return target != null && hasOnlyElementsOfOneCategory(selectedElements)
                    && areSameCategory((IIpsElement)target, selectedElements);
        }

        @SuppressWarnings("unchecked")
        protected List<IIpsElement> getSelectedElements() {
            return ((IStructuredSelection)getViewer().getSelection()).toList();
        }

        private boolean areSameCategory(IIpsElement target, List<IIpsElement> selectedElements) {
            return selectedElements.size() > 0
                    && (areBothPackageFragments(target, selectedElements) || areBothSrcFiles(target, selectedElements));
        }

        private boolean areBothSrcFiles(IIpsElement target, List<IIpsElement> selectedElements) {
            return selectedElements.get(0) instanceof IIpsSrcFile && target instanceof IIpsSrcFile;
        }

        private boolean areBothPackageFragments(IIpsElement target, List<IIpsElement> selectedElements) {
            return selectedElements.get(0) instanceof IIpsPackageFragment && target instanceof IIpsPackageFragment;
        }

        /**
         * Override the determineLocation method because we have only location after or location
         * before when moving an element. When D&D is not in moving mode, we do not have location
         * feedback. In that case we return the normal determined location instead.
         */
        @Override
        protected int determineLocation(DropTargetEvent event) {
            if (!(event.item instanceof Item item)) {
                return LOCATION_NONE;
            }
            Point coordinates = new Point(event.x, event.y);
            coordinates = getViewer().getControl().toControl(coordinates);
            if (item != null) {
                Rectangle bounds = getBounds(item);
                int offset = bounds.height / 2;
                if ((coordinates.y - bounds.y) < offset) {
                    return LOCATION_BEFORE;
                }
                if ((bounds.y + bounds.height - coordinates.y) < offset) {
                    return LOCATION_AFTER;
                }
            }
            return LOCATION_ON;
        }

        @Override
        public boolean performDrop(Object data) {
            IIpsElement target = (IIpsElement)getCurrentTarget();
            if (target != null) {
                if (getCurrentLocation() == LOCATION_BEFORE) {
                    above.accept(target, getSelectedElements());
                } else {
                    below.accept(target, getSelectedElements());
                }
                updateHandler.run();
                return true;
            } else {
                return false;
            }
        }

    }

    private class IpsPackageSortDefLabelProvider extends DefaultLabelProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof IIpsPackageFragment fragment) {
                String name;
                if (fragment.isDefaultPackage()) {
                    name = fragment.getRoot().getName();
                } else {
                    name = fragment.getName();
                }
                return QNameUtil.getUnqualifiedName(name);
            }
            return super.getText(element);
        }
    }

    /* private */ static class SortOrder implements IStructuredContentProvider {

        private IpsPackageFragment packageFragment;
        private IIpsElement[] elements;
        private boolean dirty;
        private boolean restored;

        private static IIpsElement[] addUnorderedChildren(IIpsElement[] orderedElements,
                IpsPackageFragment packageFragment) {
            // sort because IFolder#members makes no guarantee for order
            SortedSet<IIpsElement> subPackages = new TreeSet<>(
                    AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR);
            for (IIpsPackageFragment subPackage : packageFragment.getChildIpsPackageFragments()) {
                subPackages.add(subPackage);
            }

            SortedSet<IIpsElement> relevantSrcFiles = new TreeSet<>(
                    AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR);
            for (IIpsSrcFile ipsSrcFile : packageFragment.getIpsSrcFiles()) {
                if (isRelevantForSortOrder(ipsSrcFile.getIpsObjectType())) {
                    relevantSrcFiles.add(ipsSrcFile);
                }
            }

            IIpsElement[] sortOrder = new IIpsElement[subPackages.size() + relevantSrcFiles.size()];
            int i = addOrderedAndDefaultSorted(subPackages, sortOrder, 0, orderedElements);
            addOrderedAndDefaultSorted(relevantSrcFiles, sortOrder, i, orderedElements);

            return sortOrder;
        }

        private static int addOrderedAndDefaultSorted(SortedSet<IIpsElement> elements,
                IIpsElement[] sortOrder,
                int index,
                IIpsElement[] orderedElements) {
            int i = index;
            for (IIpsElement element : orderedElements) {
                if (elements.contains(element)) {
                    sortOrder[i++] = element;
                    elements.remove(element);
                }
            }
            for (IIpsElement element : elements) {
                sortOrder[i++] = element;
            }
            return i;
        }

        private static boolean isRelevantForSortOrder(IpsObjectType ipsObjectType) {
            return ipsObjectType == IpsObjectType.PRODUCT_CMPT || ipsObjectType == IpsObjectType.PRODUCT_TEMPLATE;
        }

        private void init(IpsPackageFragment packageFragment) {
            this.packageFragment = packageFragment;
            if (packageFragment == null) {
                elements = new IIpsElement[0];
            } else {
                Comparator<IIpsElement> childOrderComparator = packageFragment.getChildOrderComparator();
                IIpsElement[] orderedElements = childOrderComparator instanceof DefinedOrderComparator
                        ? ((DefinedOrderComparator)childOrderComparator).getElements()
                        : new IIpsElement[0];
                elements = addUnorderedChildren(orderedElements, packageFragment);
            }
            dirty = false;
            restored = false;
        }

        public void restore() {
            restored = true;
            dirty = false;
            elements = addUnorderedChildren(new IIpsElement[0], packageFragment);
        }

        public void save() {
            if (dirty) {
                packageFragment.setChildOrderComparator(new DefinedOrderComparator(elements));
            } else if (restored) {
                packageFragment.setChildOrderComparator(AbstractIpsPackageFragment.DEFAULT_CHILD_ORDER_COMPARATOR);
            }
        }

        @Override
        public void dispose() {
            // nothing to dispose;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            init((IpsPackageFragment)newInput);
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return elements;
        }

        public void up(List<? extends IIpsElement> list) {
            for (IIpsElement element : list) {
                int index = indexOf(element);
                swap(index, index - 1);
            }
        }

        public void down(List<? extends IIpsElement> list) {
            for (int i = list.size() - 1; i >= 0; i--) {
                int index = indexOf(list.get(i));
                swap(index, index + 1);
            }
        }

        public void below(IIpsElement target, List<? extends IIpsElement> list) {
            for (int i = list.size() - 1; i >= 0; i--) {
                moveTo(target, list.get(i), +1);
            }
        }

        private void moveTo(IIpsElement target, IIpsElement source, int offset) {
            List<IIpsElement> element = Collections.singletonList(source);
            while (indexOf(source) > indexOf(target) + offset) {
                up(element);
            }
            while (indexOf(source) < indexOf(target) + offset) {
                down(element);
            }
        }

        public void above(IIpsElement target, List<? extends IIpsElement> list) {
            for (IIpsElement source : list) {
                moveTo(target, source, -1);
            }
        }

        private int indexOf(IIpsElement element) {
            return ArrayUtils.indexOf(elements, element);
        }

        private void swap(int i, int j) {
            IIpsElement element = elements[i];
            elements[i] = elements[j];
            elements[j] = element;
            dirty = true;
        }

        public boolean isFirst(IIpsPackageFragment fragment) {
            return indexOf(fragment) == 0;
        }

        public boolean isLast(IIpsPackageFragment fragment) {
            int index = indexOf(fragment);
            return elements.length == index + 1 || !(elements[index + 1] instanceof IIpsPackageFragment);
        }

        public boolean isFirst(IIpsSrcFile srcFile) {
            int index = indexOf(srcFile);
            return index == 0 || elements[index - 1] instanceof IIpsPackageFragment;
        }

        public boolean isLast(IIpsSrcFile srcFile) {
            return indexOf(srcFile) == elements.length - 1;
        }

    }
}
