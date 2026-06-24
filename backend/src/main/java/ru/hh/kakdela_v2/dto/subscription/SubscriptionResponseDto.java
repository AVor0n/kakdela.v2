package ru.hh.kakdela_v2.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SubscriptionResponseDto {

    private List<String> subscribedEmails;
    private List<String> alreadySubscribedEmails; 
    private List<String> notFoundEmails;            

}
