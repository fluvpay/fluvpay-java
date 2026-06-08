package com.fluvpay.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 * Página de cobranças (envelope page/per_page/total/has_next/has_prev).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ChargesPage {

    @JsonProperty("data")
    private List<ChargeListItem> data;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("per_page")
    private Integer perPage;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("has_next")
    private Boolean hasNext;

    @JsonProperty("has_prev")
    private Boolean hasPrev;

    /** Itens desta página (nunca nulo; vazio quando ausente). */
    public List<ChargeListItem> getData() {
        return data == null ? Collections.emptyList() : data;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public Integer getTotal() {
        return total;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public Boolean getHasPrev() {
        return hasPrev;
    }
}
