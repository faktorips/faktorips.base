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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.ViewerButtonComposite;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * TODO AW
 * 
 * @author Alexander Weickmann
 */
public final class CategorySection extends IpsSection {

    private final IProductCmptCategory category;

    private final IProductCmptType contextType;

    private ViewerButtonComposite categoryComposite;

    public CategorySection(IProductCmptCategory category, IProductCmptType contextType, Composite parent,
            UIToolkit toolkit) {

        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        this.category = category;
        this.contextType = contextType;
        initControls();
    }

    @Override
    protected String getSectionTitle() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        setLayout(client);
        categoryComposite = new CategoryComposite(client);
    }

    private void setLayout(Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 1;
        layout.marginHeight = 2;
        parent.setLayout(layout);
    }

    // TODO AW Create toolbar

    @Override
    protected void performRefresh() {
        categoryComposite.refresh();
    }

    private class CategoryComposite extends ViewerButtonComposite {

        public CategoryComposite(Composite parent) {
            super(parent);
            initControls(getToolkit());
        }

        @Override
        protected Viewer createViewer(Composite parent, UIToolkit toolkit) {
            TableViewer tableViewer = new TableViewer(parent);
            tableViewer.setLabelProvider(new DefaultLabelProvider());
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
            tableViewer.setInput(category);
            return tableViewer;
        }

        @Override
        protected boolean createButtons(Composite buttonComposite, UIToolkit toolkit) {
            createMoveUpButton(buttonComposite, toolkit);
            createMoveDownButton(buttonComposite, toolkit);
            createChangeCategoryButton(buttonComposite, toolkit);
            return true;
        }

        private void createMoveUpButton(Composite buttonComposite, UIToolkit toolkit) {
            Button upButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonUp);
            upButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            upButton.addSelectionListener(new SelectionListener() {
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
            Button downButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonDown);
            downButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            downButton.addSelectionListener(new SelectionListener() {
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

        private void createChangeCategoryButton(Composite buttonComposite, UIToolkit toolkit) {
            Button changeCategoryButton = toolkit.createButton(buttonComposite,
                    Messages.CategorySection_buttonChangeCategory);
            changeCategoryButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
        }

        private void moveParts(boolean up) {
            if (getTableViewer().getSelection().isEmpty()) {
                return;
            }

            boolean moved = false;
            try {
                moved = category.moveProductCmptProperties(getSelectedProperties(), up);
            } catch (CoreException e) {
                // Recover by not moving any elements
                IpsPlugin.log(e);
            }

            int[] newSelection = getTableViewer().getTable().getSelectionIndices();
            if (moved) {
                int modifier = up ? -1 : 1;
                for (int i = 0; i < newSelection.length; i++) {
                    newSelection[i] = newSelection[i] + modifier;
                }

                getTableViewer().refresh();
                getTableViewer().getTable().setSelection(newSelection);
                getTableViewer().getControl().setFocus();

                refresh();
            }
        }

        private List<IProductCmptProperty> getSelectedProperties() {
            List<IProductCmptProperty> selectedProperties = new ArrayList<IProductCmptProperty>();
            IStructuredSelection structuredSelection = (IStructuredSelection)getTableViewer().getSelection();
            Iterator<?> iterator = structuredSelection.iterator();
            while (iterator.hasNext()) {
                selectedProperties.add((IProductCmptProperty)iterator.next());
            }
            return selectedProperties;
        }

        private TableViewer getTableViewer() {
            return (TableViewer)getViewer();
        }

        @Override
        protected void updateButtonEnabledStates() {
            // TODO Auto-generated method stub

        }

    }

}
