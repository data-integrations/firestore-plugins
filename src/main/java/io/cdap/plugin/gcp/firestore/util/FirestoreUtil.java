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

package io.cdap.plugin.gcp.firestore.util;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.common.base.Strings;
import io.cdap.plugin.gcp.firestore.exception.FirestoreInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

/**
 * Utility class that provides methods to connect to Firestore instance.
 */
public class FirestoreUtil {
  private static final Logger LOG = LoggerFactory.getLogger(FirestoreUtil.class);

  /**
   * Connects to Firestore instance using given credentials in JSON file, project ID and optional database Id.
   *
   * @param serviceAccount Actual credentials in JSON or a path to the file containing them
   * @param isServiceAccountFilePath If the serviceAccount parameter is a file path or not
   * @param projectId Google Cloud project ID
   * @param databaseId Cloud Firestore Database Id
   * @return Firestore service
   */
  public static Firestore getFirestore(@Nullable String serviceAccount,
                                       @Nullable Boolean isServiceAccountFilePath,
                                       String projectId,
                                       String databaseId) {
    try {
      FirestoreOptions.Builder optionsBuilder = FirestoreOptions.newBuilder()
        .setProjectId(projectId)
        .setDatabaseId(databaseId);
      LOG.debug("isServiceAccountFilePath={}, project={}, databaseId={}...",
        isServiceAccountFilePath, projectId, databaseId);
      final GoogleCredentials credential = getCredential(serviceAccount, isServiceAccountFilePath);
      optionsBuilder.setCredentials(credential);
      return optionsBuilder.build().getService();
    } catch (IOException e) {
      throw new FirestoreInitializationException("Unable to connect to Firestore", e);
    }
  }

  public static GoogleCredentials loadServiceAccountCredentials(String path) throws IOException {
    File credentialsPath = new File(path);
    try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
      return GoogleCredentials.fromStream(serviceAccountStream);
    }
  }

  /**
   * Obtains Google Cloud Service Account credential from given JSON file.
   * If give path is null or empty, obtains application default credentials.
   *
   * @param serviceAccount path to credentials defined in JSON file
   * @param isServiceAccountFilePath indicator whether service account is file path or JSON
   * @return Google Cloud Service Cloud credential
   * @throws IOException if the credential cannot be created in the current environment
   */
  private static GoogleCredentials getCredential(@Nullable String serviceAccount,
                                                 @Nullable Boolean isServiceAccountFilePath) throws IOException {
    GoogleCredentials credential;
    if (!Strings.isNullOrEmpty(serviceAccount)) {
      if (!isServiceAccountFilePath) {
        credential = loadCredentialFromStream(serviceAccount);
      } else {
        credential = loadCredentialFromFile(serviceAccount);
      }
    } else {
      credential = GoogleCredentials.getApplicationDefault();
    }
    return credential;
  }

  /**
   * Generate credentials from JSON key
   * @param serviceAccountFilePath path to service account file
   * @return Google Cloud credential
   * @throws IOException if the credential cannot be created in the current environment
   */
  private static GoogleCredentials loadCredentialFromFile(@Nullable String serviceAccountFilePath) throws IOException {
    try (InputStream inputStream = new FileInputStream(serviceAccountFilePath)) {
      return GoogleCredentials.fromStream(inputStream);
    }
  }

  /**
   * Generate credentials from JSON key
   * @param serviceAccount contents of service account JSON
   * @return Google Cloud credential
   * @throws IOException if the credential cannot be created in the current environment
   */
  private static GoogleCredentials loadCredentialFromStream(@Nullable String serviceAccount) throws IOException {
    try (InputStream jsonInputStream = new ByteArrayInputStream(serviceAccount.getBytes())) {
      return GoogleCredentials.fromStream(jsonInputStream);
    }
  }
}
