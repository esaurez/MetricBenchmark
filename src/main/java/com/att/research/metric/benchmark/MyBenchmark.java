/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.att.research.metric.benchmark;

import org.openjdk.jmh.annotations.*;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.infra.Blackhole;

public class MyBenchmark {
    @State(Scope.Benchmark)
    public static class MyState {
        private Connection createConnection(){
            try {
                Class.forName(this.driver);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
            Connection connection=null;
            try {
                if(!isMariaDb) {
                    connection = DriverManager.getConnection(connectionUrl);
                }
                else{
                    Properties connectionProps = new Properties();
                    connectionProps.put("user", user);
                    connectionProps.put("password", password);
                    connection = DriverManager.getConnection(connectionUrl,connectionProps);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return connection;
        }

        private void createTable(Connection connection){
            final String sql = "CREATE TABLE IF NOT EXISTS Persons (\n" +
                    "    PersonID int,\n" +
                    "    Counter int,\n" +
                    "    LastName varchar(255),\n" +
                    "    FirstName varchar(255),\n" +
                    "    Address varchar(255),\n" +
                    "    City varchar(255)\n" +
                    ");";

            Statement stmt = null;
            try {
                stmt = connection.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            Boolean execute=null;
            try {
                execute = stmt.execute(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            try {
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        private boolean cleanTable(){
            String cleanCmd = "DELETE FROM `Persons`;";
            Statement stmt = null;
            try {
                stmt = testConnection.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            Boolean execute=null;
            try {
                execute = stmt.execute(cleanCmd);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
            try {
                testConnection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return execute;
        }

        private void addRowsToTable(int totalNumberOfRows){
            for(int i=0; i<totalNumberOfRows; i++) {
                final StringBuilder insertSQLBuilder = new StringBuilder()
                        .append("INSERT INTO Persons VALUES (")
                        .append(i)
                        .append(", ")
                        .append(0)
                        .append(", '")
                        .append("Last-")
                        .append(i)
                        .append("', '")
                        .append("First-")
                        .append(i)
                        .append("', 'KACB', 'ATLANTA');");
                Statement stmt = null;
                try {
                    stmt = testConnection.createStatement();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                Boolean execute = null;
                try {
                    execute = stmt.execute(insertSQLBuilder.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                try {
                    testConnection.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
        @Setup(Level.Trial)
        public void doSetup() {
            System.out.println("Do Global Setup");
            Connection connection = createConnection();
            createTable(connection);
        }

        @Setup(Level.Invocation)
        public void doWarmup() {
            System.out.println("Do Setup");
            //Setup connection
            testConnection = createConnection();

            //Empty database
            boolean cleanResult = cleanTable();

            //Add new lines
            addRowsToTable(rows);

            //Commit
            try {
                testConnection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }


        @TearDown(Level.Invocation)
        public void doTearDown() {
            System.out.println("Do TearDown");
            try {
                testConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        boolean isMariaDb = false;
        String user = "root";
        String password = "music";
        public final String driver = "org.apache.calcite.avatica.remote.Driver";
        //public final String driver = "org.mariadb.jdbc.Driver";

        public final String connectionUrl = "jdbc:avatica:remote:url=http://localhost:30000;serialization=protobuf";
        //public final String connectionUrl = "jdbc:mariadb://localhost:3306/test";

        public Connection testConnection;
        @Param({ "1","10","100", "200", "300", "500", "1000", "10000" })
        public int rows;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.NANOSECONDS) @Warmup(iterations=1) @Measurement(iterations = 10)
    public void testMethod(MyState state,Blackhole blackhole) {
        //UPDATE table_name
        //SET column1 = value1, column2 = value2, ...
        //WHERE condition;
        final StringBuilder updateBuilder = new StringBuilder()
                .append("UPDATE Persons ")
                .append("SET Counter = Counter + 1,")
                .append("City = 'Sandy Springs'")
                .append(";");
        Statement stmt = null;
        try {
            stmt = state.testConnection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Boolean execute = null;
        try {
            execute = stmt.execute(updateBuilder.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            state.testConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
