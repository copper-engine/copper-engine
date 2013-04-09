
package de.scoopgmbh.network.mobile.services;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.scoopgmbh.network.mobile.services package. 
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

    private final static QName _ResetMailboxRequest_QNAME = new QName("http://services.mobile.network.scoopgmbh.de/", "resetMailboxRequest");
    private final static QName _SendSmsRequest_QNAME = new QName("http://services.mobile.network.scoopgmbh.de/", "sendSmsRequest");
    private final static QName _ResetMailboxAcknowledge_QNAME = new QName("http://services.mobile.network.scoopgmbh.de/", "resetMailboxAcknowledge");
    private final static QName _ResetMailboxResponse_QNAME = new QName("http://services.mobile.network.scoopgmbh.de/", "resetMailboxResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.scoopgmbh.network.mobile.services
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
     * Create an instance of {@link ResetMailboxAcknowledge }
     * 
     */
    public ResetMailboxAcknowledge createResetMailboxAcknowledge() {
        return new ResetMailboxAcknowledge();
    }

    /**
     * Create an instance of {@link SendSmsRequest }
     * 
     */
    public SendSmsRequest createSendSmsRequest() {
        return new SendSmsRequest();
    }

    /**
     * Create an instance of {@link Empty }
     * 
     */
    public Empty createEmpty() {
        return new Empty();
    }

    /**
     * Create an instance of {@link ResetMailboxRequest }
     * 
     */
    public ResetMailboxRequest createResetMailboxRequest() {
        return new ResetMailboxRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetMailboxRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.mobile.network.scoopgmbh.de/", name = "resetMailboxRequest")
    public JAXBElement<ResetMailboxRequest> createResetMailboxRequest(ResetMailboxRequest value) {
        return new JAXBElement<ResetMailboxRequest>(_ResetMailboxRequest_QNAME, ResetMailboxRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendSmsRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.mobile.network.scoopgmbh.de/", name = "sendSmsRequest")
    public JAXBElement<SendSmsRequest> createSendSmsRequest(SendSmsRequest value) {
        return new JAXBElement<SendSmsRequest>(_SendSmsRequest_QNAME, SendSmsRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetMailboxAcknowledge }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.mobile.network.scoopgmbh.de/", name = "resetMailboxAcknowledge")
    public JAXBElement<ResetMailboxAcknowledge> createResetMailboxAcknowledge(ResetMailboxAcknowledge value) {
        return new JAXBElement<ResetMailboxAcknowledge>(_ResetMailboxAcknowledge_QNAME, ResetMailboxAcknowledge.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetMailboxResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.mobile.network.scoopgmbh.de/", name = "resetMailboxResponse")
    public JAXBElement<ResetMailboxResponse> createResetMailboxResponse(ResetMailboxResponse value) {
        return new JAXBElement<ResetMailboxResponse>(_ResetMailboxResponse_QNAME, ResetMailboxResponse.class, null, value);
    }

}
