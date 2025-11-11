package com.finx.allocationreallocationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactUpdateRequestDTO {
    private String mobileNumber;
    private String alternateMobile;
    private String email;
    private String alternateEmail;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String updateType;
}
