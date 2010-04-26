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

package org.faktorips.runtime;

import java.lang.reflect.Method;

/**
 * 
 * @author Jan Ortmann
 */
public class DefaultUnresolvedReference implements IUnresolvedReference {

    private Object sourceObj;
    private Object sourceObjId;
    private Method establishMethod;
    private Object targetId;
    private Class<?> targetClass;

    public DefaultUnresolvedReference(Object sourceObj, Object sourceObjId, Method establishMethod,
            Class<?> targetClass, Object targetId) {

        super();
        this.sourceObj = sourceObj;
        this.sourceObjId = sourceObjId;
        this.establishMethod = establishMethod;
        this.targetClass = targetClass;
        this.targetId = targetId;
    }

    public DefaultUnresolvedReference(Object sourceObj, Object sourceObjId, String establishMethodName,
            Class<?> targetClass, Object targetId) {

        super();
        this.sourceObj = sourceObj;
        this.sourceObjId = sourceObjId;
        this.targetClass = targetClass;
        this.targetId = targetId;
        try {
            establishMethod = sourceObj.getClass().getMethod(establishMethodName, new Class[] { targetClass });
        } catch (Exception e) {
            throw new RuntimeException("Can't get method to establish association, sourceClass="
                    + sourceObj.getClass().getName() + "method=" + establishMethodName + ", targetClass="
                    + targetClass.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void resolve(IObjectReferenceStore store) throws Exception {
        Object target = store.getObject(targetClass, targetId);
        establishMethod.invoke(sourceObj, new Object[] { target });
    }

    /**
     * @return Returns the establishMethod.
     */
    public Method getEstablishMethod() {
        return establishMethod;
    }

    /**
     * @return Returns the sourceObj.
     */
    public Object getSourceObj() {
        return sourceObj;
    }

    /**
     * @return Returns the sourceObjId.
     */
    public Object getSourceObjId() {
        return sourceObjId;
    }

    /**
     * @return Returns the targetClass.
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * @return Returns the targetId.
     */
    public Object getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return "Unresolved reference: " + "From " + sourceObj + "(" + sourceObjId + ")" + "To: " + targetClass + "("
                + targetId + ")" + "Method to estabalish: " + establishMethod;
    }
}
