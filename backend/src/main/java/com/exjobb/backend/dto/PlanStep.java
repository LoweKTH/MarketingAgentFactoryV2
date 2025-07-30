package com.exjobb.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public record PlanStep(String tool, Map<String, String> parameters) {


}
