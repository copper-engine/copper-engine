How to run the COPPER performance test
======================================

1. Checkout COPPER
		git clone https://github.com/copper-engine/copper-engine.git copper
		cd copper

2. Build the performance test jarfile
		gradlew :projects:copper-performance-test:jar

3. Prepare the database
  * Oracle, MySQL, Postgres: create the database, user and schema - see https://github.com/copper-engine/copper-engine/tree/master/projects/copper-coreengine/src/main/database for the correspnding SQL files
  * H2, DerbyDB: The database schema is created by COPPER performance test during startup.
  * Apache Cassandra: create a new keyspace, or use an existing one. The database schema is created by COPPER performance test during startup.
  
4. See usage of the performance test
		cd projects/copper-performance-test/build/libs/
		java -jar copper-performance-test.jar

5. start the test as described in the usage, e.g. to start the latency performance test using an embedded H2 database:
		java -Dds.jdbcURL="jdbc:h2:mem:copperPerfTestH2DB;MVCC=TRUE" -Dds.driverClass=org.h2.Driver -jar copper-performance-test.jar latency
  	