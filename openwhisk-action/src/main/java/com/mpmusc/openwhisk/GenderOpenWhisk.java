package com.mpmusc.openwhisk;

import com.google.gson.JsonObject;
import com.mpmusc.core.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mpmusc.core.GenderDistributionRequest;
import com.mpmusc.core.GenderDistributionResult;
import com.mpmusc.core.GenderDistributionService;


public class GenderOpenWhisk {

    public static JsonObject main(JsonObject args) {
        Gson gson = new Gson();
        // Convert input JSON to GenderDistributionRequest
        GenderDistributionRequest input = gson.fromJson(args, GenderDistributionRequest.class);
        // Analyze the data
        GenderDistributionResult result = new GenderDistributionService().analyze(input);
        // Convert result object back to JsonObject
        return gson.toJsonTree(result).getAsJsonObject();
    }

}
