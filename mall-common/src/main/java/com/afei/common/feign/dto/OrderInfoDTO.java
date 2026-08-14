package com.afei.common.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderInfoDTO {
    private Long id;
    private String orderNo;
    private Long payAmount;
    private Integer status;
}
