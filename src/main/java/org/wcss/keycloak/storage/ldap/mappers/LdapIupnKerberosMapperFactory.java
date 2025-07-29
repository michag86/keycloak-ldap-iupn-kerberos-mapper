
package org.wcss.keycloak.storage.ldap.mappers;

import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;

public class LdapIupnKerberosMapperFactory extends AbstractLDAPStorageMapperFactory {

    public static final String PROVIDER_ID = "ldap-iupn-kerberos-mapper";

    @Override
    protected LdapIupnKerberosMapper createMapper(ComponentModel mapperModel, LDAPStorageProvider federationProvider) {
        return new LdapIupnKerberosMapper(mapperModel, federationProvider);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "This mapper will update Kerberos principal attribute in the DB when the attribute changes in LDAP.";
    }
}
