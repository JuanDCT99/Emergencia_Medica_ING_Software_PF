#!/usr/bin/env python3
"""
hermes-verify-pom-xml-lombok.py
Verificación del pom.xml del proyecto Emergencia_Medica_ING_Software_PF.

Revisa que:
1. El pom.xml tenga Lombok configurado como annotationProcessorPaths.
2. mvn clean compile ejecute sin errores (especialmente sin "cannot find symbol").
3. Los archivos .class de los modelos se generen correctamente.

Uso:
    python3 hermes-verify-pom-xml-lombok.py

Requiere:
    - Python 3
    - Maven instalado y disponible en PATH
    - El proyecto en la misma carpeta donde se ejecuta este script
"""

import subprocess
import sys
import xml.etree.ElementTree as ET
import os
from pathlib import Path

# Detectar la ruta del proyecto: si el script está en el proyecto, usar ese dir.
# Si no, asumir que se corre desde la raíz del proyecto o usar una ruta por defecto.
SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_DIR = SCRIPT_DIR if (SCRIPT_DIR / "pom.xml").exists() else Path.cwd()
POM_PATH = PROJECT_DIR / "pom.xml"

errors = []
warnings = []

def check_pom_has_lombok_annotation_processor():
    """Verifica que el pom.xml tenga annotationProcessorPaths con Lombok."""
    if not POM_PATH.is_file():
        errors.append(f"pom.xml no existe en {POM_PATH}")
        return False

    tree = ET.parse(str(POM_PATH))
    root = tree.getroot()
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}

    compiler_plugin = root.find(".//m:plugin[m:artifactId='maven-compiler-plugin']", ns)
    if compiler_plugin is None:
        errors.append("maven-compiler-plugin no encontrado en pom.xml")
        return False

    annotation_paths = compiler_plugin.find(".//m:annotationProcessorPaths/m:path", ns)
    if annotation_paths is None:
        errors.append("annotationProcessorPaths no configurado en maven-compiler-plugin")
        return False

    lombok_group = annotation_paths.find("m:groupId", ns)
    lombok_artifact = annotation_paths.find("m:artifactId", ns)
    lombok_version = annotation_paths.find("m:version", ns)

    if lombok_group is None or lombok_group.text != "org.projectlombok":
        errors.append("Lombok no configurado como annotation processor (groupId incorrecto o faltante)")
        return False

    if lombok_artifact is None or lombok_artifact.text != "lombok":
        errors.append("Lombok no configurado como annotation processor (artifactId incorrecto o faltante)")
        return False

    if lombok_version is None or lombok_version.text != "1.18.30":
        errors.append(f"Versión de Lombok incorrecta (esperado 1.18.30, encontrado {lombok_version.text if lombok_version else 'N/A'})")
        return False

    return True

def run_maven_compile():
    """Ejecuta mvn clean compile y verifica que no haya errores de compilación."""
    print("[*] Limpiando y compilando con Maven...")
    result = subprocess.run(
        ["mvn", "clean", "compile", "--batch-mode"],
        cwd=str(PROJECT_DIR),
        capture_output=True,
        text=True,
        timeout=120
    )

    if result.returncode != 0:
        errors.append(f"mvn compile falló con código {result.returncode}")
        if "cannot find symbol" in result.stderr or "cannot find symbol" in result.stdout:
            errors.append("Se encontraron errores 'cannot find symbol' — Lombok no está generando métodos")
        if "BUILD FAILURE" in result.stdout or "BUILD FAILURE" in result.stderr:
            errors.append("Maven reportó BUILD FAILURE")
        return False

    if "BUILD SUCCESS" not in result.stdout:
        errors.append("La salida de Maven no contenía 'BUILD SUCCESS'")
        return False

    return True

def check_compiled_classes_exist():
    """Verifica que los archivos .class de los modelos se hayan generado."""
    target_classes = PROJECT_DIR / "target" / "classes" / "com" / "ingenieria" / "software1" / "model"
    expected_models = ["Urgencia.class", "Paciente.class", "Empleado.class", "Ambulancia.class", "Triage.class"]

    missing = []
    for model in expected_models:
        class_path = target_classes / model
        if not class_path.is_file():
            missing.append(str(class_path))

    if missing:
        errors.append(f"Archivos .class faltantes después de compilación: {', '.join(missing)}")
        return False

    return True

def main():
    print("=" * 60)
    print("hermes-verify-pom-xml-lombok.py")
    print("Verificación: pom.xml con Lombok como annotationProcessorPaths")
    print("=" * 60)
    print()

    # Check 1: pom.xml tiene la configuración correcta
    print("[1/3] Verificando pom.xml...")
    if check_pom_has_lombok_annotation_processor():
        print("    ✅ pom.xml tiene Lombok como annotationProcessorPaths (1.18.30)")
    else:
        print("    ❌ pom.xml no tiene la configuración esperada")
        for e in errors:
            if "pom.xml" in e or "Lombok" in e or "annotation" in e or "maven-compiler" in e:
                print(f"       - {e}")
    print()

    # Check 2: Compilación limpia
    print("[2/3] Ejecutando mvn clean compile...")
    if run_maven_compile():
        print("    ✅ mvn clean compile → BUILD SUCCESS")
    else:
        print("    ❌ mvn clean compile falló")
        for e in errors:
            if "mvn" in e or "BUILD" in e or "cannot find" in e:
                print(f"       - {e}")
    print()

    # Check 3: Archivos .class generados
    print("[3/3] Verificando archivos .class generados...")
    if check_compiled_classes_exist():
        print("    ✅ Todos los modelos compilados (Urgencia, Paciente, Empleado, Ambulancia, Triage)")
    else:
        print("    ❌ Faltan archivos .class de los modelos")
    print()

    # Resumen final
    print("=" * 60)
    if errors:
        print(f"❌ VERIFICACIÓN FALLIDA — {len(errors)} error(es):")
        for e in errors:
            print(f"   • {e}")
        if warnings:
            print(f"\n⚠️  Advertencias ({len(warnings)}):")
            for w in warnings:
                print(f"   • {w}")
        sys.exit(1)
    else:
        print("✅ VERIFICACIÓN EXITOSA")
        print("   El pom.xml tiene Lombok configurado correctamente como")
        print("   annotationProcessorPaths y la compilación Maven es limpia.")
        sys.exit(0)

if __name__ == "__main__":
    main()
