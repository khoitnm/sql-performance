package org.tnmk.sql_performance.measure_performance;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqlPerformanceMeasurement {

  private final JdbcTemplate jdbcTemplate;

  public void executeQueries(List<QueryExecution> executions,int loopCount, int parallelCount, int skipReportSomeFirstRounds) {
    if (loopCount <= 0) {
      throw new IllegalArgumentException("loopCount %s is invalid, it must be greater than 0.".formatted(loopCount));
    }
    if (skipReportSomeFirstRounds >= loopCount) {
      throw new IllegalArgumentException("skipReportSomeFirstRounds %s must be less than loopCount %s.".formatted(skipReportSomeFirstRounds, loopCount));
    }
    executions.forEach(execution -> {
      String script = execution.getQuery();
      Object[] params = execution.getParams().toArray();
      executeQuery(loopCount, parallelCount, skipReportSomeFirstRounds, script, params);

      // Let the server cool down a bit before executing the next query.
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    });
  }

  /**
   * @param skipReportSomeFirstRounds Usually, in the first few rounds, it requires sometimes to establish connections, hence it runs much slower.
   *                                  Later rounds usually will just reuse connection pool, hence will be faster.
   *                                  Therefore, we usually don't want to include the first few rounds in the final report.
   * @param query                     Example: `{ CALL some_store_procedure(?, ?)}`
   * @param params                    Example: `"someValue", 2`
   */
  private void executeQuery(int loopCount, int parallelCount, int skipReportSomeFirstRounds, String query, Object... params) {
    List<Long> allRunTimes = new ArrayList<>(loopCount * parallelCount);
    IntStream.range(0, loopCount).forEach(i -> {
      List<Long> runTimes = executeScriptInParallel(parallelCount, query, params);
      if (i >= skipReportSomeFirstRounds) {
        allRunTimes.addAll(runTimes);
      }// else: skip it and don't add it into the result.
    });
    logExecutionTimes(query, allRunTimes);
  }

  private List<Long> executeScriptInParallel(int parallelCount, String script, Object[] params) {
    List<Long> executionTimes = IntStream.range(0, parallelCount).parallel().mapToObj(i -> {
      long startTime = System.currentTimeMillis();
      try {
        jdbcTemplate.call(connection -> {
          CallableStatement callableStatement = connection.prepareCall(script);
          for (int j = 0; j < params.length; j++) {
            callableStatement.setObject(j + 1, params[j]);
          }
          return callableStatement;
        }, new ArrayList<>());
      } catch (RuntimeException e) {
        log.error("Error when executing script: {}", script, e);
      }
      long endTime = System.currentTimeMillis();
      return endTime - startTime;
    }).collect(Collectors.toList());
    return executionTimes;
  }

  private void logExecutionTimes(String queryName, List<Long> executionTimes) {
    long totalRuntime = executionTimes.stream().mapToLong(Long::longValue).sum();
    double averageRuntime = (double) totalRuntime / executionTimes.size();

    log.info("""
        Execution {}: Average: {} ms, 90th percentile: {} ms, 80th percentile: {} ms""",
      queryName,
      averageRuntime,
      percentile(executionTimes, 90).orElse(-1L),
      percentile(executionTimes, 80).orElse(-1L)
    );
  }

  private Optional<Long> percentile(List<Long> executionTimes, double percentile) {
    List<Long> sortedExecutionTimes = executionTimes.stream().sorted().toList();
    int index = (int) Math.ceil(percentile / 100.0 * sortedExecutionTimes.size()) - 1;
    if (index < 0 || index >= sortedExecutionTimes.size()) {
      return Optional.empty();
    } else {
      return Optional.of(sortedExecutionTimes.get(index));
    }
  }
}
