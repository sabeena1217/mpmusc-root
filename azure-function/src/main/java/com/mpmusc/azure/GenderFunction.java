package com.mpmusc.azure;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.mpmusc.core.GenderDistributionRequest;
import com.mpmusc.core.GenderDistributionResult;
import com.mpmusc.core.GenderDistributionService;

import java.util.Optional;

public class GenderFunction {
    @FunctionName("genderDistribution")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<GenderDistributionRequest>> request,
            final ExecutionContext context
    ) {
        GenderDistributionService service = new GenderDistributionService();
        GenderDistributionResult result = service.analyze(request.getBody().orElse(null));

        return request.createResponseBuilder(HttpStatus.OK)
                .body(result)
                .build();
    }
}


/*

cd azure-function
mvn clean package        # builds the staging folder
# ls target/azure-functions/mpmusc-azure
mvn azure-functions:deploy


az functionapp config appsettings set \
  --name mpmusc-azure \
  --resource-group mpmusc-resources \
  --settings AzureWebJobsStorage="<connection-string-from-mpmusc-storage-account>"

 */

/*

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
      "Male": 30.0,
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
  "executionTimeMillis": 558
}
 */