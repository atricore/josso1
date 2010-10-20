/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.josso.auth.scheme;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.CertificatePrincipal;
import org.josso.auth.Credential;
import org.josso.auth.CredentialProvider;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.scheme.validation.X509CertificateValidationException;
import org.josso.auth.scheme.validation.X509CertificateValidator;

import sun.security.util.DerValue;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.security.auth.x500.X500Principal;

/**
 * Certificate-based Authentication Scheme.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: X509CertificateAuthScheme.java 568 2008-07-31 18:39:20Z sgonzalez $
 *
 * @org.apache.xbean.XBean element="strong-auth-scheme"
 */

public class X509CertificateAuthScheme extends AbstractAuthenticationScheme {
    private static final Log logger = LogFactory.getLog(X509CertificateAuthScheme.class);

    /* Component Properties */
    private String _uidOID;
    
    /* User UID */
    private String _uid;

    /* X509 Certificate validators */
    private List<X509CertificateValidator> _validators;
    
    public X509CertificateAuthScheme() {
        this.setName("strong-authentication");
    }

    /**
     * @throws SSOAuthenticationException
     */
    public boolean authenticate()
            throws SSOAuthenticationException {

        setAuthenticated(false);

        //String username = getUsername(_inputCredentials);
        X509Certificate x509Certificate = getX509Certificate(_inputCredentials);

        // Check if all credentials are present.
        if (x509Certificate == null) {

            if (logger.isDebugEnabled())
                logger.debug("X.509 Certificate not provided");

            // We don't support empty values !
            return false;
        }

        // validate certificate
        if (_validators != null) {
        	for (X509CertificateValidator validator : _validators) {
        		try {
        			validator.validate(x509Certificate);
        		} catch (X509CertificateValidationException e) {
        			logger.error("Certificate is not valid!", e);
        			return false;
        		}
        	}
        }
        
        List<X509Certificate> knownX509Certificates = getX509Certificates(getKnownCredentials());

        StringBuffer buf = new StringBuffer("\n\tSupplied Credential: ");
        buf.append(x509Certificate.getSerialNumber().toString(16));
        buf.append("\n\t\t");
        buf.append(x509Certificate.getSubjectX500Principal().getName());
        buf.append("\n\n\tExisting Credentials: ");
        for (int i=0; i<knownX509Certificates.size(); i++) {
        	X509Certificate knownX509Certificate = knownX509Certificates.get(i);
        	buf.append(i+1);
        	buf.append("\n\t\t");
        	buf.append(knownX509Certificate.getSerialNumber().toString(16));
            buf.append("\n\t\t");
            buf.append(knownX509Certificate.getSubjectX500Principal().getName());
            buf.append("\n");
        }

        logger.debug(buf.toString());

        // Validate user identity ...
        boolean valid = false;
        X509Certificate validCertificate = null;
        for (X509Certificate knownX509Certificate : knownX509Certificates) {
	        if (validateX509Certificate(x509Certificate, knownX509Certificate)) {
	        	validCertificate = knownX509Certificate;
	            break;
	        }
        }

        if (validCertificate == null) {
        	return false;
        }
        
        // Find UID
        // (We could just use getUID() to authenticate user
        // without previous validation against known certificates?)
        _uid = getUID();
        if (_uid == null) {
        	return false;
        }
        
        if (logger.isDebugEnabled())
            logger.debug("[authenticate()], Principal authenticated : " +
                    x509Certificate.getSubjectX500Principal()
            );

        // We have successfully authenticated this user.
        setAuthenticated(true);
        return true;
    }

    /**
     * Create a X.509 Certificate Credential Provider instance
     *
     * @return
     */
    protected CredentialProvider doMakeCredentialProvider() {
        return new X509CertificateCredentialProvider();
    }

    private X509Certificate buildX509Certificate(byte[] binaryCert) {
        X509Certificate cert = null;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(binaryCert);
            CertificateFactory cf =
                    CertificateFactory.getInstance("X.509");

            cert = (X509Certificate) cf.generateCertificate(bais);

            if (logger.isDebugEnabled())
                logger.debug("Building X.509 certificate result :\n " + cert);

        } catch (CertificateException ce) {
            logger.error("Error instantiating X.509 Certificate", ce);
        }

        return cert;
    }

    private X509Certificate buildX509Certificate(String plainCert) {
        return buildX509Certificate(plainCert.getBytes());
    }

    /**
     * Returns the private input credentials.
     *
     * @return the private input credentials
     */
    public Credential[] getPrivateCredentials() {
        Credential c = getX509CertificateCredential(_inputCredentials);

        if (c == null)
            return new Credential[0];

        Credential[] r = {c};
        return r;
    }

    /**
     * Returns the public input credentials.
     *
     * @return the public input credentials
     */
    public Credential[] getPublicCredentials() {
        Credential c = getX509CertificateCredential(_inputCredentials);

        if (c == null)
            return new Credential[0];

        Credential[] r = {c};
        return r;
    }

    /**
     * Instantiates a Principal for the user X509 Certificate.
     * Used as the primary key to obtain the known credentials from the associated
     * store.
     *
     * @return the Principal associated with the input credentials.
     */
    public Principal getPrincipal() {
    	if (_uid != null) {
    		return new CertificatePrincipal(_uid, getX509Certificate(_inputCredentials));
    	} else {
    		return getPrincipal(_inputCredentials);
    	}
    }

    /**
     * Instantiates a Principal for the user X509 Certificate.
     * Used as the primary key to obtain the known credentials from the associated
     * store.
     *
     * @return the Principal associated with the input credentials.
     */
    public Principal getPrincipal(Credential[] credentials) {
    	X509Certificate certificate = getX509Certificate(credentials);
    	X500Principal p = certificate.getSubjectX500Principal();
    	CertificatePrincipal targetPrincipal = null;

        if (_uidOID == null) {
            HashMap compoundName = parseCompoundName(p.getName());

            // Extract from the Distinguished Name (DN) only the Common Name (CN) since its
            // the store who sets the root naming context to be used based on the
            // store configuration.
            String cn = (String) compoundName.get("cn");

            if (cn == null)
                logger.error("Invalid Subject DN. Cannot create Principal : " +
                        p.getName()
                );

            targetPrincipal = new CertificatePrincipal(cn, certificate);
        } else {
            try {
                byte[] oidValue = getOIDBitStringValueFromCert(certificate, _uidOID);

                if (oidValue == null)
                    logger.error("No value obtained for OID " + _uidOID + ". Cannot create Principal : " +
                            p.getName()
                    );

                // TODO: what if the OID is a compound value?
                targetPrincipal = new CertificatePrincipal(new String(oidValue), certificate);
            } catch (Exception e) {
                logger.error("Fatal error obtaining UID value using OID " + _uidOID +
                        ". Cannot create Principal : " + p.getName(), e);
            }
        }

        return targetPrincipal;
    }


    /**
     * Gets the credential that represents an X.509 Certificate.
     */
    protected X509CertificateCredential getX509CertificateCredential(Credential[] credentials) {

        for (int i = 0; i < credentials.length; i++) {
            if (credentials[i] instanceof X509CertificateCredential) {
                return (X509CertificateCredential) credentials[i];
            }
        }
        return null;
    }

    /**
     * Gets the list of credentials that represent X.509 Certificates.
     */
    protected List<X509CertificateCredential> getX509CertificateCredentials(Credential[] credentials) {
    	List<X509CertificateCredential> certCredentials = new ArrayList<X509CertificateCredential>();
        for (int i = 0; i < credentials.length; i++) {
            if (credentials[i] instanceof X509CertificateCredential) {
                certCredentials.add((X509CertificateCredential) credentials[i]);
            }
        }
        return certCredentials;
    }
    
    /**
     * Gets the X.509 certificate from the supplied credentials
     *
     * @param credentials
     */
    protected X509Certificate getX509Certificate(Credential[] credentials) {
        X509CertificateCredential c = getX509CertificateCredential(credentials);
        if (c == null)
            return null;

        return (X509Certificate) c.getValue();
    }

    /**
     * Gets the list of X.509 certificates from the supplied credentials
     *
     * @param credentials
     */
    protected List<X509Certificate> getX509Certificates(Credential[] credentials) {
    	List<X509Certificate> certs = new ArrayList<X509Certificate>();
        List<X509CertificateCredential> certCredentials = getX509CertificateCredentials(credentials);
        for (X509CertificateCredential c : certCredentials) {
        	certs.add((X509Certificate) c.getValue());
        }
        return certs;
    }
    
    /**
     * This method validates the input x509 certificate agaist the expected x509 certificate.
     *
     * @param inputX509Certificate    the X.509 Certificate supplied on authentication.
     * @param expectedX509Certificate the actual X.509 Certificate
     * @return true if the certificates match or false otherwise.
     */
    protected boolean validateX509Certificate(X509Certificate inputX509Certificate,
                                              X509Certificate expectedX509Certificate) {

        if (inputX509Certificate == null && expectedX509Certificate == null)
            return false;

        return inputX509Certificate.equals(expectedX509Certificate);
    }


    /**
     * Parses a Compound name
     * (ie. CN=Java Duke, OU=Java Software Division, O=Sun Microsystems Inc, C=US) and
     * builds a HashMap object with key-value pairs.
     *
     * @param s a string containing the compound name to be parsed
     * @return a HashMap object built from the parsed key-value pairs
     * @throws IllegalArgumentException if the compound name
     *                                  is invalid
     */
    private HashMap parseCompoundName(String s) {

    	String valArray[] = null;

		if (s == null) {
			throw new IllegalArgumentException();
		}
		HashMap hm = new HashMap();

		// Escape characters noticed, so use "extended/escaped parser"
		if ((s.indexOf("\"") > 0) || (s.indexOf("\\") > 0)) {
			StringBuffer sb = new StringBuffer(s);
			boolean escaped = false;
			StringBuffer buff = new StringBuffer();
			String key = "";
			String value = "";
			for (int i = 0; i < sb.length(); i++) {
				// Quotes are begin/end, so keep a flag of escape-state
				if ('"' == sb.charAt(i)) {
					if (escaped) {
						escaped = false;
						continue;
					} else {
						escaped = true;
						continue;
					}

					// Single-character escape/advance
					// but check the length, too.
				} else if ('\\' == sb.charAt(i)) {
					i++;
					if (i >= sb.length()) {
						break;
					}

					// Split on '=' between key/value
				} else if ('=' == sb.charAt(i)) {
					key = buff.toString();
					buff = new StringBuffer();
					continue;

					// We've reached a valid delimiter, as long as we're not
					// still reading 'escaped' data
				} else if ((',' == sb.charAt(i)) && (!escaped)) {
					value = buff.toString();
					buff = new StringBuffer();

					key = key.trim().toLowerCase();
					value = value.trim();
					hm.put(key, value);

					continue;
				}
				buff.append(sb.charAt(i));
			}// for...

			// And the last one...
			value = buff.toString();
			key = key.trim().toLowerCase();
			value = value.trim();
			hm.put(key, value);

		} else { // Otherwise, no (known) escape characters, so continue on with
					// the faster parse.
			StringTokenizer st = new StringTokenizer(s, ",");
			while (st.hasMoreTokens()) {
				String pair = (String) st.nextToken();
				int pos = pair.indexOf('=');
				if (pos == -1) {
					// XXX
					// should give more detail about the illegal argument
					throw new IllegalArgumentException();
				}
				String key = pair.substring(0, pos).trim().toLowerCase();
				String val = pair.substring(pos + 1, pair.length()).trim();
				hm.put(key, val);
			}
		}

		return hm;
    }

    private byte[] getOIDBitStringValueFromCert(X509Certificate cert, String oid)
            throws Exception {

        byte[] derEncodedValue = cert.getExtensionValue(oid);
        byte[] extensionValue = null;

        DerValue dervalue = new DerValue(derEncodedValue);
        if (dervalue == null) {
            throw new IllegalArgumentException("extension not found for OID : " + oid);
        }
        if (dervalue.tag != DerValue.tag_BitString) {
            throw new IllegalArgumentException("extension vaue for OID not of type BIT_STRING: " + oid);
        }

        extensionValue = dervalue.getBitString();

        byte extensionValueBytes[] = new byte[extensionValue.length - 2];

        System.arraycopy(extensionValue, 2, extensionValueBytes, 0, extensionValueBytes.length);

        return extensionValueBytes;
    }

    /*------------------------------------------------------------ Properties

    /**
     * Sets the OID for the UID
     */
    public void setUidOID(String uidOID) {
        _uidOID = uidOID;
    }

    /**
     * Obtains the UID OID
     */
    public String getUidOID() {
        return _uidOID;
    }

    public List<X509CertificateValidator> getValidators() {
        return _validators;
    }

    public void setValidators(List<X509CertificateValidator> validators) {
        _validators = validators;
    }

}
