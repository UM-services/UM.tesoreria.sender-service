package um.tesoreria.sender.hexagonal;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Reglas de la arquitectura hexagonal + vertical slicing. Se ejecutan sobre el bytecode
 * compilado del paquete hexagonal.
 */
@AnalyzeClasses(packages = "um.tesoreria.sender.hexagonal", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

    private static final String DOMINIO = "..hexagonal..domain..";
    private static final String APLICACION = "..hexagonal..application..";
    private static final String INFRAESTRUCTURA = "..hexagonal..infrastructure..";

    @ArchTest
    static final ArchRule dominio_no_depende_de_frameworks = noClasses()
            .that().resideInAPackage(DOMINIO)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "com.fasterxml..",
                    "feign..",
                    "org.openpdf..",
                    "org.apache.kafka..",
                    "tools.jackson..",
                    "com.google.zxing..",
                    "com.github.openjson..");

    @ArchTest
    static final ArchRule dominio_no_depende_de_application_ni_infrastructure = noClasses()
            .that().resideInAPackage(DOMINIO)
            .should().dependOnClassesThat().resideInAnyPackage(APLICACION, INFRAESTRUCTURA);

    @ArchTest
    static final ArchRule application_no_depende_de_infrastructure = noClasses()
            .that().resideInAPackage(APLICACION)
            .should().dependOnClassesThat().resideInAPackage(INFRAESTRUCTURA);

    @ArchTest
    static final ArchRule datos_no_depende_de_procesos_ni_gateways = noClasses()
            .that().resideInAPackage("..hexagonal.datos..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..hexagonal.procesos..",
                    "..hexagonal.gateways..");

    @ArchTest
    static final ArchRule gateways_no_depende_de_datos_ni_procesos = noClasses()
            .that().resideInAPackage("..hexagonal.gateways..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..hexagonal.datos..",
                    "..hexagonal.procesos..");

    @ArchTest
    static final ArchRule shared_no_depende_de_los_demas_slices = noClasses()
            .that().resideInAPackage("..hexagonal.shared..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..hexagonal.datos..",
                    "..hexagonal.procesos..",
                    "..hexagonal.gateways..");

    @ArchTest
    static final ArchRule domain_ports_son_interfaces = com.tngtech.archunit.lang.syntax.ArchRuleDefinition
            .classes()
            .that().resideInAPackage("..domain.ports..")
            .should().beInterfaces();

}
