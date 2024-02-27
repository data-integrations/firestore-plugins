/*
 * Copyright © 2019 Cask Data, Inc.
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

import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.validation.CauseAttributes;
import io.cdap.cdap.etl.mock.validation.MockFailureCollector;
import io.cdap.plugin.gcp.firestore.common.FirestoreConfig;
import io.cdap.plugin.gcp.firestore.util.FirestoreConstants;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * Tests for {@link FirestoreSourceConfig}.
 */
public class FirestoreSourceConfigTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testValidateCollectionNull() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(null)
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConstants.PROPERTY_COLLECTION, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testValidateWithDatabaseNameWithSpecialCharacters() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase("a!!!==--zz")
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConfig.NAME_DATABASE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals("Database name can only include letters, numbers and hyphen characters.",
     collector.getValidationFailures().get(0).getMessage());
  }

  @Test
  public void testValidateWithDatabaseNameWithUppercaseCharacters() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase(FirestoreSourceConfigHelper.TEST_DATABASE.toUpperCase())
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConfig.NAME_DATABASE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals("Database name must be in lowercase.", collector.getValidationFailures().get(0).getMessage());
  }

  @Test
  public void testValidateWithDatabaseNameWithoutFirstLetterCharacter() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase("4testdatabase")
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConfig.NAME_DATABASE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals("Database name's first character can only be an alphabet.",
     collector.getValidationFailures().get(0).getMessage());
  }

  @Test
  public void testValidateWithDatabaseNameWithLastCharacterHyphen() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase("testdatabase-")
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConfig.NAME_DATABASE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals("Database name's last character can only be a letter or a number.",
     collector.getValidationFailures().get(0).getMessage());
  }

  @Test
  public void testValidateWithDatabaseNameWithLessThanFourCharacters() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase("tes")
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConfig.NAME_DATABASE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals("Database name should be at least 4 letters.",
     collector.getValidationFailures().get(0).getMessage());
  }

  @Test
  public void testValidateWithDatabaseNameWithMoreThanSixtyThreeCharacters() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase("testdatabase11233aaasssssssssssssssssssssssssssssssssssssssssssssssss")
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConfig.NAME_DATABASE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals("Database name cannot be more than 63 characters.",
     collector.getValidationFailures().get(0).getMessage());
  }

  @Test
  public void testValidateWithDatabaseNameShouldNotBeUUID() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase("b793f8c6-e52c-43c7-8ac4-d0cdfaecbb8e")
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConfig.NAME_DATABASE, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals("Database name cannot contain a UUID.",
     collector.getValidationFailures().get(0).getMessage());
  }

  @Test
  public void testValidateWithDatabase() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase(FirestoreSourceConfigHelper.TEST_DATABASE)
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(0, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreSourceConfigHelper.TEST_DATABASE, config.getDatabaseName());
  }

  @Test
  public void testValidateWithEmptyDatabase() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection(FirestoreSourceConfigHelper.TEST_COLLECTION)
      .setDatabase("")
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(0, collector.getValidationFailures().size());
    Assert.assertEquals("(default)", config.getDatabaseName());
  }

  @Test
  public void testValidateCollectionEmpty() {
    MockFailureCollector collector = new MockFailureCollector();
    FirestoreSourceConfig config = withFirestoreValidationMock(FirestoreSourceConfigHelper.newConfigBuilder()
      .setCollection("")
      .build(), collector);

    config.validate(collector);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(FirestoreConstants.PROPERTY_COLLECTION, collector.getValidationFailures().get(0)
      .getCauses().get(0).getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testIsIncludeDocumentIdTrue() {
    FirestoreSourceConfig config = FirestoreSourceConfigHelper.newConfigBuilder()
      .setIncludeDocumentId("true")
      .build();

    MockFailureCollector collector = new MockFailureCollector();
    Assert.assertTrue(config.isIncludeDocumentId());
    Assert.assertEquals(0, collector.getValidationFailures().size());
  }

  @Test
  public void testIsIncludeDocumentIdFalse() {
    FirestoreSourceConfig config = FirestoreSourceConfigHelper.newConfigBuilder()
      .setIncludeDocumentId("false")
      .build();

    MockFailureCollector collector = new MockFailureCollector();
    Assert.assertFalse(config.isIncludeDocumentId());
    Assert.assertEquals(0, collector.getValidationFailures().size());
  }

  @Test
  public void testIsIncludeDocumentIdInvalid() {
    FirestoreSourceConfig config = FirestoreSourceConfigHelper.newConfigBuilder()
      .setIncludeDocumentId("invalid")
      .build();

    MockFailureCollector collector = new MockFailureCollector();
    Assert.assertFalse(config.isIncludeDocumentId());
    Assert.assertEquals(0, collector.getValidationFailures().size());
  }

  private FirestoreSourceConfig withFirestoreValidationMock(FirestoreSourceConfig config, FailureCollector collector) {
    FirestoreSourceConfig spy = Mockito.spy(config);
    Mockito.doNothing().when(spy).validateFirestoreConnection(collector);
    return spy;
  }
}
