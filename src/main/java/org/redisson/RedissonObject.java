/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import org.redisson.async.AsyncOperation;
import org.redisson.async.OperationListener;
import org.redisson.connection.ConnectionManager;
import org.redisson.core.RObject;

import com.lambdaworks.redis.RedisAsyncConnection;

/**
 * Base Redisson object
 *
 * @author Nikita Koksharov
 *
 */
abstract class RedissonObject implements RObject {

    final ConnectionManager connectionManager;
    private final String name;

    public RedissonObject(ConnectionManager connectionManager, String name) {
        this.connectionManager = connectionManager;
        this.name = name;
    }

    protected <V> Promise<V> newPromise() {
        return connectionManager.getGroup().next().<V>newPromise();
    }

    public String getName() {
        return name;
    }

    public boolean delete() {
        return connectionManager.get(deleteAsync());
    }

    public Future<Boolean> deleteAsync() {
        return connectionManager.writeAsync(getName(), new AsyncOperation<Object, Boolean>() {
            @Override
            public void execute(final Promise<Boolean> promise, RedisAsyncConnection<Object, Object> async) {
                async.del(getName()).addListener(new OperationListener<Object, Boolean, Object>(promise, async, this) {
                    @Override
                    public void onOperationComplete(Future<Object> future) throws Exception {
                        promise.setSuccess(((Number) future.get()).byteValue() == 1);
                    }
                });
            }
        });
    }
    
}
