/*
 * Copyright © 2020 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.gcp.firestore.sink;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.cdap.cdap.api.data.batch.OutputFormatProvider;
import io.cdap.plugin.gcp.common.GCPConfig;
import io.cdap.plugin.gcp.firestore.sink.util.FirestoreSinkConstants;
import io.cdap.plugin.gcp.firestore.util.FirestoreConstants;

import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides FirestoreOutputFormat's class name and configuration.
 */
public class FirestoreOutputFormatProvider implements OutputFormatProvider {

  private final Map<String, String> configMap;

  /**
   * Gets properties from {@link FirestoreSink} and stores them as properties in map
   * for {@link FirestoreRecordWriter}.
   *
   * @param project Firestore project
   * @param serviceAccountPath Firestore service account path
   * @param databaseId Firestore database Id
   * @param collection Firestore collection name
   * @param shouldUseAutoGeneratedId should use auto generated document id
   * @param batchSize batch size
   */
  public FirestoreOutputFormatProvider(String project, @Nullable String serviceAccountPath, @Nullable String databaseId,
                                       String collection, String shouldUseAutoGeneratedId, String batchSize) {
    ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>()
      .put(GCPConfig.NAME_PROJECT, project)
      .put(FirestoreConstants.PROPERTY_COLLECTION, collection)
      .put(FirestoreSinkConstants.PROPERTY_ID_TYPE, shouldUseAutoGeneratedId)
      .put(FirestoreSinkConstants.PROPERTY_BATCH_SIZE, batchSize);

    if (!Strings.isNullOrEmpty(serviceAccountPath)) {
      builder.put(GCPConfig.NAME_SERVICE_ACCOUNT_FILE_PATH, serviceAccountPath);
    }
    if (!Strings.isNullOrEmpty(databaseId)) {
      builder.put(FirestoreConstants.PROPERTY_DATABASE_ID, databaseId);
    }
    this.configMap = builder.build();
  }

  @Override
  public String getOutputFormatClassName() {
    return FirestoreOutputFormat.class.getName();
  }

  @Override
  public Map<String, String> getOutputFormatConfiguration() {
    return configMap;
  }
}