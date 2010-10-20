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
 * SSOSessionManagerSOAPBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.josso.gateway.ws._1_2.wsdl;

public class SSOSessionManagerSOAPBindingSkeleton implements org.josso.gateway.ws._1_2.wsdl.SSOSessionManager, org.apache.axis.wsdl.Skeleton {
    private org.josso.gateway.ws._1_2.wsdl.SSOSessionManager impl;
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
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "AccessSessionRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "AccessSessionRequestType"), org.josso.gateway.ws._1_2.protocol.AccessSessionRequestType.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("accessSession", _params, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "AccessSessionResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "AccessSessionResponseType"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "accessSession"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("accessSession") == null) {
            _myOperations.put("accessSession", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("accessSession")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NoSuchSessionErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "NoSuchSessionError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.NoSuchSessionErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "NoSuchSessionErrorType"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SSOSessionErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOSessionError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.SSOSessionErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOSessionErrorType"));
        _oper.addFault(_fault);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SessionRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SessionRequestType"), org.josso.gateway.ws._1_2.protocol.SessionRequestType.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getSession", _params, new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SessionResponse"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SessionResponseType"));
        _oper.setElementQName(new javax.xml.namespace.QName("", "getSession"));
        _myOperationsList.add(_oper);
        if (_myOperations.get("getSession") == null) {
            _myOperations.put("getSession", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getSession")).add(_oper);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("NoSuchSessionErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "NoSuchSessionError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.NoSuchSessionErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "NoSuchSessionErrorType"));
        _oper.addFault(_fault);
        _fault = new org.apache.axis.description.FaultDesc();
        _fault.setName("SSOSessionErrorFault");
        _fault.setQName(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOSessionError"));
        _fault.setClassName("org.josso.gateway.ws._1_2.protocol.SSOSessionErrorType");
        _fault.setXmlType(new javax.xml.namespace.QName("urn:org:josso:gateway:ws:1.2:protocol", "SSOSessionErrorType"));
        _oper.addFault(_fault);
    }

    public SSOSessionManagerSOAPBindingSkeleton() {
        this.impl = new org.josso.gateway.ws._1_2.wsdl.SSOSessionManagerSOAPBindingImpl();
    }

    public SSOSessionManagerSOAPBindingSkeleton(org.josso.gateway.ws._1_2.wsdl.SSOSessionManager impl) {
        this.impl = impl;
    }
    public org.josso.gateway.ws._1_2.protocol.AccessSessionResponseType accessSession(org.josso.gateway.ws._1_2.protocol.AccessSessionRequestType accessSessionRequest) throws java.rmi.RemoteException, org.josso.gateway.ws._1_2.protocol.NoSuchSessionErrorType, org.josso.gateway.ws._1_2.protocol.SSOSessionErrorType
    {
        org.josso.gateway.ws._1_2.protocol.AccessSessionResponseType ret = impl.accessSession(accessSessionRequest);
        return ret;
    }

    public org.josso.gateway.ws._1_2.protocol.SessionResponseType getSession(org.josso.gateway.ws._1_2.protocol.SessionRequestType sessionRequest) throws java.rmi.RemoteException, org.josso.gateway.ws._1_2.protocol.NoSuchSessionErrorType, org.josso.gateway.ws._1_2.protocol.SSOSessionErrorType
    {
        org.josso.gateway.ws._1_2.protocol.SessionResponseType ret = impl.getSession(sessionRequest);
        return ret;
    }

}