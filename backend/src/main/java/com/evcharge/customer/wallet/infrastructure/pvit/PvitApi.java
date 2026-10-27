package com.evcharge.customer.wallet.infrastructure.pvit;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.function.Function;

class PvitApi {

    private static final int EXCERPT_LIMIT = 400;

    private final RestClient client;
    private final PvitSecretProvider secrets;
    private final PvitProperties properties;
    private final ObjectMapper mapper;

    PvitApi(RestClient client, PvitSecretProvider secrets, PvitProperties properties, ObjectMapper mapper) {
        this.client = client;
        this.secrets = secrets;
        this.properties = properties;
        this.mapper = mapper;
    }

    PvitStatus startPayment(String reference, BigDecimal amount, String operatorCode, String customerNumber,
                            String description) {
        ObjectNode body = mapper.createObjectNode();
        body.put("agent", properties.agent());
        body.put("amount", amount);
        body.put("product", properties.product());
        body.put("reference", reference);
        body.put("service", "RESTFUL");
        body.put("callback_url_code", properties.callbackUrlCode());
        body.put("customer_account_number", customerNumber);
        body.put("merchant_operation_account_code", properties.operationAccountCode());
        body.put("transaction_type", "PAYMENT");
        body.put("owner_charge", properties.ownerCharge());
        body.put("owner_charge_operator", "CUSTOMER");
        body.put("free_info", description);
        body.put("operator_code", operatorCode);
        JsonNode json = withSecret(secret -> client.post().uri("/v2/{code}/rest", properties.codeUrlPayment())
                .header("X-Secret", secret).header("X-Callback-MediaType", MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON).body(body.toString()).retrieve().body(String.class));
        return PvitStatus.from(json.path("status").asString(null));
    }

    PvitTransaction transaction(String reference) {
        JsonNode json = withSecret(secret -> client.get()
                .uri("/{code}/status?transactionId={id}&accountOperationCode={account}&transactionOperation=PAYMENT",
                        properties.codeUrlStatus(), reference, properties.operationAccountCode())
                .header("X-Secret", secret).retrieve().body(String.class));
        return new PvitTransaction(PvitStatus.from(json.path("status").asString(null)),
                json.path("merchant_reference_id").asString(null), json.path("reference_id").asString(null),
                decimal(json.path("amount")));
    }

    private JsonNode withSecret(Function<String, String> call) {
        try {
            return mapper.readTree(call.apply(secrets.current()));
        } catch (RestClientResponseException e) {
            if (isRejectedKey(e.getStatusCode())) {
                secrets.invalidate();
                return attempt(call);
            }
            throw failure(e);
        } catch (RestClientException e) {
            throw new PvitException("The provider could not be reached", e);
        }
    }

    private JsonNode attempt(Function<String, String> call) {
        try {
            return mapper.readTree(call.apply(secrets.current()));
        } catch (RestClientResponseException e) {
            throw failure(e);
        } catch (RestClientException e) {
            throw new PvitException("The provider could not be reached", e);
        }
    }

    private static boolean isRejectedKey(HttpStatusCode status) {
        return status.value() == 401 || status.value() == 403;
    }

    private static PvitException failure(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        return new PvitException("The provider answered " + status + ": " + excerpt(e),
                status == 400 || status == 422, status == 404);
    }

    private static String excerpt(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        return body.length() > EXCERPT_LIMIT ? body.substring(0, EXCERPT_LIMIT) : body;
    }

    private static BigDecimal decimal(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        try {
            return new BigDecimal(node.asString("0"));
        } catch (NumberFormatException e) {
            throw new PvitException("The provider sent an amount that cannot be read", e);
        }
    }
}
