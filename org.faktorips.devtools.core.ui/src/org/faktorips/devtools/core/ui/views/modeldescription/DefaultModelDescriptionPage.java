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

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.Page;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A page for presenting {@link DescriptionItem}s similiar to the outline view.
 * 
 * The attributes and their description are presented within a ExpandableComposite.
 * 
 * @author Markus Blum
 * 
 */

abstract public class DefaultModelDescriptionPage extends Page {

    private FormToolkit toolkit;

    private ScrolledForm form;
    private Composite expandableContainer;

    private List<DescriptionItem> defaultList; // List of DescriptionItems sorted by parent.
    private List<DescriptionItem> activeList; // defaultList sorted lexical or not.
    private String title;

    private Color colorGray;

    public DefaultModelDescriptionPage() {
        defaultList = new ArrayList<DescriptionItem>();
        activeList = new ArrayList<DescriptionItem>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());

        form = toolkit.createScrolledForm(parent);
        // form.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));

        TableWrapLayout layoutForm = new TableWrapLayout();
        layoutForm.verticalSpacing = 1;
        layoutForm.horizontalSpacing = 1;
        layoutForm.numColumns = 1;

        form.getBody().setLayout(layoutForm);

        registerToolbarActions();
    }

    /**
     * Add sorting action to toolbar.
     */
    private void registerToolbarActions() {
        // register sorting action
        IToolBarManager toolBarManager = getSite().getActionBars().getToolBarManager();
        toolBarManager.add(new LexicalSortingAction(this));
    }

    /**
     * Create the DescrptionPage by DescriptionItems.
     */
    private void createForm() {
        colorGray = form.getDisplay().getSystemColor(SWT.COLOR_GRAY);

        form.setText(title);

        // collect all attributes in one container
        expandableContainer = toolkit.createComposite(form.getBody());
        // expandableContainer.setBackground(form.getBody().getDisplay().getSystemColor(SWT.COLOR_GREEN));

        TableWrapLayout layout = new TableWrapLayout();
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.numColumns = 1;

        expandableContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        expandableContainer.setLayout(layout);

        int index = 2; // simple mechanism for color coding for lines
        // in alternating colors: odd/even

        for (int i = 0; i < activeList.size(); i++) {
            createExpandableControl(expandableContainer, activeList.get(i), index++);
        }
    }

    /**
     * Create a single ExpandableComposite object with name=faktorips.attributename and
     * child(text)=faktorips.description.
     * 
     * @param parent rootContainer object
     * @param column faktorips data
     * @param index flag for switching the background colour
     */
    private void createExpandableControl(Composite parent, DescriptionItem item, int index) {

        ExpandableComposite excomposite = toolkit.createExpandableComposite(parent, ExpandableComposite.TWISTIE
                | ExpandableComposite.COMPACT | ExpandableComposite.EXPANDED);

        // Set faktorips.attribute name
        excomposite.setText(StringUtils.capitalize(item.getName()));

        if ((index % 2) == 0) {
            excomposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        }

        excomposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        excomposite.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });

        // Set faktorips.attribute description
        FormText client = toolkit.createFormText(excomposite, true);
        client.setColor("gray", colorGray); //$NON-NLS-1$

        // don't ignore whitespaces and newlines
        client.setWhitespaceNormalized(false);

        StringBuffer sb = new StringBuffer();
        String description = item.getDescription().trim();
        sb.append("<form>"); //$NON-NLS-1$
        if (StringUtils.isEmpty(description)) {
            // if no desription is given show the default text in gray forground color
            sb.append("<p><span color=\"gray\">"); //$NON-NLS-1$
            sb.append(Messages.DefaultModelDescriptionPage_NoDescriptionAvailable);
            sb.append("</span></p>"); //$NON-NLS-1$
        } else {
            sb.append(description);
        }
        sb.append("</form>"); //$NON-NLS-1$
        client.setText(sb.toString(), true, true);

        client.setBackground(excomposite.getBackground());
        client.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        // client.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

        excomposite.setClient(client);
        Label spacer = toolkit.createLabel(parent, ""); //$NON-NLS-1$
        TableWrapData layoutData = new TableWrapData();
        layoutData.heightHint = 10;
        spacer.setLayoutData(layoutData);
        // client.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {

        if (toolkit != null) {
            toolkit.dispose();
        }

        if (form != null) {
            form.dispose();
        }

        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control getControl() {
        if (form == null) {
            return null;
        }

        return form;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {
        if (form == null) {
            return;
        }

        form.setFocus();
    }

    /**
     * Free the root container with its ExapndableComponents and recreate the controls with the
     * content of the activeList.
     */
    public void refresh() {
        if (expandableContainer != null) {
            expandableContainer.dispose();
        }

        if (form == null) {
            return;
        }

        createForm();
        form.reflow(true);
    }

    /**
     * "sort" action for DescriptionItems.
     * 
     * @author Markus Blum
     */
    class LexicalSortingAction extends Action {

        public LexicalSortingAction(DefaultModelDescriptionPage page) {
            super();

            setText(Messages.DefaultModelDescriptionPage_SortText);
            setToolTipText(Messages.DefaultModelDescriptionPage_SortTooltipText);
            setDescription(Messages.DefaultModelDescriptionPage_SortDescription);

            // get image: "alphabetical sort enabled"
            ImageDescriptor descriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "elcl16/alphab_sort_co.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(descriptor);
            setImageDescriptor(descriptor);

            boolean checked = IpsPlugin.getDefault().getPreferenceStore().getBoolean(
                    "DefaultModelDescriptionPage.LexicalSortingAction.isChecked"); //$NON-NLS-1$
            sortItems(checked);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            sortItems(isChecked());
            IpsPlugin.getDefault().getPreferenceStore().setValue(
                    "DefaultModelDescriptionPage.LexicalSortingAction.isChecked", isChecked()); //$NON-NLS-1$
        }

        /**
         * Set toggle and sort the DescritpionItems.
         * 
         * @param on sort lexical if on is <code>true</code>.
         */
        private void sortItems(final boolean on) {
            setChecked(on);

            BusyIndicator.showWhile(form.getDisplay(), new Runnable() {
                public void run() {
                    if (on) {
                        sortLexical();
                    } else {
                        sortDefault();
                    }

                    refresh();
                }
            });
        }
    }

    /**
     * Comparator for DescriptionItems. Sort DescriptionItems by Name & Unicodestyle: z < ä,ö,ü,ß.
     * 
     * @author Markus Blum
     */
    class DescriptionItemComparator implements Comparator<DescriptionItem> {
        /**
         * {@inheritDoc}
         */
        public int compare(DescriptionItem item1, DescriptionItem item2) {
            Assert.isNotNull(item1, "DescriptionItem1"); //$NON-NLS-1$
            Assert.isNotNull(item2, "DescriptionItem2"); //$NON-NLS-1$
            return item1.getName().compareTo(item2.getName());
        }
    }

    /**
     * Restore default order given by the parent class.
     */
    private void sortDefault() {
        Collections.copy(activeList, defaultList);
    }

    /**
     * Sort the active list lexical.
     */
    private void sortLexical() {
        Collections.sort(activeList, new DescriptionItemComparator());
    }

    /**
     * Set headline. Use the title to identify the content of the ModelDescriptionPage e.g.
     * tablename, product name ...
     * 
     * @param title set title of the form (ProductCmpt name or table name)
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set the DescriptionItems. The item list is used for creating the controls.
     * 
     * The caller defines the default sort order.
     * 
     * @param itemList List with DescriptionItems.
     */
    public void setDescriptionItems(DescriptionItem[] itemList) {
        defaultList = Arrays.asList(itemList);
        activeList = Arrays.asList(itemList);
        refresh();
    }
}
