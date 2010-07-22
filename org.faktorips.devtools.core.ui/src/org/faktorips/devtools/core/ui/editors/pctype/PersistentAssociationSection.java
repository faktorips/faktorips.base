/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.message.MessageList;

/**
 * Section to display the persistence properties of the associations specific to an
 * {@link IPolicyCmptType}.
 * <p>
 * The editable properties are Join Table Name and Fetch Type amongst others.
 * 
 * @author Roman Grutza
 */
public class PersistentAssociationSection extends SimpleIpsPartsSection {

    private ResourceManager resourceManager;

    private static final Map<Integer, AttrPropertyAndLabel> columnProperties = new HashMap<Integer, AttrPropertyAndLabel>();

    static {
        columnProperties.put(0, new AttrPropertyAndLabel(IAssociation.PROPERTY_TARGET, "Association Target")); //$NON-NLS-1$
        columnProperties.put(1, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME,
                "Join Table Name")); //$NON-NLS-1$
        columnProperties.put(2, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME,
                "Source Column Name")); //$NON-NLS-1$
        columnProperties.put(3, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME,
                "Target Column Name")); //$NON-NLS-1$
        columnProperties.put(4, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME,
                "Join Column Name")); //$NON-NLS-1$
        columnProperties.put(5, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_FETCH_TYPE, "Fetch Type")); //$NON-NLS-1$
        columnProperties.put(6, new AttrPropertyAndLabel(IPersistentAssociationInfo.PROPERTY_ORPHAN_REMOVAL,
                "Orphan Removal")); //$NON-NLS-1$
    }

    private static class AttrPropertyAndLabel {
        private String property;
        private String label;

        public AttrPropertyAndLabel(String property, String label) {
            this.property = property;
            this.label = label;
        }
    }

    public PersistentAssociationSection(IPolicyCmptType ipsObject, Composite parent, UIToolkit toolkit) {
        super(ipsObject, parent, "Associations", toolkit); //$NON-NLS-1$
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new PersistenceAssociationsComposite(getIpsObject(), parent, toolkit);
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
        bindingContext.updateUI();
    }

    private ResourceManager getResourceManager() {
        if (resourceManager == null) {
            resourceManager = new LocalResourceManager(JFaceResources.getResources());
        }
        return resourceManager;
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
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new AssociationEditDialog((IPolicyCmptTypeAssociation)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            return ((IPolicyCmptType)getIpsObject()).newPolicyCmptTypeAssociation();
        }

        @Override
        public String[] getColumnHeaders() {
            String[] result = new String[columnProperties.size()];
            for (int i = 0; i < columnProperties.size(); i++) {
                result[i] = columnProperties.get(i).label;
            }
            return result;
        }

        private class PersistentAssociationContentProvider implements IStructuredContentProvider {
            @Override
            public Object[] getElements(Object inputElement) {
                List<IPolicyCmptTypeAssociation> content = new ArrayList<IPolicyCmptTypeAssociation>();
                IPolicyCmptTypeAssociation[] policyCmptTypeAssociations = ((IPolicyCmptType)getIpsObject())
                        .getPolicyCmptTypeAssociations();
                for (int i = 0; i < policyCmptTypeAssociations.length; i++) {
                    if (!policyCmptTypeAssociations[i].getPersistenceAssociatonInfo().isTransient()) {
                        content.add(policyCmptTypeAssociations[i]);
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
                    msgList = persistenceAssociationInfo.validate(persistenceAssociationInfo.getIpsProject());
                    String property = columnProperties.get(columnIndex).property;
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
                IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)element;
                IPersistentAssociationInfo jpaAssociationInfo = association.getPersistenceAssociatonInfo();

                String property = columnProperties.get(columnIndex).property;
                String result = ""; //$NON-NLS-1$

                boolean joinTableReq = false;
                boolean foreignKeyColumnReq = false;
                try {
                    joinTableReq = jpaAssociationInfo.isJoinTableRequired();
                    foreignKeyColumnReq = !jpaAssociationInfo.isForeignKeyColumnDefinedOnTargetSide();
                } catch (Exception e) {
                    IpsPlugin.log(e);
                }

                if (IAssociation.PROPERTY_TARGET.equals(property)) {
                    result = association.getTarget();
                } else if (IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME.equals(property)) {
                    if (joinTableReq) {
                        result = jpaAssociationInfo.getJoinTableName();
                    }
                } else if (IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME.equals(property)) {
                    if (joinTableReq) {
                        result = jpaAssociationInfo.getSourceColumnName();
                    }
                } else if (IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME.equals(property)) {
                    if (joinTableReq) {
                        result = jpaAssociationInfo.getTargetColumnName();
                    }
                } else if (IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME.equals(property)) {
                    if (!joinTableReq && foreignKeyColumnReq) {
                        result = jpaAssociationInfo.getJoinColumnName();
                    }
                } else if (IPersistentAssociationInfo.PROPERTY_FETCH_TYPE.equals(property)) {
                    result = jpaAssociationInfo.getFetchType().toString();
                } else if (IPersistentAssociationInfo.PROPERTY_ORPHAN_REMOVAL.equals(property)) {
                    result = Boolean.valueOf(jpaAssociationInfo.isOrphanRemoval()).toString();
                }
                return (result == null ? "" : result); //$NON-NLS-1$
            }
        }

        @Override
        public ILabelProvider createLabelProvider() {
            return new PersistentAssociationLabelProvider();
        }

    }

}
