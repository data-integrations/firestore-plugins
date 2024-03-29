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

package io.cdap.plugin.gcp.firestore.source;

/**
 * Utility class that provides handy methods to construct Firestore Source Config for testing
 */
public class FirestoreSourceConfigHelper {

  public static final String TEST_REF_NAME = "TestRefName";
  public static final String TEST_PROJECT = "test-project";
  public static final String TEST_DATABASE = "testdatabase";
  public static final String TEST_COLLECTION = "TestCollection";

  public static ConfigBuilder newConfigBuilder() {
    return new ConfigBuilder();
  }

  public static class ConfigBuilder {
    private String referenceName = TEST_REF_NAME;
    private String project = TEST_PROJECT;
    private String serviceFilePath = "/path/to/file";
    private String databaseName = TEST_DATABASE;
    private String collection = TEST_COLLECTION;
    private String includeDocumentId = "true";
    private String idAlias = "__id__";
    private String schema;

    public ConfigBuilder setReferenceName(String referenceName) {
      this.referenceName = referenceName;
      return this;
    }

    public ConfigBuilder setProject(String project) {
      this.project = project;
      return this;
    }

    public ConfigBuilder setServiceFilePath(String serviceFilePath) {
      this.serviceFilePath = serviceFilePath;
      return this;
    }

    public ConfigBuilder setDatabase(String database) {
      this.databaseName = database;
      return this;
    }

    public ConfigBuilder setCollection(String collection) {
      this.collection = collection;
      return this;
    }

    public ConfigBuilder setIncludeDocumentId(String includeDocumentId) {
      this.includeDocumentId = includeDocumentId;
      return this;
    }

    public ConfigBuilder setIdAlias(String idAlias) {
      this.idAlias = idAlias;
      return this;
    }

    public ConfigBuilder setSchema(String schema) {
      this.schema = schema;
      return this;
    }

    public FirestoreSourceConfig build() {
      return new FirestoreSourceConfig(referenceName, project, serviceFilePath, databaseName, collection,
        "Basic", "", "", "", includeDocumentId, idAlias, schema);
    }
  }
}
