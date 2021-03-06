/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.services.persondir.support.merger;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

/**
 * Merger which implements accumulation of Map entries such that entries once
 * established are individually immutable.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class NoncollidingAttributeAdder extends BaseAdditiveAttributeMerger {

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.BaseAdditiveAttributeMerger#mergePersonAttributes(java.util.Map, java.util.Map)
     */
    @Override
    protected Map<String, List<Object>> mergePersonAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider) {
        Validate.notNull(toModify, "toModify cannot be null");
        Validate.notNull(toConsider, "toConsider cannot be null");

        for (final Map.Entry<String, List<Object>> sourceEntry : toConsider.entrySet()) {
            final String sourceKey = sourceEntry.getKey();

            if (!toModify.containsKey(sourceKey)) {
                final List<Object> sourceValue = sourceEntry.getValue();
                toModify.put(sourceKey, sourceValue);
            }
        }

        return toModify;
    }
}