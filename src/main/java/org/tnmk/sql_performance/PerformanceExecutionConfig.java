package org.tnmk.sql_performance;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "performance-execution")
public class PerformanceExecutionConfig {
  /**
   * A round of testing will execute all queries.
   * (each query will be looped many times, each loop will run many parallel threads).
   * So this is the number of round will be executed.
   */
  private int roundCount;
  /**
   * In each round, a query will be repeatedly looped many times (and each loop will run many parallel threads).
   */
  private int loopCount;
  /**
   * In each loop, a query will be executed in many times in parallel.
   */
  private int parallelCount;

  /**
   * The first few loops will be run to warm up the system,
   * but they won't be counted into the performance result because the number of those rounds a not stable yet.
   * So it must be less than {@link #loopCount}.
   */
  private int skipLoop;
}
