/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.DialogHelper;
import org.faktorips.devtools.core.ui.editors.ViewerButtonComposite;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * A section allowing the user to edit an {@link IProductCmptCategory}.
 * <p>
 * By means of this section, the user is able to change the order of the
 * {@link IProductCmptProperty}s assigned to the {@link IProductCmptCategory} that is being edited.
 * Furthermore, the {@link IProductCmptCategory} itself can be moved up, down, left or right.
 * <p>
 * A separate dialog enables the user to change the properties of an {@link IProductCmptCategory},
 * for example it's name and whether the {@link IProductCmptCategory} is marked as default for a
 * specific kind {@link IProductCmptProperty}.
 * <p>
 * Yet another dialog allows to change the {@link IProductCmptCategory} of an
 * {@link IProductCmptProperty}.
 * 
 * @author Alexander Weickmann
 */
public class CategorySection extends IpsSection {

    private final IProductCmptCategory category;

    private final IProductCmptType contextType;

    private final CategoryPage categoryPage;

    private ViewerButtonComposite categoryComposite;

    public CategorySection(IProductCmptCategory category, IProductCmptType contextType, CategoryPage categoryPage,
            Composite parent, UIToolkit toolkit) {

        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        this.category = category;
        this.contextType = contextType;
        this.categoryPage = categoryPage;

        initControls();
    }

    @Override
    protected String getSectionTitle() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        setLayout(client);
        categoryComposite = new CategoryComposite(category, contextType, categoryPage, client, toolkit);
    }

    private void setLayout(Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 1;
        layout.marginHeight = 2;
        parent.setLayout(layout);
    }

    // TODO AW 02-11-2011: Create toolbar

    @Override
    protected void performRefresh() {
        categoryComposite.refresh();
    }

    private static class CategoryComposite extends ViewerButtonComposite {

        private final IProductCmptCategory category;

        private final IProductCmptType contextType;

        private final CategoryPage categoryPage;

        private Button moveUpButton;

        private Button moveDownButton;

        private Button changeCategoryButton;

        public CategoryComposite(IProductCmptCategory category, IProductCmptType contextType,
                CategoryPage categoryPage, Composite parent, UIToolkit toolkit) {

            super(parent);

            this.category = category;
            this.contextType = contextType;
            this.categoryPage = categoryPage;

            initControls(toolkit);
        }

        @Override
        protected ContentViewer createViewer(Composite parent, UIToolkit toolkit) {
            TableViewer tableViewer = new TableViewer(toolkit.createTable(parent, SWT.NONE));

            setLabelProvider(tableViewer);
            setContentProvider(tableViewer);
            addDoubleClickChangeCategoryListenerToTableViewer(tableViewer);

            tableViewer.setInput(category);

            return tableViewer;
        }

        private void setLabelProvider(TableViewer tableViewer) {
            tableViewer.setLabelProvider(new DefaultLabelProvider());
        }

        private void setContentProvider(ContentViewer tableViewer) {
            tableViewer.setContentProvider(new IStructuredContentProvider() {
                @Override
                public Object[] getElements(Object inputElement) {
                    List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>();
                    try {
                        properties.addAll(category.findProductCmptProperties(contextType, false,
                                contextType.getIpsProject()));
                    } catch (CoreException e) {
                        // Recover by not displaying any properties
                        IpsPlugin.log(e);
                    }
                    return properties.toArray();
                }

                @Override
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    // Nothing to do
                }

                @Override
                public void dispose() {
                    // Nothing to do
                }
            });
        }

        private void addDoubleClickChangeCategoryListenerToTableViewer(TableViewer tableViewer) {
            tableViewer.getTable().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    openChangeCategoryDialog();
                }
            });
        }

        @Override
        protected boolean createButtons(Composite buttonComposite, UIToolkit toolkit) {
            createMoveUpButton(buttonComposite, toolkit);
            createMoveDownButton(buttonComposite, toolkit);
            createChangeCategoryButton(buttonComposite, toolkit);
            return true;
        }

        private void createMoveUpButton(Composite buttonComposite, UIToolkit toolkit) {
            moveUpButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonUp);
            moveUpButton
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            moveUpButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    moveParts(true);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // Nothing to do
                }
            });
        }

        private void createMoveDownButton(Composite buttonComposite, UIToolkit toolkit) {
            moveDownButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonDown);
            moveDownButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            moveDownButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    moveParts(false);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // Nothing to do
                }
            });
        }

        private void moveParts(boolean up) {
            int[] selection = getTable().getSelectionIndices();
            int[] newSelection = Arrays.copyOf(selection, selection.length);
            try {
                newSelection = category.moveProductCmptProperties(selection, up, contextType);
            } catch (CoreException e) {
                // The elements could not be moved so the new selection equals the old selection
                IpsPlugin.log(e);
                newSelection = Arrays.copyOf(selection, selection.length);
            }

            getTableViewer().refresh();
            getTable().setSelection(newSelection);
            getTableViewer().getControl().setFocus();

            refresh();
        }

        private void createChangeCategoryButton(Composite buttonComposite, UIToolkit toolkit) {
            changeCategoryButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonChangeCategory);
            changeCategoryButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            changeCategoryButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    openChangeCategoryDialog();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // Nothing to do
                }
            });
        }

        private void openChangeCategoryDialog() {
            Dialog dialog = new ChangeCategoryDialog(contextType, getSelectedProperty(), category, getShell());
            DialogHelper dialogHelper = new DialogHelper();
            int returnCode = dialogHelper.openDialogWithMemento(dialog, getSelectedProperty());
            if (returnCode == Window.OK) {
                categoryPage.refresh();
            }
        }

        private IProductCmptProperty getSelectedProperty() {
            return (IProductCmptProperty)getSelectedObject();
        }

        private Table getTable() {
            return getTableViewer().getTable();
        }

        private TableViewer getTableViewer() {
            return (TableViewer)getViewer();
        }

        @Override
        protected void updateButtonEnabledStates() {
            moveUpButton.setEnabled(isPropertySelected() && !isFirstElementSelected());
            moveDownButton.setEnabled(isPropertySelected() && !isLastElementSelected());
            changeCategoryButton.setEnabled(isPropertySelected());
        }

        private boolean isPropertySelected() {
            return !getViewer().getSelection().isEmpty();
        }

    }

}
