package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Catálogo de valores mágicos do sandbox e o comportamento simulado de cada um. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SandboxScenarios {

    @JsonProperty("info")
    private String info;

    @JsonProperty("scenarios")
    private List<Map<String, Object>> scenarios;

    /** Texto explicativo sobre os cenários do sandbox. */
    public String getInfo() {
        return info;
    }

    /** Lista de cenários (cada um um mapa livre). Nunca nulo; vazio quando ausente. */
    public List<Map<String, Object>> getScenarios() {
        return scenarios == null ? Collections.emptyList() : scenarios;
    }
}
