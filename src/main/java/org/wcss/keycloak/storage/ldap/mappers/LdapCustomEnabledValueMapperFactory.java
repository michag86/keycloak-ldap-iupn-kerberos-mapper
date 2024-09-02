package org.wcss.keycloak.storage.ldap.mappers;

import org.keycloak.component.ComponentModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;

import java.util.List;

public class LdapCustomEnabledValueMapperFactory extends AbstractLDAPStorageMapperFactory {

    public static final String PROVIDER_ID = "ldap-custom-enabled-value-mapper";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Map the user model enabled attribute based on the LDAP attribute";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
	    ProviderConfigurationBuilder config = ProviderConfigurationBuilder.create()
		    .property().name(LdapCustomEnabledValueMapper.LDAP_ATTRIBUTE)
		    .label("LDAP attribute")
		    .helpText("Name of mapped attribute on LDAP object. For example 'cn', 'uid', 'inetUserStatus' etc.")
		    .type(ProviderConfigProperty.STRING_TYPE)
		    .required(true)
		    .add()
		    .property().name(LdapCustomEnabledValueMapper.ENABLED_VALUE)
		    .label("Enabled value")
		    .helpText("Enabled value from LDAP. If the LDAP attribute equals this value the user would be enabled.")
		    .type(ProviderConfigProperty.STRING_TYPE)
		    .required(true)
		    .add()
	    	    .property().name(LdapCustomEnabledValueMapper.ALWAYS_READ_VALUE_FROM_LDAP)
		    .label("Always Read Value From LDAP")
                    .helpText("If on, then during reading of the LDAP attribute value will always used instead of the value from Keycloak DB")
                    .type(ProviderConfigProperty.BOOLEAN_TYPE).defaultValue("false").add();

	    return config.build();

    }
    @Override
    protected AbstractLDAPStorageMapper createMapper(ComponentModel mapperModel, LDAPStorageProvider federationProvider) {
        return new LdapCustomEnabledValueMapper(mapperModel, federationProvider);
    }

}
