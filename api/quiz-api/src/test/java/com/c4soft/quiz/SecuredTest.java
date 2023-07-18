package com.c4soft.quiz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;

/**
 * Avoid MethodArgumentConversionNotSupportedException with repos MockBean
 *
 * @author Jérôme Wacongne &lt;ch4mp#64;c4-soft.com&gt;
 */
@AutoConfigureAddonsWebmvcResourceServerSecurity
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ SecurityConfig.class })
public @interface SecuredTest {

}