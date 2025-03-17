/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.Page;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.ITypePart;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A page for presenting {@link DescriptionItem DescriptionItems} similar to the outline view.
 *
 * The attributes and their descriptions are presented within an ExpandableComposite.
 */
public abstract class DefaultModelDescriptionPage extends Page implements IIpsSrcFilesChangeListener {

    @SuppressWarnings("restriction")
    private static final ImageDescriptor DESC_ELCL_FILTER = org.eclipse.jdt.internal.ui.JavaPluginImages.DESC_ELCL_FILTER;

    private final Listener listener = $ -> sortAndFilterDescriptionList();

    private FormToolkit toolkit;

    private ScrolledForm form;

    private Composite expandableContainer;

    // List of DescriptionItems sorted by parent
    private List<DescriptionItem> defaultList;
    // defaultList sorted lexical or not
    private List<DescriptionItem> activeList;

    private String title;

    private Color colorGray;

    private IIpsObject ipsObject;

    private FilterEmptyDescriptionsAction filterEmptyDescriptionsAction;

    private LexicalSortingAction lexicalSortingAction;

    private FilterDescriptionsByTypeAction filterDescriptionsByTypeAction;

    private FilterDescriptionsByTypeDialog dialog;

    public DefaultModelDescriptionPage() {
        defaultList = new ArrayList<>();
        activeList = new ArrayList<>();
        IIpsModel.get().addIpsSrcFilesChangedListener(this);
    }

    /**
     * Initialize the DescriptionPage
     */
    protected void setDescriptionData() {
        if (getIpsObject() == null) {
            setTitle(Messages.DefaultModelDescriptionPage_ErrorIpsModelNotFound);
            setDescriptionItems(new ArrayList<>());
            return;
        }
        try {
            if (getIpsObject() instanceof ILabeledElement) {
                setTitle(IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel((ILabeledElement)getIpsObject()));
            } else {
                setTitle(getIpsObject().getName());
            }
            setDescriptionItems(createDescriptions());
        } catch (IpsException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Creates a List of DescriptionItems
     */
    protected abstract List<DescriptionItem> createDescriptions() throws IpsException;

    /**
     * Creates DescriptionItems
     */
    protected void createDescriptionItem(IDescribedElement describedElement, List<DescriptionItem> descriptions) {
        String label = createLabel(describedElement);
        String description = createDescription(describedElement);
        String deprecation = createDeprecation(describedElement);
        DescriptionItem item = new DescriptionItem(label, description, deprecation, describedElement);
        descriptions.add(item);
    }

    private String createLabel(IDescribedElement describedElement) {
        String label;
        if (describedElement instanceof IAssociation association) {
            if (association.is1ToMany()) {
                label = IIpsModel.get().getMultiLanguageSupport()
                        .getLocalizedPluralLabel(((ILabeledElement)describedElement));
            } else {
                label = IIpsModel.get().getMultiLanguageSupport()
                        .getLocalizedLabel(((ILabeledElement)describedElement));
            }
        } else if (describedElement instanceof ILabeledElement) {
            label = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(((ILabeledElement)describedElement));
        } else {
            label = describedElement.getName();
        }
        return label;
    }

    private String createDescription(IDescribedElement describedElement) {
        return IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(describedElement);
    }

    DescriptionItem createStructureDescriptionItem() {
        IIpsObject structure = getIpsObject();
        String localizedDescription = createDescription(structure);
        String deprecation = createDeprecation(structure);
        return new DescriptionItem(
                Messages.DefaultModelDescriptionPage_GeneralInformation,
                localizedDescription, deprecation);
    }

    private String createDeprecation(IDescribedElement describedElement) {
        if (describedElement instanceof IVersionControlledElement
                && ((IVersionControlledElement)describedElement).isDeprecated()) {
            return ((IVersionControlledElement)describedElement).getDeprecation().toString();
        }
        return null;
    }

    @Override
    public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
        if (ipsObject == null) {
            return;
        }
        if (event.getChangedIpsSrcFiles().contains(ipsObject.getIpsSrcFile())) {
            Display.getDefault().asyncExec(this::setDescriptionData);
        }
    }

    public void setIpsObject(IIpsObject ipsObject) {
        this.ipsObject = ipsObject;
    }

    public IIpsObject getIpsObject() {
        return ipsObject;
    }

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
        form.addListener(SWT.Show, listener);

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
        filterDescriptionsByTypeAction = new FilterDescriptionsByTypeAction();
        toolBarManager.add(filterEmptyDescriptionsAction);
        toolBarManager.add(lexicalSortingAction);
        toolBarManager.add(filterDescriptionsByTypeAction);
        sortAndFilterDescriptionList();
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
            excomposite.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            excomposite.setTitleBarForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
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
        clientGroup.setForeground(excomposite.getForeground());
        clientGroup.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        TableWrapLayout layout = new TableWrapLayout();
        layout.verticalSpacing = 7;
        layout.horizontalSpacing = 0;
        layout.numColumns = 1;
        layout.leftMargin = 20;
        clientGroup.setLayout(layout);

        StringBuilder sb = new StringBuilder();
        String description = item.getDescription().trim();

        // Set faktorips.attribute description
        if ((!item.hasChildren() && IpsStringUtils.isEmpty(description)) || !IpsStringUtils.isEmpty(description)) {
            createDescriptionContent(item, excomposite, clientGroup, sb, description);
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

    private void createDescriptionContent(DescriptionItem item,
            ExpandableComposite excomposite,
            Composite clientGroup,
            StringBuilder sb,
            String description) {
        FormText client = toolkit.createFormText(clientGroup, true);
        client.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        client.setBackground(excomposite.getBackground());
        client.setForeground(excomposite.getForeground());
        client.setWhitespaceNormalized(false);
        sb.append("<form>"); //$NON-NLS-1$
        String deprecation = item.getDeprecation();
        if (IpsStringUtils.isNotBlank(deprecation)) {
            sb.append("<p><b>"); //$NON-NLS-1$
            sb.append(deprecation);
            sb.append("</b></p>"); //$NON-NLS-1$
        }
        if (IpsStringUtils.isBlank(description) && !item.hasChildren()) {
            client.setColor("gray", colorGray); //$NON-NLS-1$
            // if no description is given show the default text in gray foreground color
            sb.append("<p><span color=\"gray\">"); //$NON-NLS-1$
            sb.append(Messages.DefaultModelDescriptionPage_NoDescriptionAvailable);
            sb.append("</span></p>"); //$NON-NLS-1$
        } else {
            sb.append("<p>"); //$NON-NLS-1$
            String nullPresentation = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            String formattedNullRepresentation = nullPresentation.replace("<", "&lt;").replace(">", "&gt;");
            String formattedDescription = description.replaceAll("\\R", "<br />").replace(nullPresentation,
                    formattedNullRepresentation);
            sb.append(formattedDescription);
            sb.append("</p>"); //$NON-NLS-1$
        }
        sb.append("</form>"); //$NON-NLS-1$
        try {
            client.setText(sb.toString(), true, true);
        } catch (IllegalArgumentException iae) {
            client.setText(
                    sb.toString().replace("<form><p>", "").replace("</p></form>", "").replace("&lt;", "<")
                            .replace("&gt;", ">").replace("<br />", "\n"),
                    false,
                    true);
        }
        // don't ignore whitespaces and newlines
    }

    @Override
    public void dispose() {
        IIpsModel.get().removeIpsSrcFilesChangedListener(this);
        if (toolkit != null) {
            toolkit.dispose();
        }

        if (form != null) {
            if (!form.isDisposed()) {
                form.removeListener(SWT.Show, listener);
                form.dispose();
            }
        }
        super.dispose();
    }

    @Override
    public Control getControl() {
        if (form == null) {
            return null;
        }
        return form;
    }

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

        if (form == null || form.isDisposed()) {
            return;
        }

        createForm();
        form.reflow(true);
    }

    private void sortAndFilterDescriptionList() {
        final boolean filter = filterEmptyDescriptionsAction != null && filterEmptyDescriptionsAction.isChecked();
        final boolean sort = lexicalSortingAction != null && lexicalSortingAction.isChecked();
        final boolean filterByType = filterDescriptionsByTypeAction != null
                && filterDescriptionsByTypeAction.isChecked();
        if (form == null) {
            return;
        }
        BusyIndicator.showWhile(form.getDisplay(), () -> {
            activeList = copyDescriptionItems(defaultList);
            if (filter) {
                deleteEmptyDescriptions();
            }
            if (sort) {
                sortRecursive(activeList);
            }
            if (filterByType) {
                filterDescriptionsByType();
            }
            refresh();
        });

    }

    /**
     * Filters the displayed {@link DescriptionItem}s by the selected types in the
     * {@link FilterDescriptionsByTypeDialog}
     */
    private void filterDescriptionsByType() {
        List<DescriptionItem> copyActiveList = copyDescriptionItems(activeList);
        List<Class<? extends ITypePart>> selectedPartsList = dialog.getSavedParts();
        if (!selectedPartsList.isEmpty()) {
            activeList.clear();
            for (DescriptionItem item : copyActiveList) {
                IDescribedElement element = item.getElement();
                for (Class<? extends ITypePart> part : selectedPartsList) {

                    if (element != null && part.isAssignableFrom(element.getClass())) {
                        activeList.add(item);
                        break;
                    }
                }
            }
        }

    }

    /**
     * Delete empty descriptions
     */
    private void deleteEmptyDescriptions() {
        List<DescriptionItem> copyActiveList = copyDescriptionItems(activeList);
        activeList.clear();
        for (DescriptionItem item : copyActiveList) {
            if (item.getChildren().size() > 0) {
                List<DescriptionItem> items = item.getChildren();
                List<DescriptionItem> newItems = new ArrayList<>();
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
        sortAndFilterDescriptionList();
    }

    public List<DescriptionItem> copyDescriptionItems(List<DescriptionItem> original) {
        List<DescriptionItem> copy = new ArrayList<>();
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
        copyItem.setDeprecation(item.getDeprecation());
        copyItem.setChildren(copyDescriptionItems(item.getChildren()));
        copyItem.setElement(item.getElement());
        return copyItem;
    }

    /**
     * "sort" action for DescriptionItems.
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
                    .getBoolean("DefaultModelDescriptionPage.FilterEmptyDescriptionsAction.isChecked"); //$NON-NLS-1$
            setChecked(checked);
        }

        @Override
        public void run() {
            sortAndFilterDescriptionList();
            IpsPlugin.getDefault().getPreferenceStore()
                    .setValue("DefaultModelDescriptionPage.FilterEmptyDescriptionsAction.isChecked", isChecked()); //$NON-NLS-1$
        }
    }

    /**
     * "filter by type" action for {@link DescriptionItem}.
     */
    class FilterDescriptionsByTypeAction extends Action {

        public FilterDescriptionsByTypeAction() {
            super(Messages.DefaultModelDescriptionPage_FilterDescriptionsByTypeText, SWT.OPEN);

            setToolTipText(Messages.DefaultModelDescriptionPage_FilterDescriptionsByTypeTooltipText);
            setDescription(Messages.DefaultModelDescriptionPage_FilterDescriptionsByType);
            setImageDescriptor(DESC_ELCL_FILTER);
            boolean checked = IpsPlugin.getDefault().getPreferenceStore()
                    .getBoolean("DefaultModelDescriptionPage.FilterDescriptionsByTypeAction.isChecked"); //$NON-NLS-1$

            setChecked(checked);
            dialog = new FilterDescriptionsByTypeDialog(getSite().getShell(),
                    new FilterDescriptionByTypeDialogContentProvider());
        }

        @Override
        public void run() {
            dialog.setTitle(Messages.DefaultModelDescriptionPage_FilterDescriptionsByTypeDialogTitle);
            dialog.setHelpAvailable(false);

            dialog.open();
            boolean isSaved = !dialog.getSavedParts().isEmpty();
            IpsPlugin.getDefault().getPreferenceStore()
                    .setValue("DefaultModelDescriptionPage.FilterDescriptionsByTypeAction.isChecked", isSaved); //$NON-NLS-1$
            setChecked(isSaved);
            sortAndFilterDescriptionList();
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

}
