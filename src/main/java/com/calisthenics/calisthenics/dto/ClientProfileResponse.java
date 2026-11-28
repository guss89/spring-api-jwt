package com.calisthenics.calisthenics.dto;

import com.calisthenics.calisthenics.model.ClientProfile;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientProfileResponse {

    private Long id;
    private Long userId;
    private LocalDate birthDate;
    private BigDecimal initialWeight;
    private BigDecimal initialHeight;
    private LocalDateTime createdAt;

    public static ClientProfileResponse from(ClientProfile profile) {
        return new ClientProfileResponse(profile.getId(), profile.getUser().getId(), profile.getBirthDate(),
                profile.getInitialWeight(), profile.getInitialHeight(), profile.getCreatedAt());
    }
}
