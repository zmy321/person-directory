/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.services.persondir.support.merger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.IPerson;
import org.jasig.services.persondir.support.NamedPersonImpl;

/**
 * Merges the Sets of IPersons additively calling the abstract {@link #mergePersonAttributes(Map, Map)} method on the
 * attributes of IPersons that exist in both sets. The {@link #mergeAvailableQueryAttributes(Set, Set)} and {@link #mergePossibleUserAttributeNames(Set, Set)}
 * methods do a simple addative merge of the sets. These can be overriden by subclasses.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseAdditiveAttributeMerger implements IAttributeMerger {
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.IAttributeMerger#mergeAvailableQueryAttributes(java.util.Set, java.util.Set)
     */
    public Set<String> mergeAvailableQueryAttributes(Set<String> toModify, Set<String> toConsider) {
        toModify.addAll(toConsider);
        return toModify;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.IAttributeMerger#mergePossibleUserAttributeNames(java.util.Set, java.util.Set)
     */
    public Set<String> mergePossibleUserAttributeNames(Set<String> toModify, Set<String> toConsider) {
        toModify.addAll(toConsider);
        return toModify;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.IAttributeMerger#mergeResults(java.util.Set, java.util.Set)
     */
    public final Set<IPerson> mergeResults(Set<IPerson> toModify, Set<IPerson> toConsider) {
        Validate.notNull(toModify, "toModify cannot be null");
        Validate.notNull(toConsider, "toConsider cannot be null");
        
        //Convert the toModify Set into a Map to allow for easier lookups
        final Map<String, IPerson> toModfyPeople = new LinkedHashMap<String, IPerson>();
        for (final IPerson toModifyPerson : toModify) {
            toModfyPeople.put(toModifyPerson.getName(), toModifyPerson);
        }
        
        //Merge in the toConsider people
        for (final IPerson toConsiderPerson : toConsider) {
            final String toConsiderName = toConsiderPerson.getName();
            final IPerson toModifyPerson = toModfyPeople.get(toConsiderName);
            
            //No matching toModify person, just add the new person
            if (toModifyPerson == null) {
                toModify.add(toConsiderPerson);
            }
            //Matching toModify person, merge their attributes
            else {
                final Map<String, List<Object>> toModifyAttributes = this.buildMutableAttributeMap(toModifyPerson.getAttributes());
                final Map<String, List<Object>> mergedAttributes = this.mergePersonAttributes(toModifyAttributes, toConsiderPerson.getAttributes());
                final NamedPersonImpl mergedPerson = new NamedPersonImpl(toConsiderName, mergedAttributes);
                
                //Remove then re-add the mergedPerson entry
                toModify.remove(mergedPerson);
                toModify.add(mergedPerson);
            }
        }
        
        return toModify;
    }
    
    /**
     * Do a deep clone of an attribute Map to ensure it is completley mutable.
     */
    protected Map<String, List<Object>> buildMutableAttributeMap(Map<String, List<Object>> attributes) {
        final Map<String, List<Object>> mutableValuesBuilder = this.createMutableAttributeMap(attributes.size());

        for (final Map.Entry<String, List<Object>> attrEntry : attributes.entrySet()) {
            final String key = attrEntry.getKey();
            List<Object> value = attrEntry.getValue();
            
            if (value != null) {
                value = new ArrayList<Object>(value);
            }
            
            mutableValuesBuilder.put(key, value);
        }

        return mutableValuesBuilder;
    }

    /**
     * Create the Map used when merging attributes
     */
    protected Map<String, List<Object>> createMutableAttributeMap(int size) {
        return new LinkedHashMap<String, List<Object>>(size > 0 ? size : 1);
    }

    /**
     * Modify the "toModify" argument in consideration of the "toConsider" argument.  Return the resulting Map, which
     * may or may not be the same reference as the "toModify" argument. The modification performed is
     * implementation-specific -- implementations of this interface exist to perform some particular transformation on
     * the toModify argument given the toConsider argument.
     * 
     * @param toModify - modify this map
     * @param toConsider - in consideration of this map
     * @return the modified Map
     * @throws IllegalArgumentException if either toModify or toConsider is null
     */
    protected abstract Map<String, List<Object>> mergePersonAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider);
    

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.IAttributeMerger#mergeAttributes(java.util.Map, java.util.Map)
     */
    @SuppressWarnings("deprecation")
    public Map<String, List<Object>> mergeAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider) {
        return this.mergePersonAttributes(toModify, toConsider);
    }
}
