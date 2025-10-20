package com.mpmusc.controller;

import com.mpmusc.dto.StartingRttRequest;
import com.mpmusc.service.InvocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GenderController {

    //    private final Map<String, FunctionInvoker> invokers;
    private final InvocationService service;

//    @Autowired
//    public GenderController(Map<String, FunctionInvoker> invokers) {
//        this.invokers = invokers;
//    }

//    @GetMapping("/analyze")
//    public ResponseEntity<String> analyze(@RequestParam String provider) {
//        FunctionInvoker invoker = invokers.get(provider.toLowerCase());
//        if (invoker == null) {
//            return ResponseEntity.badRequest()
//                    .body("Unknown provider: " + provider);
//        }
//        try {
//            String result = invoker.invoke("fake_employees_100k.csv");
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                    .body("Invocation error: " + e.getMessage());
//        }
//    }

    @GetMapping("/analyze")
    public ResponseEntity<?> analyze(
            @RequestParam(required = false) String provider,
            @RequestParam(defaultValue = "1") int count) {
        List<String> result = service.analyze(provider, count);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestParam String scheduleType) {
        return ResponseEntity.ok(service.schedule(scheduleType));
    }

    @GetMapping("/graphSingleFunctionRequest")
    public ResponseEntity<?> graphSingleFunctionRequest() {
        return ResponseEntity.ok(service.benchmarkProviders_singleFunctionRequest());
    }

    @GetMapping("/graphParallelFunctionRequests")
    public ResponseEntity<?> graphParallelFunctionRequests(
            @RequestParam String provider,
            @RequestParam(defaultValue = "3") int count) {
        return ResponseEntity.ok(service.benchmarkProviders_parallelFunctionRequests(provider,count));
    }

//    @PostMapping("/smartSchedule")
//    public ResponseEntity<?> smartSchedule(
//            @RequestBody StartingRttRequest request,
//            @RequestParam Integer concurrency) {
//        return ResponseEntity.ok(service.smartSchedule((StartingRttRequest) request.getStartingRtts(), (Integer) concurrency));
//    }
//
//    @PostMapping("/smartSchedule")
//    public ResponseEntity<String> smartSchedule(
//            @RequestParam(defaultValue = "1")  int concurrency) {
//        return ResponseEntity.ok(service.smartSchedule(concurrency));
//    }

}