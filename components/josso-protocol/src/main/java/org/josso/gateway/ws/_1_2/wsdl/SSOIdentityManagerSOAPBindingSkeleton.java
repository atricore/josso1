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

/**
 * SSOIdentityManagerSOAPBindingSkeleton.java
 * 
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.josso.gateway.ws._1_2.wsdl;

public class SSOIdentityManagerSOAPBindingSkeleton implements org.josso.gateway.ws._1_2.wsdl.SSOIdentityManager, org.apache.axis.wsdl.Skeleton {
    private org.josso.gateway.ws._1_2.wsdl.SSOIdentityManager impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindUserInSessionRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindUserInSessionRequestType"), org.josso.gateway.ws._1_2.protocol.FindUserInSessionRequestType.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("findUserInSession", _params, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindUserInSessionResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindUserInSessionResponseType"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "findUserInSession"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("findUserInSession") == null) {
            _myOperations.put("findUserInSession", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("findUserInSession")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidSessionErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "InvalidSessionError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.InvalidSessionErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "InvalidSessionErrorType"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NoSuchUserErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "NoSuchUserError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.NoSuchUserErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "NoSuchUserErrorType"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SSOIdentityManagerErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOIdentityManagerError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOIdentityManagerErrorType"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindUserInSecurityDomainRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindUserInSecurityDomainRequestType"), org.josso.gateway.ws._1_2.protocol.FindUserInSecurityDomainRequestType.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("findUserInSecurityDomain", _params, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindUserInSecurityDomainResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindUserInSecurityDomainResponseType"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "findUserInSecurityDomain"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("findUserInSecurityDomain") == null) {
            _myOperations.put("findUserInSecurityDomain", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("findUserInSecurityDomain")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NoSuchUserErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "NoSuchUserError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.NoSuchUserErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "NoSuchUserErrorType"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SSOIdentityManagerErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOIdentityManagerError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOIdentityManagerErrorType"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindRolesBySSOSessionIdRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindRolesBySSOSessionIdRequestType"), org.josso.gateway.ws._1_2.protocol.FindRolesBySSOSessionIdRequestType.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("findRolesBySSOSessionId", _params, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindRolesBySSOSessionIdResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "FindRolesBySSOSessionIdResponseType"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "findRolesBySSOSessionId"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("findRolesBySSOSessionId") == null) {
            _myOperations.put("findRolesBySSOSessionId", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("findRolesBySSOSessionId")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("InvalidSessionErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "InvalidSessionError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.InvalidSessionErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "InvalidSessionErrorType"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SSOIdentityManagerErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOIdentityManagerError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOIdentityManagerErrorType"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "UserExistsRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "UserExistsRequestType"), org.josso.gateway.ws._1_2.protocol.UserExistsRequestType.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("userExists", _params, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "UserExistsResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "UserExistsResponseType"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "userExists"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("userExists") == null) {
            _myOperations.put("userExists", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("userExists")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SSOIdentityManagerErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOIdentityManagerError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOIdentityManagerErrorType"));
        _oper.addFault(_fault);
    }

    public SSOIdentityManagerSOAPBindingSkeleton() {
        this.impl = new org.josso.gateway.ws._1_2.wsdl.SSOIdentityManagerSOAPBindingImpl();
    }

    public SSOIdentityManagerSOAPBindingSkeleton(org.josso.gateway.ws._1_2.wsdl.SSOIdentityManager impl) {
        this.impl = impl;
    }
    public org.josso.gateway.ws._1_2.protocol.FindUserInSessionResponseType findUserInSession(org.josso.gateway.ws._1_2.protocol.FindUserInSessionRequestType findUserInSessionRequest) throws java.rmi.RemoteException, org.josso.gateway.ws._1_2.protocol.InvalidSessionErrorType, org.josso.gateway.ws._1_2.protocol.NoSuchUserErrorType, org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType
    {
        org.josso.gateway.ws._1_2.protocol.FindUserInSessionResponseType ret = impl.findUserInSession(findUserInSessionRequest);
        return ret;
    }

    public org.josso.gateway.ws._1_2.protocol.FindUserInSecurityDomainResponseType findUserInSecurityDomain(org.josso.gateway.ws._1_2.protocol.FindUserInSecurityDomainRequestType findUserInSecurityDomainRequest) throws java.rmi.RemoteException, org.josso.gateway.ws._1_2.protocol.NoSuchUserErrorType, org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType
    {
        org.josso.gateway.ws._1_2.protocol.FindUserInSecurityDomainResponseType ret = impl.findUserInSecurityDomain(findUserInSecurityDomainRequest);
        return ret;
    }

    public org.josso.gateway.ws._1_2.protocol.FindRolesBySSOSessionIdResponseType findRolesBySSOSessionId(org.josso.gateway.ws._1_2.protocol.FindRolesBySSOSessionIdRequestType findRolesBySSOSessionIdRequest) throws java.rmi.RemoteException, org.josso.gateway.ws._1_2.protocol.InvalidSessionErrorType, org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType
    {
        org.josso.gateway.ws._1_2.protocol.FindRolesBySSOSessionIdResponseType ret = impl.findRolesBySSOSessionId(findRolesBySSOSessionIdRequest);
        return ret;
    }

    public org.josso.gateway.ws._1_2.protocol.UserExistsResponseType userExists(org.josso.gateway.ws._1_2.protocol.UserExistsRequestType userExistsRequest) throws java.rmi.RemoteException, org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType
    {
        org.josso.gateway.ws._1_2.protocol.UserExistsResponseType ret = impl.userExists(userExistsRequest);
        return ret;
    }

}
