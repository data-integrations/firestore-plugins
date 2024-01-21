package io.cdap.plugin.gcp.firestore.common;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.ServiceOptions;
import com.google.common.base.Strings;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.plugin.common.Constants;
import io.cdap.plugin.common.IdUtils;
import io.cdap.plugin.gcp.firestore.util.FirestoreConstants;

import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Contains config properties common to all GCP plugins, like project id and service account key.
 */
public class FirestoreConfig extends PluginConfig {
  public static final String NAME_PROJECT = "project";
  public static final String NAME_SERVICE_ACCOUNT_TYPE = "serviceAccountType";
  public static final String NAME_DATABASE = "databaseName";
  public static final String NAME_SERVICE_ACCOUNT_FILE_PATH = "serviceFilePath";
  public static final String NAME_SERVICE_ACCOUNT_JSON = "serviceAccountJSON";
  public static final String AUTO_DETECT = "auto-detect";
  public static final String SERVICE_ACCOUNT_FILE_PATH = "filePath";
  public static final String SERVICE_ACCOUNT_JSON = "JSON";

  @Name(Constants.Reference.REFERENCE_NAME)
  @Description("Uniquely identifies this plugin for lineage, annotating metadata, etc.")
  protected String referenceName;

  @Name(NAME_PROJECT)
  @Description("Google Cloud Project ID, which uniquely identifies a project. "
    + "It can be found on the Dashboard in the Google Cloud Platform Console.")
  @Macro
  @Nullable
  protected String project;

  @Name(NAME_DATABASE)
  @Description("Name of the Firestore Database. "
    + "If not specified, it will use '(default)'.")
  @Macro
  @Nullable
  protected String databaseName;

  @Name(NAME_SERVICE_ACCOUNT_TYPE)
  @Description("Service account type, file path where the service account is located or the JSON content of the " +
    "service account.")
  @Macro
  protected String serviceAccountType;

  @Name(NAME_SERVICE_ACCOUNT_FILE_PATH)
  @Description("Path on the local file system of the service account key used "
    + "for authorization. Can be set to 'auto-detect' when running on a Dataproc cluster. "
    + "When running on other clusters, the file must be present on every node in the cluster.")
  @Nullable
  @Macro
  protected String serviceFilePath;

  @Name(NAME_SERVICE_ACCOUNT_JSON)
  @Description("Content of the service account file.")
  @Nullable
  @Macro
  protected String serviceAccountJson;

  public String getProject() {
    String projectId = tryGetProject();
    if (projectId == null) {
      throw new IllegalArgumentException(
        "Could not detect Google Cloud project id from the environment. Please specify a project id.");
    }
    return projectId;
  }

  @Nullable
  public String tryGetProject() {
    if (containsMacro(NAME_PROJECT) && Strings.isNullOrEmpty(project)) {
      return null;
    }
    String projectId = project;
    if (Strings.isNullOrEmpty(project) || AUTO_DETECT.equals(project)) {
      projectId = ServiceOptions.getDefaultProjectId();
    }
    return projectId;
  }

  @Nullable
  public String getServiceAccountFilePath() {
    if (containsMacro(NAME_SERVICE_ACCOUNT_FILE_PATH) || serviceFilePath == null ||
      serviceFilePath.isEmpty() || AUTO_DETECT.equals(serviceFilePath)) {
      return null;
    }
    return serviceFilePath;
  }

  @Nullable
  public String getServiceAccountJson() {
    if (containsMacro(NAME_SERVICE_ACCOUNT_JSON) || Strings.isNullOrEmpty(serviceAccountJson)) {
      return null;
    }
    return serviceAccountJson;
  }

  public String getServiceAccountType() {
    if (containsMacro(NAME_SERVICE_ACCOUNT_TYPE) || Strings.isNullOrEmpty(serviceAccountType)) {
      return null;
    }
    return serviceAccountType;
  }

  @Nullable
  public Boolean isServiceAccountJson() {
    String serviceAccountType = getServiceAccountType();
    return Strings.isNullOrEmpty(serviceAccountType) ? null : serviceAccountType.equals(SERVICE_ACCOUNT_JSON);
  }

  @Nullable
  public Boolean isServiceAccountFilePath() {
    String serviceAccountType = getServiceAccountType();
    return Strings.isNullOrEmpty(serviceAccountType) ? null : serviceAccountType.equals(SERVICE_ACCOUNT_FILE_PATH);
  }

  @Nullable
  public String getServiceAccount() {
    Boolean serviceAccountJson = isServiceAccountJson();
    if (serviceAccountJson == null) {
      return null;
    }
    return serviceAccountJson ? getServiceAccountJson() : getServiceAccountFilePath();
  }

  /**
   * Validates the given referenceName to consists of characters allowed to represent a dataset.
   */
  public void validate(FailureCollector collector) {
    IdUtils.validateReferenceName(referenceName, collector);
    validateDatabaseName(collector);
  }

  public String getDatabaseName() {
    if (containsMacro(NAME_DATABASE) && Strings.isNullOrEmpty(databaseName)) {
      return null;
    } else if (Strings.isNullOrEmpty(databaseName)) {
      return "(default)";
    }
    return databaseName;
  }

  public String getReferenceName() {
    return referenceName;
  }

  /**
   * Return true if the service account is set to auto-detect but it can't be fetched from the environment.
   * This shouldn't result in a deployment failure, as the credential could be detected at runtime if the pipeline
   * runs on dataproc. This should primarily be used to check whether certain validation logic should be skipped.
   *
   * @return true if the service account is set to auto-detect but it can't be fetched from the environment.
   */
  public boolean autoServiceAccountUnavailable() {
    if (getServiceAccountFilePath() == null && SERVICE_ACCOUNT_FILE_PATH.equals(getServiceAccountType())) {
      try {
        ServiceAccountCredentials.getApplicationDefault();
      } catch (IOException e) {
        return true;
      }
    }
    return false;
  }

  /**
   * Validates the given database name to consists of characters allowed to represent a dataset.
   */
  public void validateDatabaseName(FailureCollector collector) {
    if (containsMacro(FirestoreConfig.NAME_DATABASE)) {
      return;
    }

    String databaseName = getDatabaseName();

    // Check if the database name is empty or null.
    if (Strings.isNullOrEmpty(databaseName)) {
      collector.addFailure("Database Name must be specified.", null)
        .withConfigProperty(FirestoreConfig.NAME_DATABASE);
    }

    // Check if database name contains the (default)
    if (!databaseName.equals(FirestoreConstants.DEFAULT_DATABASE_NAME)) {

      // Ensure database name includes only letters, numbers, and hyphen (-)
      // characters.
      if (!databaseName.matches("^[a-zA-Z0-9-]+$")) {
        collector.addFailure("Database name can only include letters, numbers and hyphen characters.", null)
          .withConfigProperty(FirestoreConfig.NAME_DATABASE);
      }

      // Ensure database name is in lower case.
      if (databaseName != databaseName.toLowerCase()) {
        collector.addFailure("Database name must be in lowercase.", null)
          .withConfigProperty(FirestoreConfig.NAME_DATABASE);
      }

      // The first character must be a letter.
      if (!databaseName.matches("^[a-zA-Z].*")) {
        collector.addFailure("Database name's first character can only be an alphabet.", null)
          .withConfigProperty(FirestoreConfig.NAME_DATABASE);
      }

      // The last character must be a letter or number.
      if (!databaseName.matches(".*[a-zA-Z0-9]$")) {
        collector.addFailure("Database name's last character can only be a letter or a number.", null)
          .withConfigProperty(FirestoreConfig.NAME_DATABASE);
      }

      // Minimum of 4 characters.
      if (databaseName.length() < 4) {
        collector.addFailure("Database name should be at least 4 letters.", null)
          .withConfigProperty(FirestoreConfig.NAME_DATABASE);
      }

      // Maximum of 63 characters.
      if (databaseName.length() > 63) {
        collector.addFailure("Database name cannot be more than 63 characters.", null)
          .withConfigProperty(FirestoreConfig.NAME_DATABASE);
      }

      // Should not be a UUID.
      try {
        UUID.fromString(databaseName);
        collector.addFailure("Database name cannot contain a UUID.", null)
          .withConfigProperty(FirestoreConfig.NAME_DATABASE);
      } catch (IllegalArgumentException e) { }
    }
  }
}
