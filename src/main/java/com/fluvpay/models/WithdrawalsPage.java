package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/** Página de saques (envelope limit/offset/total). */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WithdrawalsPage {

    @JsonProperty("data")
    private List<Withdrawal> data;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("total")
    private Integer total;

    /** Itens desta página (nunca nulo; vazio quando ausente). */
    public List<Withdrawal> getData() {
        return data == null ? Collections.emptyList() : data;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getTotal() {
        return total;
    }
}
