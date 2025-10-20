package com.mpmusc.openwhisk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Main {

    //    public static void main(String[] args) throws Exception {
//        // read all of STDIN into a String
//        String input = new BufferedReader(new InputStreamReader(System.in))
//                .lines().collect(Collectors.joining("\n"));
//
//        // parse JSON → call your existing action logic
//        Gson gson = new Gson();
//        JsonObject req = gson.fromJson(input, JsonObject.class);
//        JsonObject res = GenderOpenWhisk.main(req);
//
//        // write result JSON to STDOUT
//        System.out.print(gson.toJson(res));
//    }
//    public static void main(String[] args) {
//        try {
//            String input = new BufferedReader(new InputStreamReader(System.in))
//                    .lines().collect(Collectors.joining("\n"));
////            System.err.println("Sabeena input: " + input + "eod");
//
//            JsonObject req;
//            if (input.trim().isEmpty()) {
//                req = new JsonObject();
//            } else {
//                req = new Gson().fromJson(input, JsonObject.class);
//            }
////            System.err.println("Sabeena req: " + req + "eod");
//
//            JsonObject res = GenderOpenWhisk.main(req);
//            System.out.print(res.toString());
//
//            //  ← **Add this** to kill any stray threads and exit immediately
//            // Kill the JVM so the container exits immediately
//            System.exit(0);
//
//        } catch (Exception e) {
//            System.err.println("sabeena ERROR: " + e.getMessage());
//            System.exit(1);
//        }
//    }
}