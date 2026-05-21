/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToIntFunction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.editors.productcmpt.AbstractTemplateToolBarMenuBuilder;
import org.faktorips.devtools.core.ui.editors.productcmpt.Messages;
import org.faktorips.devtools.core.ui.editors.productcmpt.SimpleOpenIpsObjectPartAction;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplateLinkPmo;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplatePolicyCmptLinkCardinalityPmo;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplateValueUiStatus;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplateValueUiUtil;
import org.faktorips.devtools.core.ui.views.producttemplate.ShowTemplatePropertyUsageViewAction;
import org.faktorips.devtools.model.internal.productcmpt.PolicyCmptLinkCardinality;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.util.TemplatedValueUtil;

/**
 * Panel to display cardinality. Note that this is <strong>NOT</strong> a control.
 * <p>
 * To edit a {@link IProductCmptLink} call {@link #setProductCmptLinkToEdit(List)} and this panel
 * will be updated with the links values. If an empty list is passed to the method, this panel
 * resets and disables itself.
 *
 *
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class CardinalityPanel implements IDataChangeableReadWriteAccess {

    private Text minCard;
    private Text maxCard;
    private Text defaultCard;
    private Label minMaxCardLabel;
    private Label defaultCardLabel;
    private Button optional;
    private Button mandatory;
    private Button other;

    private CardinalityField minCardField;
    private CardinalityField maxCardField;
    private CardinalityField defaultCardField;
    private Control defaultCardSpacer;
    private final MultiLinkPmo pmo = new MultiLinkPmo();

    private boolean dataChangeable;
    private BindingContext bindingContext = new BindingContext();
    private boolean showTemplateStatus;
    private ToolBar templateLinkToolBar;
    private ToolBar templateCardinalityToolBar;

    /**
     * Creates a new Cardinality panel
     *
     * @param showTemplateStatus whether the template status button should be displayed.
     */
    public CardinalityPanel(Composite parent, UIToolkit toolkit, boolean showTemplateStatus) {
        this.showTemplateStatus = showTemplateStatus;
        createUI(parent, toolkit);
        bindFields(parent);
    }

    private void createUI(Composite parent, UIToolkit toolkit) {
        Composite kardinalityPane = toolkit.createLabelEditColumnComposite(parent);
        ((GridLayout)kardinalityPane.getLayout()).numColumns = 4;
        ((GridLayout)kardinalityPane.getLayout()).horizontalSpacing = 9;
        ((GridLayout)kardinalityPane.getLayout()).marginWidth = 5;
        ((GridLayout)kardinalityPane.getLayout()).marginHeight = 8;

        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, false, false);
        kardinalityPane.setLayoutData(layoutData);

        // create header
        Label headerLabel = toolkit.createLabel(kardinalityPane, Messages.RelationsSection_cardinality);
        toolkit.setHorizontalSpan(headerLabel, showTemplateStatus ? 3 : 4);
        createToolbar(kardinalityPane);

        ((GridData)headerLabel.getLayoutData()).horizontalAlignment = SWT.CENTER;

        // create radio buttons
        CardinalitySelectionListener listener = new CardinalitySelectionListener();
        optional = toolkit.createRadioButton(kardinalityPane, Messages.CardinalityPanel_labelOptional);
        optional.addSelectionListener(listener);
        optional.setLayoutData(new GridData());
        toolkit.setHorizontalSpan(optional, 4);
        mandatory = toolkit.createRadioButton(kardinalityPane, Messages.CardinalityPanel_labelMandatory);
        mandatory.addSelectionListener(listener);
        mandatory.setLayoutData(new GridData());
        toolkit.setHorizontalSpan(mandatory, 4);
        other = toolkit.createRadioButton(kardinalityPane, Messages.CardinalityPanel_labelOther);
        other.addSelectionListener(listener);

        // Min/Max bei Other, Default darunter
        minCard = toolkit.createText(kardinalityPane);
        GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
        gd.widthHint = 15;
        minCard.setLayoutData(gd);
        minMaxCardLabel = toolkit.createLabel(kardinalityPane, ".."); //$NON-NLS-1$
        maxCard = toolkit.createText(kardinalityPane);
        gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
        gd.widthHint = 15;
        maxCard.setLayoutData(gd);

        /*
         * Default Input Field
         */

        defaultCardSpacer = toolkit.createVerticalSpacer(kardinalityPane, 3);
        toolkit.setHorizontalSpan(defaultCardSpacer, 4);

        defaultCardLabel = toolkit.createLabel(kardinalityPane, Messages.CardinalityPanel_LabelDefaultCardinality);

        defaultCard = toolkit.createText(kardinalityPane);
        ((GridData)defaultCard.getLayoutData()).widthHint = 20;
        toolkit.setHorizontalSpan(defaultCard, 3);

        kardinalityPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(parent);
    }

    private void bindFields(Composite parent) {
        minCardField = new CardinalityField(minCard);
        maxCardField = new CardinalityField(maxCard);
        defaultCardField = new CardinalityField(defaultCard);
        bindingContext.bindContent(minCardField, pmo, IProductCmptLink.PROPERTY_MIN_CARDINALITY);
        bindingContext.bindContent(maxCardField, pmo, IProductCmptLink.PROPERTY_MAX_CARDINALITY);
        bindingContext.bindContent(defaultCardField, pmo, IProductCmptLink.PROPERTY_DEFAULT_CARDINALITY);

        bindingContext.bindEnabled(minCard, pmo, MultiLinkPmo.PROPERTY_MIN_MAX_ENABLED);
        bindingContext.bindEnabled(maxCard, pmo, MultiLinkPmo.PROPERTY_MIN_MAX_ENABLED);
        bindingContext.bindEnabled(defaultCard, pmo, MultiLinkPmo.PROPERTY_DEFAULT_ENABLED);
        bindingContext.bindEnabled(defaultCardLabel, pmo, MultiLinkPmo.PROPERTY_DEFAULT_ENABLED);

        parent.addDisposeListener($ -> bindingContext.dispose());
    }

    private void createToolbar(Composite kardinalityPane) {
        if (showTemplateStatus) {
            Composite toolBarContainer = new Composite(kardinalityPane, SWT.NONE);
            GridLayout containerLayout = new GridLayout(1, false);
            containerLayout.marginWidth = 0;
            containerLayout.marginHeight = 0;
            toolBarContainer.setLayout(containerLayout);
            toolBarContainer.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false));

            templateLinkToolBar = new ToolBar(toolBarContainer, SWT.FLAT);
            templateLinkToolBar.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
            TemplateValueUiUtil.setUpStatusToolItem(templateLinkToolBar, bindingContext, pmo.getTemplateLinkPmo());
            bindingContext.bindEnabled(templateLinkToolBar, pmo.getTemplateLinkPmo(),
                    TemplateLinkPmo.PROPERTY_STATUS_BUTTON_ENABLED);
            templateLinkToolBar.setMenu(
                    new LinkTemplateToolBarMenuBuilder(templateLinkToolBar).createTemplateMenue());

            templateCardinalityToolBar = new ToolBar(toolBarContainer, SWT.FLAT);
            GridData cardinalityToolBarData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
            cardinalityToolBarData.exclude = true;
            templateCardinalityToolBar.setLayoutData(cardinalityToolBarData);
            templateCardinalityToolBar.setVisible(false);
            TemplateValueUiUtil.setUpStatusToolItem(templateCardinalityToolBar, bindingContext,
                    pmo.getTemplateCardinalityPmo());
            bindingContext.bindEnabled(templateCardinalityToolBar, pmo.getTemplateCardinalityPmo(),
                    TemplatePolicyCmptLinkCardinalityPmo.PROPERTY_STATUS_BUTTON_ENABLED);
            templateCardinalityToolBar.setMenu(
                    new CardinalityTemplateToolBarMenuBuilder(templateCardinalityToolBar).createTemplateMenue());
        }
    }

    /**
     * Configures this {@link CardinalityPanel} to change the cardinality values of the given link
     * when the user modifies the text controls. If the given {@link IProductCmptLink} is
     * <code>null</code> this panel will be deactivated.
     *
     * @param links the {@link IProductCmptLink} currently edited by this panel or an empty list if
     *            none.
     */
    public void setProductCmptLinkToEdit(List<LinkViewItem> links) {
        pmo.setLinks(links);
        setDefaultFieldVisible(true);
        refresh();
    }

    /**
     * Activates this panel based on the selected association node (without links). The panel is
     * enabled only if the association's matching policy association has cardinality configurable.
     */
    public void setAssociationToEdit(AbstractAssociationViewItem associationViewItem) {
        pmo.setPolicyCmptLinkCardinality(associationViewItem.findPolicyCmptLinkCardinality());
        setDefaultFieldVisible(false);
        refresh();
    }

    private void setDefaultFieldVisible(boolean visible) {
        defaultCardSpacer.setVisible(visible);
        ((GridData)defaultCardSpacer.getLayoutData()).exclude = !visible;
        defaultCardLabel.setVisible(visible);
        ((GridData)defaultCardLabel.getLayoutData()).exclude = !visible;
        defaultCard.setVisible(visible);
        ((GridData)defaultCard.getLayoutData()).exclude = !visible;
        defaultCardSpacer.getParent().layout();
    }

    /**
     * Disables this panel (as in setEnabled(false)) and removes all values and selection from its
     * controls. The panel then displays no data.
     */
    public void deactivate() {
        setRadioButtonEnabled(false);
        setDefaultEnabled(false);
        setMinMaxEnabled(false);
        mandatory.setSelection(false);
        optional.setSelection(false);
        other.setSelection(false);
        minCard.setText(""); //$NON-NLS-1$
        maxCard.setText(""); //$NON-NLS-1$
        defaultCard.setText(""); //$NON-NLS-1$
        if (showTemplateStatus) {
            templateLinkToolBar.setEnabled(!pmo.isEmpty());
            templateCardinalityToolBar.setEnabled(!pmo.isEmpty());
        }
    }

    public void refresh() {
        if (!pmo.constrainsPolicyCmptTypeAssociation()) {
            deactivate();
        } else {
            update(true);
        }
        updateTemplateStatusVisibility();
    }

    private void updateTemplateStatusVisibility() {
        if (showTemplateStatus) {
            final boolean linkVisible = calculateLinkTemplateStatusVisibility();
            final boolean cardinalityVisible = !linkVisible && calculateCardinalityTemplateStatusVisibility();
            templateLinkToolBar.setVisible(linkVisible);
            ((GridData)templateLinkToolBar.getLayoutData()).exclude = !linkVisible;
            templateCardinalityToolBar.setVisible(cardinalityVisible);
            ((GridData)templateCardinalityToolBar.getLayoutData()).exclude = !cardinalityVisible;
            Composite container = templateLinkToolBar.getParent();
            container.setVisible(linkVisible || cardinalityVisible);
            ((GridData)container.getLayoutData()).exclude = !(linkVisible || cardinalityVisible);
            container.layout();
            container.getParent().layout(true, true);
        }
    }

    private boolean calculateLinkTemplateStatusVisibility() {
        if (pmo.isEmpty() || null == pmo.getTemplateLinkPmo().getTemplatedProperty()) {
            return false;
        }
        IProductCmptLink link = pmo.getTemplateLinkPmo().getTemplatedProperty();
        return ((ProductCmptLink)link).isAssociationConfiguredInTemplate();
    }

    private boolean calculateCardinalityTemplateStatusVisibility() {
        if (pmo.isEmpty() || null == pmo.getTemplateCardinalityPmo().getTemplatedProperty()) {
            return false;
        }
        IPolicyCmptLinkCardinality cardinality = pmo.getTemplateCardinalityPmo().getTemplatedProperty();
        return ((PolicyCmptLinkCardinality)cardinality).isAssociationConfiguredInTemplate();
    }

    /**
     * Method to enable or disable this panel. If no checkbox (optional, mandatory, other) is
     * selected all controls will be disabled, regardless of the given value. This is the case if no
     * ProdCmptLink is selected in the {@link LinksSection}s tree, thus this panel is disabled
     * completely.
     */
    public void update(boolean enabled) {
        update(false, enabled);
    }

    private void update(boolean forceOther, boolean enabled) {
        if (!pmo.isEmpty()) {
            boolean doEnable = enabled & isDataChangeable();
            setRadioButtonState(RadioButtonState.getStateOf(forceOther, pmo), doEnable);
        } else {
            setRadioButtonState(RadioButtonState.getStateOf(forceOther, pmo), false);
        }
    }

    private void setRadioButtonState(RadioButtonState state, boolean doEnable) {
        mandatory.setSelection(state == RadioButtonState.MANDATORY);
        optional.setSelection(state == RadioButtonState.OPTIONAL);
        other.setSelection(state == RadioButtonState.OTHER);
        setDefaultEnabled(state != RadioButtonState.MANDATORY && doEnable);
        setMinMaxEnabled(state == RadioButtonState.OTHER && doEnable);
        setRadioButtonEnabled(doEnable);
        bindingContext.updateUI();
    }

    private void setRadioButtonEnabled(boolean doEnable) {
        mandatory.setEnabled(doEnable);
        optional.setEnabled(doEnable);
        other.setEnabled(doEnable);
    }

    private void setDefaultEnabled(boolean doEnable) {
        defaultCard.setEnabled(doEnable);
        defaultCardLabel.setEnabled(doEnable);
    }

    private void setMinMaxEnabled(boolean doEnable) {
        minCard.setEnabled(doEnable);
        maxCard.setEnabled(doEnable);
        minMaxCardLabel.setEnabled(doEnable);
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        update(changeable);
    }

    /**
     * Listener to update on cardinality modifications.
     *
     * @author Thorsten Guenther
     * @author Stefan Widmaier
     */
    private class CardinalitySelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            for (LinkViewItem linkViewItem : pmo.getLinks()) {
                linkViewItem.getLink().setTemplateValueStatus(TemplateValueStatus.DEFINED);
            }
            pmo.getPolicyCmptLinkCardinality().ifPresent(c -> c.setTemplateValueStatus(TemplateValueStatus.DEFINED));
            if (e.getSource() == optional) {
                pmo.setMinCardinality(0);
                pmo.setMaxCardinality(1);
            } else if (e.getSource() == mandatory) {
                pmo.setMinCardinality(1);
                pmo.setMaxCardinality(1);
                if (!pmo.getLinks().isEmpty()) {
                    pmo.setDefaultCardinality(1);
                }
            }
            update(e.getSource() == other, true);
        }

    }

    public static class MultiLinkPmo extends PresentationModelObject {

        public static final String PROPERTY_DEFAULT_ENABLED = "defaultEnabled"; //$NON-NLS-1$
        public static final String PROPERTY_MIN_MAX_ENABLED = "minMaxEnabled"; //$NON-NLS-1$
        public static final String PROPERTY_RADIO_BUTTONS_ENABLED = "radioButtonsEnabled"; //$NON-NLS-1$

        /**
         * Must never change, thus final. UI is bound to this pmo.
         */
        private final TemplateLinkPmo templatePmo = new TemplateLinkPmo();
        private final TemplatePolicyCmptLinkCardinalityPmo templateCardinalityPmo = new TemplatePolicyCmptLinkCardinalityPmo();
        private List<LinkViewItem> links = List.of();
        private Optional<IPolicyCmptLinkCardinality> policyCmptLinkCardinality = Optional.empty();

        public List<LinkViewItem> getLinks() {
            return links;
        }

        public Optional<IPolicyCmptLinkCardinality> getPolicyCmptLinkCardinality() {
            return policyCmptLinkCardinality;
        }

        public TemplateLinkPmo getTemplateLinkPmo() {
            return templatePmo;
        }

        public TemplatePolicyCmptLinkCardinalityPmo getTemplateCardinalityPmo() {
            return templateCardinalityPmo;
        }

        public void setLinks(List<LinkViewItem> links) {
            setEditedParts(links, Optional.empty());
        }

        public void setPolicyCmptLinkCardinality(Optional<IPolicyCmptLinkCardinality> policyCmptLinkCardinality) {
            setEditedParts(List.of(), policyCmptLinkCardinality);
        }

        public void setEditedParts(List<LinkViewItem> links,
                Optional<IPolicyCmptLinkCardinality> policyCmptLinkCardinality) {
            this.links = links;
            this.policyCmptLinkCardinality = policyCmptLinkCardinality;
            updateTemplatePmos();
            notifyListeners();
        }

        private void updateTemplatePmos() {
            if (links.size() == 1) {
                templatePmo.setLink(links.get(0).getLink());
            } else {
                templatePmo.setLink(null);
            }
            policyCmptLinkCardinality.ifPresentOrElse(
                    templateCardinalityPmo::setCardinality,
                    () -> templateCardinalityPmo.setCardinality(null));
        }

        public boolean constrainsPolicyCmptTypeAssociation() {
            if (getPolicyCmptLinkCardinality().isPresent()) {
                return true;
            }
            for (LinkViewItem link : getLinks()) {
                IProductCmptLink productCmptLink = link.getLink();
                if (!productCmptLink.constrainsPolicyCmptTypeAssociation(productCmptLink.getIpsProject())) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return getLinks().isEmpty() && policyCmptLinkCardinality.isEmpty();
        }

        public boolean isMandatory() {
            return Objects.equals(getMinCardinality(), 1) && Objects.equals(getMaxCardinality(), 1)
                    && (policyCmptLinkCardinality.isPresent() || Objects.equals(getDefaultCardinality(), 1));
        }

        public boolean isOptional() {
            return Objects.equals(getMinCardinality(), 0) && Objects.equals(getMaxCardinality(), 1);
        }

        private Integer getFromLink(ToIntFunction<IProductCmptLink> getter) {
            if (getLinks().isEmpty()) {
                return null;
            }
            Integer result = getter.applyAsInt(getLinks().get(0).getLink());
            for (LinkViewItem linkViewItem : getLinks()) {
                if (!result.equals(getter.applyAsInt(linkViewItem.getLink()))) {
                    return null;
                }
            }
            return result;
        }

        public Integer getMinCardinality() {
            if (policyCmptLinkCardinality.isPresent()) {
                return policyCmptLinkCardinality.map(IPolicyCmptLinkCardinality::getMinCardinality)
                        .orElseGet(() -> findPolicyCmptTypeAssociation()
                                .map(IPolicyCmptTypeAssociation::getMinCardinality).orElse(null));
            }
            return getFromLink(IProductCmptLink::getMinCardinality);
        }

        /**
         * Sets the minimum number of target instances required in this relation.
         */
        public void setMinCardinality(Integer newValue) {
            policyCmptLinkCardinality.ifPresent(c -> c.setMinCardinality(newValue));
            for (LinkViewItem linkViewItem : getLinks()) {
                linkViewItem.getLink().setMinCardinality(newValue);
            }
        }

        /**
         * returns the default number of target instances in this relation.
         */
        public Integer getDefaultCardinality() {
            return getFromLink(IProductCmptLink::getDefaultCardinality);
        }

        /**
         * Sets the default number of target instances in this relation.
         */
        public void setDefaultCardinality(Integer newValue) {
            for (LinkViewItem linkViewItem : getLinks()) {
                linkViewItem.getLink().setDefaultCardinality(newValue);
            }
        }

        /**
         * Returns the maximum number of target instances allowed in this relation. If the number is
         * not limited CARDINALITY_MANY is returned.
         */
        public Integer getMaxCardinality() {
            if (policyCmptLinkCardinality.isPresent()) {
                return policyCmptLinkCardinality.map(IPolicyCmptLinkCardinality::getMaxCardinality)
                        .orElseGet(() -> findPolicyCmptTypeAssociation()
                                .map(IPolicyCmptTypeAssociation::getMaxCardinality).orElse(null));
            }
            return getFromLink(IProductCmptLink::getMaxCardinality);
        }

        private Optional<IPolicyCmptTypeAssociation> findPolicyCmptTypeAssociation() {
            return policyCmptLinkCardinality.map(c -> c.findAssociation(c.getIpsProject()));
        }

        /**
         * Sets the maximum number of target instances allowed in this relation. An unlimited number
         * is represented by CARDINALITY_MANY.
         */
        public void setMaxCardinality(Integer newValue) {
            policyCmptLinkCardinality.ifPresent(c -> c.setMaxCardinality(newValue));
            for (LinkViewItem linkViewItem : getLinks()) {
                linkViewItem.getLink().setMaxCardinality(newValue);
            }
        }

        public boolean isDefaultEnabled() {
            return isAllowInput() && !isMandatory();
        }

        public boolean isMinMaxEnabled() {
            return isAllowInput();
        }

        private boolean isAllowInput() {
            return !isEmpty() && isTemplateStatusAllowingInput();
        }

        private boolean isTemplateStatusAllowingInput() {
            TemplateValueUiStatus templateValueStatus = !getLinks().isEmpty()
                    ? getTemplateLinkPmo().getTemplateValueStatus()
                    : templateCardinalityPmo.getTemplateValueStatus();
            return templateValueStatus != TemplateValueUiStatus.INHERITED
                    && templateValueStatus != TemplateValueUiStatus.UNDEFINED;
        }
    }

    private enum RadioButtonState {
        MANDATORY,
        OPTIONAL,
        OTHER;

        public static RadioButtonState getStateOf(boolean forceOther, MultiLinkPmo links) {
            if (forceOther) {
                return OTHER;
            } else if (links.isMandatory()) {
                return MANDATORY;
            } else if (links.isOptional()) {
                return OPTIONAL;
            } else {
                return OTHER;
            }
        }

    }

    private class LinkTemplateToolBarMenuBuilder extends AbstractTemplateToolBarMenuBuilder {

        public LinkTemplateToolBarMenuBuilder(ToolBar toolBar) {
            super(toolBar);
        }

        @Override
        protected void addOpenTemplateAction(IMenuManager manager) {
            IProductCmptLink templateLink = pmo.getTemplateLinkPmo().findTemplateLink();
            if (templateLink != null) {
                String text = NLS.bind(Messages.AttributeValueEditComposite_MenuItem_openTemplate,
                        templateLink.getProductCmptLinkContainer().getProductCmpt().getName());
                IAction openTemplateAction = new SimpleOpenIpsObjectPartAction<>(templateLink, text);
                manager.add(openTemplateAction);
            }
        }

        @Override
        protected void addShowTemplatePropertyUsageAction(IMenuManager manager) {
            if (TemplatedValueUtil.isTemplateValue(pmo.getTemplateLinkPmo().getLink())) {
                ITemplatedValue templateValue = pmo.getTemplateLinkPmo().getLink();
                String text = Messages.CardinalityPanel_MenuItem_showUsage;
                manager.add(new ShowTemplatePropertyUsageViewAction(templateValue, text));
            } else if (pmo.getTemplateLinkPmo().findTemplateLink() != null) {
                ITemplatedValue templateValue = pmo.getTemplateLinkPmo().findTemplateLink();
                String text = NLS.bind(Messages.CardinalityPanel_MenuItem_showTemplateLinkUsage,
                        templateValue.getTemplatedValueContainer().getProductCmpt().getName());
                manager.add(new ShowTemplatePropertyUsageViewAction(templateValue, text));
            }
        }

    }

    private class CardinalityTemplateToolBarMenuBuilder extends AbstractTemplateToolBarMenuBuilder {

        public CardinalityTemplateToolBarMenuBuilder(ToolBar toolBar) {
            super(toolBar);
        }

        @Override
        protected void addOpenTemplateAction(IMenuManager manager) {
            IPolicyCmptLinkCardinality templateCardinality = pmo.getTemplateCardinalityPmo()
                    .findTemplateCardinality();
            if (templateCardinality != null) {
                String text = NLS.bind(Messages.AttributeValueEditComposite_MenuItem_openTemplate,
                        templateCardinality.getTemplatedValueContainer().getProductCmpt().getName());
                manager.add(new SimpleOpenIpsObjectPartAction<>(templateCardinality, text));
            }
        }

        @Override
        protected void addShowTemplatePropertyUsageAction(IMenuManager manager) {
            IPolicyCmptLinkCardinality cardinality = pmo.getTemplateCardinalityPmo().getCardinality();
            if (TemplatedValueUtil.isTemplateValue(cardinality)) {
                String text = Messages.CardinalityPanel_MenuItem_showUsage;
                manager.add(new ShowTemplatePropertyUsageViewAction(cardinality, text));
            } else {
                IPolicyCmptLinkCardinality templateCardinality = pmo.getTemplateCardinalityPmo()
                        .findTemplateCardinality();
                if (templateCardinality != null) {
                    String text = NLS.bind(Messages.CardinalityPanel_MenuItem_showTemplateLinkUsage,
                            templateCardinality.getTemplatedValueContainer().getProductCmpt().getName());
                    manager.add(new ShowTemplatePropertyUsageViewAction(templateCardinality, text));
                }
            }
        }

    }
}
