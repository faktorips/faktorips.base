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

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.ui.Messages;
import org.faktorips.devtools.core.ui.OverrideImageDescriptor;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * This is the implementation of the DefaultLabelProvider until v2.4
 * <p>
 * In version 2.5 we changed the api for getting images and default labels of model objects to avoid
 * images in the core plugin. The new API provides the interface {@link IPresentationObject}.
 * <p>
 * This DefaultPresentationObject is used until every IpsElement has its own presentation object
 * registered by extension.
 * 
 * @author Cornelius Dirmeier
 * @author Jan Ortmann
 * 
 * @deprecated better you implement a specific {@link IPresentationObject}
 */
@Deprecated
public class DefaultWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    @Override
    public ImageDescriptor getImageDescriptor(IIpsElement element) {
        if (element instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)element;
            return getInternalImageDescriptor(ipsSrcFile);
        } else if (element instanceof IMethod) {
            try {
                return ImageDescriptor.createFromImage(getMethodImage((IMethod)element));
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return ImageDescriptor.createFromImage(element.getImage());
    }

    public Class<? extends IIpsElement> getIpsElementClass() {
        return IIpsElement.class;
    }

    @Override
    public String getLabel(IIpsElement element) {
        if (element == null) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else if (element instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)element;
            return getInternalLabel(ipsSrcFile);
        } else if (element instanceof IEnumAttribute) {
            IEnumAttribute enumAttribute = (IEnumAttribute)element;
            return getEnumAttributeLabel(enumAttribute);
        }
        if (element instanceof IAttribute) {
            IAttribute attribute = (IAttribute)element;
            return getAttributeLabel(attribute);
        }
        if (element instanceof IAssociation) {
            IAssociation association = (IAssociation)element;
            return getAssociationLabel(association);
        }
        if (element instanceof IMethod) {
            return getMethodLabel((IMethod)element);
        }
        if (element instanceof IIpsPackageFragment) {
            if (element.getName().equals("")) { //$NON-NLS-1$
                return Messages.DefaultLabelProvider_labelDefaultPackage;
            }
        }
        return element.getName();
    }

    private static Image abstractMethodImage = null;
    private static Image overloadedMethodImage = null;
    private static Image abstractAndOverloadedMethodImage = null;

    /**
     * {@inheritDoc}
     */

    private String getEnumAttributeLabel(IEnumAttribute enumAttribute) {
        StringBuffer sb = new StringBuffer();
        sb.append(enumAttribute.getName());
        sb.append(" : "); //$NON-NLS-1$
        try {
            Datatype datatype = enumAttribute.findDatatype(enumAttribute.getIpsProject());
            sb.append((datatype == null) ? "" : datatype.getName()); //$NON-NLS-1$
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    private String getAttributeLabel(IAttribute a) {
        StringBuffer sb = new StringBuffer();
        if (a.isDerived()) {
            sb.append("/"); //$NON-NLS-1$
        }

        sb.append(a.getName());
        sb.append(" : "); //$NON-NLS-1$
        sb.append(a.getDatatype());

        return sb.toString();
    }

    private String getAssociationLabel(IAssociation association) {
        if (association.is1ToMany()) {
            return association.getTargetRolePlural();
        }

        return association.getTargetRoleSingular();
    }

    private String getMethodLabel(IMethod method) {
        StringBuffer buffer = new StringBuffer(method.getName());
        buffer.append('(');
        IParameter[] params = method.getParameters();

        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(QNameUtil.getUnqualifiedName(params[i].getDatatype()));
        }

        buffer.append(") : "); //$NON-NLS-1$
        buffer.append(QNameUtil.getUnqualifiedName(method.getDatatype()));

        return buffer.toString();
    }

    private Image getMethodImage(IMethod method) throws CoreException {
        boolean overloaded = method.findOverriddenMethod(method.getIpsProject()) != null;
        if (method.isAbstract()) {
            if (overloaded) {
                return getAbstractOverloadedMethodImage(method);
            } else {
                return getAbstractMethodImage(method);
            }
        }

        if (overloaded) {
            return getOverloadedMethodImage(method);
        }

        return method.getImage();
    }

    private static Image getAbstractMethodImage(IMethod method) {
        if (abstractMethodImage == null) {
            abstractMethodImage = new AbstractPropertyImageDescriptor(method.getImage()).createImage();
        }

        return abstractMethodImage;
    }

    private static Image getOverloadedMethodImage(IMethod method) {
        if (overloadedMethodImage == null) {
            overloadedMethodImage = new OverrideImageDescriptor(method.getImage()).createImage();
        }

        return overloadedMethodImage;
    }

    private static Image getAbstractOverloadedMethodImage(IMethod method) {
        if (abstractAndOverloadedMethodImage == null) {
            abstractAndOverloadedMethodImage = new OverrideImageDescriptor(getAbstractMethodImage(method))
                    .createImage();
        }

        return abstractAndOverloadedMethodImage;
    }

    private static class AbstractPropertyImageDescriptor extends CompositeImageDescriptor {

        private final static Point DEFAULT_SIZE = new Point(16, 16);

        private Image baseImage;
        private Point size = DEFAULT_SIZE;

        public AbstractPropertyImageDescriptor(Image image) {
            ArgumentCheck.notNull(image);
            baseImage = image;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void drawCompositeImage(int width, int height) {
            drawImage(baseImage.getImageData(), 0, 0);
            drawImage(IpsPlugin.getDefault().getImage("AbstractIndicator.gif").getImageData(), 8, 0); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Point getSize() {
            return size;
        }
    }

    public ImageDescriptor getInternalImageDescriptor(IIpsSrcFile ipsSrcFile) {
        if (ipsSrcFile.exists()) {
            return ImageDescriptor.createFromImage(ipsSrcFile.getIpsObjectType().getEnabledImage());
        } else {
            // @see IpsObject#getImage()
            return ImageDescriptor.createFromImage(IpsObjectType.IPS_SOURCE_FILE.getEnabledImage());
        }
    }

    public String getInternalLabel(IIpsSrcFile ipsSrcFile) {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
    }

}
