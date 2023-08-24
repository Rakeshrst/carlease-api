package com.rstontherun.carleaseapi.router;

import com.rstontherun.carleaseapi.handler.LeaseContractHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class LeaseContractRouter {

    @Bean
    public RouterFunction<ServerResponse> leaseContractRoutes(LeaseContractHandler handler) {
        return RouterFunctions.route()
                .GET("/api/lease/contract/contractId/{contractId}", handler::getLeaseContract)
                .GET("/api/lease/contract/customerEmail/{customerEmail}", handler::getLeaseContractByCustomerEmail)
                .POST("/api/lease/contract", handler::confirmContract)
                .POST("/api/lease/contract/endLease/{contractId}", handler::endLease)
                .build();
    }
}
