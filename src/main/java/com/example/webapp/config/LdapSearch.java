package com.example.webapp.config;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class LdapSearch {
    public List<String> getAllPersonNames() {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://regions.office.np-ivc.ru:389");
        env.put(Context.SECURITY_PRINCIPAL, "CN=ldap_user_ro,OU=Service,OU=Users,OU=nsk,OU=All,DC=regions,DC=office,DC=np-ivc,DC=ru");
        env.put(Context.SECURITY_CREDENTIALS, "i8wx6NzLssM4");

        DirContext ctx;
        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException | javax.naming.NamingException e) {
            throw new RuntimeException(e);
        }

        List<String> list = new LinkedList<String>();
        NamingEnumeration results = null;
        try {
            SearchControls controls = new SearchControls();
            String[] attrIDs = {"displayName"};
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(attrIDs) ;
            results = ctx.search("OU=All,DC=regions,DC=office,DC=np-ivc,DC=ru","(&(objectClass=user)(!(objectCategory=computer)))", controls);

            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                Attribute attr = attributes.get("displayName");
                String cn = attr.get().toString();
                list.add(cn);
            }
        } catch (NameNotFoundException e) {
        } catch (NamingException | javax.naming.NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }
        return list;
    }
}