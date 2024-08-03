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
  private int loopCount;
  private int skipLoop;
  private int parallelCount;
}
