package com.thelastcodebenders.follower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTicketFormDTO {
    private String subject;
    private long orderNumber;
    private String customSubject;
    private String message;
}
