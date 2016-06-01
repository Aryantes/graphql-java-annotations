/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class MethodDataFetcher implements DataFetcher {
    private final Method method;
    private final int envIndex;

    public MethodDataFetcher(Method method) {
        this.method = method;
        List<Class<?>> parameterTypes = Arrays.asList(method.getParameters()).stream().
                map(Parameter::getType).
                collect(Collectors.toList());
        envIndex = parameterTypes.indexOf(DataFetchingEnvironment.class);
    }

    @SneakyThrows
    @Override
    public Object get(DataFetchingEnvironment environment) {
        try {
            Object obj;
            GraphQLInvokeDetached annotationObj = method.getAnnotation(GraphQLInvokeDetached.class);
            if (annotationObj == null) {
                obj = environment.getSource();
                if (environment.getSource() == null) return null;
            } else {
                obj = method.getDeclaringClass().newInstance();
            }
            ArrayList args = new ArrayList<>(environment.getArguments().values());
            if (envIndex >= 0) {
                args.add(envIndex, environment);
            }
            return method.invoke(obj, args.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
