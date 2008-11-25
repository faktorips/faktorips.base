/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ActionBFE extends BFElement implements IActionBFE {

    private String executableMethodName;
    private String target;

    
    public ActionBFE(IIpsObject parent, int id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        this.target = element.getAttribute("target");
        this.executableMethodName = element.getAttribute("executableMethodName");
    }

    public String getDisplayString() {
        if(BFElementType.ACTION_METHODCALL.equals(getType())){
            return target + ':' + executableMethodName;
        }
        if(BFElementType.ACTION_BUSINESSFUNCTIONCALL.equals(getType())){
            return target;
        }
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("executableMethodName", this.executableMethodName);
        element.setAttribute("target", this.target);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IActionBFE.XML_TAG);
    }

    
    public String getExecutableMethodName() {
        return executableMethodName;
    }

    public String getTarget() {
        return target;
    }

    //TODO test missing
    public String getReferencedBfQualifiedName(){
        if(getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)){
            return getTarget();
        }
        return null;
    }
    
    //TODO test missing
    public String getReferencedBfUnqualifedName(){
        if(getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)){
            int index = StringUtils.lastIndexOf(getTarget(), '.');
            if(index == -1){
                return getTarget();
            }
            return getTarget().substring(index, getTarget().length() - 1);
        }
        return null;
    }
    
    public IParameterBFE getParameter(){
        return getBusinessFunction().getParameterBFE(getTarget());
    }
    
    public IBusinessFunction findBusinessFunction() throws CoreException{
        return (IBusinessFunction)getIpsProject().findIpsObject(BusinessFunctionIpsObjectType.getInstance(), getTarget());
    }

    public void setExecutableMethodName(String name) {
        String old = this.executableMethodName;
        this.executableMethodName = name;
        valueChanged(old, name);
    }

    public void setTarget(String target) {
        String old = this.target; 
        this.target = target;
        valueChanged(old, target);
    }

}
