/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
            establishMethod = sourceObj.getClass().getMethod(establishMethodName, targetClass);
        } catch (NoSuchMethodException ne) {
            throwNewRuntimeException(sourceObj, establishMethodName, targetClass);
        } catch (SecurityException e) {
            throwNewRuntimeException(sourceObj, establishMethodName, targetClass);
        }
    }

    private void throwNewRuntimeException(Object sourceObj, String establishMethodName, Class<?> targetClass) {
        throw new RuntimeException("Can't get method to establish association, sourceClass="
                + sourceObj.getClass().getName() + "method=" + establishMethodName + ", targetClass="
                + targetClass.getName());
    }

    @Override
    public void resolve(IObjectReferenceStore store) throws Exception {
        Object target = store.getObject(targetClass, targetId);
        establishMethod.invoke(sourceObj, target);
    }

    public Method getEstablishMethod() {
        return establishMethod;
    }

    public Object getSourceObj() {
        return sourceObj;
    }

    public Object getSourceObjId() {
        return sourceObjId;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Object getTargetId() {
        return targetId;
    }

    @Override
    public String toString() {
        return "Unresolved reference: " + "From " + sourceObj + "(" + sourceObjId + ")" + "To: " + targetClass + "("
                + targetId + ")" + "Method to estabalish: " + establishMethod;
    }

}
