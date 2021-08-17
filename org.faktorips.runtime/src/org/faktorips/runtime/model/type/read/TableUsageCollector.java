/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type.read;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsTableUsage;
import org.faktorips.runtime.model.annotation.IpsTableUsages;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.TableUsage;
import org.faktorips.runtime.model.type.Type;

public class TableUsageCollector extends TypePartCollector<TableUsage, TableUsageCollector.TableUsageDescriptor> {

    public TableUsageCollector() {
        super(Arrays.<AnnotationProcessor<?, TableUsageDescriptor>> asList(new TableUsageProcessor()));
    }

    @Override
    protected TableUsageDescriptor createDescriptor() {
        return new TableUsageDescriptor();
    }

    @Override
    protected String[] getNames(AnnotatedDeclaration annotatedDeclaration) {
        if (annotatedDeclaration.is(IpsTableUsages.class)) {
            return annotatedDeclaration.get(IpsTableUsages.class).value();
        } else {
            return NO_NAMES;
        }
    }

    static class TableUsageProcessor extends AnnotationProcessor<IpsTableUsage, TableUsageDescriptor> {

        public TableUsageProcessor() {
            super(IpsTableUsage.class);
        }

        @Override
        public String getName(IpsTableUsage annotation) {
            return annotation.name();
        }

        @Override
        public void process(TableUsageDescriptor descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setAnnotatedElement((Method)annotatedElement);
            descriptor.setChangingOverTime(
                    IProductComponentGeneration.class.isAssignableFrom(annotatedDeclaration.getImplementationClass()));
        }

    }

    protected static class TableUsageDescriptor extends PartDescriptor<TableUsage> {

        private boolean changingOverTime;

        private Method annotatedElement;

        public boolean isValid() {
            return getAnnotatedElement() != null;
        }

        public Method getAnnotatedElement() {
            return annotatedElement;
        }

        public void setAnnotatedElement(Method annotatedElement) {
            this.annotatedElement = annotatedElement;
        }

        public boolean isChangingOverTime() {
            return changingOverTime;
        }

        public void setChangingOverTime(boolean changingOverTime) {
            this.changingOverTime = changingOverTime;
        }

        @Override
        public TableUsage create(ModelElement parentElement) {
            Type type = (Type)parentElement;
            if (isValid()) {
                return createValid(type);
            } else {
                throw new IllegalArgumentException(type.getDeclarationClass() + " lists \"" + getName()
                        + "\" as one of it's @IpsTableUsages but no matching @IpsTableUsage could be found.");
            }
        }

        private TableUsage createValid(Type type) {
            return new TableUsage(type, getAnnotatedElement());
        }
    }

}
