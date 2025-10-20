package com.mpmusc.core;

/**
 * Request object for gender distribution analysis.
 * Assumes the file is always located in the resources directory.
 */
public class GenderDistributionRequest {
    private String filename; // Must refer to a file in resources/

    public GenderDistributionRequest() {
    }

    public GenderDistributionRequest(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
