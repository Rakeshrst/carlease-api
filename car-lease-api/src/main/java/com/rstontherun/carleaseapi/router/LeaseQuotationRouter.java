package com.rstontherun.carleaseapi.router;

import com.rstontherun.carleaseapi.handler.LeaseQuotationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class LeaseQuotationRouter {

    @Bean
    public RouterFunction<ServerResponse> leaseQuotationRoutes(LeaseQuotationHandler handler) {
        return RouterFunctions.route()
                .GET("/api/lease/quote/quotationId/{quotationId}", handler::getLeaseQuotationByQuoteId)
                .GET("/api/lease/quote/customerEmail/{customerEmail}", handler::getLeaseQuotationByCustomerEmail)
                .POST("/api/lease/quote", handler::calculateLeaseRate)
                .build();
    }
}