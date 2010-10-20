package org.josso.auth.scheme.validation;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class TestCRL {

	private static Log log = LogFactory.getLog(TestCRL.class);

    protected ApplicationContext context;
    
    protected CRLX509CertificateValidator validator;
    
    @Before
    public void initAppContext() {
    	context = new ClassPathXmlApplicationContext("/org/josso/auth/scheme/validation/context.xml");
    	validator = (CRLX509CertificateValidator) context.getBean("crl-validator");
    }

    @Test
	public void testValidCertificate() throws CertificateException {
		boolean valid = false;
		
		try {
			X509Certificate certificate = buildX509Certificate("certs/valid.cer");
			validator.validate(certificate);
			valid = true;
		} catch (X509CertificateValidationException e) {
			log.debug(e, e);
		}
		
		Assert.assertEquals(valid, true);
	}
    
	@Test
	public void testRevokedCertificate() throws CertificateException {
		boolean valid = true;
		
		try {
			X509Certificate certificate = buildX509Certificate("certs/revoked.cer");
			validator.validate(certificate);
			valid = false;
		} catch (X509CertificateValidationException e) {
			log.debug(e, e);
			valid = false;
		} catch (CertificateException e) {
			log.debug(e, e);
		}
		
		Assert.assertEquals(valid, false);
	}
	
	@Test
	public void testExpiredCertificate() throws CertificateException {
		boolean valid = true;
		
		try {
			X509Certificate certificate = buildX509Certificate("certs/expired.cer");
			validator.validate(certificate);
			valid = false;
		} catch (X509CertificateValidationException e) {
			log.debug(e, e);
			valid = false;
		} catch (CertificateException e) {
			log.debug(e, e);
		}
		
		Assert.assertEquals(valid, false);
	}
	
	private X509Certificate buildX509Certificate(String certFile) throws CertificateException {
        X509Certificate cert = null;

        try {
        	InputStream is = getClass().getResourceAsStream(certFile);
            CertificateFactory cf =
                    CertificateFactory.getInstance("X.509");

            cert = (X509Certificate) cf.generateCertificate(is);

        } catch (CertificateException e) {
        	log.error(e, e);
        	throw e;
        }

        return cert;
    }
}
