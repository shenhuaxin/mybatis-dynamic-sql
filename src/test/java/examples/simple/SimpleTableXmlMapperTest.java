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
package examples.simple;

import static examples.simple.SimpleTableQBESupport.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mybatis.qbe.sql.SqlConditions.isEqualTo;
import static org.mybatis.qbe.sql.SqlConditions.isIn;
import static org.mybatis.qbe.sql.SqlConditions.isNull;
import static org.mybatis.qbe.sql.delete.DeleteSupportBuilder.deleteFrom;
import static org.mybatis.qbe.sql.select.SelectSupportBuilder.selectCount;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.qbe.sql.delete.DeleteSupport;
import org.mybatis.qbe.sql.select.SelectSupport;

public class SimpleTableXmlMapperTest {

    private static final String JDBC_URL = "jdbc:hsqldb:mem:aname";
    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver"; 
    
    private SqlSessionFactory sqlSessionFactory;
    
    @Before
    public void setup() throws Exception {
        Class.forName(JDBC_DRIVER);
        InputStream is = getClass().getResourceAsStream("/examples/simple/CreateSimpleDB.sql");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ScriptRunner sr = new ScriptRunner(connection);
            sr.setLogWriter(null);
            sr.runScript(new InputStreamReader(is));
        }
        
        is = getClass().getResourceAsStream("/examples/simple/MapperConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }
    
    @Test
    public void testSelectByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            SelectSupport selectSupport = selectByExample()
                    .where(id, isEqualTo(1))
                    .or(occupation, isNull())
                    .build();
            
            List<SimpleTableRecord> rows = mapper.selectMany(selectSupport);
            
            assertThat(rows.size(), is(3));
        } finally {
            session.close();
        }
    }

    @Test
    public void testSelectByExampleWithTypeHandler() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            SelectSupport selectSupport = selectByExample()
                    .where(employed, isEqualTo(false))
                    .orderBy(id)
                    .build();
            
            List<SimpleTableRecord> rows = mapper.selectMany(selectSupport);
            
            assertThat(rows.size(), is(2));
            assertThat(rows.get(0).getId(), is(3));
            assertThat(rows.get(1).getId(), is(6));
        } finally {
            session.close();
        }
    }

    @Test
    public void testFirstNameIn() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            
            SelectSupport selectSupport = selectByExample()
                    .where(firstName, isIn("Fred", "Barney"))
                    .build();
            
            List<SimpleTableRecord> rows = mapper.selectMany(selectSupport);
            
            assertThat(rows.size(), is(2));
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            DeleteSupport deleteSupport = deleteFrom(simpleTable)
                    .where(occupation, isNull())
                    .build();
            int rows = mapper.delete(deleteSupport);
            
            assertThat(rows, is(2));
        } finally {
            session.close();
        }
    }

    @Test
    public void testDeleteByPrimaryKey() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            DeleteSupport deleteSupport = buildDeleteByPrimaryKeySupport(2);
            int rows = mapper.delete(deleteSupport);
            
            assertThat(rows, is(1));
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsert() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(buildFullInsertSupport(record));
            
            assertThat(rows, is(1));
        } finally {
            session.close();
        }
    }

    @Test
    public void testInsertSelective() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(false);
            
            int rows = mapper.insert(buildSelectiveInsertSupport(record));
            assertThat(rows, is(1));
            
            SimpleTableRecord returnedRecord = mapper.selectOne(buildSelectByPrimaryKeySupport(100));
            assertThat(returnedRecord.getOccupation(), is(nullValue()));
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateByPrimaryKey() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(buildFullInsertSupport(record));
            assertThat(rows, is(1));
            
            record.setOccupation("Programmer");
            rows = mapper.update(buildFullUpdateByPrimaryKeySupport(record));
            assertThat(rows, is(1));
            
            SimpleTableRecord newRecord = mapper.selectOne(buildSelectByPrimaryKeySupport(100));
            assertThat(newRecord.getOccupation(), is("Programmer"));
            
        } finally {
            session.close();
        }
    }

    @Test
    public void testUpdateByPrimaryKeySelective() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SimpleTableRecord record = new SimpleTableRecord();
            record.setId(100);
            record.setFirstName("Joe");
            record.setLastName("Jones");
            record.setBirthDate(new Date());
            record.setEmployed(true);
            record.setOccupation("Developer");
            
            int rows = mapper.insert(buildFullInsertSupport(record));
            assertThat(rows, is(1));

            SimpleTableRecord updateRecord = new SimpleTableRecord();
            updateRecord.setId(100);
            updateRecord.setOccupation("Programmer");
            rows = mapper.update(buildSelectiveUpdateByPrimaryKeySupport(updateRecord));
            assertThat(rows, is(1));
            
            SimpleTableRecord newRecord = mapper.selectOne(buildSelectByPrimaryKeySupport(100));
            assertThat(newRecord.getOccupation(), is("Programmer"));
            assertThat(newRecord.getFirstName(), is("Joe"));
            
        } finally {
            session.close();
        }
    }


    @Test
    public void testCountByExample() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            SimpleTableXmlMapper mapper = session.getMapper(SimpleTableXmlMapper.class);
            SelectSupport selectSupport = selectCount()
                    .from(simpleTable)
                    .where(occupation, isNull())
                    .build();
            long rows = mapper.count(selectSupport);
            
            assertThat(rows, is(2L));
        } finally {
            session.close();
        }
    }
}
