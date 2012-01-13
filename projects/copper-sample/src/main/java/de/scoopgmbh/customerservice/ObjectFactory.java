
/*
 * Copyright 2002-2012 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.customerservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.scoopgmbh.customerservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ResetMailbox_QNAME = new QName("http://customerservice.scoopgmbh.de/", "resetMailbox");
    private final static QName _GetCustomersByName_QNAME = new QName("http://customerservice.scoopgmbh.de/", "getCustomersByName");
    private final static QName _GetCustomersByNameResponse_QNAME = new QName("http://customerservice.scoopgmbh.de/", "getCustomersByNameResponse");
    private final static QName _NoSuchCustomer_QNAME = new QName("http://customerservice.scoopgmbh.de/", "NoSuchCustomer");
    private final static QName _ResetMailboxResponse_QNAME = new QName("http://customerservice.scoopgmbh.de/", "resetMailboxResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.scoopgmbh.customerservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResetMailboxResponse }
     * 
     */
    public ResetMailboxResponse createResetMailboxResponse() {
        return new ResetMailboxResponse();
    }

    /**
     * Create an instance of {@link NoSuchCustomer }
     * 
     */
    public NoSuchCustomer createNoSuchCustomer() {
        return new NoSuchCustomer();
    }

    /**
     * Create an instance of {@link GetCustomersByNameResponse }
     * 
     */
    public GetCustomersByNameResponse createGetCustomersByNameResponse() {
        return new GetCustomersByNameResponse();
    }

    /**
     * Create an instance of {@link GetCustomersByName }
     * 
     */
    public GetCustomersByName createGetCustomersByName() {
        return new GetCustomersByName();
    }

    /**
     * Create an instance of {@link ResetMailbox }
     * 
     */
    public ResetMailbox createResetMailbox() {
        return new ResetMailbox();
    }

    /**
     * Create an instance of {@link Customer }
     * 
     */
    public Customer createCustomer() {
        return new Customer();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetMailbox }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://customerservice.scoopgmbh.de/", name = "resetMailbox")
    public JAXBElement<ResetMailbox> createResetMailbox(ResetMailbox value) {
        return new JAXBElement<ResetMailbox>(_ResetMailbox_QNAME, ResetMailbox.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomersByName }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://customerservice.scoopgmbh.de/", name = "getCustomersByName")
    public JAXBElement<GetCustomersByName> createGetCustomersByName(GetCustomersByName value) {
        return new JAXBElement<GetCustomersByName>(_GetCustomersByName_QNAME, GetCustomersByName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomersByNameResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://customerservice.scoopgmbh.de/", name = "getCustomersByNameResponse")
    public JAXBElement<GetCustomersByNameResponse> createGetCustomersByNameResponse(GetCustomersByNameResponse value) {
        return new JAXBElement<GetCustomersByNameResponse>(_GetCustomersByNameResponse_QNAME, GetCustomersByNameResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NoSuchCustomer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://customerservice.scoopgmbh.de/", name = "NoSuchCustomer")
    public JAXBElement<NoSuchCustomer> createNoSuchCustomer(NoSuchCustomer value) {
        return new JAXBElement<NoSuchCustomer>(_NoSuchCustomer_QNAME, NoSuchCustomer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetMailboxResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://customerservice.scoopgmbh.de/", name = "resetMailboxResponse")
    public JAXBElement<ResetMailboxResponse> createResetMailboxResponse(ResetMailboxResponse value) {
        return new JAXBElement<ResetMailboxResponse>(_ResetMailboxResponse_QNAME, ResetMailboxResponse.class, null, value);
    }

}
