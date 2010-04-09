/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.util.StringUtil;

public class PersistentAttributeSection extends SimpleIpsPartsSection {

    public PersistentAttributeSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(ipsObject, parent, "Attributes", toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new PersistenceAttributesComposite(getIpsObject(), parent, toolkit);
    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    class PersistenceAttributesComposite extends PersistenceComposite {

        @Override
        public String[] getColumnHeaders() {
            return new String[] { "Attribute Name", "Column Name", "Unique", "Nullable", "Size", "Precision", "Scale",
                    "Column Definition", "Converter" };
        }

        public PersistenceAttributesComposite(IIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
            super(ipsObject, parent, toolkit);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new PersistentAttributeContentProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            AttributeEditDialog attributeEditDialog = new AttributeEditDialog((IPolicyCmptTypeAttribute)part, shell);

            return attributeEditDialog;
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            return ((IPolicyCmptType)getIpsObject()).newPolicyCmptTypeAttribute();
        }

        private class PersistentAttributeContentProvider implements IStructuredContentProvider {
            public Object[] getElements(Object inputElement) {
                IPolicyCmptTypeAttribute[] pcAttributes = ((IPolicyCmptType)getIpsObject())
                        .getPolicyCmptTypeAttributes();
                List<IPolicyCmptTypeAttribute> persistableAttributes = new ArrayList<IPolicyCmptTypeAttribute>();
                for (IPolicyCmptTypeAttribute pcAttribute : pcAttributes) {
                    if (pcAttribute.getPersistenceAttributeInfo().isTransient()) {
                        continue;
                    }
                    AttributeType attributeType = pcAttribute.getAttributeType();
                    if (attributeType == AttributeType.CHANGEABLE
                            || attributeType == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL) {
                        persistableAttributes.add(pcAttribute);
                    }
                }
                return persistableAttributes.toArray();
            }

            public void dispose() {
                // nothing to do
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing to do
            }
        }

        private class PersistentAttributeLabelProvider extends LabelProvider implements ITableLabelProvider {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)element;
                ValueDatatype valueDatatype = null;
                try {
                    valueDatatype = attribute.findDatatype(attribute.getIpsProject());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
                IPersistentAttributeInfo attributeInfo = attribute.getPersistenceAttributeInfo();

                String result = "";
                switch (columnIndex) {
                    case 0:
                        result = attribute.getName();
                        break;
                    case 1:
                        String rawTableColumnName = attributeInfo.getTableColumnName();
                        result = attributeInfo.getIpsProject().getTableNamingStrategy()
                                .getTableName(rawTableColumnName);
                        break;
                    case 2:
                        if (!isUseSqlDefinition(attributeInfo)) {
                            result = String.valueOf(attributeInfo.getTableColumnUnique());
                        }
                        break;
                    case 3:
                        if (!isUseSqlDefinition(attributeInfo)) {
                            result = String.valueOf(attributeInfo.getTableColumnNullable());
                        }
                        break;
                    case 4:
                        if (!isUseSqlDefinition(attributeInfo) && PersistenceUtil.isSupportingLenght(valueDatatype)) {
                            result = String.valueOf(attributeInfo.getTableColumnSize());
                        }
                        break;
                    case 5:
                        if (!isUseSqlDefinition(attributeInfo)
                                && PersistenceUtil.isSupportingDecimalPlaces(valueDatatype)) {
                            result = String.valueOf(attributeInfo.getTableColumnPrecision());
                        }
                        break;
                    case 6:
                        if (!isUseSqlDefinition(attributeInfo)
                                && PersistenceUtil.isSupportingDecimalPlaces(valueDatatype)) {
                            result = String.valueOf(attributeInfo.getTableColumnScale());
                        }
                        break;
                    case 7:
                        result = StringUtil.unqualifiedName(attributeInfo.getSqlColumnDefinition());
                        break;
                    case 8:
                        result = StringUtil.unqualifiedName(attributeInfo.getConverterQualifiedClassName());
                        break;
                    default:
                        result = "";
                }
                return (result == null ? "" : result);
            }

            private boolean isUseSqlDefinition(IPersistentAttributeInfo attributeInfo) {
                return StringUtils.isNotEmpty(attributeInfo.getSqlColumnDefinition());
            }
        }

        @Override
        public ILabelProvider createLabelProvider() {
            return new PersistentAttributeLabelProvider();
        }
    }
}
