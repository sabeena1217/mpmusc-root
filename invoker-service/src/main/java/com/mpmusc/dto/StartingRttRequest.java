package com.mpmusc.dto;

import lombok.Data;

import java.util.Map;

@Data
public class StartingRttRequest {
    private Map<String, Map<Integer, Long>> startingRtts;
}
