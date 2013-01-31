/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import java.util.Locale;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.binding.IpsObjectPartPmo;
import org.faktorips.devtools.core.ui.controls.MultilingualValueAttributeControl;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.MultilingualTextCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;

/**
 * {@link DatatypeEditingSupport} that creates its cell editors configured for multiple languages.
 */
public class MultilingualDatatypeEditingSupport extends DatatypeEditingSupport {

    private final UIToolkit toolkit;
    private final TableViewer multiValuetableViewer;
    private final IAttributeValue attributeValue;
    private final IIpsProject ipsProject;
    private final BindingContext bindingContext;

    public MultilingualDatatypeEditingSupport(UIToolkit toolkit, TableViewer tableViewer, IIpsProject ipsProject,
            ValueDatatype datatype, IElementModifier elementModifier, IAttributeValue attributeValue) {
        super(toolkit, tableViewer, ipsProject, datatype, elementModifier);
        this.toolkit = toolkit;
        this.multiValuetableViewer = tableViewer;
        this.attributeValue = attributeValue;
        this.ipsProject = ipsProject;
        this.bindingContext = new BindingContext();
        tableViewer.getTable().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent arg0) {
                dispose();
            }
        });
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(Object element) {
        SingleValueHolder singleValueHolder = null;
        if (element instanceof SingleValueHolder) {
            singleValueHolder = (SingleValueHolder)element;
        } else if (element instanceof SingleValueViewItem) {
            singleValueHolder = ((SingleValueViewItem)element).getSingleValueHolder();
        }
        MultilingualValueAttributeControl control = new MultilingualValueAttributeControl(
                multiValuetableViewer.getTable(), toolkit, attributeValue, ipsProject, singleValueHolder);
        MultilingualTextCellEditor cellEditor = new MultilingualTextCellEditor(control, multiValuetableViewer);
        MultilingualValueHolderPmo valueHolderPMO = new MultilingualValueHolderPmo(attributeValue, singleValueHolder,
                IpsPlugin.getMultiLanguageSupport().getLocalizationLocale());

        bindingContext.bindContent(cellEditor.getTextField(), valueHolderPMO,
                MultilingualValueHolderPmo.PROPERTY_LOCALIZED_STRING_VALUE);
        bindingContext.add(new ControlPropertyBinding(multiValuetableViewer.getControl(), valueHolderPMO,
                MultilingualValueHolderPmo.PROPERTY_LOCALIZED_STRING_VALUE, ILocalizedString.class) {
            @Override
            public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                if (!multiValuetableViewer.isCellEditorActive()) {
                    // Only refresh the value if no cell editor is currently active;
                    // otherwise the editing will be interrupted by the refresh, because
                    // the focus of the cell editor is lost.
                    multiValuetableViewer.refresh();
                }
            }
        });

        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, multiValuetableViewer, 1);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);

        return cellEditor;
    }

    public void dispose() {
        bindingContext.dispose();
    }

    public static class MultilingualValueHolderPmo extends IpsObjectPartPmo {
        public static final String PROPERTY_LOCALIZED_STRING_VALUE = "localizedStringValue"; //$NON-NLS-1$

        private final Locale locale;
        private final SingleValueHolder singleValueHolder;

        public MultilingualValueHolderPmo(IAttributeValue attributeValue, SingleValueHolder singleValueHolder,
                Locale locale) {
            super(attributeValue);
            this.locale = locale;
            this.singleValueHolder = singleValueHolder;
        }

        public ILocalizedString getLocalizedStringValue() {
            IValue<?> value = singleValueHolder.getValue();
            return value == null || value.getContent() == null ? null : ((IInternationalString)value.getContent())
                    .get(locale);
        }

        @Override
        public IAttributeValue getIpsObjectPartContainer() {
            return (IAttributeValue)super.getIpsObjectPartContainer();
        }

        public void setLocalizedStringValue(ILocalizedString newValue) {
            IValue<?> value = singleValueHolder.getValue();
            if (value != null) {
                IInternationalString currentString = (IInternationalString)value.getContent();
                currentString.add(newValue);
            }
            notifyListeners();
        }
    }
}
