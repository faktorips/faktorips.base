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
import org.faktorips.runtime.model.annotation.IpsFormula;
import org.faktorips.runtime.model.annotation.IpsFormulas;
import org.faktorips.runtime.model.type.Formula;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.Type;

public class FormulaCollector extends TypePartCollector<Formula, FormulaCollector.FormulaDescriptor> {

    public FormulaCollector() {
        super(Arrays.<AnnotationProcessor<?, FormulaDescriptor>> asList(new FormulaProcessor()));
    }

    @Override
    protected FormulaDescriptor createDescriptor() {
        return new FormulaDescriptor();
    }

    @Override
    protected String[] getNames(AnnotatedDeclaration annotatedDeclaration) {
        if (annotatedDeclaration.is(IpsFormulas.class)) {
            return annotatedDeclaration.get(IpsFormulas.class).value();
        } else {
            return NO_NAMES;
        }
    }

    static class FormulaProcessor extends AnnotationProcessor<IpsFormula, FormulaDescriptor> {

        public FormulaProcessor() {
            super(IpsFormula.class);
        }

        @Override
        public String getName(IpsFormula annotation) {
            return annotation.name();
        }

        @Override
        public void process(FormulaDescriptor descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setAnnotatedElement((Method)annotatedElement);
            descriptor.setChangingOverTime(
                    IProductComponentGeneration.class.isAssignableFrom(annotatedDeclaration.getImplementationClass()));
        }

    }

    protected static class FormulaDescriptor extends PartDescriptor<Formula> {

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
        public Formula create(ModelElement parentElement) {
            Type type = (Type)parentElement;
            if (isValid()) {
                return createValid(type);
            } else {
                throw new IllegalArgumentException(type.getDeclarationClass() + " lists \"" + getName()
                        + "\" as one of it's @IpsFormulas but no matching @IpsFormula could be found.");
            }
        }

        private Formula createValid(Type type) {
            return new Formula(type, getAnnotatedElement(), changingOverTime);
        }
    }

}
