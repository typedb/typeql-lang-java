/*
 * Copyright (C) 2021 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.vaticle.typeql.lang.query.builder;

import com.vaticle.typeql.lang.common.TypeQLArg;
import com.vaticle.typeql.lang.common.TypeQLToken;
import com.vaticle.typeql.lang.common.exception.TypeQLException;
import com.vaticle.typeql.lang.pattern.variable.UnboundVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.vaticle.typeql.lang.common.exception.ErrorMessage.INVALID_SORTING_ORDER;

public interface Sortable<S, O, L> {

    default S sort(String var) {
        UnboundVariable[] vars = new UnboundVariable[] { UnboundVariable.named(var) };
        return sort(vars);
    }

    default S sort(List<String> vars, String order) {
        TypeQLArg.Order o = TypeQLArg.Order.of(order);
        if (o == null) throw TypeQLException.of(
                INVALID_SORTING_ORDER.message(TypeQLArg.Order.ASC, TypeQLArg.Order.DESC)
        );
        return sort(vars.stream().map(UnboundVariable::named).toArray(UnboundVariable[]::new), o);
    }

    default S sort(List<String> vars, TypeQLArg.Order order) {
        return sort(vars.stream().map(UnboundVariable::named).toArray(UnboundVariable[]::new), order);
    }

    default S sort(UnboundVariable[] vars) {
        return sort(new Sorting(vars));
    }

    default S sort(UnboundVariable[] vars, TypeQLArg.Order order) {
        return sort(new Sorting(vars, order));
    }

    S sort(Sorting sorting);

    O offset(long offset);

    L limit(long limit);

    class Sorting {

        private final UnboundVariable[] vars;
        private final TypeQLArg.Order order;
        private final int hash;

        public Sorting(UnboundVariable[] vars) {
            this(vars, null);
        }

        public Sorting(UnboundVariable[] vars, TypeQLArg.Order order) {
            this.vars = vars;
            this.order = order;
            this.hash = Objects.hash(Arrays.hashCode(vars()), order());
        }

        public UnboundVariable[] vars() {
            return vars;
        }

        public TypeQLArg.Order order() {
            return order == null ? TypeQLArg.Order.ASC : order;
        }

        @Override
        public String toString() {
            StringBuilder sort = new StringBuilder();
            sort.append(Stream.of(vars).map(UnboundVariable::toString).reduce((a, b) -> a + ", "  + b).get());
            if (order != null) sort.append(TypeQLToken.Char.SPACE).append(order);
            return sort.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sorting that = (Sorting) o;
            return (Arrays.equals(this.vars(), that.vars()) &&
                    this.order().equals(that.order()));
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
