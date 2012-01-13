
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
package de.scoopgmbh.orchestration;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.scoopgmbh.orchestration package. 
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

    private final static QName _ResetMailbox_QNAME = new QName("http://orchestration.scoopgmbh.de/", "resetMailbox");
    private final static QName _ResetMailboxResponse_QNAME = new QName("http://orchestration.scoopgmbh.de/", "resetMailboxResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.scoopgmbh.orchestration
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
     * Create an instance of {@link ResetMailbox }
     * 
     */
    public ResetMailbox createResetMailbox() {
        return new ResetMailbox();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetMailbox }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orchestration.scoopgmbh.de/", name = "resetMailbox")
    public JAXBElement<ResetMailbox> createResetMailbox(ResetMailbox value) {
        return new JAXBElement<ResetMailbox>(_ResetMailbox_QNAME, ResetMailbox.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetMailboxResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://orchestration.scoopgmbh.de/", name = "resetMailboxResponse")
    public JAXBElement<ResetMailboxResponse> createResetMailboxResponse(ResetMailboxResponse value) {
        return new JAXBElement<ResetMailboxResponse>(_ResetMailboxResponse_QNAME, ResetMailboxResponse.class, null, value);
    }

}
