package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.fl.FlFunction;


/**
 *
 */
public class DefaultLabelProvider extends LabelProvider {
    
    public DefaultLabelProvider() {
        super();
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        try {
            if (element instanceof IMethod) {
                return getMethodImage((IMethod)element);
            }
            if ((element instanceof IIpsElement)) {
                return ((IIpsElement)element).getImage();
            }
            if (element instanceof Datatype) {
                return IpsPlugin.getDefault().getImage("Datatype.gif");
            }
            if (element instanceof Parameter) {
                return IpsPlugin.getDefault().getImage("Parameter.gif");
            }
            if (element instanceof FlFunction) {
                return IpsPlugin.getDefault().getImage("Function.gif");
            }
            return super.getImage(element); 
        } catch (Exception e) {
            IpsPlugin.log(e);
            return super.getImage(element);
        }
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element==null) {
            return "null";
        }
        if (!(element instanceof IIpsElement)) {
            return element.toString();
        }
        IIpsElement pdElement = (IIpsElement)element;
        if (element instanceof IIpsPackageFragment) {
            if (pdElement.getName().equals("")) {
                return "(default package)";
            }
        }
        if (element instanceof IMethod) {
            return getMethodLabel((IMethod)element);
        }
        return pdElement.getName();
    }
    
    private String getMethodLabel(IMethod method) {
        StringBuffer buffer = new StringBuffer(method.getName());
        buffer.append('(');
        Parameter[] params = method.getParameters();
        for (int i=0; i<params.length; i++) {
            if (i>0) {
                buffer.append(", ");
            }
            buffer.append(params[i].getDatatype());
        }
        buffer.append(')');
        return buffer.toString();
    }
    
    private Image getMethodImage(IMethod method) throws CoreException {
        IPolicyCmptType type = method.getPolicyCmptType(); 
        IPolicyCmptType[] supertypes = type.getSupertypeHierarchy().getAllSupertypes(type);
        for (int i=0; i<supertypes.length; i++) {
            if (supertypes[i].hasSameMethod(method)) {
                return new OverrideImageDescriptor(method.getImage()).createImage();
            }
        }
        return method.getImage();
    }
    
}
