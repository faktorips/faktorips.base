/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueComparator;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.FormulaEditControl;
import org.faktorips.devtools.core.ui.controls.TableContentsUsageRefControl;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * Section to display and edit the formulas of a product
 * 
 * @author Thorsten Guenther
 */
public class FormulasSection extends IpsSection {

    /** Generation which holds the informations to display */
    private IProductCmptGeneration generation;

    /** Pane which serves as parent for all controlls created inside this */
    private Composite rootPane;

    /** Toolkit to handle common ui-operations */
    private UIToolkit toolkit;

    /** List of controls displaying data (needed to enable/disable). */
    private List<TextButtonControl> editControls = new ArrayList<TextButtonControl>();

    /** Controller to handle update of ui and model automatically. */
    private CompositeUIController uiMasterController;

    /** Label which is displayed if no formulas are defined. */
    private Label noFormulasLabel;

    public FormulasSection(IProductCmptGeneration generation, Composite parent, UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
        ArgumentCheck.notNull(generation);

        this.generation = generation;
        initControls();
        setText(Messages.FormulasSection_calculationFormulas);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 2;
        layout.marginWidth = 1;
        client.setLayout(layout);
        rootPane = toolkit.createLabelEditColumnComposite(client);
        rootPane.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout workAreaLayout = (GridLayout)rootPane.getLayout();
        workAreaLayout.marginHeight = 5;
        workAreaLayout.marginWidth = 5;
        this.toolkit = toolkit;

        // following line forces the paint listener to draw a light grey border around
        // the text control. Can only be understood by looking at the FormToolkit.PaintBorder class.
        rootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(rootPane);
        createEditControls();
    }

    @Override
    protected void performRefresh() {
        uiMasterController.updateUI();
    }

    /**
     * Create the ui-elements
     */
    private void createEditControls() {
        uiMasterController = new CompositeUIController();
        IpsObjectUIController ctrl = new IpsObjectUIController(generation.getIpsObject());
        uiMasterController.add(ctrl);

        IFormula[] formulas = generation.getFormulas();
        Arrays.sort(
                formulas,
                new PropertyValueComparator(generation.getProductCmpt().getProductCmptType(), generation
                        .getIpsProject()));

        ITableContentUsage usages[] = generation.getTableContentUsages();

        // handle the "no formulas defined" label
        if (formulas.length + usages.length == 0 && noFormulasLabel == null) {
            noFormulasLabel = toolkit.createLabel(rootPane, Messages.FormulasSection_noFormulasDefined);
        } else if (formulas.length + usages.length > 0 && noFormulasLabel != null) {
            noFormulasLabel.dispose();
            noFormulasLabel = null;
        }

        // create table content usages fields
        for (final ITableContentUsage usage : usages) {
            try {
                // create label as hyperlink to open the corresponding table content in a new new
                // editor
                String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(usage);
                Hyperlink hyperlink = toolkit.createHyperlink(rootPane, localizedCaption);
                hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
                    @Override
                    public void linkActivated(HyperlinkEvent event) {
                        try {
                            ITableContents tc = usage.findTableContents(generation.getIpsProject());
                            if (tc != null) {
                                IpsUIPlugin.getDefault().openEditor(tc.getIpsSrcFile());
                            }
                        } catch (CoreException e) {
                            IpsPlugin.logAndShowErrorDialog(e);
                        }
                    }
                });

                // use description of table structure usage as tooltip
                ITableStructureUsage tsu = findTableStructureUsage(usage.getStructureUsage());
                if (tsu != null) {
                    String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(tsu);
                    hyperlink.setToolTipText(localizedDescription);
                }

                TableContentsUsageRefControl tcuControl = new TableContentsUsageRefControl(generation.getIpsProject(),
                        rootPane, toolkit, tsu);
                ctrl.add(new TextButtonField(tcuControl), usage, ITableContentUsage.PROPERTY_TABLE_CONTENT);
                addFocusControl(tcuControl.getTextControl());
                this.editControls.add(tcuControl);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }

        // create formula edit fields
        for (IFormula formula : formulas) {
            String localizedCaption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(formula);
            Label label = toolkit.createFormLabel(rootPane, localizedCaption);
            try {
                IMethod signature = formula.findFormulaSignature(generation.getIpsProject());
                if (signature != null) {
                    String localizedDescription = IpsPlugin.getMultiLanguageSupport()
                            .getLocalizedDescription(signature);
                    label.setToolTipText(localizedDescription);
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }

            FormulaEditControl evc = new FormulaEditControl(rootPane, toolkit, formula, getShell(), this);
            ctrl.add(new TextField(evc.getTextControl()), formula, IFormula.PROPERTY_EXPRESSION);
            addFocusControl(evc.getTextControl());
            this.editControls.add(evc);

            try {
                FormulaCompletionProcessor completionProcessor = new FormulaCompletionProcessor(formula);
                ContentAssistHandler.createHandlerForText(evc.getTextControl(),
                        CompletionUtil.createContentAssistant(completionProcessor));
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }

        rootPane.layout(true);
        rootPane.redraw();
    }

    private ITableStructureUsage findTableStructureUsage(String rolename) throws CoreException {
        IIpsProject ipsProject = generation.getIpsProject();
        org.faktorips.devtools.core.model.productcmpttype.IProductCmptType type = generation
                .findProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        return type.findTableStructureUsage(rolename, ipsProject);
    }

    @Override
    public void setEnabled(boolean enabled) {
        // to get the disabled look, we have to disable all the input-fields
        // manually :-(
        for (Iterator<TextButtonControl> iter = editControls.iterator(); iter.hasNext();) {
            Control element = iter.next();
            element.setEnabled(enabled);

        }
        rootPane.layout(true);
        rootPane.redraw();
    }

}
