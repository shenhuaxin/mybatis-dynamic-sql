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
package org.mybatis.qbe.sql.where.condition;

import org.mybatis.qbe.SingleValueCondition;

public class IsNotLike extends SingleValueCondition<String> {

    protected IsNotLike(String value) {
        super(value);
    }

    @Override
    public String render(String columnName, String placeholder) {
        return String.format("%s not like %s", columnName, placeholder); //$NON-NLS-1$
    }
    
    public static IsNotLike of(String value) {
        return new IsNotLike(value);
    }
}
