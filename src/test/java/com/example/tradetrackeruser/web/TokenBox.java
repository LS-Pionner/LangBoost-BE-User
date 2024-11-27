package com.example.tradetrackeruser.web;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenBox {

    private String authToken;
    private String refreshToken;

}