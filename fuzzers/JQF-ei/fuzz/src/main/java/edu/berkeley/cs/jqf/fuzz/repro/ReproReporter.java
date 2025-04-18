package edu.berkeley.cs.jqf.fuzz.repro;

import com.influxdb.client.*;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class ReproReporter {

    String testClassName;
    String testMethodName;
    String fuzzer;
    String repetition;
    String token;
    String username;
    String password;
    String experiment;
    String dbLocation;
    String orgName;
    Instant startTime;
    Connection connection;

    WatchService watcher;
    Map<WatchKey, Path> keys = new HashMap<>();
    Set<String> allBranchCovered = new HashSet<>();

    private void register(String path) throws IOException {
        Path p = Paths.get(path);
        WatchKey key = p.register(watcher, ENTRY_CREATE);
        keys.put(key, p);
        System.out.println("Start monitoring: " + p.normalize());
    }

    public void processEvents() throws IOException, ClassNotFoundException, SQLException {
        // Run the Junit test
        for (;;) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                return;
            }
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                Path name = (Path) event.context();
                Path fullPath = dir.resolve(name);

                ReproGuidance guidance = new ReproGuidance(fullPath.toFile(), (File) null);
                GuidedFuzzing.unsetGuidance();
                GuidedFuzzing.run(testClassName, testMethodName, guidance, null);


                Set<String> newCoverage = guidance.getBranchesCovered();
                System.out.println("New covered: " + newCoverage.size());
                newCoverage.removeAll(allBranchCovered);
                Instant now = Instant.now();
                allBranchCovered.addAll(newCoverage);
                if (newCoverage.size() != 0) {
                    List<String> data = Arrays.asList(testClassName, testMethodName, fuzzer, repetition, experiment,
                            String.valueOf(allBranchCovered.size()),
                            String.valueOf(now.toEpochMilli() - startTime.toEpochMilli())).stream().map(
                                    it -> "'" + it +"'"
                    ).collect(Collectors.toList());
                    Statement stmt = connection.createStatement();
                    String sql = "insert into ei_results (\"CLASS_NAME\", \"METHOD_NAME\", \"FUZZER\", " +
                            "\"REPETITION\", " +
                            "\"EXPERIMENT\", " +
                            "\"TOTAL\", \"TIME\") VALUES(" + String.join(",", data) +  ")";
                    System.out.println(sql);
                    stmt.executeUpdate(sql);
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    InfluxDBClient influxDBClient;
    public ReproReporter(String args[]) throws IOException, ClassNotFoundException, SQLException {
        testClassName  = args[0];
        testMethodName = args[1];
        fuzzer = args[2];
        repetition = args[3];
        experiment = args[4];


        dbLocation = args[6];
        username = args[7];
        password = args[8];

        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(dbLocation, username, password);


        watcher = FileSystems.getDefault().newWatchService();
        startTime = Instant.now();

        register(args[5] + "/corpus");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        if (args.length < 3) {
            System.err.println("Usage: java " + ReproReporter.class + " TEST_CLASS TEST_METHOD FUZZER " +
                    "APPLICATION REPETITION TOKEN ORG BUCKET SEED_FOLDER");
            System.exit(1);
        }
        ReproReporter reporter = new ReproReporter(args);
        System.out.println("Reporter initialized!");
        reporter.processEvents();
    }
}
