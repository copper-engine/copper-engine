
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

    private final static QName _GetCustomersByMsisdnResponse_QNAME = new QName("http://customerservice.scoopgmbh.de/", "getCustomersByMsisdnResponse");
    private final static QName _GetCustomersByMsisdnRequest_QNAME = new QName("http://customerservice.scoopgmbh.de/", "getCustomersByMsisdnRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.scoopgmbh.customerservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetCustomersByMsisdnRequest }
     * 
     */
    public GetCustomersByMsisdnRequest createGetCustomersByMsisdnRequest() {
        return new GetCustomersByMsisdnRequest();
    }

    /**
     * Create an instance of {@link GetCustomersByMsisdnResponse }
     * 
     */
    public GetCustomersByMsisdnResponse createGetCustomersByMsisdnResponse() {
        return new GetCustomersByMsisdnResponse();
    }

    /**
     * Create an instance of {@link Customer }
     * 
     */
    public Customer createCustomer() {
        return new Customer();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomersByMsisdnResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://customerservice.scoopgmbh.de/", name = "getCustomersByMsisdnResponse")
    public JAXBElement<GetCustomersByMsisdnResponse> createGetCustomersByMsisdnResponse(GetCustomersByMsisdnResponse value) {
        return new JAXBElement<GetCustomersByMsisdnResponse>(_GetCustomersByMsisdnResponse_QNAME, GetCustomersByMsisdnResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomersByMsisdnRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://customerservice.scoopgmbh.de/", name = "getCustomersByMsisdnRequest")
    public JAXBElement<GetCustomersByMsisdnRequest> createGetCustomersByMsisdnRequest(GetCustomersByMsisdnRequest value) {
        return new JAXBElement<GetCustomersByMsisdnRequest>(_GetCustomersByMsisdnRequest_QNAME, GetCustomersByMsisdnRequest.class, null, value);
    }

}
