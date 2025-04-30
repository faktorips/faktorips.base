/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.inputformat.GregorianCalendarFormat;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyContentProvider;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;

/**
 * Wizard page that previews the changes made to product component generation IDs and valid-from
 * dates.
 */
public class UpdateValidFromPreviewPage extends WizardPage {

    public static final String PAGE_ID = "updateValidFrom.preview"; //$NON-NLS-1$

    private static final String PAGE_TITLE = Messages.UpdateValidFromSourcePage_pageTitle;

    private TreeViewer treeViewer;
    private Label validFromLabel;

    public UpdateValidFromPreviewPage(String pageName) {
        super(pageName);
        setTitle(PAGE_TITLE);
        setDescription(Messages.UpdateValidFromPreviewPage_description);
        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite labelComp = toolkit.createLabelEditColumnComposite(root);
        toolkit.createLabel(labelComp, Messages.UpdateValidFromPreviewPage_validFrom);
        validFromLabel = toolkit.createLabel(labelComp, "");
        updateValidFromLabel();

        treeViewer = new TreeViewer(root, SWT.BORDER | SWT.FULL_SELECTION);
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(false);
        treeViewer.setUseHashlookup(true);

        treeViewer.setContentProvider(new DeepCopyContentProvider(true, false));

        treeViewer.setLabelProvider(new UpdateValidFromLabelProvider(getPresentationModel()) {
            @Override
            public void update(ViewerCell cell) {
                Object element = cell.getElement();

                String newId = getGenerationID(element);
                String oldId = getOldName(element);
                boolean changed = StringUtils.isNotBlank(newId) && !newId.equals(oldId);

                String mainText = changed ? newId : oldId;
                StyledString styled = new StyledString(mainText);

                styled.append(getValidFromSuffix(element));

                cell.setText(styled.getString());
                cell.setStyleRanges(styled.getStyleRanges());
                cell.setImage(getObjectImage(element, true));
            }
        });

        treeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updateValidFromLabel();
            treeViewer.setInput(getStructure());
            treeViewer.expandAll();
        }
    }

    /**
     * Updates the "Valid From" label at the top of the preview UI.
     */
    private void updateValidFromLabel() {
        GregorianCalendar validFrom = getPresentationModel().getNewValidFrom();
        if (validFrom != null) {
            validFromLabel.setText(GregorianCalendarFormat.newInstance().format(validFrom));
        }
    }

    /**
     * Gets the current presentation model from the wizard.
     */
    private UpdateValidfromPresentationModel getPresentationModel() {
        return ((IpsUpdateValidfromWizard)getWizard()).getPresentationModel();
    }

    /**
     * Returns the root tree structure of the product components.
     */
    private IProductCmptTreeStructure getStructure() {
        return getPresentationModel().getStructure();
    }
}
