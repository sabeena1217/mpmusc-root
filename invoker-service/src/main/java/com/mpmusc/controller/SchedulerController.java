package com.mpmusc.controller;

import com.mpmusc.dto.ScheduleResult;
import com.mpmusc.invoker.FunctionInvoker;
import com.mpmusc.service.SmartSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SchedulerController {

    private final SmartSchedulerService schedulerService;
    // You must provide the map of provider invokers (wired in your config)
    private final Map<String, FunctionInvoker> invokers;

    public SchedulerController(SmartSchedulerService schedulerService,
                               Map<String, FunctionInvoker> invokers) {
        this.schedulerService = schedulerService;
        this.invokers = invokers;
    }

//    @PostMapping("/smartSchedule")
//    public ResponseEntity<ScheduleResult> smartSchedule(@RequestParam(defaultValue = "1") int concurrency) {
//        ScheduleResult result = schedulerService.computeOptimalAllocation(concurrency, invokers);
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/smartSchedule")
    public ResponseEntity<ScheduleResult> smartSchedule(@RequestParam(defaultValue = "1") int concurrency) {
        // invokers map is injected as before
        ScheduleResult result = schedulerService.computeAndExecute(concurrency, invokers, "fake_employees_100k.csv");
        return ResponseEntity.ok(result);
    }
}


//
//1 & 1080 & 940 & 685 \\
//2 & 1122 & 966	 & 719  \\
//3 & 1079 & 1098 & 	607 \\
//4 & 1081 & 910	 & 571  \\
//5 & 1087 & 904	 & 618  \\
//6 & 1077 & 933	 & 583  \\
//7 & 1087 & 892	 & 750  \\
//8 & 1087 & 925	 & 561  \\
//9 & 1099 & 910	 & 600  \\
//10 & 1079 & 912	 & 612  \\
//11 & 1086 & 1085 & 	577 \\
//12 & 1130 & 965	 & 563  \\
//13 & 1109 & 931	 & 634  \\
//14 & 1081 & 906	 & 559  \\
//15 & 1087 & 1038 & 	615 \\
//16 & 1082 & 931	 & 840  \\
//17 & 1098 & 917	 & 583  \\
//18 & 1088 & 963	 & 603  \\
//19 & 1075 & 907	 & 714  \\
//20 & 1077 & 893	 & 645  \\
//21 & 1129 & 974	 & 808  \\
//22 & 1076 & 935	 & 580  \\
//23 & 1089 & 912	 & 624  \\
//24 & 1106 & 941	 & 566  \\
//25 & 1143 & 960	 & 731  \\
//26 & 1091 & 937	 & 590  \\
//27 & 1105 & 944	 & 644  \\
//28 & 1115 & 944	 & 886  \\
//29 & 1087 & 932	 & 600  \\
//30 & 1127 & 949	 & 578  \\
//31 & 1085 & 960	 & 625  \\
//32 & 1082 & 899	 & 600  \\
//33 & 1103 & 969	 & 598  \\
//34 & 1105 & 958	 & 587  \\
//35 & 1082 & 900	 & 627  \\
//36 & 1119 & 980	 & 684  \\
//37 & 1093 & 977	 & 589  \\
//38 & 1093 & 930	 & 548  \\
//39 & 1082 & 986	 & 593  \\
//40 & 1095 & 930	 & 578 \\