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
package org.josso.seam.console;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.josso.seam.console.model.Username;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.security.MessageDigest;

@Stateless
@Name("register")
public class RegisterBean implements Register {

    @Logger
    private Log log;

    @PersistenceContext
    private EntityManager em;

    @In
    FacesMessages facesMessages;

    @In
    UsernameHome usernameHome;

    @In
    Identity identity;

    /**
     * Password confirmation
     */
    private String password;
    private String confirm;

    //seam-gen method
    public String register () {
        //implement your business logic here
        if ( password.equals( confirm ) ) {
            String hash = null;
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance( "MD5" );
                byte raw[] = md.digest( password.getBytes() );
                hash = encodeBase16( raw );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            log.info( "Register.register() action called" );
            //log.info(password + " " + confirm);
            Username username = usernameHome.getInstance();
            username.setPasswd( hash );
            em.persist( username );
            return "success";
        } else {
            facesMessages.add( "Passwords are not identical." );
            return "failure";
        }
    }

    //  seam-gen method
    public String update () {
        //implement your business logic here
        if ( password.equals( confirm ) ) {
            String hash = null;
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance( "MD5" );
                byte raw[] = md.digest( password.getBytes() );
                hash = encodeBase16( raw );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            log.info( "Register.register() action called" );
            //log.info(password + " " + confirm);
            Username username = usernameHome.getInstance();
            username.setPasswd( hash );

            String user = identity.getUsername();
            Boolean isAdmin = identity.hasRole( "admin" );
            if ( !user.equals( username.getLogin() ) && !isAdmin ) {
                facesMessages.add( "Only the user or the admin can make updates." );
                return "failure";
            }

            em.merge( username );
            return "success";
        } else {
            facesMessages.add( "Passwords are not identical" );
            return "failure";
        }
    }

    @NotNull
    @Length(min = 4, max = 32)
    public String getConfirm () {
        return confirm;
    }


    public void setConfirm ( String confirm ) {
        this.confirm = confirm;
    }

    @NotNull
    @Length(min = 4, max = 32)
    public String getPassword () {
        return password;
    }


    public void setPassword ( String password ) {
        this.password = password;
    }

    //add additional action methods
    /**
     * Base16 encoding (HEX).
     */
    protected String encodeBase16 ( byte[] bytes ) {
        StringBuffer sb = new StringBuffer( bytes.length * 2 );
        for ( int i = 0; i < bytes.length; i++ ) {
            byte b = bytes[ i ];
            // top 4 bits
            char c = (char) ( ( b >> 4 ) & 0xf );
            if ( c > 9 )
                c = (char) ( ( c - 10 ) + 'a' );
            else
                c = (char) ( c + '0' );
            sb.append( c );
            // bottom 4 bits
            c = (char) ( b & 0xf );
            if ( c > 9 )
                c = (char) ( ( c - 10 ) + 'a' );
            else
                c = (char) ( c + '0' );
            sb.append( c );
        }
        return sb.toString();
    }


}
