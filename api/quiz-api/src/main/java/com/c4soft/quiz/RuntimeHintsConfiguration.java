package com.c4soft.quiz;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import com.c4soft.quiz.domain.Quiz_;
import liquibase.changelog.visitor.ValidatingVisitorGeneratorFactory;
import liquibase.database.LiquibaseTableNamesFactory;
import liquibase.report.ShowSummaryGeneratorFactory;
import liquibase.ui.LoggerUIService;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ImportRuntimeHints({RuntimeHintsConfiguration.LiquibaseRuntimeHints.class,
    RuntimeHintsConfiguration.JpamodelgenRuntimeHints.class})
@Slf4j
public class RuntimeHintsConfiguration {

  static class LiquibaseRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
      List.of(LoggerUIService.class, LiquibaseTableNamesFactory.class,
          ShowSummaryGeneratorFactory.class, ValidatingVisitorGeneratorFactory.class)
          .forEach(clazz -> hints.reflection().registerType(clazz,
              MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));
    }
  }

  static class JpamodelgenRuntimeHints implements RuntimeHintsRegistrar {
    private static final String METAMODEL_PACKAGE = Quiz_.class.getPackageName();

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
      InputStream stream = ClassLoader.getSystemClassLoader()
          .getResourceAsStream((METAMODEL_PACKAGE.replaceAll("\\.", "/")));
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      reader.lines().filter(line -> line.endsWith("_.class")).map(line -> {
        try {
          return Class.forName("%s.%s".formatted(METAMODEL_PACKAGE, line.split("\\.")[0]));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }).forEach(clazz -> {
        log.info("Adding native hints for {}", clazz.getName());
        hints.reflection().registerType(clazz, MemberCategory.PUBLIC_FIELDS);
      });
    }
  }
}
