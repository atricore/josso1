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
package org.josso.auth.scheme.validation;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base X509 Certificate validator.
 */
public abstract class AbstractX509CertificateValidator implements X509CertificateValidator {

	private static final Log log = LogFactory
			.getLog(AbstractX509CertificateValidator.class);

	protected String _url;
	protected String _httpProxyHost;
	protected String _httpProxyPort;
	protected String _trustStore;
	protected String _trustPassword;
	protected List<String> _caCertAliases;
	protected List<String> _trustAnchorCertAliases;
	
	private KeyStore _keystore;
	private Set<TrustAnchor> _trustAnchors;
	private List<X509Certificate> _caCerts;
	private boolean _initialized = false;
	
	/**
     * Initialize the keystore and trusted certificates.
     */
    public synchronized void initialize() {
        try {
        	if (_initialized) {
        		return;
        	}
        	if (_trustStore == null) {
        		log.error("TrustStore is not set!");
        		throw new RuntimeException("Can't initialize keystore!");
        	}
        	if (_trustAnchorCertAliases == null || _trustAnchorCertAliases.size() == 0) {
        		log.error("Trust anchor certificate aliases are not set!");
        		throw new RuntimeException("Trust anchor certificate aliases are not set!");
        	}
        	
        	// load keystore
        	_keystore = KeyStore.getInstance("JKS");
			char[] trustPass = null;
			if (_trustPassword != null) {
				trustPass = _trustPassword.toCharArray();
			}
			_keystore.load(getClass().getResourceAsStream(_trustStore), trustPass);
		    
		    // load trust anchor certificates
		    _trustAnchors = new HashSet<TrustAnchor>();
		    for (String trustAnchorCertAlias : _trustAnchorCertAliases) {
		    	Certificate certificate = _keystore.getCertificate(trustAnchorCertAlias);
				if (certificate != null && certificate instanceof X509Certificate) {
					TrustAnchor ta = new TrustAnchor((X509Certificate)certificate, null);
					_trustAnchors.add(ta);
				}
			}
		    
		    // load intermediate CA certificates
		    _caCerts = new ArrayList<X509Certificate>();
		    if (_caCertAliases != null && _caCertAliases.size() > 0) {
		    	for (String caCertAlias : _caCertAliases) {
			    	Certificate certificate = _keystore.getCertificate(caCertAlias);
					if (certificate != null && certificate instanceof X509Certificate) {
						_caCerts.add((X509Certificate)certificate);
					}
				}
		    }
			
            _initialized = true;

        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException("Can't initialize keystore : " + e.getMessage(), e);
        }
    }
	
	/**
	 * Generates certificate path from supplied client certificate
	 * and CA certificates.
	 * 
	 * @param clientCertificate client certificate
	 * @return certificate path
	 * @throws CertificateException
	 */
	protected CertPath generateCertificatePath(X509Certificate clientCertificate) 
			throws CertificateException {
		if (!_initialized) {
			initialize();
		}
		List<X509Certificate> certs = new ArrayList<X509Certificate>();
		certs.add(clientCertificate);
		certs.addAll(_caCerts);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		return cf.generateCertPath(certs);
	}

	/**
	 * Generates trust anchors.
	 * 
	 * @return trust anchors
	 * @throws CertificateException
	 */
	protected Set<TrustAnchor> generateTrustAnchors() throws CertificateException {
		if (!_initialized) {
			initialize();
		}
		return _trustAnchors;
	}
	
	/**
	 * Gets certificate from keystore.
	 * 
	 * @param alias alias
	 * @return certificate or null
	 * @throws CertificateException
	 */
	protected X509Certificate getCertificate(String alias) throws CertificateException {
		if (alias == null) {
			return null;
		}
		if (!_initialized) {
			initialize();
		}
		try {
			return (X509Certificate) _keystore.getCertificate(alias);
		} catch (KeyStoreException e) {
			log.error(e, e);
			throw new RuntimeException("Error getting certificate from keystore : " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return _url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		_url = url;
	}

	/**
	 * @return the httpProxyHost
	 */
	public String getHttpProxyHost() {
		return _httpProxyHost;
	}

	/**
	 * @param httpProxyHost the httpProxyHost to set
	 */
	public void setHttpProxyHost(String httpProxyHost) {
		_httpProxyHost = httpProxyHost;
	}

	/**
	 * @return the httpProxyPort
	 */
	public String getHttpProxyPort() {
		return _httpProxyPort;
	}

	/**
	 * @param httpProxyPort the httpProxyPort to set
	 */
	public void setHttpProxyPort(String httpProxyPort) {
		_httpProxyPort = httpProxyPort;
	}

	/**
	 * @return the trustStore
	 */
	public String getTrustStore() {
		return _trustStore;
	}

	/**
	 * @param trustStore the trustStore to set
	 */
	public void setTrustStore(String trustStore) {
		_trustStore = trustStore;
	}

	/**
	 * @return the trustPassword
	 */
	public String getTrustPassword() {
		return _trustPassword;
	}

	/**
	 * @param trustPassword the trustPassword to set
	 */
	public void setTrustPassword(String trustPassword) {
		_trustPassword = trustPassword;
	}

	/**
	 * @return the trustAnchorCertAliases
	 */
	public List<String> getTrustAnchorCertAliases() {
		return _trustAnchorCertAliases;
	}

	/**
	 * @param trustAnchorCertAliases the trustAnchorCertAliases to set
	 */
	public void setTrustAnchorCertAliases(List<String> trustAnchorCertAliases) {
		_trustAnchorCertAliases = trustAnchorCertAliases;
	}

	/**
	 * @return the caCertAliases
	 */
	public List<String> getCaCertAliases() {
		return _caCertAliases;
	}

	/**
	 * @param caCertAliases the caCertAliases to set
	 */
	public void setCaCertAliases(List<String> caCertAliases) {
		_caCertAliases = caCertAliases;
	}
}
