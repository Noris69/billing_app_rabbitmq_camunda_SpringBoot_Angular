package ma.atos.billing.invoice.billing_invoice.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class PointDeVenteExportRoute extends RouteBuilder {

    public static final String POINT_DE_VENTE_CREATED_ENDPOINT = "direct:point-de-vente-created";

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("Export JSON point de vente echoue : ${exception.message}");

        from(POINT_DE_VENTE_CREATED_ENDPOINT)
                .routeId("point-de-vente-created-json-export")
                .setHeader(
                        Exchange.FILE_NAME,
                        simple("point-de-vente-${body.id}-${date:now:yyyyMMddHHmmssSSS}.json")
                )
                .marshal().json(JsonLibrary.Jackson)
                .to("file:exports/points-de-vente?charset=utf-8")
                .log("Point de vente ${header.CamelFileName} exporte en JSON");
    }
}
