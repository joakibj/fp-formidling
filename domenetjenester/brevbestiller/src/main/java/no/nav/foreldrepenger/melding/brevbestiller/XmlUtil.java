package no.nav.foreldrepenger.melding.brevbestiller;

import no.nav.foreldrepenger.melding.datamapper.DokumentBestillerFeil;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

public class XmlUtil {
    private XmlUtil() {

    }

    public static String elementTilString(Element element) {
        DOMImplementationLS lsImpl = (DOMImplementationLS) element.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
        return serializer.writeToString(element);
    }

    public static XMLGregorianCalendar finnDatoVerdiAvUtenTidSone(LocalDate dato) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                    dato.getYear(),
                    dato.getMonthValue(),
                    dato.getDayOfMonth(),
                    DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED);
        } catch (DatatypeConfigurationException e) {
            throw DokumentBestillerFeil.datokonverteringsfeil(dato.toString(), e);

        }
    }

    public static LocalDate finnDatoVerdiAv(XMLGregorianCalendar calendar) {
        return LocalDate.of(calendar.getYear(), calendar.getMonth(), calendar.getDay());
    }
}
