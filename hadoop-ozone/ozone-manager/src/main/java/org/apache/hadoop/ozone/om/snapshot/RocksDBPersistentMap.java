/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.ozone.om.snapshot;

import java.io.IOException;
import org.apache.hadoop.hdds.utils.db.CodecRegistry;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

/**
 * Persistent map backed by RocksDB.
 */
public class RocksDBPersistentMap <K, V> implements PersistentMap<K, V> {
  private final RocksDB db;
  private final ColumnFamilyHandle columnFamilyHandle;
  private final CodecRegistry codecRegistry;
  private final Class<K> keyType;
  private final Class<V> valueType;

  RocksDBPersistentMap(RocksDB db,
                       ColumnFamilyHandle columnFamilyHandle,
                       CodecRegistry codecRegistry,
                       Class<K> keyType,
                       Class<V> valueType) {
    this.db = db;
    this.columnFamilyHandle = columnFamilyHandle;
    this.codecRegistry = codecRegistry;
    this.keyType = keyType;
    this.valueType = valueType;
  }

  @Override
  public V get(K key) {
    try {
      byte[] rawKey = codecRegistry.asRawData(key);
      byte[] rawValue = db.get(columnFamilyHandle, rawKey);
      return codecRegistry.asObject(rawValue, valueType);
    } catch (IOException | RocksDBException exception) {
      // TODO:: Fail gracefully.
      throw new RuntimeException(exception);
    }
  }

  @Override
  public void put(K key, V value) {
    try {
      byte[] rawKey = codecRegistry.asRawData(key);
      byte[] rawValue = codecRegistry.asRawData(value);
      db.put(columnFamilyHandle, rawKey, rawValue);
    } catch (IOException | RocksDBException exception) {
      // TODO:: Fail gracefully.
      throw new RuntimeException(exception);
    }
  }
}
