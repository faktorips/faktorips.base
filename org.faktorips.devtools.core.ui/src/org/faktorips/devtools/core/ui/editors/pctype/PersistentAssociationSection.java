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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.runtime.MessageList;

/**
 * Section to display the persistence properties of the associations specific to an
 * {@link IPolicyCmptType}.
 * <p>
 * The editable properties are Join Table Name and Fetch Type amongst others.
 * 
 * @author Roman Grutza
 */
public class PersistentAssociationSection extends SimpleIpsPartsSection {

    private static final Map<Integer, AttrPropertyAndLabel> COLUMN_PROPERTIES = new HashMap<>();

    static {
        COLUMN_PROPERTIES.put(0, new AttrPropertyAndLabel(IAssociation.PROPERTY_TARGET,
                Messages.PersistentAssociationSection_labelAssociationTarget));
        COLUMN_PROPERTIES.put(1, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME,
                Messages.PersistentAssociationSection_labelJoinTableName));
        COLUMN_PROPERTIES.put(2, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME,
                Messages.PersistentAssociationSection_labelSourceColumnName));
        COLUMN_PROPERTIES.put(3, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME,
                Messages.PersistentAssociationSection_labelTargetColumnName));
        COLUMN_PROPERTIES.put(4, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME,
                Messages.PersistentAssociationSection_labelJoinColumnName));
        COLUMN_PROPERTIES.put(5, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NULLABLE,
                Messages.PersistentAssociationSection_labelJoinColumnNullable));
        COLUMN_PROPERTIES.put(6, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_FETCH_TYPE,
                Messages.PersistentAssociationSection_labelFetchType));
        COLUMN_PROPERTIES.put(7, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_ORPHAN_REMOVAL,
                Messages.PersistentAssociationSection_labelOrphanRemoval));
        COLUMN_PROPERTIES.put(8, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_INDEX_NAME,
                Messages.PersistentSection_labelIndexName));
    }

    private ResourceManager resourceManager;

    public PersistentAssociationSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(ipsObject, parent, null, ExpandableComposite.TITLE_BAR,
                Messages.PersistentAssociationSection_titleAssociations, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new PersistenceAssociationsComposite(getIpsObject(), parent, toolkit);
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

    public class PersistenceAssociationsComposite extends PersistenceComposite {

        public PersistenceAssociationsComposite(IIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
            super(ipsObject, parent, toolkit);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new PersistentAssociationContentProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AssociationEditDialog((IPolicyCmptTypeAssociation)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() {
            return ((IPolicyCmptType)getIpsObject()).newPolicyCmptTypeAssociation();
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
        public ILabelProvider createLabelProvider() {
            return new PersistentAssociationLabelProvider();
        }

        private class PersistentAssociationContentProvider implements IStructuredContentProvider {
            @Override
            public Object[] getElements(Object inputElement) {
                List<IPolicyCmptTypeAssociation> content = new ArrayList<>();
                List<IPolicyCmptTypeAssociation> policyCmptTypeAssociations = ((IPolicyCmptType)getIpsObject())
                        .getPolicyCmptTypeAssociations();
                for (IPolicyCmptTypeAssociation policyCmptTypeAssociation : policyCmptTypeAssociations) {
                    if (!policyCmptTypeAssociation.getPersistenceAssociatonInfo().isTransient()) {
                        content.add(policyCmptTypeAssociation);
                    }
                }
                return content.toArray();
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

        private class PersistentAssociationLabelProvider extends LabelProvider implements ITableLabelProvider {

            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                IPersistentAssociationInfo persistenceAssociationInfo = ((IPolicyCmptTypeAssociation)element)
                        .getPersistenceAssociatonInfo();
                MessageList msgList;
                try {
                    // we must validate both the PersistenceTypeInfo and and the PersistenceTypeInfo
                    // because the PersistenceTypeInfo validates duplicate column names
                    msgList = persistenceAssociationInfo.getPolicyComponentTypeAssociation().getPolicyCmptType()
                            .getPersistenceTypeInfo().validate(persistenceAssociationInfo.getIpsProject());
                    msgList.add(persistenceAssociationInfo.validate(persistenceAssociationInfo.getIpsProject()));
                    String property = COLUMN_PROPERTIES.get(columnIndex).property;
                    if (property == null) {
                        return null;
                    }
                    Image image = getOverlayImageFor(msgList.getMessagesFor(persistenceAssociationInfo, property));
                    if (image != null) {
                        return image;
                    }
                    // if there is no message for the given property
                    // check for messages for the object only
                    if (columnIndex == 0) {
                        image = getOverlayImageFor(msgList.getMessagesFor(persistenceAssociationInfo));
                        if (image != null) {
                            return image;
                        }
                    }
                } catch (CoreRuntimeException e) {
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
                IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)element;
                IPersistentAssociationInfo jpaAssociationInfo = association.getPersistenceAssociatonInfo();

                String property = COLUMN_PROPERTIES.get(columnIndex).property;

                boolean joinTableReq = false;
                boolean foreignKeyColumnReq = false;
                try {
                    joinTableReq = jpaAssociationInfo.isJoinTableRequired();
                    foreignKeyColumnReq = !jpaAssociationInfo.isForeignKeyColumnDefinedOnTargetSide();
                } catch (CoreRuntimeException e) {
                    IpsPlugin.log(e);
                }

                String result = getColumnText(association, jpaAssociationInfo, property, joinTableReq,
                        foreignKeyColumnReq);
                return (result == null ? "" : result); //$NON-NLS-1$
            }

            private String getColumnText(IPolicyCmptTypeAssociation association,
                    IPersistentAssociationInfo jpaAssociationInfo,
                    String property,
                    boolean joinTableReq,
                    boolean foreignKeyColumnReq) {
                if (IAssociation.PROPERTY_TARGET.equals(property)) {
                    return association.getTarget();
                } else if (IPersistentAssociationInfo.PROPERTY_FETCH_TYPE.equals(property)) {
                    return jpaAssociationInfo.getFetchType().toString();
                } else if (IPersistentAssociationInfo.PROPERTY_ORPHAN_REMOVAL.equals(property)) {
                    return Boolean.valueOf(jpaAssociationInfo.isOrphanRemoval()).toString();
                } else if (IPersistentAssociationInfo.PROPERTY_INDEX_NAME.equals(property)) {
                    return jpaAssociationInfo.getIndexName();
                } else if (joinTableReq) {
                    return getColumnTextJoinTableReq(jpaAssociationInfo, property);
                } else if (!joinTableReq && foreignKeyColumnReq) {
                    return getColumnTextForeignKeyColReq(jpaAssociationInfo, property);
                } else {
                    return StringUtils.EMPTY;
                }
            }

            private String getColumnTextForeignKeyColReq(IPersistentAssociationInfo jpaAssociationInfo,
                    String property) {
                if (IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME.equals(property)) {
                    return jpaAssociationInfo.getJoinColumnName();
                } else if (IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NULLABLE.equals(property)) {
                    return Boolean.valueOf(jpaAssociationInfo.isJoinColumnNullable()).toString();
                } else {
                    return StringUtils.EMPTY;
                }
            }

            private String getColumnTextJoinTableReq(IPersistentAssociationInfo jpaAssociationInfo, String property) {
                if (IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME.equals(property)) {
                    return jpaAssociationInfo.getJoinTableName();
                } else if (IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME.equals(property)) {
                    return jpaAssociationInfo.getSourceColumnName();
                } else if (IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME.equals(property)) {
                    return jpaAssociationInfo.getTargetColumnName();
                } else {
                    return StringUtils.EMPTY;
                }
            }
        }

    }

}
