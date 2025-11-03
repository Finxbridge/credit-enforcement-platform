package com.finx.communication.domain.dto.email;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Msg91Recipient {
    private List<Msg91EmailTo> to;
    private Map<String, String> variables;
}
