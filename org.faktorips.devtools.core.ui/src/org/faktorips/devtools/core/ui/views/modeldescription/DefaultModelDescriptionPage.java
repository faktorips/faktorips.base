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

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * A page for presenting {@link DescriptionItem}s similar to the outline view.
 * 
 * The attributes and their description are presented within a ExpandableComposite.
 * 
 * @author Markus Blum
 * 
 */
abstract public class DefaultModelDescriptionPage extends Page implements IIpsSrcFilesChangeListener {

    private FormToolkit toolkit;

    private ScrolledForm form;
    private Composite expandableContainer;

    private List<DescriptionItem> defaultList; // List of DescriptionItems sorted by parent.
    private List<DescriptionItem> activeList; // defaultList
    // sorted lexical
    // or not.
    private String title;

    private Color colorGray;

    private IIpsObject ipsObject;

    private FilterEmptyDescriptionsAction filterEmptyDescriptionsAction;

    private LexicalSortingAction lexicalSortingAction;

    public DefaultModelDescriptionPage() {
        defaultList = new ArrayList<DescriptionItem>();
        activeList = new ArrayList<DescriptionItem>();
        IpsPlugin.getDefault().getIpsModel().addIpsSrcFilesChangedListener(this);

    }

    /**
     * Initialize the DescriptionPage
     */
    protected void setDescriptionData() {
        try {
            if (getIpsObject() instanceof ILabeledElement) {
                setTitle(IpsPlugin.getMultiLanguageSupport().getLocalizedLabel((ILabeledElement)getIpsObject()));
            } else {
                setTitle(getIpsObject().getName());
            }
            setDescriptionItems(createDescriptions());
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

    }

    /**
     * Creates a List of DescriptionItems
     * 
     */
    protected abstract List<DescriptionItem> createDescriptions() throws CoreException;

    /**
     * Creates DescriptionItems
     */
    protected void createDescriptionItem(IDescribedElement describedElement, List<DescriptionItem> descriptions) {
        String label;
        if (describedElement instanceof ILabeledElement) {
            label = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(((ILabeledElement)describedElement));
        } else {
            label = describedElement.getName();
        }
        String description = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(describedElement);
        DescriptionItem item = new DescriptionItem(label, description);
        descriptions.add(item);
    }

    @Override
    public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
        if (ipsObject == null) {
            return;
        }
        if (event.getChangedIpsSrcFiles().contains(ipsObject.getIpsSrcFile())) {
            setDescriptionData();
        }
    }

    public void setIpsObject(IIpsObject ipsObject) {
        this.ipsObject = ipsObject;
    }

    public IIpsObject getIpsObject() {
        return ipsObject;
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
        filterEmptyDescriptionsAction = new FilterEmptyDescriptionsAction();
        lexicalSortingAction = new LexicalSortingAction();
        toolBarManager.add(filterEmptyDescriptionsAction);
        toolBarManager.add(lexicalSortingAction);
        // if (lexicalSortingAction.isChecked()) {
        // sortLexical();
        // }
    }

    /**
     * Create the DescrptionPage by DescriptionItems.
     */
    private void createForm() {
        colorGray = form.getDisplay().getSystemColor(SWT.COLOR_GRAY);

        form.setText(title);

        // collect all attributes in one container
        expandableContainer = toolkit.createComposite(form.getBody());

        TableWrapLayout layout = new TableWrapLayout();
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 0;
        layout.numColumns = 1;

        expandableContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        expandableContainer.setLayout(layout);

        for (int i = 0; i < activeList.size(); i++) {
            // i simple mechanism for color coding for lines
            // in alternating colors: odd/even
            createExpandableControl(expandableContainer, activeList.get(i), i);
        }
    }

    /**
     * Create a single ExpandableComposite object with name=faktorips.attributename and
     * child(text)=faktorips.description.
     * 
     * @param parent rootContainer object
     * @param index flag for switching the background colour
     */
    private void createExpandableControl(Composite parent, DescriptionItem item, int index) {
        ExpandableComposite excomposite = toolkit.createExpandableComposite(parent, ExpandableComposite.TWISTIE
                | ExpandableComposite.COMPACT | ExpandableComposite.EXPANDED);

        // Set faktorips.attribute name
        excomposite.setText(StringUtils.capitalize(item.getName()));
        if ((index % 2) == 0 && !item.hasChildren()) {
            excomposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        }

        excomposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        excomposite.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });

        Composite clientGroup = toolkit.createComposite(excomposite);
        clientGroup.setBackground(excomposite.getBackground());
        clientGroup.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        TableWrapLayout layout = new TableWrapLayout();
        layout.verticalSpacing = 7;
        layout.horizontalSpacing = 0;
        layout.numColumns = 1;
        layout.leftMargin = 20;
        clientGroup.setLayout(layout);

        StringBuffer sb = new StringBuffer();
        String description = item.getDescription().trim();

        // Set faktorips.attribute description
        if ((!item.hasChildren() && StringUtils.isEmpty(description)) || !StringUtils.isEmpty(description)) {
            FormText client = toolkit.createFormText(clientGroup, true);
            client.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
            client.setBackground(excomposite.getBackground());
            client.setWhitespaceNormalized(false);
            if (StringUtils.isEmpty(description) && !item.hasChildren()) {
                client.setColor("gray", colorGray); //$NON-NLS-1$
                sb.append("<form>"); //$NON-NLS-1$
                // if no description is given show the default text in gray foreground color
                sb.append("<p><span color=\"gray\">"); //$NON-NLS-1$
                sb.append(Messages.DefaultModelDescriptionPage_NoDescriptionAvailable);
                sb.append("</span></p>"); //$NON-NLS-1$
                sb.append("</form>"); //$NON-NLS-1$
                client.setText(sb.toString(), true, true);
            } else {
                sb.append(description);
                client.setText(sb.toString(), false, true);
            }
            // don't ignore whitespaces and newlines
        }

        if (item.hasChildren()) {
            List<DescriptionItem> children = item.getChildren();
            int i = 0;
            for (DescriptionItem child : children) {
                createExpandableControl(clientGroup, child, i);
                i++;
            }
        }

        excomposite.setClient(clientGroup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeIpsSrcFilesChangedListener(this);
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

    private void sortAndFilterDescriptionList() {
        final boolean filter = filterEmptyDescriptionsAction != null && filterEmptyDescriptionsAction.isChecked();
        final boolean sort = lexicalSortingAction != null && lexicalSortingAction.isChecked();
        BusyIndicator.showWhile(form.getDisplay(), new Runnable() {
            @Override
            public void run() {
                activeList = copyDescriptionItems(defaultList);
                if (filter) {
                    deleteEmptyDescriptions();
                }
                if (sort) {
                    sortRecursive(activeList);
                }
                refresh();
            }
        });
    }

    /**
     * "sort" action for DescriptionItems.
     * 
     * @author Markus Blum
     */
    class LexicalSortingAction extends Action {

        public LexicalSortingAction() {
            super(Messages.DefaultModelDescriptionPage_SortText, SWT.TOGGLE);

            setToolTipText(Messages.DefaultModelDescriptionPage_SortTooltipText);
            setDescription(Messages.DefaultModelDescriptionPage_SortDescription);

            // get image: "alphabetical sort enabled"
            ImageDescriptor descriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "elcl16/alphab_sort_co.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(descriptor);
            setImageDescriptor(descriptor);

            boolean checked = IpsPlugin.getDefault().getPreferenceStore()
                    .getBoolean("DefaultModelDescriptionPage.LexicalSortingAction.isChecked"); //$NON-NLS-1$
            setChecked(checked);
            sortAndFilterDescriptionList();
        }

        @Override
        public void run() {
            sortAndFilterDescriptionList();
            IpsPlugin.getDefault().getPreferenceStore()
                    .setValue("DefaultModelDescriptionPage.LexicalSortingAction.isChecked", isChecked()); //$NON-NLS-1$
        }

    }

    /**
     * "filter" action for DescriptionItems.
     * 
     * @author Quirin Stoll
     */
    class FilterEmptyDescriptionsAction extends Action {
        public FilterEmptyDescriptionsAction() {
            super(Messages.DefaultModelDescriptionPage_FilterEmptyText, SWT.TOGGLE);

            setToolTipText(Messages.DefaultModelDescriptionPage_FilterEmptyTooltipText);
            setDescription(Messages.DefaultModelDescriptionPage_FilterEmptyDescription);
            ImageDescriptor descriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/cfilter.gif"); //$NON-NLS-1$
            setHoverImageDescriptor(descriptor);
            setImageDescriptor(descriptor);

            boolean checked = IpsPlugin.getDefault().getPreferenceStore()
                    .getBoolean("DefaultModelDescriptionPage.LexicalSortingAction.isChecked"); //$NON-NLS-1$
            setChecked(checked);
            sortAndFilterDescriptionList();
        }

        @Override
        public void run() {
            sortAndFilterDescriptionList();
            IpsPlugin.getDefault().getPreferenceStore()
                    .setValue("DefaultModelDescriptionPage.LexicalSortingAction.isChecked", isChecked()); //$NON-NLS-1$
        }
    }

    /**
     * Comparator for DescriptionItems. Sort DescriptionItems by Name & Unicodestyle: z < ä,ö,ü,ß.
     * 
     * @author Markus Blum
     */
    class DescriptionItemComparator implements Comparator<DescriptionItem> {
        @Override
        public int compare(DescriptionItem item1, DescriptionItem item2) {
            Assert.isNotNull(item1, "DescriptionItem1"); //$NON-NLS-1$
            Assert.isNotNull(item2, "DescriptionItem2"); //$NON-NLS-1$
            return item1.getName().compareTo(item2.getName());
        }
    }

    /**
     * Delete empty descriptions
     * 
     */
    private void deleteEmptyDescriptions() {
        List<DescriptionItem> copyActiveList = copyDescriptionItems(activeList);
        activeList.clear();
        for (DescriptionItem item : copyActiveList) {
            if (item.getChildren().size() > 0) {
                List<DescriptionItem> items = item.getChildren();
                List<DescriptionItem> newItems = new ArrayList<DescriptionItem>();
                for (DescriptionItem item2 : items) {
                    if (!item2.getDescription().isEmpty()) {
                        newItems.add(item2);
                    }
                }
                if (newItems.size() > 0) {
                    item.setChildren(newItems);
                    activeList.add(item);
                }
            } else {
                if (!item.getDescription().isEmpty()) {
                    activeList.add(item);
                }
            }
        }
    }

    private void sortRecursive(List<DescriptionItem> list) {
        Collections.sort(list, new DescriptionItemComparator());
        for (DescriptionItem item : list) {
            if (item.hasChildren()) {
                sortRecursive(item.getChildren());
            }
        }
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
     * @param items List with DescriptionItems.
     */
    public void setDescriptionItems(List<DescriptionItem> items) {
        defaultList = items;
        activeList = copyDescriptionItems(defaultList);
        refresh();
    }

    public List<DescriptionItem> copyDescriptionItems(List<DescriptionItem> original) {
        List<DescriptionItem> copy = new ArrayList<DescriptionItem>();
        for (DescriptionItem item : original) {
            DescriptionItem itemCopy = copyItem(item);
            copy.add(itemCopy);
        }
        return copy;

    }

    private DescriptionItem copyItem(DescriptionItem item) {
        DescriptionItem copyItem = new DescriptionItem();
        copyItem.setName(item.getName());
        copyItem.setDescription(item.getDescription());
        copyItem.setChildren(copyDescriptionItems(item.getChildren()));
        return copyItem;
    }

}
