package org.tnmk.sql_performance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.tnmk.sql_performance.measure_performance.ScriptConfig;
import org.tnmk.sql_performance.measure_performance.SqlPerformanceMeasurement;

import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqlPerformanceStartupRunner implements CommandLineRunner {

  private final SqlPerformanceMeasurement sqlPerformanceMeasurement;
  private final ScriptConfig scriptConfig;
  private final PerformanceExecutionConfig performanceExecutionConfig;

  @Override
  public void run(String... args) {
    IntStream.range(0, 10).forEach(i -> {
      log.info("\nRound {}...", i);
      sqlPerformanceMeasurement.executeQueries(
        scriptConfig.getExecutions(),
        performanceExecutionConfig.getLoopCount(),
        performanceExecutionConfig.getParallelCount(),
        performanceExecutionConfig.getSkipLoop()
      );
    });
  }
}
