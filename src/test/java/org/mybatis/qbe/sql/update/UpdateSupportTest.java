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
package org.mybatis.qbe.sql.update;

import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.mybatis.qbe.sql.SqlConditions.*;
import static org.mybatis.qbe.sql.update.UpdateSupportBuilder.*;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;

import org.junit.Test;
import org.mybatis.qbe.sql.SqlColumn;
import org.mybatis.qbe.sql.SqlTable;
import org.mybatis.qbe.sql.update.UpdateSupport;
import org.mybatis.qbe.sql.update.UpdateSupportBuilder.UpdateSupportBuildStep2.SetValuesCollector;

public class UpdateSupportTest {
    private static final SqlTable foo = SqlTable.of("foo");
    private static final SqlColumn<Integer> id = SqlColumn.of("id", JDBCType.INTEGER);
    private static final SqlColumn<String> firstName = SqlColumn.of("firstName", JDBCType.VARCHAR);
    private static final SqlColumn<String> lastName = SqlColumn.of("lastName", JDBCType.VARCHAR);
    private static final SqlColumn<String> occupation = SqlColumn.of("occupation", JDBCType.VARCHAR);

    @Test
    public void testUpdateParameter() {
        UpdateSupport updateSupport = update(foo)
                .set(firstName, "fred")
                .set(lastName, "jones")
                .setNull(occupation)
                .where(id, isEqualTo(3))
                .build();
        
        String expectedSetClause = "set firstName = {parameters.up1}, lastName = {parameters.up2}, occupation = {parameters.up3}";
                
        assertThat(updateSupport.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = {parameters.p1}";
        assertThat(updateSupport.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(updateSupport.getParameters().size(), is(4));
        assertThat(updateSupport.getParameters().get("up1"), is("fred"));
        assertThat(updateSupport.getParameters().get("up2"), is("jones"));
        assertThat(updateSupport.getParameters().get("up3"), is(nullValue()));
        assertThat(updateSupport.getParameters().get("p1"), is(3));
    }

    @Test
    public void testUpdateParameterStartWithNull() {
        UpdateSupport updateSupport = update(foo)
                .setNull(occupation)
                .set(firstName, "fred")
                .set(lastName, "jones")
                .where(id, isEqualTo(3))
                .and(firstName, isEqualTo("barney"))
                .build();
        
        String expectedSetClause = "set occupation = {parameters.up1}, firstName = {parameters.up2}, lastName = {parameters.up3}";
                
        assertThat(updateSupport.getSetClause(), is(expectedSetClause));
        
        String expectedWhereClauses = "where id = {parameters.p1} and firstName = {parameters.p2}";
        assertThat(updateSupport.getWhereClause(), is(expectedWhereClauses));
        
        assertThat(updateSupport.getParameters().size(), is(5));
        assertThat(updateSupport.getParameters().get("up1"), is(nullValue()));
        assertThat(updateSupport.getParameters().get("up2"), is("fred"));
        assertThat(updateSupport.getParameters().get("up3"), is("jones"));
        assertThat(updateSupport.getParameters().get("p1"), is(3));
        assertThat(updateSupport.getParameters().get("p2"), is("barney"));
    }
    
    @Test
    public void testParallelStream() {
        AtomicInteger sequence = new AtomicInteger(1);
        List<ColumnAndValue<?>> setColumns = new ArrayList<>();
        setColumns.add(ColumnAndValue.of(occupation, sequence.getAndIncrement()));
        setColumns.add(ColumnAndValue.of(firstName, "fred", sequence.getAndIncrement()));
        setColumns.add(ColumnAndValue.of(lastName, "jones", sequence.getAndIncrement()));
        
        SetValuesCollector collector = setColumns.parallelStream().collect(Collector.of(
                SetValuesCollector::new,
                SetValuesCollector::add,
                SetValuesCollector::merge));
        
        assertThat(collector.getSetClause(), is("set occupation = {parameters.up1}, firstName = {parameters.up2}, lastName = {parameters.up3}"));
        assertThat(collector.parameters.size(), is(3));
        assertThat(collector.parameters.get("up1"), is(nullValue()));
        assertThat(collector.parameters.get("up2"), is("fred"));
        assertThat(collector.parameters.get("up3"), is("jones"));
    }

    @Test
    public void testFullUpdateStatement() {
        UpdateSupport updateSupport = update(foo)
                .set(firstName, "fred")
                .set(lastName, "jones")
                .setNull(occupation)
                .where(id, isEqualTo(3))
                .build();
        
        String expectedStatement = "update foo " 
                + "set firstName = {parameters.up1}, lastName = {parameters.up2}, occupation = {parameters.up3} "
                + "where id = {parameters.p1}";
                
        assertThat(updateSupport.getFullUpdateStatement(), is(expectedStatement));
        
        assertThat(updateSupport.getParameters().size(), is(4));
        assertThat(updateSupport.getParameters().get("up1"), is("fred"));
        assertThat(updateSupport.getParameters().get("up2"), is("jones"));
        assertThat(updateSupport.getParameters().get("up3"), is(nullValue()));
        assertThat(updateSupport.getParameters().get("p1"), is(3));
    }
}
