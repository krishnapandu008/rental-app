package com.rental.location.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Area {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private List<String> streets;
}