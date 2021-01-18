/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.util.PersistenceUtil;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

public class PersistentAttributeSection extends SimpleIpsPartsSection {

    private static final Map<Integer, AttrPropertyAndLabel> COLUMN_PROPERTIES = new HashMap<Integer, AttrPropertyAndLabel>();

    private ResourceManager resourceManager;

    static {
        COLUMN_PROPERTIES.put(0, new AttrPropertyAndLabel(IIpsElement.PROPERTY_NAME,
                Messages.PersistentAttributeSection_labelAttributeName));
        COLUMN_PROPERTIES.put(1, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME,
                Messages.PersistentAttributeSection_labelColumnName));
        COLUMN_PROPERTIES.put(2, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_UNIQE,
                Messages.PersistentAttributeSection_labelUnique));
        COLUMN_PROPERTIES.put(3, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NULLABLE,
                Messages.PersistentAttributeSection_labelNullable));
        COLUMN_PROPERTIES.put(4, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SIZE,
                Messages.PersistentAttributeSection_labelSize));
        COLUMN_PROPERTIES.put(5, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_PRECISION,
                Messages.PersistentAttributeSection_labelPrecision));
        COLUMN_PROPERTIES.put(6, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SCALE,
                Messages.PersistentAttributeSection_labelScale));
        COLUMN_PROPERTIES.put(7, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_SQL_COLUMN_DEFINITION,
                Messages.PersistentAttributeSection_labelColumnDefinition));
        COLUMN_PROPERTIES.put(8, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_CONVERTER,
                Messages.PersistentAttributeSection_labelConverter));
        COLUMN_PROPERTIES.put(9, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_INDEX_NAME,
                Messages.PersistentSection_labelIndexName));
    }

    public PersistentAttributeSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(ipsObject, parent, null, ExpandableComposite.TITLE_BAR,
                Messages.PersistentAttributeSection_titleAttributes, toolkit);
        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (resourceManager != null) {
                    resourceManager.dispose();
                }
            }
        });
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new PersistenceAttributesComposite(getIpsObject(), parent, toolkit);
    }

    private ResourceManager getResourceManager() {
        if (resourceManager == null) {
            resourceManager = new LocalResourceManager(JFaceResources.getResources());
        }
        return resourceManager;
    }

    private static class AttrPropertyAndLabel {
        private String property;
        private String label;

        public AttrPropertyAndLabel(String property, String label) {
            this.property = property;
            this.label = label;
        }
    }

    class PersistenceAttributesComposite extends PersistenceComposite {

        public PersistenceAttributesComposite(IIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
            super(ipsObject, parent, toolkit);
        }

        @Override
        public String[] getColumnHeaders() {
            String[] result = new String[COLUMN_PROPERTIES.size()];
            for (int i = 0; i < COLUMN_PROPERTIES.size(); i++) {
                result[i] = COLUMN_PROPERTIES.get(i).label;
            }
            return result;
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new PersistentAttributeContentProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            AttributeEditDialog attributeEditDialog = new AttributeEditDialog((IPolicyCmptTypeAttribute)part, shell);

            return attributeEditDialog;
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return ((IPolicyCmptType)getIpsObject()).newPolicyCmptTypeAttribute();
        }

        @Override
        public ILabelProvider createLabelProvider() {
            return new PersistentAttributeLabelProvider();
        }

        private class PersistentAttributeContentProvider implements IStructuredContentProvider {
            @Override
            public Object[] getElements(Object inputElement) {
                List<IPolicyCmptTypeAttribute> pcAttributes = ((IPolicyCmptType)getIpsObject())
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

            @Override
            public void dispose() {
                // nothing to do
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing to do
            }
        }

        private class PersistentAttributeLabelProvider extends LabelProvider implements ITableLabelProvider {
            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                IPersistentAttributeInfo persistenceAttributeInfo = ((IPolicyCmptTypeAttribute)element)
                        .getPersistenceAttributeInfo();
                MessageList msgList;
                try {
                    // we must validate both the PersistenceTypeInfo and and the PersistenceTypeInfo
                    // because the PersistenceTypeInfo validates duplicate column names
                    msgList = persistenceAttributeInfo.getPolicyComponentTypeAttribute().getPolicyCmptType()
                            .getPersistenceTypeInfo().validate(persistenceAttributeInfo.getIpsProject());
                    msgList.add(persistenceAttributeInfo.validate(persistenceAttributeInfo.getIpsProject()));
                    String property = COLUMN_PROPERTIES.get(columnIndex).property;
                    if (property == null) {
                        return null;
                    }
                    Image image = getOverlayImageFor(msgList.getMessagesFor(persistenceAttributeInfo, property));
                    if (image != null) {
                        return image;
                    }
                    // if there is no message for the given property
                    // check for messages for the object only
                    if (columnIndex == 0) {
                        image = getOverlayImageFor(msgList.getMessagesFor(persistenceAttributeInfo));
                        if (image != null) {
                            return image;
                        }
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
                return null;
            }

            private Image getOverlayImageFor(MessageList messages) {
                if (messages.size() > 0) {
                    ImageDescriptor descriptor = IpsProblemOverlayIcon.getOverlay(messages.getSeverity());
                    if (descriptor != null) {
                        return (Image)getResourceManager().get(descriptor);
                    }
                }
                return null;
            }

            @Override
            public String getColumnText(Object element, int columnIndex) {
                IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)element;
                ValueDatatype valueDatatype = null;
                valueDatatype = attribute.findDatatype(attribute.getIpsProject());
                IPersistentAttributeInfo attributeInfo = attribute.getPersistenceAttributeInfo();

                String property = COLUMN_PROPERTIES.get(columnIndex).property;
                if (IIpsElement.PROPERTY_NAME.equals(property)) {
                    return attribute.getName();
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME.equals(property)) {
                    return attributeInfo.getTableColumnName();
                } else if (IPersistentAttributeInfo.PROPERTY_SQL_COLUMN_DEFINITION.equals(property)) {
                    return StringUtil.unqualifiedName(attributeInfo.getSqlColumnDefinition());
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_CONVERTER.equals(property)) {
                    return StringUtil.unqualifiedName(attributeInfo.getConverterQualifiedClassName());
                } else if (IPersistentAttributeInfo.PROPERTY_INDEX_NAME.equals(property)) {
                    return StringUtil.unqualifiedName(attributeInfo.getIndexName());
                } else if (!isUseSqlDefinition(attributeInfo)) {
                    return getColumnTextNoSqlDefinition(valueDatatype, attributeInfo, property);
                }
                return StringUtils.EMPTY;
            }

            private String getColumnTextNoSqlDefinition(ValueDatatype valueDatatype,
                    IPersistentAttributeInfo attributeInfo,
                    String property) {
                if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_UNIQE.equals(property)) {
                    return String.valueOf(attributeInfo.getTableColumnUnique());
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NULLABLE.equals(property)) {
                    return String.valueOf(attributeInfo.getTableColumnNullable());
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SIZE.equals(property)) {
                    if (PersistenceUtil.isSupportingLenght(valueDatatype)) {
                        return String.valueOf(attributeInfo.getTableColumnSize());
                    } else {
                        return StringUtils.EMPTY;
                    }
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_PRECISION.equals(property)) {
                    if (PersistenceUtil.isSupportingDecimalPlaces(valueDatatype)) {
                        return String.valueOf(attributeInfo.getTableColumnPrecision());
                    } else {
                        return StringUtils.EMPTY;
                    }
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SCALE.equals(property)) {
                    if (PersistenceUtil.isSupportingDecimalPlaces(valueDatatype)) {
                        return String.valueOf(attributeInfo.getTableColumnScale());
                    } else {
                        return StringUtils.EMPTY;
                    }
                } else {
                    return StringUtils.EMPTY;
                }
            }

            private boolean isUseSqlDefinition(IPersistentAttributeInfo attributeInfo) {
                return StringUtils.isNotEmpty(attributeInfo.getSqlColumnDefinition());
            }
        }
    }

}
