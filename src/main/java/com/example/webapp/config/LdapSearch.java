package com.example.webapp.config;
import org.springframework.ldap.NamingException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.Properties;

public class LdapSearch {

    DirContext connection;
    public void ldapConnection() {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://regions.office.np-ivc.ru:389");
        env.put(Context.SECURITY_PRINCIPAL, "CN=ldap_user_ro,OU=Service,OU=Users,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru");
        env.put(Context.SECURITY_CREDENTIALS, "i8wx6NzLssM4");
        try {
            connection = new InitialDirContext(env);
        } catch (javax.naming.NamingException ex) {
            System.out.println(ex.getMessage());
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    public String getAllUsers() throws NamingException, javax.naming.NamingException {
        String searchFilter = "(objectClass=user)";
        String[] reqAtt = { "displayName" };
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(reqAtt);
        NamingEnumeration<SearchResult> users = connection.search("OU=Active,OU=Users,OU=nsk,DC=regions,DC=office,DC=np-ivc,DC=ru", searchFilter, controls);
        SearchResult result = null;

        result = (SearchResult) users.next();
        Attributes attr = result.getAttributes();
        String name = attr.get("displayName").get(0).toString();
        return name;

//        while (users.hasMore()) {
//            result = (SearchResult) users.next();
//            Attributes attr = result.getAttributes();
//            String name = attr.get("displayName").get(0).toString();
//            System.out.println(attr.get("displayName"));
//        }
    }
}
