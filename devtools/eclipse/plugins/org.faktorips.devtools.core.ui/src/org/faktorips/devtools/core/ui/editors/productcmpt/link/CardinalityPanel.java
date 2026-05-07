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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplateValueUiStatus;
import org.faktorips.devtools.core.ui.editors.productcmpt.TemplateValueUiUtil;
import org.faktorips.devtools.core.ui.views.producttemplate.ShowTemplatePropertyUsageViewAction;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.util.TemplatedValueUtil;

import edu.umd.cs.findbugs.annotations.CheckForNull;

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
    private ToolBar templateStatusToolBar;

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
            templateStatusToolBar = new ToolBar(kardinalityPane, SWT.FLAT);
            templateStatusToolBar.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false));
            TemplateValueUiUtil.setUpStatusToolItem(templateStatusToolBar, bindingContext, pmo.getTemplateLinkPmo());
            bindingContext.bindEnabled(templateStatusToolBar, pmo.getTemplateLinkPmo(),
                    TemplateLinkPmo.PROPERTY_STATUS_BUTTON_ENABLED);
            templateStatusToolBar.setMenu(new TemplateToolBarMenuBuilder(templateStatusToolBar).createTemplateMenue());
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
        pmo.setSelectedViewItem(null);
        setDefaultFieldVisible(true);
        refresh();
    }

    /**
     * Activates this panel based on the selected association node (without links). The panel is
     * enabled only if the association's matching policy association has cardinality configurable.
     */
    public void setAssociationToEdit(AssociationViewItem associationViewItem) {
        pmo.setLinks(Collections.emptyList());
        pmo.setSelectedViewItem(associationViewItem);
        setDefaultFieldVisible(false);
        refresh();
    }

    /**
     * Activates this panel based on the selected policy association node (without links). Used for
     * policy associations with configurable cardinality that are not matched by any product
     * association.
     */
    public void setAssociationToEdit(PolicyAssociationViewItem viewItem) {
        pmo.setLinks(Collections.emptyList());
        pmo.setSelectedViewItem(viewItem);
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
            templateStatusToolBar.setEnabled(!pmo.isEmpty());
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
            templateStatusToolBar.setVisible(calculateTemplateStatusVisibility());
        }
    }

    private boolean calculateTemplateStatusVisibility() {
        if (pmo.isEmpty() || null == pmo.getTemplateLinkPmo()
                || null == pmo.getTemplateLinkPmo().getTemplatedProperty()) {
            return false;
        } else {
            IProductCmptLink link = pmo.getTemplateLinkPmo().getTemplatedProperty();
            return ((ProductCmptLink)link).isAssociationConfiguredInTemplate();
        }
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
        private List<LinkViewItem> links = Collections.emptyList();
        @CheckForNull
        private AbstractAssociationViewItem selectedViewItem;

        public List<LinkViewItem> getLinks() {
            return links;
        }

        public TemplateLinkPmo getTemplateLinkPmo() {
            return templatePmo;
        }

        public void setLinks(List<LinkViewItem> links) {
            this.links = links;
            updateTemplatePmo(links);
            notifyListeners();
        }

        public void setSelectedViewItem(@CheckForNull AbstractAssociationViewItem viewItem) {
            selectedViewItem = viewItem;
        }

        @CheckForNull
        private IPolicyCmptLinkCardinality getOrCreatePolicyCmptLinkCardinality() {
            if (selectedViewItem instanceof PolicyAssociationViewItem policyItem) {
                String name = policyItem.getAssociation().getName();
                return policyItem.getLinkContainer().getPolicyCmptLinkCardinality(name)
                        .orElseGet(() -> policyItem.getLinkContainer().newPolicyCmptLinkCardinality(name));
            }
            if (!(selectedViewItem instanceof AssociationViewItem assocItem)) {
                return null;
            }
            IPolicyCmptTypeAssociation matching = findMatchingPolicyAssociation();
            if (matching == null) {
                return null;
            }
            String assocName = matching.getName();
            return assocItem.getLinkContainer().getPolicyCmptLinkCardinality(assocName)
                    .orElseGet(() -> assocItem.getLinkContainer().newPolicyCmptLinkCardinality(assocName));
        }

        @CheckForNull
        private IPolicyCmptTypeAssociation findMatchingPolicyAssociation() {
            if (!(selectedViewItem instanceof AssociationViewItem assocItem)) {
                return null;
            }
            IProductCmptTypeAssociation association = assocItem.getAssociation();
            return association.findMatchingPolicyCmptTypeAssociation(association.getIpsProject());
        }

        private void updateTemplatePmo(List<LinkViewItem> links2) {
            if (links2.size() == 1) {
                templatePmo.setLink(links.get(0).getLink());
            } else {
                templatePmo.setLink(null);
            }
        }

        public boolean constrainsPolicyCmptTypeAssociation() {
            if (getLinks().isEmpty()) {
                if (selectedViewItem instanceof PolicyAssociationViewItem policyItem) {
                    IPolicyCmptTypeAssociation assoc = policyItem.getAssociation();
                    return assoc.isConfigurable() && assoc.isCardinalityConfigurable();
                }
                IPolicyCmptTypeAssociation matching = findMatchingPolicyAssociation();
                return matching != null && matching.isConfigurable() && matching.isCardinalityConfigurable();
            }
            for (LinkViewItem link : getLinks()) {
                IProductCmptLink productCmptLink = link.getLink();
                if (!productCmptLink.isCardinalityConfigurable(productCmptLink.getIpsProject())) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return getLinks().isEmpty() && selectedViewItem == null;
        }

        public boolean isMandatory() {
            if (!getLinks().isEmpty()) {
                return Objects.equals(getMinCardinality(), 1) && Objects.equals(getMaxCardinality(), 1)
                        && Objects.equals(getDefaultCardinality(), 1);
            }
            return Objects.equals(getMinCardinality(), 1) && Objects.equals(getMaxCardinality(), 1);
        }

        public boolean isOptional() {
            return Objects.equals(getMinCardinality(), 0) && Objects.equals(getMaxCardinality(), 1);
        }

        @CheckForNull
        private Integer getAssociationCardinality(Function<IPolicyCmptLinkCardinality, Integer> configuredGetter,
                Function<IPolicyCmptTypeAssociation, Integer> modelGetter) {
            if (selectedViewItem instanceof PolicyAssociationViewItem policyItem) {
                IPolicyCmptTypeAssociation assoc = policyItem.getAssociation();
                return policyItem.getLinkContainer()
                        .getPolicyCmptLinkCardinality(assoc.getName())
                        .map(configuredGetter)
                        .orElseGet(() -> modelGetter.apply(assoc));
            }
            if (!(selectedViewItem instanceof AssociationViewItem assocItem)) {
                return null;
            }
            IPolicyCmptTypeAssociation matching = findMatchingPolicyAssociation();
            if (matching == null) {
                return null;
            }
            return assocItem.getLinkContainer()
                    .getPolicyCmptLinkCardinality(matching.getName())
                    .map(configuredGetter)
                    .orElseGet(() -> modelGetter.apply(matching));
        }

        private void setAssociationCardinality(Integer newValue,
                BiConsumer<IPolicyCmptLinkCardinality, Integer> setter) {
            if (newValue == null) {
                return;
            }
            IPolicyCmptLinkCardinality linkCardinality = getOrCreatePolicyCmptLinkCardinality();
            if (linkCardinality != null) {
                setter.accept(linkCardinality, newValue);
            }
        }

        public Integer getMinCardinality() {
            if (getLinks().isEmpty()) {
                return getAssociationCardinality(IPolicyCmptLinkCardinality::getMinCardinality,
                        IPolicyCmptTypeAssociation::getMinCardinality);
            }
            Integer result = getLinks().get(0).getLink().getMinCardinality();
            for (LinkViewItem linkViewItem : getLinks()) {
                if (!result.equals(linkViewItem.getLink().getMinCardinality())) {
                    return null;
                }
            }
            return result;
        }

        /**
         * Sets the minimum number of target instances required in this relation.
         */
        public void setMinCardinality(Integer newValue) {
            if (getLinks().isEmpty()) {
                setAssociationCardinality(newValue, IPolicyCmptLinkCardinality::setMinCardinality);
                return;
            }
            for (LinkViewItem linkViewItem : getLinks()) {
                linkViewItem.getLink().setMinCardinality(newValue);
            }
        }

        /**
         * returns the default number of target instances in this relation.
         */
        public Integer getDefaultCardinality() {
            if (getLinks().isEmpty()) {
                return null;
            }
            Integer result = getLinks().get(0).getLink().getDefaultCardinality();
            for (LinkViewItem linkViewItem : getLinks()) {
                if (!result.equals(linkViewItem.getLink().getDefaultCardinality())) {
                    return null;
                }
            }
            return result;
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
            if (getLinks().isEmpty()) {
                return getAssociationCardinality(IPolicyCmptLinkCardinality::getMaxCardinality,
                        IPolicyCmptTypeAssociation::getMaxCardinality);
            }
            Integer result = getLinks().get(0).getLink().getMaxCardinality();
            for (LinkViewItem linkViewItem : getLinks()) {
                if (!result.equals(linkViewItem.getLink().getMaxCardinality())) {
                    return null;
                }
            }
            return result;
        }

        /**
         * Sets the maximum number of target instances allowed in this relation. An unlimited number
         * is represented by CARDINALITY_MANY.
         */
        public void setMaxCardinality(Integer newValue) {
            if (getLinks().isEmpty()) {
                setAssociationCardinality(newValue, IPolicyCmptLinkCardinality::setMaxCardinality);
                return;
            }
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
            TemplateValueUiStatus templateValueStatus = getTemplateLinkPmo().getTemplateValueStatus();
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

    private class TemplateToolBarMenuBuilder extends AbstractTemplateToolBarMenuBuilder {

        public TemplateToolBarMenuBuilder(ToolBar toolBar) {
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
}
