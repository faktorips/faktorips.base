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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
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
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

public class PersistentAttributeSection extends SimpleIpsPartsSection {

    private ResourceManager resourceManager;

    private static final Map<Integer, AttrPropertyAndLabel> columnProperties = new HashMap<Integer, AttrPropertyAndLabel>();

    static {
        columnProperties.put(0, new AttrPropertyAndLabel(IIpsElement.PROPERTY_NAME, "Attribute Name")); //$NON-NLS-1$
        columnProperties.put(1, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME,
                "Column Name")); //$NON-NLS-1$
        columnProperties.put(2,
                new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_UNIQE, "Unique")); //$NON-NLS-1$
        columnProperties.put(3, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NULLABLE,
                "Nullable")); //$NON-NLS-1$
        columnProperties.put(4, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SIZE, "Size")); //$NON-NLS-1$
        columnProperties.put(5, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_PRECISION,
                "Precision")); //$NON-NLS-1$
        columnProperties
                .put(6, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SCALE, "Scale")); //$NON-NLS-1$
        columnProperties.put(7, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_SQL_COLUMN_DEFINITION,
                "Column Definition")); //$NON-NLS-1$
        columnProperties.put(8, new AttrPropertyAndLabel(IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_CONVERTER,
                "Converter")); //$NON-NLS-1$
    }

    private static class AttrPropertyAndLabel {
        private String property;
        private String label;

        public AttrPropertyAndLabel(String property, String label) {
            this.property = property;
            this.label = label;
        }
    }

    public PersistentAttributeSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(ipsObject, parent, "Attributes", toolkit); //$NON-NLS-1$
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

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    class PersistenceAttributesComposite extends PersistenceComposite {

        @Override
        public String[] getColumnHeaders() {
            String[] result = new String[columnProperties.size()];
            for (int i = 0; i < columnProperties.size(); i++) {
                result[i] = columnProperties.get(i).label;
            }
            return result;
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
            @Override
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
                    msgList = persistenceAttributeInfo.validate(persistenceAttributeInfo.getIpsProject());
                    String property = columnProperties.get(columnIndex).property;
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
                if (messages.getNoOfMessages() > 0) {
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
                try {
                    valueDatatype = attribute.findDatatype(attribute.getIpsProject());
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
                IPersistentAttributeInfo attributeInfo = attribute.getPersistenceAttributeInfo();

                String property = columnProperties.get(columnIndex).property;
                String result = ""; //$NON-NLS-1$
                if (IIpsElement.PROPERTY_NAME.equals(property)) {
                    result = attribute.getName();
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME.equals(property)) {
                    result = attributeInfo.getTableColumnName();
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_UNIQE.equals(property)) {
                    if (!isUseSqlDefinition(attributeInfo)) {
                        result = String.valueOf(attributeInfo.getTableColumnUnique());
                    }
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NULLABLE.equals(property)) {
                    if (!isUseSqlDefinition(attributeInfo)) {
                        result = String.valueOf(attributeInfo.getTableColumnNullable());
                    }
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SIZE.equals(property)) {
                    if (!isUseSqlDefinition(attributeInfo) && PersistenceUtil.isSupportingLenght(valueDatatype)) {
                        result = String.valueOf(attributeInfo.getTableColumnSize());
                    }
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_PRECISION.equals(property)) {
                    if (!isUseSqlDefinition(attributeInfo) && PersistenceUtil.isSupportingDecimalPlaces(valueDatatype)) {
                        result = String.valueOf(attributeInfo.getTableColumnPrecision());
                    }
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SCALE.equals(property)) {
                    if (!isUseSqlDefinition(attributeInfo) && PersistenceUtil.isSupportingDecimalPlaces(valueDatatype)) {
                        result = String.valueOf(attributeInfo.getTableColumnScale());
                    }
                } else if (IPersistentAttributeInfo.PROPERTY_SQL_COLUMN_DEFINITION.equals(property)) {
                    result = StringUtil.unqualifiedName(attributeInfo.getSqlColumnDefinition());
                } else if (IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_CONVERTER.equals(property)) {
                    result = StringUtil.unqualifiedName(attributeInfo.getConverterQualifiedClassName());
                }
                return (result == null ? "" : result); //$NON-NLS-1$
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

    @Override
    public void dispose() {
        getResourceManager().dispose();
        super.dispose();
    }
}
