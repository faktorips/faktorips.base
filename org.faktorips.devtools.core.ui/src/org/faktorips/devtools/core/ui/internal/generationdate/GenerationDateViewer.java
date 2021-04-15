/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.generationdate;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.Messages;

/**
 * A simple combo box viewer to view a list of generation dates
 * 
 * @author dirmeier
 */
public class GenerationDateViewer extends ComboViewer {

    private final Button prevButton;
    private final Button nextButton;

    public GenerationDateViewer(Composite rootComposite) {
        super(new Composite(rootComposite, SWT.NONE), SWT.READ_ONLY);
        Composite parent = getCombo().getParent();
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        String generationConceptName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();

        getCombo().setToolTipText(
                NLS.bind(Messages.ProductStructureExplorer_selectAdjustmentToolTip, generationConceptName));
        getCombo().setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof GenerationDate) {
                    return ((GenerationDate)element).getText();
                }
                return super.getText(element);
            }
        });

        prevButton = new Button(parent, SWT.NONE);
        nextButton = new Button(parent, SWT.NONE);
        initButtons(generationConceptName);
    }

    private void initButtons(String generationConceptName) {
        getPrevButton().setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowLeft_small.gif", true)); //$NON-NLS-1$
        getPrevButton().setToolTipText(
                NLS.bind(Messages.ProductStructureExplorer_prevAdjustmentToolTip, generationConceptName));
        getPrevButton().setEnabled(false);

        getNextButton().setImage(IpsUIPlugin.getImageHandling().getSharedImage("ArrowRight_small.gif", true)); //$NON-NLS-1$
        getNextButton().setToolTipText(
                NLS.bind(Messages.ProductStructureExplorer_nextAdjustmentToolTip, generationConceptName));
        getNextButton().setEnabled(false);

        getPrevButton().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = getCombo().getSelectionIndex();
                setSelection(selectedIndex + 1);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });

        getNextButton().addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectedIndex = getCombo().getSelectionIndex();
                setSelection(selectedIndex - 1);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Nothing to do
            }
        });
        addSelectionChangedListener($ -> {
            GenerationDate adjDate = getSelectedDate();
            if (adjDate != null) {
                updateButtons();
            }
        });
    }

    public GenerationDate getSelectedDate() {
        if (getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)getSelection();
            if (structuredSelection.getFirstElement() instanceof GenerationDate) {
                return (GenerationDate)structuredSelection.getFirstElement();
            }
        }
        return null;
    }

    public void setSelection(int index) {
        Object o = getElementAt(index);
        setSelection(o);
    }

    public void setSelection(Object o) {
        setSelection(new StructuredSelection(o), true);
    }

    public Button getPrevButton() {
        return prevButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public void updateButtons() {
        int index = getCombo().getSelectionIndex();
        getNextButton().setEnabled(index > 0);
        getPrevButton().setEnabled(index < getCombo().getItems().length - 1);
    }

}
