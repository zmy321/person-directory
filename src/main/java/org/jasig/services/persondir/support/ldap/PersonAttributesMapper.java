/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support.ldap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.springframework.ldap.core.AttributesMapper;

/**
 * Provides {@link net.sf.ldaptemplate.AttributesMapper} for use with a {@link net.sf.ldaptemplate.LdapTemplate}
 * to parse ldap query results into the person attribute Map format.
 * 
 * @author Eric Dalquist 
 * @version $Revision$
 */
class PersonAttributesMapper implements AttributesMapper {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final Map<String, Set<String>> ldapAttributesToPortalAttributes;
    
    /**
     * Create a mapper with the ldap to portal attribute mappings. Please read the
     * documentation for {@link org.jasig.portal.services.persondir.support.ldap.LdapPersonAttributeDao#setLdapAttributesToPortalAttributes(Map)}
     * 
     * @param ldapAttributesToPortalAttributes Map of ldap to portal attributes.
     * @see org.jasig.portal.services.persondir.support.ldap.LdapPersonAttributeDao#setLdapAttributesToPortalAttributes(Map)
     */
    public PersonAttributesMapper(Map<String, Set<String>> ldapAttributesToPortalAttributes) {
        if (ldapAttributesToPortalAttributes == null) {
            throw new IllegalArgumentException("ldapAttributesToPortalAttributes may not be null");
        }
        
        this.ldapAttributesToPortalAttributes = ldapAttributesToPortalAttributes;
    }
    
    /**
     * @return Returns the ldapAttributesToPortalAttributes.
     */
    public Map<String, Set<String>> getLdapAttributesToPortalAttributes() {
        return this.ldapAttributesToPortalAttributes;
    }

    /**
     * Performs mapping after an LDAP query for a set of user attributes. Takes each key in the ldap
     * to portal attribute Map and tries to find it in the returned Attributes set. For each found
     * Attribute the value is added to the attribute Map as the value or in the value Set with the
     * portal attribute name as the key. String and byte[] may be values.
     * 
     * @see net.sf.ldaptemplate.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
     */
    public Object mapFromAttributes(Attributes attributes) throws NamingException {
        final Map<String, List<Object>> rowResults = new HashMap<String, List<Object>>();

        for (final Map.Entry<String, Set<String>> attributeMappingEntry : this.ldapAttributesToPortalAttributes.entrySet()) {
            final String ldapAttributeName = attributeMappingEntry.getKey();

            // The attribute exists
            final Attribute attribute = attributes.get(ldapAttributeName);
            if (attribute != null) {
                // See if the ldap attribute is mapped
                Set<String> attributeNames = attributeMappingEntry.getValue();

                // No mapping was found, just use the ldap attribute name
                if (attributeNames == null) {
                    attributeNames = Collections.singleton(ldapAttributeName);
                }
                
                int valueCount = 0;
                for (final NamingEnumeration<?> attrValueEnum = attribute.getAll(); attrValueEnum.hasMore(); valueCount++) {
                    Object attributeValue = attrValueEnum.next();

                    // Convert everything except byte[] to String
                    if (!(attributeValue instanceof byte[])) {
                        if (this.logger.isWarnEnabled()) {
                            this.logger.warn("Converting value " + valueCount + " of LDAP attribute '" + ldapAttributeName + "' from byte[] to String");
                        }
                        
                        attributeValue = attributeValue.toString();
                    }

                    // Run through the mapped attribute names
                    for (final String attributeName : attributeNames) {
                        MultivaluedPersonAttributeUtils.addResult(rowResults, attributeName, attributeValue);
                    }
                }
                
                if (this.logger.isDebugEnabled()) {
                    if (this.ldapAttributesToPortalAttributes.containsKey(ldapAttributeName)) {
                        this.logger.debug("Added " + valueCount + " attributes under mapped names '" + attributeNames + "' for source attribute '" + ldapAttributeName + "'");
                    }
                    else {
                        this.logger.debug("Added " + valueCount + " attributes for source attribute '" + ldapAttributeName + "'");
                    }
                }
            }
        }

        return rowResults;
    }
}