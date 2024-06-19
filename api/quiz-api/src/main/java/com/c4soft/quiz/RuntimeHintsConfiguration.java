package com.c4soft.quiz;

import java.util.List;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import liquibase.database.LiquibaseTableNamesFactory;
import liquibase.report.ShowSummaryGeneratorFactory;
import liquibase.ui.LoggerUIService;

@Configuration
@ImportRuntimeHints({RuntimeHintsConfiguration.LiquibaseRuntimeHints.class})
public class RuntimeHintsConfiguration {

  static class LiquibaseRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
      List.of(LoggerUIService.class, LiquibaseTableNamesFactory.class,
          ShowSummaryGeneratorFactory.class)
          .forEach(clazz -> hints.reflection().registerType(clazz,
              MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));
    }
  }
}
