/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.api.impl.transaction.expression;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.api.impl.transaction.expression.EvaluateExpressionsInstanceLevel;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.expression.Expression;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;


/**
 * @author Romain Bioteau
 *
 */
public class EvaluateExpressionsInstanceLevelExt extends EvaluateExpressionsInstanceLevel {

    private final BusinessDataRepository bdrService;

    public EvaluateExpressionsInstanceLevelExt(final Map<Expression, Map<String, Serializable>> expressions, final long containerId,
            final String containerType,
            final long processDefinitionId, final ExpressionResolverService expressionService, final BusinessDataRepository bdrService) {
        super(expressions, containerId, containerType, processDefinitionId, expressionService);
        this.bdrService = bdrService;
    }

    @Override
    protected Map<String, Serializable> getPartialContext(final Expression exp) {
        final Map<String, Serializable> partialContext = super.getPartialContext(exp);
        if (partialContext.isEmpty()) {
            return partialContext;
        }

        final Map<String, Serializable> result = new HashMap<String, Serializable>();
        for (final Entry<String, Serializable> entry : partialContext.entrySet()) {
            result.put(entry.getKey(), reconnectEntities(entry.getValue()));
        }

        return result;
    }

    protected Serializable reconnectEntities(final Serializable value) {
        if (isACollectionOfEntities(value)) {
            final Collection<?> collection = (Collection<?>) value;
            try {
                final Collection<Entity> newCollection = collection.getClass().newInstance();
                for (final Object item : collection) {
                    newCollection.add(bdrService.merge((Entity) item));
                }
                return (Serializable) newCollection;
            } catch (final InstantiationException e) {
                throw new IllegalStateException(e);
            } catch (final IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        } else if (isAnEntity(value)) {
            return bdrService.merge((Entity) value);
        } else {
            return value;
        }
    }

    private boolean isAnEntity(final Serializable value) {
        return value instanceof Entity;
    }

    protected boolean isACollectionOfEntities(final Serializable value) {
        return value instanceof Collection<?> && !((Collection<?>) value).isEmpty() && ((Collection<?>) value).iterator().next() instanceof Entity;
    }

}
