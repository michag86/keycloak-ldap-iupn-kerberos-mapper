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
            // First occurence of ",DC=" in ldapDistinguishedName
            int index = ldapDistinguishedName.indexOf(",DC=");
            if (index != -1) {
                // all after first ",DC=", replace ",DC=" with "." and convert upper case
                String ldapDistinguishedNameDomainPart = ldapDistinguishedName.substring(0, index);
                String ldapDnsDomain = ldapDistinguishedNameDomainPart.replace(",DC=", ".");
                String ldapDnsDomainUpperCase = ldapDnsDomain.toUpperCase();
                String combinedKerberosPrincial = ldapSamAccountName + "@" + ldapDnsDomainUpperCase;
            } else {
                String combinedKerberosPrincial = null;
            }
            LdapIupnKerberosMapper.logger.debugf(
                "User: %s, " +
                "kerberosPrincipalAttribute: %s, " +
                "localKerberosPrincipal: %s, " +
                "ldapSamAccountName: %s, " +
                "ldapDistinguishedName: %s"+
                "combinedKerberosPrincial: %s", 
                user.getUsername(), kerberosPrincipalAttribute, localKerberosPrincipal, ldapSamAccountName, ldapDistinguishedName, combinedKerberosPrincial);
            if (combinedKerberosPrincial != null && localKerberosPrincipal != null) {
                // update the Kerberos principal stored in DB as user's attribute if it doesn't match LDAP
                if (!ldapSamAccountName.equals(localKerberosPrincipal)) {
                    user.setSingleAttribute(KERBEROS_PRINCIPAL, combinedKerberosPrincial);
                }
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
}
