/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
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
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.internal.ProductCmptDecorator;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * This type selection composite contains of two columns. On the left hand you see a list of types
 * you could select. On the right hand you see the description of the selected element.
 * 
 * @author dirmeier
 */
public class TypeAndTemplateSelectionComposite extends Composite {

    private final UIToolkit toolkit;
    private final ResourceManager resourceManager;
    private Label title;
    private TableViewer typeListViewer;
    private StructuredViewerField<IIpsObject> typeListField;
    private TreeViewer templateTreeViewer;
    private StructuredViewerField<ProductCmptViewItem> templateListField;
    private final NewProductCmptPMO pmo;
    private BindingContext bindingContext;
    private Text typeSearchText;
    private TypeSelectionFilter typeFilter;
    private Text templateSearchText;
    private TypeSelectionFilter templateFilter;
    private StyledText compositeDescription;

    /**
     * Constructs a new type selection composite.
     * 
     * @param parent the parent composite
     * @param toolkit the {@link UIToolkit} to create the internal controls
     * @param pmo a presentation model object to bind the selected type
     */
    public TypeAndTemplateSelectionComposite(Composite parent, UIToolkit toolkit, NewProductCmptPMO pmo) {
        super(parent, SWT.NONE);
        this.toolkit = toolkit;
        this.pmo = pmo;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        bindingContext = new BindingContext();

        setLayoutAndLayoutData();

        createControls();
        addDisposeListener($ -> {
            resourceManager.dispose();
            bindingContext.dispose();
        });
    }

    private void setLayoutAndLayoutData() {
        GridLayout layout = new GridLayout(3, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 10;
        setLayout(layout);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayoutData(gridData);
    }

    private void createControls() {
        title = toolkit.createLabel(this, StringUtils.EMPTY);
        title.setLayoutData(new GridData(SWT.BEGINNING, SWT.END, false, false));

        // empty composites to fill layout next to title label
        toolkit.createGridComposite(this, 0, false, true);
        toolkit.createGridComposite(this, 0, false, true);

        Composite typeBlockComposite = toolkit.createGridComposite(this, 1, false, false);
        Composite searchTypeComposite = toolkit.createLabelEditColumnComposite(typeBlockComposite);
        typeSearchText = toolkit.createText(searchTypeComposite);
        toolkit.createLabel(searchTypeComposite, Messages.TypeSelectionComposite_msgLabel_Filter);
        typeSearchText.setMessage(Messages.TypeSelectionComposite_msg_Filter);

        Composite templateBlockComposite = toolkit.createGridComposite(this, 1, false, false);
        Composite searchTemplateComposite = toolkit.createLabelEditColumnComposite(templateBlockComposite);
        templateSearchText = toolkit.createText(searchTemplateComposite);
        toolkit.createLabel(searchTemplateComposite, Messages.TypeSelectionComposite_msgLabel_Filter);
        templateSearchText.setMessage(Messages.TypeSelectionComposite_msg_Filter);

        typeFilter = new TypeSelectionFilter();
        typeListViewer = new TableViewer(typeBlockComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        setupViewer(typeListViewer, typeFilter);
        typeListViewer.setContentProvider(new ArrayContentProvider());
        ColumnViewerToolTipSupport.enableFor(typeListViewer, ToolTip.NO_RECREATE);
        typeListField = new StructuredViewerField<>(typeListViewer, IIpsObject.class);

        templateFilter = new TypeSelectionFilter();
        templateTreeViewer = new TreeViewer(templateBlockComposite,
                SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        setupViewer(templateTreeViewer, templateFilter);
        templateTreeViewer.setContentProvider(new TreeContentProvider());
        templateTreeViewer.expandAll();
        ColumnViewerToolTipSupport.enableFor(templateTreeViewer, ToolTip.NO_RECREATE);
        templateListField = new StructuredViewerField<>(templateTreeViewer,
                ProductCmptViewItem.class);

        Composite descriptionLabelComposite = toolkit.createGridComposite(this, 1, false, false);
        toolkit.createLabel(descriptionLabelComposite, Messages.TypeSelectionComposite_label_description);
        compositeDescription = toolkit.createStyledMultilineText(descriptionLabelComposite);
        compositeDescription.setEditable(false);

        bindContent();
    }

    private void updateDescription() {
        IIpsObject typeValue = typeListField.getValue();
        if (typeValue == null) {
            compositeDescription.setText(StringUtils.EMPTY);
            compositeDescription.setEnabled(false);
            return;
        }

        StyledTextUtil.clear(compositeDescription);
        compositeDescription.setEnabled(true);

        appendNameAndDescription(typeValue);
        ProductCmptViewItem productCmptViewItem = templateListField == null ? null : templateListField.getValue();
        IProductCmpt templateValue = productCmptViewItem == null ? null : productCmptViewItem.getProductCmpt();
        if (templateValue == null) {
            // no template selected
            return;
        }
        StyledTextUtil.appendNewLine(compositeDescription);
        appendNameAndDescription(templateValue);
    }

    /***
     * Helper method to fill in type and template description
     * 
     * @param ipsObject The target object we want to describe
     */
    public void appendNameAndDescription(IIpsObject ipsObject) {
        String typeName = ipsObject.getName();
        StyledTextUtil.appendLineStyled(compositeDescription, typeName, SWT.BOLD);
        String typeDescriptionString = getDescription(ipsObject);
        if (StringUtils.isEmpty(typeDescriptionString)) {
            StyledTextUtil.appendLineStyled(compositeDescription,
                    Messages.TypeSelectionComposite_label_noDescriptionAvailable, SWT.ITALIC);
        } else {
            StyledTextUtil.appendLinePlain(compositeDescription, typeDescriptionString);
        }
    }

    private String getDescription(IIpsObject object) {
        DescriptionFinder descriptionFinder = new DescriptionFinder(object.getIpsProject());
        descriptionFinder.start(object);
        return descriptionFinder.getLocalizedDescription();
    }

    private void setupViewer(StructuredViewer viewer, TypeSelectionFilter filter) {
        viewer.addFilter(filter);
        viewer.setComparator(new ViewerComparator());
        viewer.setLabelProvider(new LabelProvider());
        GridData listLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listLayoutData.heightHint = 200;
        listLayoutData.widthHint = 300;
        viewer.getControl().setLayoutData(listLayoutData);
    }

    private void bindContent() {
        bindingContext.bindContent(typeSearchText, new FilterPMO(typeFilter, typeListViewer),
                FilterPMO.TEXT_FOR_FILTER);
        bindingContext.bindContent(typeListField, pmo, NewProductCmptPMO.PROPERTY_SELECTED_TYPE);

        bindingContext.bindContent(templateSearchText, new FilterPMO(templateFilter, templateTreeViewer),
                FilterPMO.TEXT_FOR_FILTER);
        bindingContext.bindContent(templateListField, pmo, NewProductCmptPMO.PROPERTY_SELECTED_TEMPLATE);

        typeListViewer.setInput(pmo.getSubtypes());
        templateTreeViewer.setInput(pmo.getTemplates());

        bindingContext
                .add(ViewerRefreshBinding.refresh(typeListViewer, pmo, NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE));
        bindingContext
                .add(ViewerRefreshBinding.refresh(typeListViewer, pmo, NewProductCmptPMO.PROPERTY_SELECTED_TEMPLATE));
        bindingContext.add(ViewerRefreshBinding.refreshAndExpand(templateTreeViewer, pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_BASE_TYPE));
        bindingContext.add(ViewerRefreshBinding.refreshAndExpand(templateTreeViewer, pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_TYPE));

        bindingContext.add(new PropertyChangeBinding<>(compositeDescription, pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_TEMPLATE, ProductCmptViewItem.class) {
            @Override
            protected void propertyChanged(ProductCmptViewItem oldValue, ProductCmptViewItem newValue) {
                updateDescription();
            }
        });

        bindingContext.add(new PropertyChangeBinding<>(compositeDescription, pmo,
                NewProductCmptPMO.PROPERTY_SELECTED_TYPE, IProductCmptType.class) {
            @Override
            protected void propertyChanged(IProductCmptType oldValue, IProductCmptType newValue) {
                updateDescription();
            }
        });

        bindingContext.updateUI();
    }

    public void clearValidationStatus() {
        bindingContext.clearValidationStatus();
    }

    public void setTitle(String titleString) {
        title.setText(titleString);
    }

    public void addDoubleClickListener(IDoubleClickListener listener) {
        typeListViewer.addDoubleClickListener(listener);
    }

    static class LabelProvider extends ColumnLabelProvider {

        private final ProductCmptDecorator productCmptDecorator = (ProductCmptDecorator)IIpsDecorators
                .get(IpsObjectType.PRODUCT_CMPT);

        private final LocalizedLabelProvider delegate = new LocalizedLabelProvider();

        @Override
        public Image getImage(Object element) {
            if (element instanceof IProductCmptType) {
                IProductCmptType productCmptType = (IProductCmptType)element;
                ImageDescriptor descriptorForInstancesOf = productCmptDecorator
                        .getImageDescriptorForInstancesOf(productCmptType);
                return JFaceResources.getResources().createImage(descriptorForInstancesOf);
            } else {
                return delegate.getImage(element);
            }
        }

        @Override
        public String getText(Object element) {
            if (element == NewProductCmptPMO.NULL_TEMPLATE) {
                return Messages.TypeAndTemplateSelectionComposite_noTemplate;
            } else {
                return delegate.getText(element);
            }
        }

        @Override
        public String getToolTipText(Object element) {
            String text = null;
            if (element instanceof IDescribedElement) {
                IDescribedElement type = (IDescribedElement)element;
                text = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(type);
            } else if (element instanceof ProductCmptViewItem) {
                IProductCmpt productCmpt = ((ProductCmptViewItem)element).getProductCmpt();
                if (productCmpt != null) {
                    text = IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(productCmpt);
                }
            } else {
                text = super.getToolTipText(element);
            }
            if (StringUtils.isEmpty(text)) {
                return null;
            } else {
                return text;
            }
        }
    }

    public static class FilterPMO extends PresentationModelObject {

        private static final String TEXT_FOR_FILTER = "textForFilter"; //$NON-NLS-1$
        private final TypeSelectionFilter filter;
        private final Viewer viewer;

        public FilterPMO(TypeSelectionFilter filter, Viewer viewer) {
            this.filter = filter;
            this.viewer = viewer;
        }

        public void setTextForFilter(String searchText) {
            filter.setSearchText(searchText);
            viewer.refresh();
        }

        public String getTextForFilter() {
            return filter.getSearchText();
        }
    }

    private static class TreeContentProvider extends ArrayContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ProductCmptViewItem) {
                ProductCmptViewItem viewItem = (ProductCmptViewItem)parentElement;
                return viewItem.getChildren().toArray();
            } else {
                return new Object[0];
            }
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof ProductCmptViewItem) {
                ProductCmptViewItem viewItem = (ProductCmptViewItem)element;
                return viewItem.getParent();
            } else {
                return null;
            }
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

    }

    // /**
    // * Convenience wrapper for {@link StyledText}. Allows to easily add formatted text blocks to a
    // * widget. CAVEAT: This class has no authority over changes to the referenced widget from
    // other
    // * sources. TODO: Create wrapper class for {@link StyledText}
    // *
    // * @author NKammerer
    // */
    // static class StyledTextStringUtil {
    // private StyledText textWidget;
    //
    // /**
    // * @param targetWidget Is created outside and only passed as reference!
    // */
    // public StyledTextStringUtil(StyledText targetWidget) {
    // textWidget = targetWidget;
    // }
    //
    // /***
    // * Helper method to fill in type and template description
    // *
    // * @param ipsObject The target object we want to describe
    // */
    // private void appendNameAndDescription(IIpsObject ipsObject) {
    // String typeName = ipsObject.getName();
    // appendLineStyled(typeName, SWT.BOLD);
    // String typeDescriptionString = getDescription(ipsObject);
    // if (StringUtils.isEmpty(typeDescriptionString)) {
    // appendLineStyled(Messages.TypeSelectionComposite_label_noDescriptionAvailable, SWT.ITALIC);
    // } else {
    // appendLinePlain(typeDescriptionString);
    // }
    // }
    //
    // /**
    // * Set the widget's content to empty.
    // */
    // public void clear() {
    // textWidget.setText(StringUtils.EMPTY);
    // }
    //
    // /**
    // * Append text without formatting
    // *
    // * @param text The String to append.
    // */
    // public void appendPlain(String text) {
    // textWidget.append(text);
    // }
    //
    // /**
    // * Appends newline characters, if it's not the beginning of the content.
    // *
    // * @param text Is added after the line break!
    // */
    // public void appendLinePlain(String text) {
    // if (textWidget.getCharCount() > 0) {
    // textWidget.append(System.lineSeparator());
    // }
    // appendPlain(text);
    // }
    //
    // public void appendNewLine() {
    // appendLinePlain(StringUtils.EMPTY);
    // }
    //
    // /**
    // * Appends newline characters, if it's not the beginning of the content.
    // *
    // * @param text Is added with formatting after the line break!
    // * @param fontStyle SWT formatting style to configure {@link StyleRange}
    // */
    // public void appendLineStyled(String text, int fontStyle) {
    // if (textWidget.getCharCount() > 0) {
    // textWidget.append(System.lineSeparator());
    // }
    // appendStyled(text, fontStyle);
    // }
    //
    // /**
    // * Append text with custom formatting
    // *
    // * @param text A string to append
    // * @param fontStyle SWT formatting style to configure {@link StyleRange}
    // */
    // public void appendStyled(String text, int fontStyle) {
    // // get current index, set text, adjust style object and return style
    // int currentStart = textWidget.getCharCount();
    // textWidget.append(text);
    //
    // StyleRange style = new StyleRange(currentStart, text.length(), null, null, fontStyle);
    // textWidget.setStyleRange(style);
    // }
    // }

}
