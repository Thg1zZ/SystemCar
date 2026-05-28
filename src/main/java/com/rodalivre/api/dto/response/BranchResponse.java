package com.rodalivre.api.dto.response;

import com.rodalivre.domain.entity.Branch;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BranchResponse {
    private UUID id;
    private String name;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private Boolean active;

    public static BranchResponse fromEntity(Branch branch) {
        BranchResponse response = new BranchResponse();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setStreet(branch.getStreet());
        response.setCity(branch.getCity());
        response.setState(branch.getState());
        response.setZipCode(branch.getZipCode());
        response.setLatitude(branch.getLatitude());
        response.setLongitude(branch.getLongitude());
        response.setPhone(branch.getPhone());
        response.setActive(branch.getActive());
        return response;
    }
}
