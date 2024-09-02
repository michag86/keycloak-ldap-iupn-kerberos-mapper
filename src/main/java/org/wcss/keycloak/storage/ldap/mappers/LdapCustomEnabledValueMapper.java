package org.wcss.keycloak.storage.ldap.mappers;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.models.utils.UserModelDelegate;

public class LdapCustomEnabledValueMapper extends AbstractLDAPStorageMapper {
	private static final Logger logger = Logger.getLogger(LdapCustomEnabledValueMapper.class);

	public static final String ALWAYS_READ_VALUE_FROM_LDAP = "always.read.value.from.ldap";
	public static final String LDAP_ATTRIBUTE = "ldap.attribute";
	public static final String ENABLED_VALUE = "enabled.ldap.value";

	public LdapCustomEnabledValueMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
		super(mapperModel, ldapProvider);
	}

	@Override
	public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
		String ldapAttrName = getLdapAttributeName();
		String ldapAttrValue = ldapUser.getAttributeAsString(ldapAttrName);
		String enabledValue = getLdapEnabledValue();

		boolean isEnabled = (ldapAttrValue != null && ldapAttrValue.equalsIgnoreCase(enabledValue));

		if (ldapAttrValue == null) {
			LdapCustomEnabledValueMapper.logger.warnf("Failed to enable user: %s, LDAP attribute value is null", user.getUsername());
		}

		LdapCustomEnabledValueMapper.logger.debugf(
				"User: %s is enabled: %s, " +
				"LDAP attribute name: %s, " +
				"LDAP attribute value: %s, " +
				"Expected value: %s", 
				user.getUsername(), isEnabled, ldapAttrName, ldapAttrValue, enabledValue);

		user.setEnabled(isEnabled);
	}


	@Override
	public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
		final String ldapAttrName = getLdapAttributeName();
		final String ldapAttrValue = ldapUser.getAttributeAsString(ldapAttrName);
		final String enabledValue = getLdapEnabledValue();
		boolean isAlwaysReadValueFromLDAP = parseBooleanParameter(mapperModel, ALWAYS_READ_VALUE_FROM_LDAP);
		if (isAlwaysReadValueFromLDAP) {

			delegate = new UserModelDelegate(delegate) {

				@Override
				public boolean isEnabled() {
					return ldapAttrValue != null && ldapAttrValue.equalsIgnoreCase(enabledValue);
				}

			};
		}

		return delegate;
	}

	@Override
	public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {
		// Not supported
	}

	@Override
	public void beforeLDAPQuery(LDAPQuery query) {
		String ldapAttrName = getLdapAttributeName();

		// Add mapped attribute to returning ldap attributes
		query.addReturningLdapAttribute(ldapAttrName);
	}

	String getLdapAttributeName() {
		return mapperModel.getConfig().getFirst(LDAP_ATTRIBUTE);
	}

	String getLdapEnabledValue() {
		return mapperModel.getConfig().getFirst(ENABLED_VALUE);
	}
}
