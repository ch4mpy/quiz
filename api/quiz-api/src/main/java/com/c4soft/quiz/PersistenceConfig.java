/* (C)2024 */
package com.c4soft.quiz;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableSpringDataWebSupport
@EnableJpaRepositories
@EntityScan
public class PersistenceConfig {
}
