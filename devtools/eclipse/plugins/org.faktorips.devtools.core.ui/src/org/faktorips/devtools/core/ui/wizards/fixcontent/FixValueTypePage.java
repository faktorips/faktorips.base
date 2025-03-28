/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixcontent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.devtools.model.value.ValueTypeMismatch;

/**
 * Shows the mismatches in {@link ValueType}
 *
 * @author frank
 * @since 3.9
 */
public class FixValueTypePage extends WizardPage {

    private TableViewer fTableViewer;
    private TabularContentStrategy<?, ?> contentStrategy;

    public FixValueTypePage(TabularContentStrategy<?, ?> contentStrategy) {
        super(Messages.FixContentWizard_assignColumnMismatchPageTitle);
        setTitle(Messages.FixContentWizard_assignColumnMismatchPageTitle);
        this.contentStrategy = contentStrategy;
        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        Composite workArea = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        workArea.setLayout(layout);
        setControl(workArea);

        fTableViewer = new TableViewer(workArea);
        fTableViewer.setLabelProvider(new ValueTypeErrorsLabelProvider());
        fTableViewer.setContentProvider(new ValueTypeErrorsContentProvider());

        Table tableControl = fTableViewer.getTable();
        tableControl.setFont(JFaceResources.getDialogFont());
        GridData gd = new GridData(GridData.FILL_BOTH);
        tableControl.setLayoutData(gd);
        fTableViewer.setInput(getValueTypeMismatch());
    }

    private List<String> getValueTypeMismatch() {
        Map<String, ValueTypeMismatch> mismatchMap = contentStrategy.checkAllContentAttributeValueTypeMismatch();
        String defaultlanguage = contentStrategy.getIpsProject().getReadOnlyProperties().getDefaultLanguage()
                .getLocale().getLanguage();
        List<String> list = new ArrayList<>();
        for (Entry<String, ValueTypeMismatch> columnEntry : mismatchMap.entrySet()) {
            ValueTypeMismatch valueTypeMismatch = columnEntry.getValue();
            switch (valueTypeMismatch) {
                case INTERNATIONAL_STRING_TO_STRING -> list.add(NLS
                        .bind(Messages.FixContentWizard_messageNoMultilingual, columnEntry.getKey(), defaultlanguage));
                case STRING_TO_INTERNATIONAL_STRING -> list.add(
                        NLS.bind(Messages.FixContentWizard_messageMultilingual, columnEntry.getKey(), defaultlanguage));
                case NO_MISMATCH -> {
                    // no message
                }
            }
        }
        return list;
    }

    public boolean isPageNecessary() {
        return getValueTypeMismatch().size() > 0;
    }

    private class ValueTypeErrorsLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof String) {
                return (String)element;
            }
            return super.getText(element);
        }
    }

    private static class ValueTypeErrorsContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            List<?> messageList = (List<?>)inputElement;
            return messageList.toArray();
        }

        @Override
        public void dispose() {
            // empty default implementation
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // empty default implementation
        }
    }

}
