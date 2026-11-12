package com.example.epmc.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grant {
    private String id;
    private String title;
    private String funder;
    private String institution;
    private String link;
    private PublicationStatus publicationStatus;
    private String error;
}
