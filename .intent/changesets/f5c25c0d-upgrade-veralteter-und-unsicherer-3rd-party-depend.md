# Upgrade veralteter und unsicherer 3rd-Party-Dependencies

## Summary

Mehrere in `gradle.properties` gepinnte Abhängigkeiten des `copper-engine`-Projekts sind veraltet oder weisen bekannte Sicherheitslücken auf. Dieser Changeset hebt alle betroffenen Libraries auf sichere und aktuelle Versionen.

## Motivation

- **`snakeyaml 1.33`**: CVE-2022-1471 (CVSS 9.8 – Critical) – Remote Code Execution durch unsichere Deserialisierung. Fix seit Version 2.0.
- **`postgresql:postgresql 9.1-901-1.jdbc4`**: Extrem veralteter (~12 Jahre), falsch koordinierter JDBC-Treiber; der aktuelle Treiber unter `org.postgresql:postgresql:42.7.11` enthält kritische Sicherheits- und Bugfixes.
- **`mysql:mysql-connector-java 8.0.33`**: Der Maven-Namespace `mysql:` ist von Oracle nicht mehr gepflegt; der Nachfolger lautet `com.mysql:mysql-connector-j`, aktuell Version 9.2.0.
- **`cassandra-unit 4.3.1.0`**: Seit Jahren inaktiv, letzter Release 2022; Eignung als Test-Dependency ist zu prüfen.

## Approach

### Schritt 1 – snakeyaml 1.33 → 2.6 ✅

**`gradle.properties`**
```
snakeyamlVersion = 2.6
```

**`projects/copper-ext/src/main/java/org/copperengine/ext/persistent/YamlSerializer.java`**

snakeyaml 2.x entfernt den Konstruktor `new Yaml(DumperOptions)`. Ersatz durch explizite `LoaderOptions` + `Representer`:

```java
// Neue Imports
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

protected Yaml initialYaml() {
    LoaderOptions lO = new LoaderOptions();
    DumperOptions dO = new DumperOptions();
    dO.setAllowReadOnlyProperties(true);
    Representer representer = new Representer(dO);
    Yaml yaml = new Yaml(new Constructor(lO), representer, dO);
    yaml.setBeanAccess(BeanAccess.FIELD);
    return yaml;
}
```

**`projects/copper-ext/src/test/java/org/copperengine/ext/persistent/YamlSerializerTest.java`**

`compatibilityUserClass` deserialisiert einen snakeyaml-1.33-erzeugten String mit `!!`-Tag. In 2.x ist dieses Verhalten für unbekannte Typen standardmäßig gesperrt. Den Test anpassen: entweder den `Constructor` mit einem erlaubten Typen-Filter konfigurieren oder den Test auf das neue Serialisierungsformat umstellen und das alte Format als `assertThrows` dokumentieren.

### Schritt 2 – postgresql:postgresql:9.1-901-1.jdbc4 → org.postgresql:postgresql:42.7.11

**`gradle.properties`**
```
postgresqlVersion = 42.7.11
```

**`build.gradle`** (copper-regtest und copper-performance-test):
```
# vorher
testImplementation "postgresql:postgresql:$postgresqlVersion"
implementation   "postgresql:postgresql:$postgresqlVersion"

# nachher
testImplementation "org.postgresql:postgresql:$postgresqlVersion"
implementation   "org.postgresql:postgresql:$postgresqlVersion"
```

### Schritt 3 – mysql:mysql-connector-java:8.0.33 → com.mysql:mysql-connector-j:9.2.0

**`gradle.properties`**
```
mysqlVersion = 9.2.0
```

**`build.gradle`** (copper-regtest und copper-performance-test):
```
# vorher
testImplementation "mysql:mysql-connector-java:$mysqlVersion"
implementation   "mysql:mysql-connector-java:$mysqlVersion"

# nachher
testImplementation "com.mysql:mysql-connector-j:$mysqlVersion"
implementation   "com.mysql:mysql-connector-j:$mysqlVersion"
```

### Schritt 4 – cassandra-unit 4.3.1.0 evaluieren

Prüfen, ob `cassandra-unit` aus den Test-Dependencies von `copper-cassandra:cassandra-storage` entfernt werden kann. `cassandra-all` ist bereits direkt eingebunden; Cassandra-Tests ggf. auf Testcontainers oder eine andere aktiv gewartete Alternative migrieren.

## Testing Notes

1. `./gradlew build` muss nach den Änderungen ohne Fehler durchlaufen
2. Regressionstests (`copper-regtest`) mit den aktiven Datenbank-Adaptern (H2, Derby, MySQL, PostgreSQL) ausführen
3. Cassandra-Tests nach Entfernung von `cassandra-unit` prüfen, ob alle Testfälle noch abgedeckt sind
4. `snakeyaml`-Upgrade: `compatibilityUserClass`-Test anpassen (siehe Schritt 1)


## Specs

- [Upgrade veralteter und unsicherer 3rd-Party-Dependencies](../specs/c4268f36.md)

---

[View in Intent](https://app.onintent.build/?project=78178bb0-7757-4796-97d1-c1c67ba2d024&changeset=f5c25c0d-ec93-4a56-b097-5cd9c7d750b0&tab=detail)