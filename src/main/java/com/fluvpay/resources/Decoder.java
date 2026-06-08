package com.fluvpay.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fluvpay.FluvPay;
import com.fluvpay.FluvPayError;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Converte a resposta JSON em modelos tipados e codifica segmentos de path. */
final class Decoder {

    private Decoder() {
    }

    static <T> T decode(FluvPay client, JsonNode node, Class<T> type) {
        try {
            return client.mapper().treeToValue(node, type);
        } catch (Exception ex) {
            throw new FluvPayError(
                    "Falha ao interpretar a resposta da FluvPay como "
                            + type.getSimpleName() + ".",
                    null, null, null, null, ex);
        }
    }

    /** Codifica um segmento de path para uso seguro na URL. */
    static String segment(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
