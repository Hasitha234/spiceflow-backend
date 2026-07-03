package com.spiceflow.backend.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(packages = "com.spiceflow.backend", importOptions = {ImportOption.DoNotIncludeTests.class})
public class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_only_access_services = classes()
        .that().resideInAPackage("..controller..")
        .should().onlyAccessClassesThat().resideInAnyPackage(
            "..controller..", "..service..", "..dto..", "..common..",
            "java..", "org.springframework..", "..security..", "org.slf4j..",
            "jakarta..", "..auth.entity..")
        .because("Controllers should not bypass services and access repositories or entities directly.");

    @ArchTest
    static final ArchRule services_should_not_access_controllers = noClasses()
        .that().resideInAPackage("..service..")
        .should().accessClassesThat().resideInAPackage("..controller..")
        .because("Services should not know about the web layer.");

    @ArchTest
    static final ArchRule repositories_should_not_depend_on_web = noClasses()
        .that().resideInAPackage("..repository..")
        .should().accessClassesThat().resideInAnyPackage("..controller..", "..security.config..")
        .because("Repositories should not depend on web or security configuration layers.");

    @ArchTest
    static final ArchRule no_field_injection = noFields()
        .should().beAnnotatedWith(Autowired.class)
        .because("Field injection is forbidden. Use constructor injection instead.");

    /**
     * Controllers may receive the User entity as an @AuthenticationPrincipal argument because
     * Spring Security injects it — that is an acceptable coupling. All other entity types must
     * never leak into the controller layer; services should return DTOs instead.
     */
    @ArchTest
    static final ArchRule entities_should_not_be_exposed_outside_service_layer = noClasses()
        .that().resideInAPackage("..controller..")
        .should().dependOnClassesThat(
            resideInAPackage("..entity..").and(not(assignableTo(UserDetails.class))))
        .because("Entities should never leak to controllers. Return mapped DTOs from services.");
}
