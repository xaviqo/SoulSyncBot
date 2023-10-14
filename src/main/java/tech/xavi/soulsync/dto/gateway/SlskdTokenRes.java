package tech.xavi.soulsync.dto.gateway;

import lombok.*;

@NoArgsConstructor @AllArgsConstructor
@Data @Builder @ToString
public class SlskdTokenRes {
    String token;
    long expires;
    long issued;
}
