/**
 *    Copyright 2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.qbe.mybatis3;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.insert.InsertSupportBuilder.insert;

import java.sql.JDBCType;

import org.junit.Test;
import org.mybatis.qbe.sql.SqlTable;
import org.mybatis.qbe.sql.insert.InsertSupport;

public class InsertSupportTest {
    private static final SqlTable foo = SqlTable.of("foo");
    private static final MyBatis3Column<Integer> id = MyBatis3Column.of("id", JDBCType.INTEGER);
    private static final MyBatis3Column<String> firstName = MyBatis3Column.of("first_name", JDBCType.VARCHAR);
    private static final MyBatis3Column<String> lastName = MyBatis3Column.of("last_name", JDBCType.VARCHAR);
    private static final MyBatis3Column<String> occupation = MyBatis3Column.of("occupation", JDBCType.VARCHAR);

    @Test
    public void testFullInsertSupportBuilder() {
        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<?> insertSupport = insert(record)
                .into(foo)
                .withColumnMapping(id, "id", record::getId)
                .withColumnMapping(firstName, "firstName", record::getFirstName)
                .withColumnMapping(lastName, "lastName", record::getLastName)
                .withColumnMapping(occupation, "occupation", record::getOccupation)
                .buildFullInsert();

        String expectedColumnsPhrase = "(id, first_name, last_name, occupation)";
        assertThat(insertSupport.getColumnsPhrase(), is(expectedColumnsPhrase));

        String expectedValuesPhrase = "values (#{record.id,jdbcType=INTEGER}, "
                + "#{record.firstName,jdbcType=VARCHAR}, "
                + "#{record.lastName,jdbcType=VARCHAR}, "
                + "#{record.occupation,jdbcType=VARCHAR})";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
    }

    @Test
    public void testSelectiveInsertSupportBuilder() {
        TestRecord record = new TestRecord();
        record.setLastName("jones");
        record.setOccupation("dino driver");
        
        InsertSupport<?> insertSupport = insert(record)
                .into(foo)
                .withColumnMapping(id, "id", record::getId)
                .withColumnMapping(firstName, "firstName", record::getFirstName)
                .withColumnMapping(lastName, "lastName", record::getLastName)
                .withColumnMapping(occupation, "occupation", record::getOccupation)
                .buildSelectiveInsert();

        String expectedColumnsPhrase = "(last_name, occupation)";
        assertThat(insertSupport.getColumnsPhrase(), is(expectedColumnsPhrase));

        String expectedValuesPhrase = "values (#{record.lastName,jdbcType=VARCHAR}, "
                + "#{record.occupation,jdbcType=VARCHAR})";
        assertThat(insertSupport.getValuesPhrase(), is(expectedValuesPhrase));
    }

    public static class TestRecord {
        private Integer id;
        private String firstName;
        private String lastName;
        private String occupation;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }
    }
}
