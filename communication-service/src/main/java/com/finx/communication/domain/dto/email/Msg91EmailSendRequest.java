package com.finx.communication.domain.dto.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Msg91EmailSendRequest {
    private List<Msg91Recipient> recipients;
    private Msg91From from;
    private String domain;
    @JsonProperty("template_id")
    private String templateId;
    private List<Msg91ReplyTo> replyTo;
    private List<Msg91Attachment> attachments = new ArrayList<>();
}