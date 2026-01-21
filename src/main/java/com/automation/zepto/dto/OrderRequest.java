package com.automation.zepto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private List<String> products;

    @JsonProperty("upi_id")
    private String upiId;
}
