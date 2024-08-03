package org.tnmk.sql_performance.measure_performance;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "executions")
public class QueryExecution {
  private String query;
  private List<Object> params;
}
