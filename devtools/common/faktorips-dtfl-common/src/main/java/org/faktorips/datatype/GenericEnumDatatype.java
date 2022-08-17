/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.faktorips.runtime.MessageList;
import org.faktorips.util.MethodAccess;

/**
 * Generic enum datatype. See the superclass for more Details.
 * 
 * @author Jan Ortmann
 */
public abstract class GenericEnumDatatype extends GenericValueDatatype implements EnumDatatype {

    public static final String MSGCODE_PREFIX_GET_NAME_METHOD = MSGCODE_PREFIX + "getNameMethod"; //$NON-NLS-1$

    private String getNameMethodName = "getName"; //$NON-NLS-1$

    private boolean isSupportingNames = false;
    private boolean cacheData = false;
    private String[] cachedValueIds = null;
    private String[] cachedValueNames = null;

    public GenericEnumDatatype() {
        super();
    }

    /**
     * Returns {@code true} if the values' id and names are cached, {@code false} otherwise.
     */
    public boolean isCacheData() {
        return cacheData;
    }

    /**
     * Sets to {@code true} if the values' ids and names should be cached, otherwise {@code false}.
     * Setting {@code false} also clears the cache.
     */
    public void setCacheData(boolean cacheData) {
        this.cacheData = cacheData;
        if (!cacheData) {
            clearCache();
        }
    }

    private MethodAccess getNameMethod() {
        return MethodAccess.of(getAdaptedClass(), getGetNameMethodName());
    }

    @Override
    public MessageList checkReadyToUse() {
        MessageList ml = super.checkReadyToUse();
        getNameMethod()
                .check(ml, MSGCODE_PREFIX_GET_NAME_METHOD)
                .exists()
                .isNotStatic()
                .returnTypeIsCompatible(String.class);
        return ml;
    }

    /**
     * Returns the name of the getName(String) method.
     */
    public String getGetNameMethodName() {
        return getNameMethodName;
    }

    /**
     * Sets the name of the getName(String) method.
     */
    public void setGetNameMethodName(String getNameMethodName) {
        this.getNameMethodName = getNameMethodName;
        clearCache();
    }

    @Override
    public boolean isSupportingNames() {
        return isSupportingNames;
    }

    public void setIsSupportingNames(boolean isSupportingNames) {
        this.isSupportingNames = isSupportingNames;
        clearCache();
    }

    @Override
    public String[] getAllValueIds(boolean includeNull) {
        try {
            String[] ids = getAllValueIdsFromCache();
            // caching disabled
            if (ids == null) {
                ids = getAllValueIdsFromClass();
            }
            int indexOfNull = getIndeoxOfNullOrNullObject(ids);
            ArrayList<String> result = new ArrayList<>();
            result.addAll(Arrays.asList(ids));
            if (!includeNull && indexOfNull >= 0) {
                result.remove(indexOfNull);
            } else if (includeNull && indexOfNull == -1) {
                if (hasNullObject()) {
                    result.add(getNullObjectId());
                } else {
                    result.add(null);
                }
            }
            return result.toArray(new String[result.size()]);
            // CSOFF: Illegal Catch
        } catch (Exception e) {
            // CSON: Illegal Catch
            throw new RuntimeException("Error invoking method " + getAllValuesMethodName(), e); //$NON-NLS-1$
        }
    }

    /**
     * Returns the value id's from the underlying enum class' via it's get-all-values method.
     */
    private String[] getAllValueIdsFromClass() {
        Object result = getAllValuesMethod()
                .invokeStatic("to get all values"); //$NON-NLS-1$
        Object[] values;
        if (result instanceof Collection) {
            values = ((Collection<?>)result).toArray(Object[]::new);
        } else {
            values = (Object[])result;
        }
        String[] ids = new String[values.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = valueToString(values[i]);
        }
        return ids;
    }

    private int getIndeoxOfNullOrNullObject(String[] valueIds) {
        for (int i = 0; i < valueIds.length; i++) {
            if ((valueIds[i] == null) || (hasNullObject() && Objects.equals(valueIds[i], getNullObjectId()))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getValueName(String id) {
        if (!isSupportingNames) {
            throw new UnsupportedOperationException(
                    "This enumeration type does not support a getName(String) method, enumeration type class: " //$NON-NLS-1$
                            + getAdaptedClass());
        }
        String[] ids = getAllValueIdsFromCache();
        if (ids != null) {
            for (int i = 0; i < ids.length; i++) {
                if (Objects.equals(id, ids[i])) {
                    String[] names = getAllValueNamesFromCache();
                    return names[i];
                }
            }
        }
        return getValueNameFromClass(id);
    }

    private String getValueNameFromClass(String id) {
        Object value = getValue(id);
        return getNameFromValue(value);
    }

    protected String getNameFromValue(Object value) {
        if (value == null) {
            return null;
        }
        return getNameMethod().invoke("to get the name for a value", value); //$NON-NLS-1$
    }

    @Override
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        String[] ids = getAllValueIdsFromCache();
        if (ids == null) {
            return super.isParsable(value);
        }
        for (String id : ids) {
            if (value.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all value IDs from the cache. If caching is enabled, but the cache is empty, the
     * cache is populated with the IDs retrieved from {@link #getAllValueIdsFromClass()}. If caching
     * is disabled, the method returns {@code null}.
     * <p>
     * Package private to allow testing.
     */
    String[] getAllValueIdsFromCache() {
        if (!cacheData) {
            return null;
        }
        if (cachedValueIds == null) {
            initCache();
        }
        return cachedValueIds;
    }

    /**
     * Returns all value names from the cache. If caching is enabled, but the cache is empty, the
     * cache is populated with the names for all values retrieved from
     * {@link #getAllValueIdsFromClass()}. If caching is disabled, the method returns {@code null}.
     * <p>
     * Package private to allow testing.
     */
    String[] getAllValueNamesFromCache() {
        if (!isSupportingNames) {
            throw new RuntimeException("Datatype " + this + " does not support names."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!cacheData) {
            return null;
        }
        if (cachedValueNames == null) {
            initCache();
        }
        return cachedValueNames;
    }

    /**
     * Initializes the cache with the data from the underlying class.
     */
    public void initCache() {
        String[] ids;
        try {
            ids = getAllValueIdsFromClass();
            // CSOFF: Illegal Catch
        } catch (Exception e) {
            // CSON: Illegal Catch
            throw new RuntimeException("Error initializing cache for datatype " + this, e); //$NON-NLS-1$
        }
        cachedValueIds = new String[ids.length];
        if (isSupportingNames) {
            cachedValueNames = new String[ids.length];
        }
        for (int i = 0; i < ids.length; i++) {
            cachedValueIds[i] = ids[i];
            if (isSupportingNames) {
                cachedValueNames[i] = getValueNameFromClass(ids[i]);
            }
        }
    }

    @Override
    protected void clearCache() {
        super.clearCache();
        cachedValueIds = null;
        cachedValueNames = null;
    }

}
