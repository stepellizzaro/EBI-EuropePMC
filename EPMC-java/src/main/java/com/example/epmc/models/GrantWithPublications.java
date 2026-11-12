package com.example.epmc.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrantWithPublications {
    private Grant grant;
    private List<Publication> publications;
}
