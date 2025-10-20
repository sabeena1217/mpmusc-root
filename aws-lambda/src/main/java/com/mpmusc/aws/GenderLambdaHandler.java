package com.mpmusc.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.mpmusc.core.*;

public class GenderLambdaHandler implements RequestHandler<GenderDistributionRequest, GenderDistributionResult> {
    public GenderDistributionResult handleRequest(GenderDistributionRequest input, Context context) {
        return new GenderDistributionService().analyze(input);
    }
}

    /*

    {"distribution":{"Engineering":{"Female":22.29,"Male":75.27,"Non-binary":2.44},"Education":{"Female":53.67,"Male":40.39,"Non-binary":5.95},"Finance":{"Male":50.44,"Female":44.53,"Non-binary":5.03},"Legal":{"Male":50.21,"Female":44.48,"Non-binary":5.31},"Healthcare":{"Male":30.0,"Female":63.3,"Non-binary":6.7},"HR":{"Male":30.43,"Female":63.24,"Non-binary":6.33},"IT":{"Male":69.46,"Female":27.45,"Non-binary":3.09},"Manufacturing":{"Female":18.04,"Male":79.97,"Non-binary":1.99},"Marketing":{"Female":45.33,"Male":49.91,"Non-binary":4.76}},"executionTimeMillis":3178}
{
  "distribution": {
    "Engineering": {
      "Female": 22.29,
      "Male": 75.27,
      "Non-binary": 2.44
    },
    "Education": {
      "Female": 53.67,
      "Male": 40.39,
      "Non-binary": 5.95
    },
    "Finance": {
      "Male": 50.44,
      "Female": 44.53,
      "Non-binary": 5.03
    },
    "Legal": {
      "Male": 50.21,
      "Female": 44.48,
      "Non-binary": 5.31
    },
    "Healthcare": {
      "Male": 30,
      "Female": 63.3,
      "Non-binary": 6.7
    },
    "HR": {
      "Male": 30.43,
      "Female": 63.24,
      "Non-binary": 6.33
    },
    "IT": {
      "Male": 69.46,
      "Female": 27.45,
      "Non-binary": 3.09
    },
    "Manufacturing": {
      "Female": 18.04,
      "Male": 79.97,
      "Non-binary": 1.99
    },
    "Marketing": {
      "Female": 45.33,
      "Male": 49.91,
      "Non-binary": 4.76
    }
  },
  "executionTimeMillis": 3178
}

{"distribution":{"Engineering":{"Female":22.29,"Male":75.27,"Non-binary":2.44},"Education":{"Female":53.67,"Male":40.39,"Non-binary":5.95},"Finance":{"Male":50.44,"Female":44.53,"Non-binary":5.03},"Legal":{"Male":50.21,"Female":44.48,"Non-binary":5.31},"Healthcare":{"Male":30.0,"Female":63.3,"Non-binary":6.7},"HR":{"Male":30.43,"Female":63.24,"Non-binary":6.33},"IT":{"Male":69.46,"Female":27.45,"Non-binary":3.09},"Manufacturing":{"Female":18.04,"Male":79.97,"Non-binary":1.99},"Marketing":{"Female":45.33,"Male":49.91,"Non-binary":4.76}},"executionTimeMillis":527}
    */

