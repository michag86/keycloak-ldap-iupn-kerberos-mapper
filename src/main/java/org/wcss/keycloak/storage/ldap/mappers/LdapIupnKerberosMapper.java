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

import static org.keycloak.federation.kerberos.KerberosFederationProvider.KERBEROS_PRINCIPAL;

public class LdapIupnKerberosMapper extends AbstractLDAPStorageMapper {
    private static final Logger logger = Logger.getLogger(LdapIupnKerberosMapper.class);

    public LdapIupnKerberosMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
        super(mapperModel, ldapProvider);
    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        String kerberosPrincipalAttribute = "sAMAccountName";
        String kerberosDistinguishedNameAttribute = "distinguishedName";

        if (kerberosPrincipalAttribute != null) {
            String localKerberosPrincipal = user.getFirstAttribute(KERBEROS_PRINCIPAL);
            String ldapSamAccountName = ldapUser.getAttributeAsString(kerberosPrincipalAttribute);
            String ldapDistinguishedName = ldapUser.getAttributeAsString(kerberosDistinguishedNameAttribute);
            String combinedKerberosPrincipal = buildKerberosPrincipal(ldapSamAccountName, ldapDistinguishedName);
            LdapIupnKerberosMapper.logger.debugf(
                "User: %s, " +
                "kerberosPrincipalAttribute: %s, " +
                "localKerberosPrincipal: %s, " +
                "ldapSamAccountName: %s, " +
                "ldapDistinguishedName: %s" +
                "combinedKerberosPrincipal: %s",
                user.getUsername(), kerberosPrincipalAttribute, localKerberosPrincipal, ldapSamAccountName, ldapDistinguishedName, combinedKerberosPrincipal);
            if (combinedKerberosPrincipal != null && !combinedKerberosPrincipal.equals(localKerberosPrincipal)) {
                // update the Kerberos principal stored in DB as user's attribute if it doesn't match LDAP
                user.setSingleAttribute(KERBEROS_PRINCIPAL, combinedKerberosPrincipal);
            }
        }
    }

    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {

    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        return delegate;
    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {
        String kerberosPrincipalAttribute = "sAMAccountName";
        String kerberosDistinguishedNameAttribute = "distinguishedName";
		query.addReturningLdapAttribute(kerberosPrincipalAttribute);
		query.addReturningLdapAttribute(kerberosDistinguishedNameAttribute);

    }
    private String buildKerberosPrincipal(String samAccountName, String distinguishedName) {
        if (samAccountName == null || distinguishedName == null) {
            return null;
        }
        int index = distinguishedName.indexOf(",DC=");
        if (index != -1) {
            String domain = distinguishedName.substring(index + 4).replace(",DC=", ".").toUpperCase();
            return samAccountName + "@" + domain;
        } else { 
            return null;
        }
    }
}
