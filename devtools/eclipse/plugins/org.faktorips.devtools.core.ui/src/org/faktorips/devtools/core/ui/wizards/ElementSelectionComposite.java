/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import java.util.Collection;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.StyledTextUtil;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.binding.PropertyChangeBinding;
import org.faktorips.devtools.core.ui.binding.ViewerRefreshBinding;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;
import org.faktorips.devtools.core.ui.util.DescriptionFinder;
import org.faktorips.devtools.core.ui.wizards.productdefinition.Messages;
import org.faktorips.devtools.core.ui.wizards.productdefinition.TypeSelectionFilter;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * This composite contains two columns. On the left hand you see a list of {@link IDescribedElement
 * described} {@link IIpsElement elements} you could select. On the right hand you see the
 * description of the selected element.
 */
public class ElementSelectionComposite<E extends IIpsElement & IDescribedElement> extends Composite {

    private final UIToolkit toolkit;
    private final ResourceManager resourceManager;
    private TableViewer listViewer;
    private StructuredViewerField<E> listViewerField;
    private StyledText description;
    private final PresentationModelObject pmo;
    private final String property;
    private BindingContext bindingContext;
    private TypeSelectionFilter filter;
    private Text searchText;
    private Collection<?> inputList;
    private IBaseLabelProvider labelProvider;
    private Class<E> elementClass;

    /**
     * Constructs a new type selection composite.
     * 
     * @param parent the parent composite
     * @param toolkit the {@link UIToolkit} to create the internal controls
     * @param pmo a presentation model object to bind the selected type
     * @param property the property of the presentation model object
     * @param inputList The input list for the type selection. This list instance should never
     *            change, the content may change of course
     */
    // CSOFF: ParameterNumberCheck
    public ElementSelectionComposite(Composite parent, UIToolkit toolkit, BindingContext bindingContext,
            PresentationModelObject pmo, String property, Collection<? extends E> inputList,
            IBaseLabelProvider labelProvider, Class<E> elementClass) {
        super(parent, SWT.NONE);
        this.toolkit = toolkit;
        this.pmo = pmo;
        this.property = property;
        this.inputList = inputList;
        this.labelProvider = labelProvider;
        this.elementClass = elementClass;
        this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
        this.bindingContext = bindingContext;

        setLayoutAndLayoutData();

        createControls();
        addDisposeListener($ -> resourceManager.dispose());
    }
    // CSON: ParameterNumberCheck

    private void setLayoutAndLayoutData() {
        GridLayout layout = new GridLayout(2, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 10;
        setLayout(layout);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayoutData(gridData);
    }

    protected void createControls() {
        Composite searchComposite = toolkit.createLabelEditColumnComposite(this);
        searchText = toolkit.createText(searchComposite);
        toolkit.createLabel(searchComposite, Messages.TypeSelectionComposite_msgLabel_Filter);
        searchText.setMessage(Messages.TypeSelectionComposite_msg_Filter);

        toolkit.createLabel(this, Messages.TypeSelectionComposite_label_description);

        listViewer = new TableViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        filter = new TypeSelectionFilter();
        listViewer.addFilter(filter);
        listViewer.setComparator(new ViewerComparator());
        listViewer.setContentProvider(new ArrayContentProvider());
        listViewer.setLabelProvider(labelProvider);
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        listViewer.getControl().setLayoutData(listLayoutData);
        listViewerField = new StructuredViewerField<>(listViewer, elementClass);

        description = toolkit.createStyledMultilineText(this);
        description.setEditable(false);
        bindContent();
    }

    private void bindContent() {
        bindingContext.bindContent(searchText, new FilterPMO(), FilterPMO.TEXT_FOR_FILTER);
        bindingContext.bindContent(listViewerField, pmo, property);

        listViewer.setInput(inputList);

        bindingContext.add(new PropertyChangeBinding<>(description, pmo, property, elementClass) {

            @Override
            protected void propertyChanged(E oldValue, E newValue) {
                updateDescription(newValue);
            }

        });

        bindingContext.add(ViewerRefreshBinding.refresh(listViewer, pmo));
    }

    public void clearValidationStatus() {
        bindingContext.clearValidationStatus();
    }

    public void addDoubleClickListener(IDoubleClickListener listener) {
        listViewer.addDoubleClickListener(listener);
    }

    private void updateDescription(E element) {
        if (element == null) {
            description.setText(IpsStringUtils.EMPTY);
        } else {
            String deprecationDescription = getDeprecationDescription(element);
            String descriptionString = getDescription(element);
            StyledTextUtil.clear(description);
            if (IpsStringUtils.isNotEmpty(deprecationDescription)) {
                StyledTextUtil.appendStyled(description, deprecationDescription,
                        SWT.BOLD);
                description.append(System.lineSeparator());
            }
            if (IpsStringUtils.isEmpty(descriptionString) && IpsStringUtils.isEmpty(deprecationDescription)) {
                StyledTextUtil.appendStyled(description,
                        Messages.TypeSelectionComposite_label_noDescriptionAvailable,
                        SWT.ITALIC);
                description.setEnabled(false);
            } else {
                description.append(descriptionString);
                description.setEnabled(true);
                description.setEditable(false);
            }

        }
    }

    private String getDeprecationDescription(E element) {
        if (element instanceof IVersionControlledElement
                && ((IVersionControlledElement)element).isDeprecated()) {
            return ((IVersionControlledElement)element).getDeprecation().toString();
        }
        return null;
    }

    private String getDescription(E element) {
        DescriptionFinder descriptionFinder = new DescriptionFinder(element.getIpsProject());
        descriptionFinder.start(element);
        return descriptionFinder.getLocalizedDescription();
    }

    protected UIToolkit getToolkit() {
        return toolkit;
    }

    public class FilterPMO extends PresentationModelObject {
        private static final String TEXT_FOR_FILTER = "textForFilter"; //$NON-NLS-1$

        public void setTextForFilter(String searchText) {
            filter.setSearchText(searchText);
            listViewer.refresh();
        }

        public String getTextForFilter() {
            return filter.getSearchText();
        }
    }
}
