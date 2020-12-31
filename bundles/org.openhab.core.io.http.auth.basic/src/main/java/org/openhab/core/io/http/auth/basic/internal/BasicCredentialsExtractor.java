/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.io.http.auth.basic.internal;

import java.util.Base64;
import java.util.Optional;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.openhab.core.auth.Credentials;
import org.openhab.core.auth.UsernamePasswordCredentials;
import org.openhab.core.io.http.auth.CredentialsExtractor;
import org.osgi.service.component.annotations.Component;

/**
 * Extract user name and password from incoming request.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
@Component(property = { "context=javax.servlet.http.HttpServletRequest" })
public class BasicCredentialsExtractor implements CredentialsExtractor<HttpServletRequest> {
    static HashMap<String, String> authCache = new HashMap<String, UsernamePasswordCredentials>();

    @Override
    public Optional<Credentials> retrieveCredentials(HttpServletRequest request) {
        String authenticationHeader = request.getHeader("Authorization");

        if (authenticationHeader == null) {
            return Optional.empty();
        }

        String[] tokens = authenticationHeader.split(" ");
        if (tokens.length == 2) {
            String authType = tokens[0];
            if (HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(authType)) {
                String auth_value = tokens[1];
                cached_value = BasicCredentialsExtractor.authCache.get(auth_value);
                if (cached_value != null) {
                    return cached_value;
                }
                    
                String usernameAndPassword = new String(Base64.getDecoder().decode(auth_value));

                tokens = usernameAndPassword.split(":");
                if (tokens.length == 2) {
                    String username = tokens[0];
                    String password = tokens[1];

                    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
                    if (BasicCredentialsExtractor.authCache.size() > 9) {
                        Object remName = null;
                        for (Object obj : BasicCredentialsExtractor.authCache.keySet()) {
                            remName = obj;
                            break;
                        }
                        BasicCredentialsExtractor.authCache.remove(remName);
                    }
                    BasicCredentialsExtractor.authCache.put(auth_value, creds);
                    return Optional.of(creds);
                }
            }
        }

        return Optional.empty();
    }
}
